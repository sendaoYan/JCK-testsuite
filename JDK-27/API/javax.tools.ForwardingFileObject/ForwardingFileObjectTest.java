/*
 * @test
 * @summary javax.tools.ForwardingFileObject conformance: white-box structure + black-box behavior.
 * @run main ForwardingFileObjectTest
 */

import java.lang.reflect.*;
import java.net.URI;

public class ForwardingFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.ForwardingFileObject");
        check(!c.isInterface(), "not an interface");
        check(hasInterface(c, "javax.tools.FileObject"), "implements FileObject");
        Field f = c.getDeclaredField("fileObject");
        check(java.lang.reflect.Modifier.isProtected(f.getModifiers())
                && java.lang.reflect.Modifier.isFinal(f.getModifiers()), "protected final fileObject field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final javax.tools.SimpleJavaFileObject sjfo = new javax.tools.SimpleJavaFileObject(
                URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class X {}";
            }
        };
        javax.tools.ForwardingFileObject<javax.tools.FileObject> f =
                new javax.tools.ForwardingFileObject<javax.tools.FileObject>(sjfo) {};

        check(f.toUri().equals(sjfo.toUri()), "toUri forwarded");
        check(f.getName().equals(sjfo.getName()), "getName forwarded");
        check(f.getLastModified() == sjfo.getLastModified(), "getLastModified forwarded");
        check("class X {}".contentEquals(f.getCharContent(false)), "getCharContent forwarded");

        // openReader delegates to the wrapped getCharContent.
        java.io.Reader r = f.openReader(false);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[64];
        int n;
        while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
        r.close();
        check("class X {}".contentEquals(sb), "openReader forwarded");

        // openInputStream / openOutputStream / openWriter are unsupported by the delegate.
        try {
            f.openInputStream();
            throw new AssertionError("openInputStream should be unsupported");
        } catch (UnsupportedOperationException expected) {
            check(true, "openInputStream unsupported");
        }
        try {
            f.openOutputStream();
            throw new AssertionError("openOutputStream should be unsupported");
        } catch (UnsupportedOperationException expected) {
            check(true, "openOutputStream unsupported");
        }
        check(!f.delete(), "delete forwarded (false)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
