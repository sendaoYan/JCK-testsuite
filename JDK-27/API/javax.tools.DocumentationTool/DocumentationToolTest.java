/*
 * @test
 * @summary javax.tools.DocumentationTool conformance: interface.
 * @run main DocumentationToolTest
 */

import java.lang.reflect.*;


public class DocumentationToolTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 2, "2 superinterface(s)");
        check(hasInterface(c, "javax.tools.Tool"), "extends javax.tools.Tool");
        check(hasInterface(c, "javax.tools.OptionChecker"), "extends javax.tools.OptionChecker");
        check(hasMethod(c, "getTask"), "declares getTask");
        check(hasMethod(c, "getStandardFileManager"), "declares getStandardFileManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
