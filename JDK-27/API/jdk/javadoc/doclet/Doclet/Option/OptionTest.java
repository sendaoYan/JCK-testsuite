/*
 * @test
 * @summary jdk.javadoc.doclet.Doclet.Option conformance: interface.
 * @run main OptionTest
 */

import java.lang.reflect.*;


public class OptionTest {
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
        Class<?> c = Class.forName("jdk.javadoc.doclet.Doclet$Option");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getArgumentCount"), "declares getArgumentCount");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getKind"), "declares getKind");
        check(hasMethod(c, "getNames"), "declares getNames");
        check(hasMethod(c, "getParameters"), "declares getParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
