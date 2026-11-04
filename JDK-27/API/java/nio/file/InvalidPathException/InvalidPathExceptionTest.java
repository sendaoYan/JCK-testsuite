/*
 * @test
 * @summary java.nio.file.InvalidPathException conformance: white-box structure + black-box behavior.
 * @run main InvalidPathExceptionTest
 */

import java.nio.file.InvalidPathException;

public class InvalidPathExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(InvalidPathException.class), "hierarchy: extends IllegalArgumentException");

        InvalidPathException e = new InvalidPathException("in put", "reason", 3);
        check("in put".equals(e.getInput()), "getInput");
        check("reason".equals(e.getReason()), "getReason");
        check(e.getIndex() == 3, "getIndex");
        check(e.getMessage().contains("reason"), "message has reason");
        InvalidPathException e2 = new InvalidPathException("x", "why");
        check(e2.getIndex() == -1, "no-index is -1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // embedded NUL is rejected with the offending index
        boolean threw = false;
        try { java.nio.file.Paths.get("bad\u0000path"); }
        catch (InvalidPathException e) {
            threw = true;
            check(e.getIndex() == -1 || e.getIndex() == 3, "index of NUL reported");
            check(e.getInput().equals("bad\u0000path"), "input echoed");
            check(e.getReason() != null, "reason present");
        }
        check(threw, "NUL byte -> InvalidPathException");
        // valid paths do not throw
        check(java.nio.file.Paths.get("plain.txt") != null, "valid path accepted");
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

    // ==== batch A11: constructor matrix / message / serialization / NUL triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(InvalidPathException.class.getSuperclass()
                == java.lang.IllegalArgumentException.class,
                "direct superclass is IllegalArgumentException");
        check(RuntimeException.class.isAssignableFrom(InvalidPathException.class),
                "caught as RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(InvalidPathException.class),
                "Serializable");
        check(InvalidPathException.class.getConstructors().length == 2,
                "2 public constructors (String,String) and (String,String,int)");
        checkThrowsExact(NullPointerException.class,
                () -> new InvalidPathException(null, "r"), "null input -> NPE");
        checkThrowsExact(NullPointerException.class,
                () -> new InvalidPathException("i", null), "null reason -> NPE");
        checkThrowsExact(NullPointerException.class,
                () -> new InvalidPathException(null, "r", 0), "null input (3-arg) -> NPE");
        checkThrowsExact(IllegalArgumentException.class,
                () -> new InvalidPathException("i", "r", -2), "index -2 -> IAE");
        checkThrowsExact(IllegalArgumentException.class,
                () -> new InvalidPathException("i", "r", -5), "index -5 -> IAE");
        check(new InvalidPathException("i", "r", -1).getIndex() == -1, "index -1 legal");
        check(new InvalidPathException("i", "r", 0).getIndex() == 0, "index 0 legal");
        check(new InvalidPathException("i", "r", 99).getIndex() == 99,
                "index beyond input length kept as-is (no validation)");
    }

    static void testContractGettersAndMessage() throws Exception {
        InvalidPathException e = new InvalidPathException("input", "reason", 3);
        check("input".equals(e.getInput()), "getInput");
        check("reason".equals(e.getReason()), "getReason");
        check(e.getIndex() == 3, "getIndex");
        check("reason at index 3: input".equals(e.getMessage()), "3-arg message format");
        check("java.nio.file.InvalidPathException: reason at index 3: input".equals(e.toString()),
                "toString exact");
        InvalidPathException e2 = new InvalidPathException("input", "reason");
        check(e2.getIndex() == -1, "default index -1");
        check("reason: input".equals(e2.getMessage()), "2-arg message format (no index)");
        check(new InvalidPathException("i", "r").getCause() == null, "default cause null");
        InvalidPathException once = new InvalidPathException("i", "r");
        once.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> once.initCause(new RuntimeException("c2")), "initCause one-shot");
    }

    static void testContractSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(InvalidPathException.class).getSerialVersionUID()
                == 4355821422286746137L, "serialVersionUID == 4355821422286746137");
        Object back = serialRoundTrip(new InvalidPathException("input", "reason", 3));
        check(back instanceof InvalidPathException, "round-trip preserves class");
        InvalidPathException r = (InvalidPathException) back;
        check("input".equals(r.getInput()) && "reason".equals(r.getReason())
                && r.getIndex() == 3, "round-trip keeps input/reason/index");
        check("reason at index 3: input".equals(r.getMessage()),
                "round-trip recomposes message");
        InvalidPathException r2 = (InvalidPathException)
                serialRoundTrip(new InvalidPathException("i", "r"));
        check(r2.getIndex() == -1, "round-trip keeps default index -1");
    }

    static void testContractRealTrigger() throws Exception {
        boolean threw = false;
        try {
            java.nio.file.Paths.get("a\u0000b");
        } catch (InvalidPathException e) {
            threw = true;
            check("a\u0000b".equals(e.getInput()),
                    "input echoed");
            check("Nul character not allowed".equals(e.getReason()), "reason is NUL message");
            check(e.getIndex() == -1, "NUL is not a per-index error: index -1");
            check("Nul character not allowed: a\u0000b"
                    .equals(e.getMessage()), "message 'reason: input' form");
        }
        check(threw, "embedded NUL -> InvalidPathException from Paths.get");
        threw = false;
        try {
            java.nio.file.FileSystems.getDefault().getPath("x\u0000y");
        } catch (InvalidPathException e) {
            threw = true;
            check("Nul character not allowed".equals(e.getReason()), "fs.getPath same reason");
        }
        check(threw, "embedded NUL -> InvalidPathException from FileSystem.getPath");
        checkThrowsExact(NullPointerException.class,
                () -> java.nio.file.Paths.get((String) null),
                "Paths.get(null) -> NullPointerException (not InvalidPath)");
        check(java.nio.file.Paths.get("plain.txt") != null, "valid path accepted");
        check(java.nio.file.Paths.get("a/b/c").getNameCount() == 3, "valid multi-segment path");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractGettersAndMessage();
        testContractSerialization();
        testContractRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
