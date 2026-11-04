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
        testInterfaceContract();
        testStandaloneContract();
        testCompletedPathInRealOperation();
        testFailedPathInRealOperation();
        testHandlerUsedByFileChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== A7 batch: deepened contract coverage =====================

    /** Recording implementation capturing both callback paths with their arguments. */
    static final class LogHandler<V, A> implements CompletionHandler<V, A> {
        int completedCalls;
        int failedCalls;
        V lastResult;
        A lastAttachment;
        Throwable lastFailure;
        public void completed(V result, A attachment) {
            completedCalls++;
            lastResult = result;
            lastAttachment = attachment;
        }
        public void failed(Throwable exc, A attachment) {
            failedCalls++;
            lastFailure = exc;
            lastAttachment = attachment;
        }
    }

    static void testInterfaceContract() throws Exception {
        java.lang.reflect.Method[] ms = CompletionHandler.class.getMethods();
        check(ms.length == 2, "exactly two methods, got " + ms.length);
        int defaults = 0;
        for (java.lang.reflect.Method m : ms)
            if (m.isDefault()) defaults++;
        check(defaults == 0, "no default methods");
        for (java.lang.reflect.Method m : ms) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " is abstract");
            check(m.getReturnType() == void.class, m.getName() + " returns void");
        }
        check(CompletionHandler.class.getTypeParameters().length == 2,
              "two type parameters <V, A>");
        check(CompletionHandler.class.getTypeParameters()[0].getName().equals("V"), "V parameter");
        check(CompletionHandler.class.getTypeParameters()[1].getName().equals("A"), "A parameter");
        java.lang.reflect.Method completed = CompletionHandler.class.getMethod("completed", Object.class, Object.class);
        java.lang.reflect.Method failed = CompletionHandler.class.getMethod("failed", Throwable.class, Object.class);
        check(completed != null && failed != null, "completed(V,A) and failed(Throwable,A) signatures");
        check(completed.getParameterTypes()[0] == Object.class
              && completed.getParameterTypes()[1] == Object.class, "completed takes (V, A)");
        check(failed.getParameterTypes()[0] == Throwable.class
              && failed.getParameterTypes()[1] == Object.class, "failed takes (Throwable, A)");
    }

    static void testStandaloneContract() {
        LogHandler<Integer, String> h = new LogHandler<Integer, String>();
        check(h.completedCalls == 0 && h.failedCalls == 0, "no invocations initially");
        h.completed(7, "att");
        check(h.completedCalls == 1 && h.failedCalls == 0, "completed recorded once");
        check(h.lastResult != null && h.lastResult == 7, "result recorded");
        check("att".equals(h.lastAttachment), "attachment recorded");
        java.io.IOException boom = new java.io.IOException("boom");
        h.failed(boom, "err");
        check(h.failedCalls == 1 && h.completedCalls == 1, "failed recorded once");
        check(h.lastFailure == boom, "failure object identity preserved");
        check("err".equals(h.lastAttachment), "failure attachment recorded");
        // null is a legal attachment on both paths
        LogHandler<Integer, Object> hn = new LogHandler<Integer, Object>();
        hn.completed(1, null);
        hn.failed(boom, null);
        check(hn.lastAttachment == null, "null attachment allowed on both paths");
        check(hn.completedCalls == 1 && hn.failedCalls == 1, "null attachment does not disturb dispatch");
        // one handler instance can be reused across operations
        hn.completed(2, "x");
        hn.completed(3, "y");
        check(hn.completedCalls == 3, "handler is a plain object, reusable");
        check(hn.lastResult != null && hn.lastResult == 3, "last invocation wins");
    }

    static void testCompletedPathInRealOperation() throws Exception {
        java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open();
        try {
            server.bind(new java.net.InetSocketAddress(java.net.InetAddress.getLoopbackAddress(), 0));
            java.nio.channels.AsynchronousSocketChannel client =
                    java.nio.channels.AsynchronousSocketChannel.open();
            try {
                client.connect(server.getLocalAddress()).get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel peer =
                        server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                // handler write: completed with the byte count and the attachment
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final Integer[] got = new Integer[1];
                final Object[] att = new Object[1];
                client.write(java.nio.ByteBuffer.wrap(new byte[] {4, 5}), "token",
                        new CompletionHandler<Integer, String>() {
                            public void completed(Integer r, String a) {
                                got[0] = r; att[0] = a; done.countDown();
                            }
                            public void failed(Throwable t, String a) { done.countDown(); }
                        });
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "completed invoked");
                check(got[0] != null && got[0] == 2, "completed result is the write count");
                check("token".equals(att[0]), "completed attachment is the caller's token");
                // handler read on the peer side
                final java.util.concurrent.CountDownLatch rdone = new java.util.concurrent.CountDownLatch(1);
                final Integer[] rgot = new Integer[1];
                final java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(2);
                peer.read(buf, 99L, new CompletionHandler<Integer, Long>() {
                    public void completed(Integer r, Long a) { rgot[0] = r; rdone.countDown(); }
                    public void failed(Throwable t, Long a) { rdone.countDown(); }
                });
                check(rdone.await(10, java.util.concurrent.TimeUnit.SECONDS), "read completed invoked");
                check(rgot[0] != null && rgot[0] == 2, "read result is the byte count");
                check(buf.get(0) == 4 && buf.get(1) == 5, "buffer holds the written bytes");
                peer.close();
            } finally {
                client.close();
            }
        } finally {
            server.close();
        }
    }

    static void testFailedPathInRealOperation() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        // failure path A: connecting to a refused port reports a precise IOException
        int deadPort;
        try (java.net.ServerSocket probe = new java.net.ServerSocket(0, 1, lo)) {
            deadPort = probe.getLocalPort();
        }
        java.nio.channels.AsynchronousSocketChannel dead =
                java.nio.channels.AsynchronousSocketChannel.open();
        try {
            final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
            final Throwable[] err = new Throwable[1];
            final Object[] att = new Object[1];
            dead.connect(new java.net.InetSocketAddress(lo, deadPort), "refused",
                    new CompletionHandler<Void, String>() {
                        public void completed(Void v, String a) { done.countDown(); }
                        public void failed(Throwable t, String a) {
                            err[0] = t; att[0] = a; done.countDown();
                        }
                    });
            check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "failure handler invoked");
            check(err[0] != null, "failure delivered: " + err[0]);
            check("refused".equals(att[0]), "failure attachment delivered");
        } finally {
            dead.close();
        }
        // failure path B: closing a channel with a pending read raises AsynchronousCloseException
        java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open();
        try {
            server.bind(new java.net.InetSocketAddress(lo, 0));
            java.nio.channels.AsynchronousSocketChannel client =
                    java.nio.channels.AsynchronousSocketChannel.open();
            try {
                client.connect(server.getLocalAddress()).get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel peer =
                        server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final Throwable[] err = new Throwable[1];
                client.read(java.nio.ByteBuffer.allocate(1), null,
                        new CompletionHandler<Integer, Object>() {
                            public void completed(Integer r, Object a) { done.countDown(); }
                            public void failed(Throwable t, Object a) { err[0] = t; done.countDown(); }
                        });
                client.close();
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                      "close terminates the pending read");
                check(err[0] instanceof java.nio.channels.AsynchronousCloseException,
                      "pending read + close -> AsynchronousCloseException, got " + err[0]);
                peer.close();
            } finally {
                client.close();
            }
        } finally {
            server.close();
        }
    }

    static void testHandlerUsedByFileChannel() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck-ch");
        try {
            java.nio.file.Path f = dir.resolve("ch.bin");
            java.nio.channels.AsynchronousFileChannel ch = java.nio.channels.AsynchronousFileChannel.open(
                    f, java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE);
            try {
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final Integer[] got = new Integer[1];
                ch.write(java.nio.ByteBuffer.wrap(new byte[] {1}), 0, null,
                        new CompletionHandler<Integer, Object>() {
                            public void completed(Integer r, Object a) { got[0] = r; done.countDown(); }
                            public void failed(Throwable t, Object a) { done.countDown(); }
                        });
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "file write completed");
                check(got[0] != null && got[0] == 1, "file write handler result");
                // failing file operation: read with a negative position fails with IAE
                boolean iae = false;
                try {
                    ch.read(java.nio.ByteBuffer.allocate(1), -1, null,
                            new CompletionHandler<Integer, Object>() {
                                public void completed(Integer r, Object a) { }
                                public void failed(Throwable t, Object a) { }
                            });
                } catch (IllegalArgumentException e) {
                    iae = true;
                }
                check(iae, "argument validation happens before the handler is consulted");
            } finally {
                ch.close();
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("ch.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }


}
