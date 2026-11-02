/*
 * @test
 * @summary jdk.javadoc.doclet.Reporter conformance: interface.
 * @run main ReporterTest
 */

import java.lang.reflect.*;


public class ReporterTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.Reporter");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "print"), "declares print");
        check(hasMethod(c, "getStandardWriter"), "declares getStandardWriter");
        check(hasMethod(c, "getDiagnosticWriter"), "declares getDiagnosticWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
