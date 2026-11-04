/*
 * @test
 * @summary java.nio.file.Paths conformance: white-box structure (reflection) + black-box behavior
 *          (get(String,...) and get(URI), multi-arg joining and empty path).
 * @run main PathsTest
 */

import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.Paths");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "superclass Object");
        hasMethod(c, "get", String.class, String[].class);
        hasMethod(c, "get", URI.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() {
        Path p = Paths.get("a", "b", "c");
        check(p.getNameCount() == 3, "getNameCount 3");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("a".equals(p.getName(0).toString()), "getName(0)");

        check(Paths.get("a", "b", "c").equals(Paths.get("a/b/c")), "multi-arg equals joined");
        check(Paths.get("a", "b").equals(Paths.get("a", "b")), "repeat get equals");
        check("a/b".equals(Paths.get("a", "b").toString()), "joined toString");
        check("".equals(Paths.get("").toString()), "empty path toString empty");

        Path abs = Paths.get(URI.create("file:///tmp/x"));
        check(abs.isAbsolute(), "get(URI) absolute");
        check("/tmp/x".equals(abs.toString()), "get(URI) path");

        boolean npe = false;
        try { Paths.get((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "get(null) NPE");

        boolean npe2 = false;
        try { Paths.get((URI) null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "get((URI)null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
