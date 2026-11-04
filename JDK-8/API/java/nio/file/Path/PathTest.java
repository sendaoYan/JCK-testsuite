/*
 * @test
 * @summary java.nio.file.Path conformance: white-box structure (reflection) + black-box behavior
 *          (component access, resolution, relativize, File interop; JDK-8 has no Path.of).
 * @run main PathTest
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Path");
        check(c.isInterface(), "Path is an interface");
        check(Modifier.isAbstract(c.getModifiers()), "interface is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.lang.Comparable"), "extends Comparable");
        check(hasInterface(c, "java.lang.Iterable"), "extends Iterable");
        check(hasInterface(c, "java.nio.file.Watchable"), "extends Watchable");
        hasMethod(c, "getFileName");
        hasMethod(c, "getName", int.class);
        hasMethod(c, "subpath", int.class, int.class);
        hasMethod(c, "startsWith", String.class);
        hasMethod(c, "endsWith", String.class);
        hasMethod(c, "resolve", String.class);
        hasMethod(c, "resolveSibling", String.class);
        hasMethod(c, "relativize", Path.class);
        hasMethod(c, "normalize");
        hasMethod(c, "toUri");
        hasMethod(c, "toAbsolutePath");
        hasMethod(c, "toRealPath", java.nio.file.LinkOption[].class);
        hasMethod(c, "toFile");
        hasMethod(c, "register", java.nio.file.WatchService.class, java.nio.file.WatchEvent.Kind[].class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Paths.get("/a/b/c");
        check(p.isAbsolute(), "isAbsolute");
        check("/".equals(p.getRoot().toString()), "getRoot");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("/a/b".equals(p.getParent().toString()), "getParent");
        check(p.getNameCount() == 3, "getNameCount");
        check("a".equals(p.getName(0).toString()), "getName(0)");
        check("b".equals(p.getName(1).toString()), "getName(1)");
        check("a/b".equals(p.subpath(0, 2).toString()), "subpath");
        check(p.startsWith(Paths.get("/a")), "startsWith Path");
        check(p.startsWith("/a"), "startsWith String");
        check(p.endsWith(Paths.get("c")), "endsWith Path");
        check(p.endsWith("c"), "endsWith String");
        check("/a/b/c".equals(p.normalize().toString()), "normalize");

        check("/a/b/c/d".equals(p.resolve("d").toString()), "resolve String");
        check("/a/b/d".equals(p.resolveSibling("d").toString()), "resolveSibling String");
        check("/a/b/c".equals(Paths.get("/a/b").resolve(Paths.get("c")).toString()), "resolve Path");
        check("/a/c".equals(Paths.get("/a/b").resolveSibling(Paths.get("c")).toString()), "resolveSibling Path");

        check("/a/b".equals(Paths.get("/a//b/./").normalize().toString()), "normalize folds");

        check("d".equals(Paths.get("/a/b/c").relativize(Paths.get("/a/b/c/d")).toString()), "relativize child");
        check("../x/y".equals(Paths.get("/a/b").relativize(Paths.get("/a/x/y")).toString()), "relativize up");

        List<String> names = new ArrayList<>();
        for (Path n : p) names.add(n.toString());
        check(names.size() == 3 && names.get(0).equals("a") && names.get(2).equals("c"), "iterator");

        check(p.toAbsolutePath().isAbsolute(), "toAbsolutePath");
        check("file".equals(p.toUri().getScheme()), "toUri scheme is file");
        check(p.toFile().getName().equals("c"), "toFile");

        check(Paths.get(URI.create("file:///a/b")).equals(Paths.get("/a/b")), "Paths.get(URI) equality");

        check(p.equals(Paths.get("/a/b/c")) && p.hashCode() == Paths.get("/a/b/c").hashCode(), "equals/hashCode");
        check(p.compareTo(Paths.get("/a/b/c")) == 0, "compareTo equal");
        check(p.toString() != null, "toString non-null");

        boolean iae = false;
        try { p.subpath(0, 4); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "subpath out of bounds IAE");

        boolean iae2 = false;
        try { p.getName(-1); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "getName negative IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
