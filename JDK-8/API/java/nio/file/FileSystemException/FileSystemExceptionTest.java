/*
 * @test
 * @summary java.nio.file.FileSystemException conformance: white-box structure + black-box behavior.
 * @run main FileSystemExceptionTest
 */

import java.nio.file.FileSystemException;

public class FileSystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(FileSystemException.class),
              "hierarchy: extends IOException");

        FileSystemException e = new FileSystemException("f", "o", "r");
        check("f".equals(e.getFile()), "getFile");
        check("o".equals(e.getOtherFile()), "getOtherFile");
        check("r".equals(e.getReason()), "getReason");
        FileSystemException e2 = new FileSystemException("only");
        check(e2.getOtherFile() == null && e2.getReason() == null, "1-arg nulls");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path missing = dir.resolve("ghost");
            try {
                java.nio.file.Files.readAttributes(missing,
                        java.nio.file.attribute.BasicFileAttributes.class);
                check(false, "readAttributes on missing file should fail");
            } catch (java.nio.file.NoSuchFileException e) {
                check(e.getFile().endsWith("ghost"), "getFile carries path");
                check(e.getOtherFile() == null, "single file involved");
            }
            FileSystemException e = new FileSystemException("a", "b", "why");
            check(e.getMessage() != null && e.getMessage().contains("a")
                  && e.getMessage().contains("b") && e.getMessage().contains("why"),
                  "message composes file/other/reason");
            FileSystemException single = new FileSystemException("solo");
            check(single.getMessage() != null && single.getMessage().contains("solo"),
                  "message composes single file");
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

    // ==== batch A11: full constructor / message / cause / serialization contract ====
    static void testContractConstructors() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = FileSystemException.class.getConstructors();
        check(ctors.length == 2, "exactly 2 public constructors, found " + ctors.length);
        int noArg = 0, oneArg = 0, threeArg = 0;
        for (java.lang.reflect.Constructor<?> c : ctors) {
            if (c.getParameterCount() == 0) noArg++;
            if (c.getParameterCount() == 1) oneArg++;
            if (c.getParameterCount() == 3) threeArg++;
        }
        check(noArg == 0, "no public no-arg constructor");
        check(oneArg == 1, "exactly one (String file) constructor");
        check(threeArg == 1, "exactly one (String,String,String) constructor");
        FileSystemException one = new FileSystemException("f");
        check("f".equals(one.getFile()) && one.getOtherFile() == null && one.getReason() == null,
              "1-arg stores file only");
        FileSystemException nullArg = new FileSystemException(null);
        check(nullArg.getFile() == null && nullArg.getMessage() == null,
              "(null) accepted; message null");
        FileSystemException nnn = new FileSystemException(null, null, null);
        check(nnn.getFile() == null && nnn.getOtherFile() == null && nnn.getReason() == null,
              "(null,null,null) accepted");
        FileSystemException fnr = new FileSystemException("f", null, "r");
        check("f".equals(fnr.getFile()) && fnr.getOtherFile() == null && "r".equals(fnr.getReason()),
              "(f,null,r) stored");
        FileSystemException nor = new FileSystemException(null, "o", null);
        check(nor.getFile() == null && "o".equals(nor.getOtherFile()) && nor.getReason() == null,
              "(null,o,null) stored");
    }

    static void testContractMessageMatrix() throws Exception {
        // format "file -> other: reason" with per-null branches (jdk8u372 & jdk-27.0.1 measured)
        check("f -> o: r".equals(new FileSystemException("f", "o", "r").getMessage()), "msg(f,o,r)");
        check("f".equals(new FileSystemException("f", null, null).getMessage()), "msg(f) == file");
        check("f -> o".equals(new FileSystemException("f", "o", null).getMessage()), "msg(f,o)");
        check("f: r".equals(new FileSystemException("f", null, "r").getMessage()), "msg(f,r)");
        check(" -> o: r".equals(new FileSystemException(null, "o", "r").getMessage()), "msg(o,r)");
        check("r".equals(new FileSystemException(null, null, "r").getMessage()), "msg(r) == reason");
        check(" -> o".equals(new FileSystemException(null, "o", null).getMessage()), "msg(o)");
        check(new FileSystemException(null, null, null).getMessage() == null, "msg(nulls) == null");
        FileSystemException e = new FileSystemException("f", "o", "r");
        check(e.getLocalizedMessage().equals(e.getMessage()), "getLocalizedMessage delegates");
    }

    static void testContractCauseAndToString() throws Exception {
        FileSystemException e = new FileSystemException("f");
        check(e.getCause() == null, "default cause null");
        RuntimeException c1 = new RuntimeException("c1");
        check(e.initCause(c1) == e, "initCause returns this");
        check(e.getCause() == c1, "initCause stores cause");
        checkThrowsExact(IllegalStateException.class, () -> e.initCause(new RuntimeException("c2")),
                "initCause is one-shot");
        FileSystemException self = new FileSystemException("x");
        checkThrowsExact(IllegalArgumentException.class, () -> self.initCause(self),
                "self-causation rejected with IAE");
        check("java.nio.file.FileSystemException: a -> b: c"
                .equals(new FileSystemException("a", "b", "c").toString()), "toString full form");
        check("java.nio.file.FileSystemException: f"
                .equals(new FileSystemException("f").toString()), "toString file-only form");
        check("java.nio.file.FileSystemException"
                .equals(new FileSystemException(null, null, null).toString()), "toString bare form");
        check(new FileSystemException("x").fillInStackTrace() instanceof FileSystemException,
                "fillInStackTrace returns this type");
        check(new FileSystemException("x").getStackTrace() != null, "stack trace captured");
    }

    static void testContractSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(FileSystemException.class).getSerialVersionUID()
                == -3055425747967319812L, "serialVersionUID == -3055425747967319812");
        Object back = serialRoundTrip(new FileSystemException("sf", "so", "sr"));
        check(back instanceof FileSystemException, "round-trip preserves class");
        FileSystemException r = (FileSystemException) back;
        check("sf".equals(r.getFile()) && "so".equals(r.getOtherFile()) && "sr".equals(r.getReason()),
                "round-trip keeps all three getters");
        check("sf -> so: sr".equals(r.getMessage()), "round-trip message recomposed");
        FileSystemException rn = (FileSystemException) serialRoundTrip(
                new FileSystemException(null, null, null));
        check(rn.getFile() == null && rn.getOtherFile() == null && rn.getReason() == null
                && rn.getMessage() == null, "round-trip keeps nulls");
        FileSystemException rc = new FileSystemException("cf");
        rc.initCause(new RuntimeException("cc"));
        FileSystemException r2 = (FileSystemException) serialRoundTrip(rc);
        check(r2.getCause() instanceof RuntimeException
                && "cc".equals(r2.getCause().getMessage()), "round-trip keeps chained cause");
    }

    static void testContractFamilyInheritance() throws Exception {
        Class<?>[] family = {
            java.nio.file.AccessDeniedException.class,
            java.nio.file.AtomicMoveNotSupportedException.class,
            java.nio.file.DirectoryNotEmptyException.class,
            java.nio.file.FileAlreadyExistsException.class,
            java.nio.file.FileSystemLoopException.class,
            java.nio.file.NoSuchFileException.class,
            java.nio.file.NotDirectoryException.class,
            java.nio.file.NotLinkException.class };
        for (Class<?> c : family) {
            check(FileSystemException.class.isAssignableFrom(c) && c != FileSystemException.class,
                    c.getSimpleName() + " is a proper FileSystemException subtype");
            check(java.io.IOException.class.isAssignableFrom(c),
                    c.getSimpleName() + " is caught as IOException");
            check(java.io.Serializable.class.isAssignableFrom(c),
                    c.getSimpleName() + " is Serializable");
        }
    }

    static void testContractRealWorldCarry() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path ghost = dir.resolve("ghost");
            try {
                java.nio.file.Files.readAttributes(ghost,
                        java.nio.file.attribute.BasicFileAttributes.class);
                check(false, "readAttributes on missing file must fail");
            } catch (FileSystemException e) {
                check(e instanceof java.nio.file.NoSuchFileException, "exact subtype carried");
                check(ghost.toString().equals(e.getFile()), "getFile is the failing path");
                check(e.getOtherFile() == null && e.getReason() == null,
                        "single-file op leaves other/reason null");
                check(ghost.toString().equals(e.getMessage()), "message == path for reason-less op");
            }
        } finally {
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractConstructors();
        testContractMessageMatrix();
        testContractCauseAndToString();
        testContractSerialization();
        testContractFamilyInheritance();
        testContractRealWorldCarry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
