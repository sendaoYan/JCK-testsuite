/*
 * @test
 * @summary javax.smartcardio.TerminalFactory conformance: white-box structure + black-box behavior.
 * @run main TerminalFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TerminalFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void assertThrows(Class<? extends Throwable> t, ThrowingRunnable r, String m) {
        try { r.run(); } catch (Throwable x) {
            check(t.isInstance(x), m + " (threw " + x.getClass().getSimpleName() + ")");
            return;
        }
        check(false, m + " (no throw)");
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.TerminalFactory");
        check(Modifier.isFinal(c.getModifiers()), "TerminalFactory is final");
        check(!c.isInterface(), "TerminalFactory not interface");
        for (String n : new String[]{"getDefaultType","getDefault","getInstance","getProvider","getType","terminals","toString"}) {
            check(hasMethod(c, n), "TerminalFactory declares " + n);
        }
        // getInstance is overloaded (String,Object), (String,Object,String), (String,Object,Provider)
        int count = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("getInstance")) count++;
        check(count == 3, "TerminalFactory has 3 getInstance overloads (got " + count + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default type is always non-empty
        String type = javax.smartcardio.TerminalFactory.getDefaultType();
        check(type != null && type.length() > 0, "TerminalFactory getDefaultType non-empty");

        // getDefault() never throws; always returns a factory (possibly type None)
        javax.smartcardio.TerminalFactory f = javax.smartcardio.TerminalFactory.getDefault();
        check(f != null, "TerminalFactory getDefault non-null");
        check(type.equals(f.getType()), "TerminalFactory getType == getDefaultType");
        check(f.getProvider() != null && f.getProvider().getName() != null, "TerminalFactory getProvider non-null");
        check(f.toString() != null && f.toString().contains(type), "TerminalFactory toString contains type");
        check(f.terminals() != null, "TerminalFactory terminals() non-null");

        // In a no-hardware environment the default type is "None": deterministic empty terminals.
        if ("None".equals(type)) {
            javax.smartcardio.CardTerminals ct = f.terminals();
            check(ct.list().isEmpty(), "None factory list() empty");
            check(ct.list(javax.smartcardio.CardTerminals.State.ALL).isEmpty(), "None factory list(ALL) empty");
            check(ct.getTerminal("anything") == null, "None factory getTerminal -> null");
            assertThrows(IllegalStateException.class, () -> ct.waitForChange(1), "None factory waitForChange ISE");
        } else {
            // PC/SC-capable machine: terminals() must at least be callable; do not assume readers.
            javax.smartcardio.CardTerminals ct = f.terminals();
            try {
                java.util.List<javax.smartcardio.CardTerminal> l = ct.list();
                check(l != null, "PC/SC factory list() callable");
            } catch (javax.smartcardio.CardException e) {
                // no readers / backend failure is also an acceptable deterministic path
                check(true, "PC/SC factory list() raised CardException (acceptable)");
            }
        }

        // getInstance("None") is documented as unobtainable via getInstance
        assertThrows(java.security.NoSuchAlgorithmException.class,
                () -> javax.smartcardio.TerminalFactory.getInstance("None", null),
                "getInstance(None) NoSuchAlgorithmException");
        assertThrows(NullPointerException.class,
                () -> javax.smartcardio.TerminalFactory.getInstance(null, null),
                "getInstance(null type) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
