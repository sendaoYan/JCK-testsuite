/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.AccessDeniedException conformance: white-box structure + black-box behavior.
 * @run main AccessDeniedExceptionTest
 */

import java.nio.file.AccessDeniedException;

public class AccessDeniedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(AccessDeniedException.class), "hierarchy: extends FileSystemException");

        AccessDeniedException e = new AccessDeniedException("file");
        check("file".equals(e.getFile()), "getFile");
        check(e.getReason() == null, "no reason by default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        if (System.getProperty("user.name").equals("root")) {
            System.out.println("NOTE: running as root; permission denial part skipped");
            return;
        }
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("denied.txt");
            java.nio.file.Files.writeString(f, "x");
            java.nio.file.Files.setPosixFilePermissions(f,
                    java.util.EnumSet.noneOf(java.nio.file.attribute.PosixFilePermission.class));
            boolean denied = false;
            try {
                java.nio.file.Files.newByteChannel(f,
                        java.nio.file.StandardOpenOption.READ).close();
            } catch (AccessDeniedException e) {
                denied = true;
                check(e.getFile() != null, "denied exception carries file");
            }
            check(denied, "000-mode file -> AccessDeniedException");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("denied.txt"));
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

    // ==== batch A11: constructor / getter / serialization / real trigger ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(AccessDeniedException.class.getSuperclass() == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(AccessDeniedException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(AccessDeniedException.class),
                "Serializable via IOException");
        check(AccessDeniedException.class.getConstructors().length == 2,
                "exactly 2 public constructors (String) and (String,String,String)");
        AccessDeniedException one = new AccessDeniedException("f");
        check("f".equals(one.getFile()) && one.getOtherFile() == null && one.getReason() == null,
                "1-arg stores file only");
        check("f".equals(one.getMessage()), "message == file for 1-arg");
        check("java.nio.file.AccessDeniedException: f".equals(one.toString()), "toString exact");
        AccessDeniedException three = new AccessDeniedException("f", "o", "r");
        check("f".equals(three.getFile()) && "o".equals(three.getOtherFile())
                && "r".equals(three.getReason()), "3-arg stores all");
        check("f -> o: r".equals(three.getMessage()), "inherited message composition");
        check(new AccessDeniedException(null).getFile() == null, "null file accepted");
        check(new AccessDeniedException(null).getMessage() == null, "null file -> null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        AccessDeniedException e = new AccessDeniedException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(AccessDeniedException.class).getSerialVersionUID()
                == 4943049599949219617L, "serialVersionUID == 4943049599949219617");
        Object back = serialRoundTrip(new AccessDeniedException("af", null, "ar"));
        check(back instanceof AccessDeniedException, "round-trip preserves subclass identity");
        AccessDeniedException r = (AccessDeniedException) back;
        check("af".equals(r.getFile()) && r.getOtherFile() == null && "ar".equals(r.getReason()),
                "round-trip keeps getters");
        check("af: ar".equals(r.getMessage()), "round-trip message (f,null,r) == 'af: ar'");
    }

    static void testContractRealTrigger() throws Exception {
        if ("root".equals(System.getProperty("user.name"))) {
            System.out.println("NOTE: running as root; chmod-based denial part skipped");
            return;
        }
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path f = dir.resolve("denied.txt");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            java.nio.file.Files.setPosixFilePermissions(f,
                    java.nio.file.attribute.PosixFilePermissions.fromString("---------"));
            try {
                java.nio.file.Files.readAllBytes(f);
                check(false, "read of 000-mode file must fail");
            } catch (AccessDeniedException e) {
                check(f.toString().equals(e.getFile()), "trigger: getFile is the denied path");
                check(e.getOtherFile() == null && e.getReason() == null,
                        "trigger: other/reason null");
                check(f.toString().equals(e.getMessage()), "trigger: message == path");
            }
            checkThrowsExactIO(AccessDeniedException.class,
                    () -> java.nio.file.Files.newByteChannel(f,
                            java.nio.file.StandardOpenOption.READ).close(),
                    "open 000-mode file READ -> AccessDeniedException");
            checkThrowsExactIO(AccessDeniedException.class,
                    () -> java.nio.file.Files.write(f, "y".getBytes("UTF-8")),
                    "write 000-mode file -> AccessDeniedException");
            java.nio.file.Files.setPosixFilePermissions(f,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
            java.nio.file.Path roDir = dir.resolve("rodir");
            java.nio.file.Files.createDirectory(roDir);
            java.nio.file.Files.setPosixFilePermissions(roDir,
                    java.nio.file.attribute.PosixFilePermissions.fromString("r-x------"));
            try {
                java.nio.file.Files.createFile(roDir.resolve("new.txt"));
                check(false, "create in r-x dir must fail");
            } catch (AccessDeniedException e) {
                check(e.getFile().endsWith("new.txt"), "denied create reports the child path");
            } finally {
                java.nio.file.Files.setPosixFilePermissions(roDir,
                        java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"));
            }
        } finally {
            try { java.nio.file.Files.setPosixFilePermissions(dir.resolve("denied.txt"),
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")); }
            catch (Exception ignore) { }
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractCauseAndSerialization();
        testContractRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
