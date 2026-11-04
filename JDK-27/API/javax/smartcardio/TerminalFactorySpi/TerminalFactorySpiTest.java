/*
 * @test
 * @summary javax.smartcardio.TerminalFactorySpi conformance: white-box structure + black-box behavior.
 * @run main TerminalFactorySpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TerminalFactorySpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.TerminalFactorySpi");
        check(Modifier.isAbstract(c.getModifiers()), "TerminalFactorySpi is abstract");
        check(!c.isInterface(), "TerminalFactorySpi not interface");
        check(c.getSuperclass() == Object.class, "TerminalFactorySpi extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "TerminalFactorySpi protected ctor");
        Method m = c.getDeclaredMethod("engineTerminals");
        check(m != null, "TerminalFactorySpi declares engineTerminals");
        check(Modifier.isAbstract(m.getModifiers()), "TerminalFactorySpi engineTerminals abstract");
        check(Modifier.isProtected(m.getModifiers()), "TerminalFactorySpi engineTerminals protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // a concrete SPI implementation returning a deterministic CardTerminals
        final javax.smartcardio.CardTerminals result = new javax.smartcardio.CardTerminals() {
            public java.util.List<javax.smartcardio.CardTerminal> list(javax.smartcardio.CardTerminals.State state) {
                return java.util.Collections.emptyList();
            }
            public boolean waitForChange(long timeout) { return false; }
        };

        // engineTerminals is protected, so expose it via a subclass wrapper
        class MySpi extends javax.smartcardio.TerminalFactorySpi {
            protected javax.smartcardio.CardTerminals engineTerminals() { return result; }
            javax.smartcardio.CardTerminals publicTerminals() { return engineTerminals(); }
        }

        MySpi spi = new MySpi();
        check(spi.publicTerminals() == result, "TerminalFactorySpi engineTerminals identity");
        check(spi.publicTerminals().list(javax.smartcardio.CardTerminals.State.ALL).isEmpty(),
                "TerminalFactorySpi engineTerminals returns usable CardTerminals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
