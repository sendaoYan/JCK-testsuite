/*
 * @test
 * @summary java.nio.channels.CompletionHandler conformance: white-box structure + black-box behavior.
 * @run main CompletionHandlerTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CompletionHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void deleteRecursively(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            try (var s = Files.list(p)) { for (Path c : s.toList()) deleteRecursively(c); }
        }
        Files.deleteIfExists(p);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.CompletionHandler");
        check(c.isInterface(), "CompletionHandler is an interface");
        check(c.getTypeParameters().length == 2, "two type parameters V and A");
        check(c.getTypeParameters()[0].getName().equals("V"), "V parameter");
        check(c.getTypeParameters()[1].getName().equals("A"), "A parameter");
        for (String n : new String[] {"completed", "failed"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("ch.bin");

            // completed() is invoked with the result and the attachment
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(file,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                CountDownLatch done = new CountDownLatch(1);
                final Integer[] got = new Integer[1];
                final String[] att = new String[1];
                ch.write(ByteBuffer.wrap(new byte[] {42, 43}), 0, "token",
                        new CompletionHandler<Integer, String>() {
                            public void completed(Integer result, String attachment) {
                                got[0] = result; att[0] = attachment; done.countDown();
                            }
                            public void failed(Throwable exc, String attachment) {
                                done.countDown();
                            }
                        });
                check(done.await(10, TimeUnit.SECONDS), "handler completed in time");
                check(got[0] != null && got[0] == 2, "completed result is bytes written");
                check("token".equals(att[0]), "attachment passed through");

                // negative position is rejected synchronously
                boolean badPos = false;
                try { ch.write(ByteBuffer.wrap(new byte[] {1}), -5, null,
                        new CompletionHandler<Integer, Object>() {
                            public void completed(Integer result, Object attachment) { }
                            public void failed(Throwable exc, Object attachment) { }
                        }); }
                catch (IllegalArgumentException e) { badPos = true; }
                check(badPos, "negative position -> IllegalArgumentException");

                // failed() path: connecting to a refused loopback port reports failure
                int deadPort;
                try (java.net.ServerSocket probe = new java.net.ServerSocket(0,
                        1, InetAddress.getLoopbackAddress())) {
                    deadPort = probe.getLocalPort();
                }
                try (java.nio.channels.AsynchronousSocketChannel dead =
                        java.nio.channels.AsynchronousSocketChannel.open()) {
                    CountDownLatch failed = new CountDownLatch(1);
                    final Throwable[] err = new Throwable[1];
                    dead.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), deadPort),
                            "refused",
                            new CompletionHandler<Void, String>() {
                                public void completed(Void v, String a) { failed.countDown(); }
                                public void failed(Throwable exc, String a) {
                                    err[0] = exc; failed.countDown();
                                }
                            });
                    check(failed.await(10, TimeUnit.SECONDS), "failure handler invoked in time");
                    check(err[0] != null, "failure reported: " + err[0]);
                }

                // a user handler can also be used standalone (protocol-style usage)
                RecordingHandler<Integer, Long> h = new RecordingHandler<>();
                h.completed(7, 99L);
                h.failed(new IOException("x"), 99L);
                check(h.completedCalls == 1 && h.failedCalls == 1, "handler usable standalone");
                check(h.lastAttachment.equals(99L), "attachment recorded");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    static final class RecordingHandler<V, A> implements CompletionHandler<V, A> {
        int completedCalls, failedCalls;
        A lastAttachment;
        public void completed(V result, A attachment) {
            completedCalls++; lastAttachment = attachment;
        }
        public void failed(Throwable exc, A attachment) {
            failedCalls++; lastAttachment = attachment;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
