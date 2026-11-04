/*
 * @test
 * @summary javax.tools.ForwardingJavaFileObject conformance: white-box structure + black-box behavior.
 * @run main ForwardingJavaFileObjectTest
 */

import java.lang.reflect.*;
import java.net.URI;

public class ForwardingJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.ForwardingJavaFileObject");
        check(!c.isInterface(), "not an interface");
        check(hasInterface(c, "javax.tools.JavaFileObject"), "implements JavaFileObject");
        check(c.getSuperclass() == javax.tools.ForwardingFileObject.class, "extends ForwardingFileObject");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final javax.tools.SimpleJavaFileObject sjfo = new javax.tools.SimpleJavaFileObject(
                URI.create("string:///X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return "class X {}";
            }
        };
        javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject> f =
                new javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject>(sjfo) {};

        check(f.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind forwarded");
        check(f.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible X/SOURCE true");
        check(!f.isNameCompatible("Y", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible Y/SOURCE false");
        check(!f.isNameCompatible("X", javax.tools.JavaFileObject.Kind.CLASS), "isNameCompatible X/CLASS false");
        check(f.getNestingKind() == null, "getNestingKind forwarded null");
        check(f.getAccessLevel() == null, "getAccessLevel forwarded null");
        check("class X {}".contentEquals(f.getCharContent(false)), "getCharContent forwarded");
        check(f.toUri().equals(sjfo.toUri()), "toUri forwarded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
