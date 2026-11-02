/*
 * @test
 * @summary javax.smartcardio.CardTerminals conformance: abstract class.
 * @run main CardTerminalsTest
 */

import java.lang.reflect.*;


public class CardTerminalsTest {
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
        Class<?> c = Class.forName("javax.smartcardio.CardTerminals");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "list"), "declares list");
        check(hasMethod(c, "getTerminal"), "declares getTerminal");
        check(hasMethod(c, "waitForChange"), "declares waitForChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
