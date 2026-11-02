/*
 * @test
 * @summary javax.smartcardio.CardTerminal conformance: abstract class.
 * @run main CardTerminalTest
 */

import java.lang.reflect.*;


public class CardTerminalTest {
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
        Class<?> c = Class.forName("javax.smartcardio.CardTerminal");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "isCardPresent"), "declares isCardPresent");
        check(hasMethod(c, "waitForCardPresent"), "declares waitForCardPresent");
        check(hasMethod(c, "waitForCardAbsent"), "declares waitForCardAbsent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
