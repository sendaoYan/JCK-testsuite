/*
 * @test
 * @summary javax.smartcardio.CardChannel conformance: white-box structure + black-box behavior.
 * @run main CardChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CardChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardChannel");
        check(Modifier.isAbstract(c.getModifiers()), "CardChannel is abstract");
        check(!c.isInterface(), "CardChannel not interface");
        check(c.getSuperclass() == Object.class, "CardChannel extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "CardChannel protected ctor");
        for (String n : new String[]{"getCard","getChannelNumber","transmit","close"}) {
            check(hasMethod(c, n), "CardChannel declares " + n);
        }
        // transmit is overloaded (CommandAPDU and ByteBuffer,ByteBuffer)
        int transmitCount = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("transmit")) transmitCount++;
        check(transmitCount == 2, "CardChannel has 2 transmit overloads (got " + transmitCount + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final javax.smartcardio.Card[] cardHolder = new javax.smartcardio.Card[1];
        final int[] closed = new int[1];

        javax.smartcardio.Card card = new javax.smartcardio.Card() {
            public javax.smartcardio.ATR getATR() { return new javax.smartcardio.ATR(new byte[]{0x3B, 0x00}); }
            public String getProtocol() { return "T=0"; }
            public javax.smartcardio.CardChannel getBasicChannel() { return null; }
            public javax.smartcardio.CardChannel openLogicalChannel() { return null; }
            public void beginExclusive() {}
            public void endExclusive() {}
            public byte[] transmitControlCommand(int controlCode, byte[] cmd) { return cmd; }
            public void disconnect(boolean reset) {}
        };
        cardHolder[0] = card;

        javax.smartcardio.CardChannel ch = new javax.smartcardio.CardChannel() {
            public javax.smartcardio.Card getCard() { return cardHolder[0]; }
            public int getChannelNumber() { return 0; }
            public javax.smartcardio.ResponseAPDU transmit(javax.smartcardio.CommandAPDU c) {
                // deterministic: echo SW 0x9000 and return data as-is
                return new javax.smartcardio.ResponseAPDU(new byte[]{(byte) 0x90, 0x00});
            }
            public int transmit(ByteBuffer cmd, ByteBuffer resp) {
                byte[] data = new byte[cmd.remaining()];
                cmd.get(data);
                resp.put(data);
                return data.length;
            }
            public void close() { closed[0]++; }
        };

        check(ch.getCard() == card, "CardChannel getCard identity");
        check(ch.getChannelNumber() == 0, "CardChannel getChannelNumber");

        // transmit(CommandAPDU) -> ResponseAPDU
        javax.smartcardio.CommandAPDU cmd = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, new byte[]{1, 2});
        javax.smartcardio.ResponseAPDU resp = ch.transmit(cmd);
        check(resp.getSW() == 0x9000 && resp.getNr() == 0, "CardChannel transmit(CommandAPDU) ResponseAPDU");

        // transmit(ByteBuffer, ByteBuffer) round-trip
        ByteBuffer in = ByteBuffer.wrap(new byte[]{5, 6, 7, 8});
        ByteBuffer out = ByteBuffer.allocate(4);
        int n = ch.transmit(in, out);
        check(n == 4 && !in.hasRemaining(), "CardChannel transmit(ByteBuffer) consumed input");
        out.flip();
        check(Arrays.equals(new byte[]{5, 6, 7, 8}, Arrays.copyOfRange(out.array(), 0, 4)), "CardChannel transmit(ByteBuffer) data round-trip");

        // close callable
        ch.close();
        check(closed[0] == 1, "CardChannel close called once");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
