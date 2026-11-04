/*
 * @test
 * @summary javax.smartcardio.CardTerminal conformance: white-box structure + black-box behavior.
 * @run main CardTerminalTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CardTerminalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardTerminal");
        check(Modifier.isAbstract(c.getModifiers()), "CardTerminal is abstract");
        check(!c.isInterface(), "CardTerminal not interface");
        check(c.getSuperclass() == Object.class, "CardTerminal extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "CardTerminal protected ctor");
        for (String n : new String[]{"getName","connect","isCardPresent","waitForCardPresent","waitForCardAbsent"}) {
            check(hasMethod(c, n), "CardTerminal declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] presentCalls = new int[1];
        final int[] connectCalls = new int[1];

        javax.smartcardio.CardTerminal term = new javax.smartcardio.CardTerminal() {
            public String getName() { return "Terminal-1"; }
            public javax.smartcardio.Card connect(String protocol) throws javax.smartcardio.CardException {
                connectCalls[0]++;
                if (protocol == null) {
                    return new javax.smartcardio.Card() {
                        public javax.smartcardio.ATR getATR() { return new javax.smartcardio.ATR(new byte[]{0x3B, 0x00}); }
                        public String getProtocol() { return "T=0"; }
                        public javax.smartcardio.CardChannel getBasicChannel() { return null; }
                        public javax.smartcardio.CardChannel openLogicalChannel() { return null; }
                        public void beginExclusive() {}
                        public void endExclusive() {}
                        public byte[] transmitControlCommand(int c, byte[] b) { return b; }
                        public void disconnect(boolean reset) {}
                    };
                }
                // no card present -> deterministic exception for "*"
                throw new javax.smartcardio.CardNotPresentException("no card");
            }
            public boolean isCardPresent() { presentCalls[0]++; return false; }
            public boolean waitForCardPresent(long timeout) { return false; }
            public boolean waitForCardAbsent(long timeout) { return true; }
        };

        check("Terminal-1".equals(term.getName()), "CardTerminal getName");

        // isCardPresent callable, deterministic false
        check(!term.isCardPresent() && presentCalls[0] == 1, "CardTerminal isCardPresent");

        // wait methods deterministic
        check(!term.waitForCardPresent(0), "CardTerminal waitForCardPresent false");
        check(term.waitForCardAbsent(0), "CardTerminal waitForCardAbsent true");
        check(term.waitForCardPresent(1000) == false, "CardTerminal waitForCardPresent(timeout)");
        check(term.waitForCardAbsent(1000) == true, "CardTerminal waitForCardAbsent(timeout)");

        // connect("*") with no card -> CardNotPresentException
        boolean threw = false;
        try { term.connect("*"); } catch (javax.smartcardio.CardNotPresentException e) { threw = true; }
        check(threw && connectCalls[0] == 1, "CardTerminal connect no-card -> CardNotPresentException");

        // connect(null) -> deterministic Card
        try {
            javax.smartcardio.Card card = term.connect(null);
            check(card != null && "T=0".equals(card.getProtocol()), "CardTerminal connect(null) -> Card");
        } catch (javax.smartcardio.CardException e) {
            check(false, "CardTerminal connect(null) should not throw");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
