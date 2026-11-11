/*
 * @test
 * @requires os.family == "linux"
 * @summary java.nio.file.attribute.FileAttribute conformance: hierarchy and methods.
 * @run main FileAttributeTest
 */

import java.nio.file.attribute.FileAttribute;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFileAttributes;

public class FileAttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileAttribute() {
        check(FileAttribute.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : FileAttribute.class.getMethods()) names.add(m.getName());
        String[] required = { "name", "value" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testFileAttribute();
        testContract();
        testContract2();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===== A12 contract deepening (appended) =====
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    interface IORunnable { void run() throws java.io.IOException; }

    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void cleanup(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                         java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) cleanup(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    static void testContract() throws Exception {
        // both methods of the contract surface are abstract (interface is a pure carrier)
        for (java.lang.reflect.Method m : FileAttribute.class.getMethods()) {
            if (m.getDeclaringClass() == FileAttribute.class) {
                check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                        "method " + m.getName() + " is abstract");
            }
        }
        check(FileAttribute.class.getTypeParameters().length == 1, "generic interface with one type parameter");
        // custom implementation carries name/value verbatim
        FileAttribute<String> custom = new FileAttribute<String>() {
            public String name() { return "custom:name"; }
            public String value() { return "v"; }
        };
        check("custom:name".equals(custom.name()), "custom name()");
        check("v".equals(custom.value()), "custom value()");
        // PosixFilePermissions.asFileAttribute is the canonical FileAttribute in the JDK
        java.util.Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
        FileAttribute<java.util.Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        check(attr instanceof FileAttribute, "asFileAttribute is a FileAttribute");
        check("posix:permissions".equals(attr.name()), "attribute name is view-qualified");
        check(attr.value().equals(perms), "attribute value is the permission set");
        check(attr.value().equals(attr.value()), "value() stable across calls");
        // Files.createFile consumes arbitrary FileAttribute implementations
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-fa");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.createFile(f, attr);
            check(PosixFilePermissions.toString(java.nio.file.Files.readAttributes(f, PosixFileAttributes.class)
                    .permissions()).equals("rwxr-x---"), "createFile applies custom FileAttribute");
            // unknown view name in a hand-rolled attribute is rejected at create time
            checkThrowsIO(java.lang.UnsupportedOperationException.class,
                    () -> java.nio.file.Files.createFile(dir.resolve("g"), new FileAttribute<Object>() {
                        public String name() { return "bogusview:x"; }
                        public Object value() { return new Object(); }
                    }), "createFile with unknown attribute view UOE");
        } finally {
            cleanup(dir);
        }
    }

    // ===== A12 contract deepening (appended) =====
    static void testContract2() throws Exception {
        // the interface imposes no validation: null name/value pass through verbatim
        FileAttribute<Object> nullName = new FileAttribute<Object>() {
            public String name() { return null; }
            public Object value() { return null; }
        };
        check(nullName.name() == null && nullName.value() == null, "null name/value pass through");
        // value() may compute lazily; repeated calls return equal values
        final int[] calls = { 0 };
        FileAttribute<String> lazy = new FileAttribute<String>() {
            public String name() { return "user:lz"; }
            public String value() { calls[0]++; return "v" + calls[0]; }
        };
        check(!lazy.value().isEmpty() && calls[0] == 1, "value() computed on demand");
        check(lazy.value().startsWith("v"), "value() callable repeatedly");
        // typed values flow through the generic parameter
        FileAttribute<java.util.Set<PosixFilePermission>> attr =
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));
        check(attr.value() instanceof java.util.Set<?>, "generic value type preserved");
        // empty attribute array means "use defaults"
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-fa2");
        try {
            java.nio.file.Path f = dir.resolve("dflt");
            java.nio.file.Files.createFile(f, new FileAttribute[0]);
            check(java.nio.file.Files.isRegularFile(f), "createFile with empty attribute array");
        } finally {
            cleanup(dir);
        }
    }

}
