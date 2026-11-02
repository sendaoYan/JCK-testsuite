/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedText conformance: interface.
 * @run main AccessibleExtendedTextTest
 */

import java.lang.reflect.*;


public class AccessibleExtendedTextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getTextRange"), "declares getTextRange");
        check(hasMethod(c, "getTextSequenceAt"), "declares getTextSequenceAt");
        check(hasMethod(c, "getTextSequenceAfter"), "declares getTextSequenceAfter");
        check(hasMethod(c, "getTextSequenceBefore"), "declares getTextSequenceBefore");
        check(hasMethod(c, "getTextBounds"), "declares getTextBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
