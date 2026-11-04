/*
 * @test
 * @summary javax.smartcardio.CardTerminals conformance: white-box structure + black-box behavior.
 * @run main CardTerminalsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardTerminalsTest {
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
        Class<?> c = Class.forName("javax.smartcardio.CardTerminals");
        check(Modifier.isAbstract(c.getModifiers()), "CardTerminals is abstract");
        check(!c.isInterface(), "CardTerminals not interface");
        check(c.getSuperclass() == Object.class, "CardTerminals extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "CardTerminals protected ctor");
        // list(State) and waitForChange(long) are abstract; list()/getTerminal()/waitForChange() are concrete
        for (String n : new String[]{"list","getTerminal","waitForChange"}) {
            check(hasMethod(c, n), "CardTerminals declares " + n);
        }
        check(Modifier.isAbstract(c.getDeclaredMethod("list", Class.forName("javax.smartcardio.CardTerminals$State")).getModifiers()),
                "CardTerminals list(State) abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("waitForChange", long.class).getModifiers()),
                "CardTerminals waitForChange(long) abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("list").getModifiers()), "CardTerminals list() concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("getTerminal", String.class).getModifiers()), "CardTerminals getTerminal() concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final List<javax.smartcardio.CardTerminal> terminals = new ArrayList<>();
        terminals.add(new javax.smartcardio.CardTerminal() {
            public String getName() { return "T1"; }
            public javax.smartcardio.Card connect(String p) { return null; }
            public boolean isCardPresent() { return false; }
            public boolean waitForCardPresent(long t) { return false; }
            public boolean waitForCardAbsent(long t) { return true; }
        });
        terminals.add(new javax.smartcardio.CardTerminal() {
            public String getName() { return "T2"; }
            public javax.smartcardio.Card connect(String p) { return null; }
            public boolean isCardPresent() { return true; }
            public boolean waitForCardPresent(long t) { return true; }
            public boolean waitForCardAbsent(long t) { return false; }
        });

        final long[] waitTimeout = new long[]{-1};
        javax.smartcardio.CardTerminals cts = new javax.smartcardio.CardTerminals() {
            public List<javax.smartcardio.CardTerminal> list(javax.smartcardio.CardTerminals.State state) {
                if (state == null) throw new NullPointerException();
                if (state == javax.smartcardio.CardTerminals.State.ALL) return terminals;
                return Collections.emptyList();
            }
            public boolean waitForChange(long timeout) throws javax.smartcardio.CardException {
                waitTimeout[0] = timeout;
                if (timeout < 0) throw new IllegalArgumentException();
                return false;
            }
        };

        // concrete list() delegates to list(ALL)
        check(cts.list().size() == 2, "CardTerminals list() size");
        check(cts.list().contains(terminals.get(0)) && cts.list().contains(terminals.get(1)), "CardTerminals list() contents");
        check(cts.list(javax.smartcardio.CardTerminals.State.ALL).size() == 2, "CardTerminals list(ALL) size");
        check(cts.list(javax.smartcardio.CardTerminals.State.CARD_PRESENT).isEmpty(), "CardTerminals list(state) empty (stub)");

        // list(null) -> NPE
        assertThrows(NullPointerException.class, () -> cts.list(null), "CardTerminals list(null) NPE");

        // getTerminal via concrete list() lookup
        javax.smartcardio.CardTerminal t1 = cts.getTerminal("T1");
        check(t1 != null && "T1".equals(t1.getName()), "CardTerminals getTerminal hit");
        check(cts.getTerminal("missing") == null, "CardTerminals getTerminal miss -> null");
        assertThrows(NullPointerException.class, () -> cts.getTerminal(null), "CardTerminals getTerminal(null) NPE");

        // concrete waitForChange() delegates to waitForChange(0)
        cts.waitForChange();
        check(waitTimeout[0] == 0, "CardTerminals waitForChange() delegates timeout 0");
        check(!cts.waitForChange(100), "CardTerminals waitForChange(timeout) false");
        check(waitTimeout[0] == 100, "CardTerminals waitForChange(timeout) passes through");
        assertThrows(IllegalArgumentException.class, () -> cts.waitForChange(-1), "CardTerminals waitForChange(-1) IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
