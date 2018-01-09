package com.swoval.files.apple;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileSystemApi implements AutoCloseable {
    private long handle;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FileSystemApi(Consumer<FileEvent> c, Consumer<String> pc) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        executor.submit(() -> {
            this.handle = FileSystemApi.init(c, pc);
            latch.countDown();
            loop();
        });
        latch.await();
    }

    private AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void close() {
        if (closed.compareAndSet(false,true)) {
            stopLoop();
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
                close(handle);
            } catch (InterruptedException e) {
            }
        }
    }

    public void loop() {
        loop(handle);
    }

    public int createStream(String path, double latency, int flags) {
        if (closed.get()) throw new IllegalStateException();
        return createStream(path, latency, flags, handle);
    }

    public void stopLoop() {
        stopLoop(handle);
    }

    public void stopStream(int streamHandle) {
        if (!closed.get()) stopStream(handle, streamHandle);
    }

    private static final String NATIVE_LIBRARY = "sbt-apple-file-system0";

    private static final void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void loadPackaged() {
        try {
            String lib = System.mapLibraryName(NATIVE_LIBRARY);
            Path tmp = Files.createTempDirectory("jni-");
            String line = null;
            try {
                Process process = new ProcessBuilder("uname", "-sm").start();
                InputStream is = process.getInputStream();
                line = new BufferedReader(new InputStreamReader(is)).lines().findFirst().get();
                is.close();
            } catch (Exception e) {
                exit("Error running `uname` command");
            }
            String[] parts = line.split(" ");
            if (parts.length != 2) {
                exit("Could not determine platform: 'uname -sm' returned unexpected string: " + line);
            } else {
                String arch = parts[1].toLowerCase().replaceAll("\\s", "");
                String kernel = parts[0].toLowerCase().replaceAll("\\s", "");
                String plat = arch + "-" + kernel;
                String resourcePath = "/native/" + plat + "/" + lib;
                InputStream resourceStream = FileSystemApi.class.getResourceAsStream(resourcePath);
                if (resourceStream == null) {
                    throw new UnsatisfiedLinkError(
                            "Native library " + lib + " (" + resourcePath + ") cannot be found on the classpath.");
                }

                Path extractedPath = tmp.resolve(lib);

                try {
                    Files.copy(resourceStream, extractedPath);
                } catch (Exception e) {
                    throw new UnsatisfiedLinkError("Error while extracting native library: " + e);
                }

                System.load(extractedPath.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            exit("Couldn't load packaged library " + NATIVE_LIBRARY);
        }
    }

    static {
        try {
            System.loadLibrary(NATIVE_LIBRARY);
        } catch (UnsatisfiedLinkError e) {
            loadPackaged();
        }
    }

    public static FileSystemApi apply(Consumer<FileEvent> consumer, Consumer<String> pathConsumer)
            throws InterruptedException {
        return new FileSystemApi(consumer, pathConsumer);
    }

    public static native void loop(long handle);

    public static native void close(long handle);

    public static native long init(Consumer<FileEvent> consumer, Consumer<String> pathConsumer);

    public static native int createStream(String path, double latency, int flags, long handle);

    public static native void stopLoop(long handle);

    public static native void stopStream(long handle, int streamHandle);

}