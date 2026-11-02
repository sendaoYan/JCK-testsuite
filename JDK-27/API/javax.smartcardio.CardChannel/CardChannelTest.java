/*
 * @test
 * @summary javax.smartcardio.CardChannel conformance: abstract class.
 * @run main CardChannelTest
 */

import java.lang.reflect.*;


public class CardChannelTest {
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
        Class<?> c = Class.forName("javax.smartcardio.CardChannel");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getCard"), "declares getCard");
        check(hasMethod(c, "getChannelNumber"), "declares getChannelNumber");
        check(hasMethod(c, "transmit"), "declares transmit");
        check(hasMethod(c, "close"), "declares close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
