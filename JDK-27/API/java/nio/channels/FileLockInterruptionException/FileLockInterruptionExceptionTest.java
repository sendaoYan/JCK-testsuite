/*
 * @test
 * @summary java.nio.channels.FileLockInterruptionException conformance: white-box structure + black-box behavior.
 * @run main FileLockInterruptionExceptionTest
 */

import java.nio.channels.FileLockInterruptionException;

public class FileLockInterruptionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(FileLockInterruptionException.class), "hierarchy: extends java.io.IOException");
        check(new FileLockInterruptionException() != null, "no-arg constructor");
        check(new FileLockInterruptionException().getMessage() == null, "no-arg message is null");
        check(new FileLockInterruptionException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructor + state behavior; the interrupt race itself is not deterministic
        FileLockInterruptionException e = new FileLockInterruptionException();
        check(e.getMessage() == null, "no message");
        String s = e.toString();
        check(s.contains("FileLockInterruptionException"), "toString names the class");
        check(java.io.IOException.class.isAssignableFrom(e.getClass()), "IOException family");
        // fillInStackTrace yields a usable stack for logging
        check(e.getStackTrace() != null, "stack trace available");
    }

    // ---- A5 additions: serialization identity + deterministic interrupt trigger ----
    static void testSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(FileLockInterruptionException.class)
              .getSerialVersionUID() == 7104080643653532383L, "serialVersionUID fixed (实测)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new FileLockInterruptionException());
        }
        Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof FileLockInterruptionException, "round-trip class preserved");
        check(((FileLockInterruptionException) back).getMessage() == null, "round-trip message null");
        FileLockInterruptionException ex = new FileLockInterruptionException();
        java.io.IOException chained = new java.io.IOException("root");
        ex.initCause(chained);
        check(ex.getCause() == chained, "initCause stores the cause");
        check(ex.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.io.IOException.class == FileLockInterruptionException.class.getSuperclass(),
              "direct IOException subclass");
        check(!RuntimeException.class.isAssignableFrom(FileLockInterruptionException.class),
              "checked exception");
    }

    // 确定性触发：中断标记置位后 lock() 立即抛 FileLockInterruptionException（实测）
    static void testInterruptTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck-a5-flie27");
        try {
            java.nio.file.Path file = dir.resolve("flie.bin");
            java.nio.file.Files.write(file, new byte[4]);
            try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(file,
                    java.nio.file.StandardOpenOption.READ, java.nio.file.StandardOpenOption.WRITE)) {
                java.nio.channels.FileLock fl = ch.tryLock(0, 4, false);
                check(fl != null && fl.isValid(), "tryLock works when not interrupted");
                fl.release();

                Thread.currentThread().interrupt();
                try {
                    ch.lock();
                    check(false, "interrupted lock() must throw FileLockInterruptionException");
                } catch (FileLockInterruptionException expected) {
                    check(true, "interrupted lock() throws FileLockInterruptionException (实测)");
                } finally {
                    check(Thread.interrupted(), "interrupt status preserved and cleared");
                }
                check(!ch.isOpen(), "channel closed by the interruption (InterruptibleChannel 契约)");
            }
        } finally {
            try (var s = java.nio.file.Files.list(dir)) {
                s.forEach(p -> { try { java.nio.file.Files.delete(p); } catch (Exception ignore) { } });
            }
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        testInterruptTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
