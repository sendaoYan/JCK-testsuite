/*
 * @test
 * @summary javax.smartcardio.ATR conformance: white-box structure + black-box behavior.
 * @run main ATRTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ATRTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void assertThrows(Class<? extends Throwable> t, Runnable r, String m) {
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
    static byte[] hex(String s) {
        String[] parts = s.trim().split("\\s+");
        byte[] out = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = (byte) Integer.parseInt(parts[i], 16);
        return out;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.ATR");
        check(Modifier.isFinal(c.getModifiers()), "ATR is final");
        check(!c.isInterface(), "ATR not interface");
        check(c.getSuperclass() == Object.class, "ATR extends Object");
        check(Serializable.class.isAssignableFrom(c), "ATR implements Serializable");
        Constructor<?> ctor = c.getConstructor(byte[].class);
        check(ctor != null, "ATR(byte[]) ctor present");
        for (String n : new String[]{"getBytes", "getHistoricalBytes", "toString", "equals", "hashCode"}) {
            check(hasMethod(c, n), "ATR declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // header-only ATR: {3B 00} -> no historical bytes
        byte[] raw = new byte[]{0x3B, 0x00};
        javax.smartcardio.ATR atr = new javax.smartcardio.ATR(raw);
        byte[] got = atr.getBytes();
        check(got.length == 2, "ATR getBytes length");
        check(got[0] == 0x3B && got[1] == 0x00, "ATR getBytes content");
        check(atr.getHistoricalBytes().length == 0, "ATR no historical bytes");
        check("ATR: 2 bytes".equals(atr.toString()), "ATR toString");

        // defensive copy: mutating input array must not change ATR
        raw[1] = 0x7F;
        check(atr.getBytes()[1] == 0x00, "ATR defensive copy of input");

        // defensive copy: mutating returned array must not change ATR
        byte[] returned = atr.getBytes();
        returned[0] = 0x00;
        check(atr.getBytes()[0] == 0x3B, "ATR defensive copy of output");

        // equals / hashCode
        javax.smartcardio.ATR atr2 = new javax.smartcardio.ATR(new byte[]{0x3B, 0x00});
        check(atr.equals(atr2) && atr2.equals(atr), "ATR equals");
        check(atr.hashCode() == atr2.hashCode(), "ATR hashCode");
        check(!atr.equals(new javax.smartcardio.ATR(new byte[]{0x3F, 0x00})), "ATR not equals different bytes");
        check(!atr.equals(null) && !atr.equals("x"), "ATR not equals non-ATR");

        // null -> NPE
        assertThrows(NullPointerException.class, () -> new javax.smartcardio.ATR(null), "ATR null NPE");

        // historical bytes parsing (cases from JDK HistoricalBytes test)
        byte[] a1 = hex("3B 65 00 00 9C 02 02 07 02");
        javax.smartcardio.ATR h1 = new javax.smartcardio.ATR(a1);
        check(Arrays.equals(h1.getHistoricalBytes(), hex("9C 02 02 07 02")), "ATR historical bytes (TA only)");

        byte[] a2 = hex("3B 95 18 40 FF 62 01 02 01 04");
        javax.smartcardio.ATR h2 = new javax.smartcardio.ATR(a2);
        check(Arrays.equals(h2.getHistoricalBytes(), hex("62 01 02 01 04")), "ATR historical bytes (TA/TB/TC)");

        // historical bytes defensive copy
        byte[] hb = h1.getHistoricalBytes();
        hb[0] = 0;
        check((h1.getHistoricalBytes()[0] & 0xff) == 0x9C, "ATR historical bytes defensive copy");

        // first byte not 0x3B/0x3F -> no historical bytes parsed
        javax.smartcardio.ATR inv = new javax.smartcardio.ATR(new byte[]{0x00, 0x00, 0x01, 0x02});
        check(inv.getHistoricalBytes().length == 0, "ATR invalid first byte no historical");

        // empty / very short ATR accepted as raw bytes
        javax.smartcardio.ATR empty = new javax.smartcardio.ATR(new byte[0]);
        check(empty.getBytes().length == 0 && empty.getHistoricalBytes().length == 0, "ATR empty array");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
