/*
 * @test
 * @summary java.nio.file.FileAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main FileAlreadyExistsExceptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;

public class FileAlreadyExistsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure ----
    static void testWhiteBox() {
        check(java.nio.file.FileSystemException.class.isAssignableFrom(FileAlreadyExistsException.class),
              "hierarchy: extends FileSystemException");

        FileAlreadyExistsException e = new FileAlreadyExistsException("f");
        check("f".equals(e.getFile()), "getFile");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("exists.txt");
            java.nio.file.Files.createFile(f);
            boolean create = false;
            try { java.nio.file.Files.createFile(f); }
            catch (FileAlreadyExistsException e) { create = true; }
            check(create, "createFile twice -> FileAlreadyExistsException");

            java.nio.file.Path sub = dir.resolve("sub");
            java.nio.file.Files.createDirectory(sub);
            boolean mkdir = false;
            try { java.nio.file.Files.createDirectory(sub); }
            catch (FileAlreadyExistsException e) { mkdir = true; }
            check(mkdir, "createDirectory twice -> FileAlreadyExistsException");

            java.nio.file.Path src = dir.resolve("src.txt");
            java.nio.file.Files.write(src, "src".getBytes(StandardCharsets.UTF_8));
            boolean copyNoReplace = false;
            try {
                java.nio.file.Files.copy(src, f);
            } catch (FileAlreadyExistsException e) { copyNoReplace = true; }
            check(copyNoReplace, "copy without REPLACE -> FileAlreadyExistsException");
            java.nio.file.Files.copy(src, f, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            check(new String(java.nio.file.Files.readAllBytes(f), StandardCharsets.UTF_8)
                  .equals("src"), "REPLACE_EXISTING overwrites");
        } finally {
            try { java.nio.file.Files.deleteIfExists(dir.resolve("exists.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir.resolve("src.txt")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir.resolve("sub")); } catch (Exception ignore) { }
            try { java.nio.file.Files.deleteIfExists(dir); } catch (Exception ignore) { }
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

    // ==== batch A11: constructor / serialization / real triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check(FileAlreadyExistsException.class.getSuperclass()
                == java.nio.file.FileSystemException.class,
                "direct superclass is FileSystemException");
        check(java.io.IOException.class.isAssignableFrom(FileAlreadyExistsException.class),
                "caught as IOException");
        check(java.io.Serializable.class.isAssignableFrom(FileAlreadyExistsException.class),
                "Serializable");
        check(FileAlreadyExistsException.class.getConstructors().length == 2,
                "2 public constructors (String) and (String,String,String)");
        FileAlreadyExistsException one = new FileAlreadyExistsException("f");
        check("f".equals(one.getFile()) && one.getOtherFile() == null && one.getReason() == null,
                "1-arg getters");
        check("f".equals(one.getMessage()), "message == file");
        check("java.nio.file.FileAlreadyExistsException: f".equals(one.toString()),
                "toString exact");
        FileAlreadyExistsException three = new FileAlreadyExistsException("f", "o", "r");
        check("f".equals(three.getFile()) && "o".equals(three.getOtherFile())
                && "r".equals(three.getReason()), "3-arg getters");
        check("f -> o: r".equals(three.getMessage()), "inherited message composition");
        check(new FileAlreadyExistsException(null).getFile() == null
                && new FileAlreadyExistsException(null).getMessage() == null,
                "null file accepted with null message");
    }

    static void testContractCauseAndSerialization() throws Exception {
        FileAlreadyExistsException e = new FileAlreadyExistsException("f");
        check(e.getCause() == null, "default cause null");
        e.initCause(new RuntimeException("c"));
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new RuntimeException("c2")), "initCause one-shot");
        check(java.io.ObjectStreamClass.lookup(FileAlreadyExistsException.class)
                .getSerialVersionUID() == 7579540934498831181L,
                "serialVersionUID == 7579540934498831181");
        Object back = serialRoundTrip(new FileAlreadyExistsException("f", "o", "r"));
        check(back instanceof FileAlreadyExistsException,
                "round-trip preserves subclass identity");
        FileAlreadyExistsException r = (FileAlreadyExistsException) back;
        check("f".equals(r.getFile()) && "o".equals(r.getOtherFile())
                && "r".equals(r.getReason()), "round-trip keeps getters");
        check("f -> o: r".equals(r.getMessage()), "round-trip message");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Path f = dir.resolve("exists.txt");
            java.nio.file.Files.createFile(f);
            try {
                java.nio.file.Files.createFile(f);
                check(false, "createFile twice must fail");
            } catch (FileAlreadyExistsException e) {
                check(f.toString().equals(e.getFile()), "getFile is the existing path");
                check(e.getOtherFile() == null && e.getReason() == null, "other/reason null");
            }
            java.nio.file.Path src = dir.resolve("src.txt");
            java.nio.file.Files.write(src, "src".getBytes("UTF-8"));
            try {
                java.nio.file.Files.copy(src, f);
                check(false, "copy without REPLACE_EXISTING must fail");
            } catch (FileAlreadyExistsException e) {
                check(f.toString().equals(e.getFile()), "copy: getFile is the TARGET");
                check(e.getOtherFile() == null, "copy: other null");
            }
            try {
                java.nio.file.Files.move(src, f);
                check(false, "move onto existing without REPLACE_EXISTING must fail");
            } catch (FileAlreadyExistsException e) {
                check(f.toString().equals(e.getFile()), "move: getFile is the TARGET");
                check(e.getOtherFile() == null, "move: other null (not the source)");
                check(f.toString().equals(e.getMessage()), "move: message == target path");
            }
            check(java.nio.file.Files.exists(src), "failed move leaves source intact");
            // createDirectories over an existing directory is idempotent (no FAE)
            java.nio.file.Files.createDirectories(dir);
            check(java.nio.file.Files.exists(dir), "createDirectories(existing) idempotent");
            java.nio.file.Files.copy(src, f, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            check("src".equals(new String(java.nio.file.Files.readAllBytes(f), "UTF-8")),
                    "REPLACE_EXISTING overwrites");
        } finally {
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
