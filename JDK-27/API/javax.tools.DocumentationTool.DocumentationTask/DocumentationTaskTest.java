/*
 * @test
 * @summary javax.tools.DocumentationTool.DocumentationTask conformance: interface.
 * @run main DocumentationTaskTest
 */

import java.lang.reflect.*;


public class DocumentationTaskTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$DocumentationTask");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.util.concurrent.Callable"), "extends java.util.concurrent.Callable");
        check(hasMethod(c, "addModules"), "declares addModules");
        check(hasMethod(c, "setLocale"), "declares setLocale");
        check(hasMethod(c, "call"), "declares call");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
