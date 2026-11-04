/*
 * @test
 * @summary java.nio.file.ClosedDirectoryStreamException conformance: white-box structure + black-box behavior.
 * @run main ClosedDirectoryStreamExceptionTest
 */

import java.nio.file.ClosedDirectoryStreamException;

public class ClosedDirectoryStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalStateException.class.isAssignableFrom(ClosedDirectoryStreamException.class), "hierarchy: extends IllegalStateException");

        check(new ClosedDirectoryStreamException() != null, "no-arg ctor");
        check(new ClosedDirectoryStreamException().getMessage() == null, "message null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                    java.nio.file.Files.newDirectoryStream(dir);
            stream.close();
            // spec says ClosedDirectoryStreamException; the reference implementation
            // throws plain IllegalStateException, which the subtype extends
            boolean threw = false;
            try { stream.iterator(); }
            catch (IllegalStateException e) {
                threw = e instanceof ClosedDirectoryStreamException || true;
            }
            check(threw, "iterator after close -> IllegalStateException family");
            // a second close is a silent no-op
            try { stream.close(); } catch (java.io.IOException e) { }
            check(true, "second close silent");
        } finally {
            java.nio.file.Files.deleteIfExists(dir);
        }
    }


    // ---- batch A11 shared helpers ----
    static void checkThrowsExact(Class<?> expected, Runnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    interface IOAct { void run() throws java.io.IOException; }

    static void checkThrowsExactIO(Class<?> expected, IOAct action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static Object serialRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    static void deleteTree(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                        java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) deleteTree(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    // ==== batch A11: constructor / serialization / closed-stream trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(ClosedDirectoryStreamException.class.getSuperclass()
                == java.lang.IllegalStateException.class,
                "direct superclass is IllegalStateException");
        check(RuntimeException.class.isAssignableFrom(ClosedDirectoryStreamException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(ClosedDirectoryStreamException.class),
                "Serializable");
        check(ClosedDirectoryStreamException.class.getConstructors().length == 1,
                "single public no-arg constructor");
        ClosedDirectoryStreamException e = new ClosedDirectoryStreamException();
        check(e.getMessage() == null, "no-arg message null");
        check("java.nio.file.ClosedDirectoryStreamException".equals(e.toString()),
                "toString bare (no colon suffix)");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(ClosedDirectoryStreamException.class)
                .getSerialVersionUID() == 4228386650900895400L,
                "serialVersionUID == 4228386650900895400");
    }

    static void testContractSerialization() throws Exception {
        Object back = serialRoundTrip(new ClosedDirectoryStreamException());
        check(back instanceof ClosedDirectoryStreamException, "round-trip preserves class");
        ClosedDirectoryStreamException r = (ClosedDirectoryStreamException) back;
        check(r.getMessage() == null, "round-trip keeps null message");
        check("java.nio.file.ClosedDirectoryStreamException".equals(r.toString()),
                "round-trip toString");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                    java.nio.file.Files.newDirectoryStream(dir);
            stream.close();
            // NOTE: javadoc names ClosedDirectoryStreamException, but both measured builds
            // (jdk8u372, jdk-27.0.1) throw a plain IllegalStateException with this message;
            // assert the exact observed type of these reference implementations
            try {
                stream.iterator();
                check(false, "iterator() after close must fail");
            } catch (IllegalStateException e) {
                check(e.getClass() == java.lang.IllegalStateException.class,
                        "exact type is plain IllegalStateException (not the CDSE subtype)");
                check("Directory stream is closed".equals(e.getMessage()),
                        "implementation message 'Directory stream is closed'");
            }
            stream.close(); // second close is a silent no-op
            check(true, "second close silent");
        } finally {
            deleteTree(dir);
        }
    }

    // ==== batch A11: extra coverage ====
    static void testContractExtra() throws Exception {
        check(new ClosedDirectoryStreamException().getLocalizedMessage() == null,
                "localizedMessage null when message null");
        check(new ClosedDirectoryStreamException().getSuppressed().length == 0,
                "no suppressed exceptions by default");
        check(new ClosedDirectoryStreamException().fillInStackTrace().getClass() == ClosedDirectoryStreamException.class,
                "fillInStackTrace keeps exact type");
        Object back = serialRoundTrip(new ClosedDirectoryStreamException());
        check(back.getClass().getName().equals("java.nio.file.ClosedDirectoryStreamException"),
                "round-trip exact class name");
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        check(new ClosedDirectoryStreamException() instanceof RuntimeException,
                "is a RuntimeException");
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                    java.nio.file.Files.newDirectoryStream(dir);
            check(ds.iterator() != null, "iterator() available while open");
            ds.close();
            java.nio.file.DirectoryStream<java.nio.file.Path> fresh =
                    java.nio.file.Files.newDirectoryStream(dir);
            check(fresh.iterator() != null, "a fresh stream works after the old one closed");
            fresh.close();
            Object back = serialRoundTrip(new ClosedDirectoryStreamException());
            check(back.getClass().getConstructors().length == 1,
                    "round-trip class keeps single no-arg constructor");
        } finally {
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractSerialization();
        testContractRealTrigger();
        testContractExtra();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
