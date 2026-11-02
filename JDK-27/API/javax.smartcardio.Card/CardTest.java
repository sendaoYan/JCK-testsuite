/*
 * @test
 * @summary javax.smartcardio.Card conformance: abstract class.
 * @run main CardTest
 */

import java.lang.reflect.*;


public class CardTest {
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
        Class<?> c = Class.forName("javax.smartcardio.Card");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getATR"), "declares getATR");
        check(hasMethod(c, "getProtocol"), "declares getProtocol");
        check(hasMethod(c, "getBasicChannel"), "declares getBasicChannel");
        check(hasMethod(c, "openLogicalChannel"), "declares openLogicalChannel");
        check(hasMethod(c, "beginExclusive"), "declares beginExclusive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
