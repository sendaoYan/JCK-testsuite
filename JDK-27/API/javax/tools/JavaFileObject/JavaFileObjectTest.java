/*
 * @test
 * @summary javax.tools.JavaFileObject conformance: white-box structure + black-box behavior.
 * @run main JavaFileObjectTest
 */

import java.lang.reflect.*;
import java.net.URI;

public class JavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.JavaFileObject");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.tools.FileObject"), "extends javax.tools.FileObject");
        check(hasMethod(c, "getKind"), "declares getKind");
        check(hasMethod(c, "isNameCompatible"), "declares isNameCompatible");
        check(hasMethod(c, "getNestingKind"), "declares getNestingKind");
        check(hasMethod(c, "getAccessLevel"), "declares getAccessLevel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A SimpleJavaFileObject is the reference JavaFileObject implementation.
        javax.tools.JavaFileObject jfo = new javax.tools.SimpleJavaFileObject(
                URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class X {}";
            }
        };
        check(jfo.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind SOURCE");
        check(jfo.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible X/SOURCE");
        check(!jfo.isNameCompatible("X", javax.tools.JavaFileObject.Kind.CLASS), "not compatible X/CLASS");
        check(!jfo.isNameCompatible("Y", javax.tools.JavaFileObject.Kind.SOURCE), "not compatible Y/SOURCE");
        check(jfo.getNestingKind() == null, "getNestingKind null");
        check(jfo.getAccessLevel() == null, "getAccessLevel null");
        check("class X {}".contentEquals(jfo.getCharContent(false)), "getCharContent round-trip");
        check(jfo.toUri().equals(URI.create("string:///X.java")), "toUri round-trip");

        // forSource is a convenient JavaFileObject factory.
        javax.tools.JavaFileObject src =
                javax.tools.SimpleJavaFileObject.forSource(URI.create("string:///Z.java"), "class Z {}");
        check(src.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "forSource kind SOURCE");
        check("class Z {}".contentEquals(src.getCharContent(false)), "forSource content");
        check(src.isNameCompatible("Z", javax.tools.JavaFileObject.Kind.SOURCE), "forSource name compatible");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
