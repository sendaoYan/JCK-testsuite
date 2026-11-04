/*
 * @test
 * @summary java.nio.file.attribute.AttributeView conformance: white-box structure + black-box behavior.
 * @run main AttributeViewTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

public class AttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.AttributeView");
        check(c.isInterface(), "AttributeView is an interface");
        check(c.getMethods().length == 1 && c.getMethods()[0].getName().equals("name"),
              "single method name()");
        check(c.getDeclaredMethods()[0].getReturnType() == String.class, "name() returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("av.txt");
            Files.writeString(file, "x");

            // every concrete view names its scheme through the common interface
            AttributeView basic = Files.getFileAttributeView(file, BasicFileAttributeView.class);
            check(basic != null, "basic view present");
            check(basic.name().equals("basic"), "basic name");
            check(((BasicFileAttributeView) basic).readAttributes().size() == 1,
                  "usable through the concrete type");

            // owner/posix views (posix guarded) report their scheme names
            AttributeView owner = Files.getFileAttributeView(file,
                    java.nio.file.attribute.FileOwnerAttributeView.class);
            check(owner != null && owner.name().equals("owner"), "owner name");
            if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews()
                    .contains("posix")) {
                AttributeView posix = Files.getFileAttributeView(file,
                        java.nio.file.attribute.PosixFileAttributeView.class);
                check(posix != null && posix.name().equals("posix"), "posix name");
            }
            if (Files.getFileStore(file).supportsFileAttributeView(
                    java.nio.file.attribute.DosFileAttributeView.class)) {
                AttributeView dos = Files.getFileAttributeView(file,
                        java.nio.file.attribute.DosFileAttributeView.class);
                check(dos != null && dos.name().equals("dos"), "dos name");
            }

            // user view on Linux
            if (Files.getFileStore(file).supportsFileAttributeView(
                    java.nio.file.attribute.UserDefinedFileAttributeView.class)) {
                AttributeView user = Files.getFileAttributeView(file,
                        java.nio.file.attribute.UserDefinedFileAttributeView.class);
                check(user != null && user.name().equals("user"), "user name");
            }
        } finally {
            Files.deleteIfExists(dir.resolve("av.txt"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
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
        // the interface declares exactly one contract method, abstract name()
        int own = 0;
        for (java.lang.reflect.Method m : AttributeView.class.getDeclaredMethods()) {
            own++;
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "method " + m.getName() + " abstract");
            check(m.getName().equals("name"), "sole declared method is name()");
            check(m.getReturnType() == String.class, "name() returns String");
            check(m.getParameterTypes().length == 0, "name() takes no arguments");
        }
        check(own == 1, "AttributeView declares exactly one method");
        check(AttributeView.class.getTypeParameters().length == 0, "not generic");
        // custom implementation honours the contract
        AttributeView custom = new AttributeView() {
            public String name() { return "myview"; }
        };
        check("myview".equals(custom.name()), "custom implementation name()");
        // every concrete view obtained from Files is an AttributeView with its registered name
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-av");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.write(f, "x".getBytes("UTF-8"));
            BasicFileAttributeView b = java.nio.file.Files.getFileAttributeView(f, BasicFileAttributeView.class);
            PosixFileAttributeView p = java.nio.file.Files.getFileAttributeView(f, PosixFileAttributeView.class);
            DosFileAttributeView d = java.nio.file.Files.getFileAttributeView(f, DosFileAttributeView.class);
            UserDefinedFileAttributeView u = java.nio.file.Files.getFileAttributeView(f,
                    UserDefinedFileAttributeView.class);
            FileOwnerAttributeView o = java.nio.file.Files.getFileAttributeView(f, FileOwnerAttributeView.class);
            AttributeView[] views = { b, p, d, u, o };
            String[] names = { "basic", "posix", "dos", "user", "owner" };
            for (int i = 0; i < views.length; i++) {
                check(views[i] != null, names[i] + " view available");
                check(views[i] instanceof AttributeView, names[i] + " view is an AttributeView");
                check(names[i].equals(views[i].name()), names[i] + " view name()");
            }
            // FileStoreAttributeView is an AttributeView (but NOT a FileAttributeView)
            check(AttributeView.class.isAssignableFrom(FileStoreAttributeView.class),
                  "FileStoreAttributeView is an AttributeView");
            check(!FileAttributeView.class.isAssignableFrom(FileStoreAttributeView.class),
                  "FileStoreAttributeView is not a FileAttributeView");
            // all registered view names are advertised by the filesystem
            java.util.Set<String> supported = java.nio.file.FileSystems.getDefault().supportedFileAttributeViews();
            for (String n : names) {
                check(supported.contains(n), "supportedFileAttributeViews contains " + n);
            }
            // Files.getFileAttributeView type parameter is bounded by FileAttributeView (API shape)
            java.lang.reflect.Method g = java.nio.file.Files.class.getMethod("getFileAttributeView",
                    java.nio.file.Path.class, Class.class, java.nio.file.LinkOption[].class);
            check(g.getTypeParameters().length == 1
                  && java.nio.file.attribute.FileAttributeView.class
                         .isAssignableFrom((Class<?>) g.getTypeParameters()[0].getBounds()[0]),
                  "getFileAttributeView type parameter bounded by FileAttributeView");
        } finally {
            cleanup(dir);
        }
    }

}
