/*
 * @test
 * @summary java.nio.channels.FileLockInterruptionException conformance: hierarchy and constructor.
 * @run main FileLockInterruptionExceptionTest
 */

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileLockInterruptionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileLockInterruptionException() {
        check(IOException.class.isAssignableFrom(FileLockInterruptionException.class), "extends IOException");
        FileLockInterruptionException e = new FileLockInterruptionException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IOException, "instanceof IOException");
    }

    // ---- A5: hierarchy, serialization, deterministic interrupt trigger ----
    static void testHierarchyAndSerialization() throws Exception {
        check(!RuntimeException.class.isAssignableFrom(FileLockInterruptionException.class),
              "checked exception (not a RuntimeException)");
        check(ObjectStreamClass.lookup(FileLockInterruptionException.class).getSerialVersionUID()
              == 7104080643653532383L, "serialVersionUID fixed (实测)");
        check(IOException.class.isAssignableFrom(FileLockInterruptionException.class),
              "Serializable via IOException");

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new FileLockInterruptionException());
        }
        Object back = new ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof FileLockInterruptionException, "round-trip class preserved");
        check(((FileLockInterruptionException) back).getMessage() == null, "round-trip message null");

        FileLockInterruptionException ex = new FileLockInterruptionException();
        check(ex.getCause() == null, "no cause by default");
        IOException chained = new IOException("root");
        ex.initCause(chained);
        check(ex.getCause() == chained, "initCause stores the cause");
        check(ex.toString().contains("FileLockInterruptionException"), "toString names the class");
        check(ex.getStackTrace() != null, "stack trace available");
        check(ex.getSuppressed().length == 0, "no suppressed exceptions by default");
    }

    // 确定性触发：中断标记置位后 lock() 立即抛 FileLockInterruptionException（实测）
    static void testInterruptTrigger() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-flie");
        try {
            Path file = dir.resolve("flie.bin");
            Files.write(file, new byte[4]);
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                // 未中断时锁正常获取
                FileLock fl = ch.tryLock(0, 4, false);
                check(fl != null && fl.isValid(), "tryLock works when not interrupted");
                fl.release();

                // 确定性触发：置中断标记后 lock() 立即抛 FLIE，且通道按契约关闭
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
                check(java.lang.reflect.Modifier.isPublic(
                        FileLockInterruptionException.class.getModifiers()), "public class");
                check(IOException.class == FileLockInterruptionException.class.getSuperclass(),
                      "direct IOException subclass");
            }
        } finally {
            try (java.util.stream.Stream<Path> s = Files.list(dir)) {
                for (Path p : s.collect(java.util.stream.Collectors.toList())) Files.deleteIfExists(p);
            }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testFileLockInterruptionException();
        testHierarchyAndSerialization();
        testInterruptTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
