/*
 * @test
 * @summary javax.tools.SimpleJavaFileObject conformance: white-box structure + black-box behavior.
 * @run main SimpleJavaFileObjectTest
 */

import java.lang.reflect.*;
import java.net.URI;

public class SimpleJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.SimpleJavaFileObject");
        check(!c.isInterface(), "not an interface");
        check(hasInterface(c, "javax.tools.JavaFileObject"), "implements JavaFileObject");
        Field uri = c.getDeclaredField("uri");
        Field kind = c.getDeclaredField("kind");
        check(java.lang.reflect.Modifier.isProtected(uri.getModifiers())
                && java.lang.reflect.Modifier.isFinal(uri.getModifiers()), "protected final uri field");
        check(java.lang.reflect.Modifier.isProtected(kind.getModifiers())
                && java.lang.reflect.Modifier.isFinal(kind.getModifiers()), "protected final kind field");
        Constructor<?> ctor = c.getDeclaredConstructors()[0];
        check(java.lang.reflect.Modifier.isProtected(ctor.getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final URI uri = URI.create("string:///X.java");
        javax.tools.JavaFileObject jfo = new javax.tools.SimpleJavaFileObject(
                uri, javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class X {}";
            }
        };

        check(jfo.toUri().equals(uri), "toUri round-trip");
        check("/X.java".equals(jfo.getName()), "getName is uri path");
        check(jfo.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind");
        check(jfo.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible X/SOURCE");
        check(!jfo.isNameCompatible("Y", javax.tools.JavaFileObject.Kind.SOURCE), "not compatible Y/SOURCE");
        check(jfo.getNestingKind() == null, "getNestingKind null");
        check(jfo.getAccessLevel() == null, "getAccessLevel null");
        check(jfo.getLastModified() == 0L, "getLastModified 0");
        check(!jfo.delete(), "delete false");
        check("class X {}".contentEquals(jfo.getCharContent(false)), "getCharContent");

        // openReader wraps getCharContent.
        java.io.Reader r = jfo.openReader(false);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[64];
        int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        r.close();
        check("class X {}".contentEquals(sb), "openReader wraps content");

        // Unsupported operations throw.
        try {
            jfo.openInputStream();
            throw new AssertionError("openInputStream should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "openInputStream unsupported");
        }
        try {
            jfo.openOutputStream();
            throw new AssertionError("openOutputStream should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "openOutputStream unsupported");
        }

        check(jfo.toString() != null && jfo.toString().contains(uri.toString()), "toString contains uri");

        // forSource factory returns SOURCE kind with the given content.
        javax.tools.JavaFileObject src =
                javax.tools.SimpleJavaFileObject.forSource(URI.create("string:///Z.java"), "class Z {}");
        check(src.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "forSource kind");
        check("class Z {}".contentEquals(src.getCharContent(false)), "forSource content");
        check(src.isNameCompatible("Z", javax.tools.JavaFileObject.Kind.SOURCE), "forSource name compatible");

        // Constructor rejects a URI with no path.
        try {
            new javax.tools.SimpleJavaFileObject(URI.create("urn:test"), javax.tools.JavaFileObject.Kind.SOURCE) {};
            throw new AssertionError("null-path URI should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "constructor rejects null-path URI");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
