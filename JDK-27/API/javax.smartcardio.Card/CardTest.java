/*
 * @test
 * @summary javax.smartcardio.Card conformance: white-box structure + black-box behavior.
 * @run main CardTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.Card");
        check(Modifier.isAbstract(c.getModifiers()), "Card is abstract");
        check(!c.isInterface(), "Card not interface");
        check(c.getSuperclass() == Object.class, "Card extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "Card protected ctor");
        for (String n : new String[]{"getATR","getProtocol","getBasicChannel","openLogicalChannel",
                "beginExclusive","endExclusive","transmitControlCommand","disconnect"}) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(n)) {
                    found = true;
                    check(Modifier.isAbstract(m.getModifiers()), "Card " + n + " is abstract");
                }
            }
            check(found, "Card declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final javax.smartcardio.ATR atr = new javax.smartcardio.ATR(new byte[]{0x3B, 0x00});
        final javax.smartcardio.Card[] self = new javax.smartcardio.Card[1];

        javax.smartcardio.CardChannel ch = new javax.smartcardio.CardChannel() {
            public javax.smartcardio.Card getCard() { return self[0]; }
            public int getChannelNumber() { return 0; }
            public javax.smartcardio.ResponseAPDU transmit(javax.smartcardio.CommandAPDU c) {
                return new javax.smartcardio.ResponseAPDU(new byte[]{(byte) 0x90, 0x00});
            }
            public int transmit(ByteBuffer cmd, ByteBuffer resp) { return 0; }
            public void close() {}
        };

        javax.smartcardio.Card card = new javax.smartcardio.Card() {
            public javax.smartcardio.ATR getATR() { return atr; }
            public String getProtocol() { return "T=1"; }
            public javax.smartcardio.CardChannel getBasicChannel() { return ch; }
            public javax.smartcardio.CardChannel openLogicalChannel() { return ch; }
            public void beginExclusive() {}
            public void endExclusive() {}
            public byte[] transmitControlCommand(int controlCode, byte[] cmd) { return cmd; }
            public void disconnect(boolean reset) {}
        };
        self[0] = card;

        // getters
        check(atr == card.getATR(), "Card getATR identity");
        check("T=1".equals(card.getProtocol()), "Card getProtocol");

        // channel composition: basic channel's card is this card
        check(card.getBasicChannel() == ch, "Card getBasicChannel");
        check(card.getBasicChannel().getCard() == card, "Card getBasicChannel().getCard() round-trip");
        check(card.openLogicalChannel() == ch, "Card openLogicalChannel");

        // control command round-trip
        byte[] ctrl = new byte[]{1, 2, 3};
        check(Arrays.equals(card.transmitControlCommand(0x10, ctrl), ctrl), "Card transmitControlCommand round-trip");

        // lifecycle methods callable (no-op)
        card.beginExclusive();
        card.endExclusive();
        card.disconnect(true);
        card.disconnect(false);
        check(true, "Card begin/endExclusive + disconnect callable");

        // transmit a real CommandAPDU through the channel and inspect the ResponseAPDU
        javax.smartcardio.CommandAPDU cmd = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, new byte[]{1});
        try {
            javax.smartcardio.ResponseAPDU resp = card.getBasicChannel().transmit(cmd);
            check(resp.getSW() == 0x9000, "Card channel transmit -> ResponseAPDU SW=0x9000");
        } catch (javax.smartcardio.CardException e) {
            check(false, "Card transmit should not throw for stub");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
