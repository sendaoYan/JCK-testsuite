/*
 * @test
 * @summary java.nio.file.attribute.AttributeView conformance: hierarchy and methods.
 * @run main AttributeViewTest
 */

import java.nio.file.attribute.AttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
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

    static void testAttributeView() {
        check(AttributeView.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : AttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "name" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testAttributeView();
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
