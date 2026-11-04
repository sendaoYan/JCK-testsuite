/*
 * @test
 * @summary javax.smartcardio.CommandAPDU conformance: white-box structure + black-box behavior.
 * @run main CommandAPDUTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CommandAPDUTest {
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
    static boolean arrayEquals(byte[] a, byte[] b) { return Arrays.equals(a, b); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CommandAPDU");
        check(Modifier.isFinal(c.getModifiers()), "CommandAPDU is final");
        check(!c.isInterface(), "CommandAPDU not interface");
        check(Serializable.class.isAssignableFrom(c), "CommandAPDU implements Serializable");
        for (String n : new String[]{"getCLA","getINS","getP1","getP2","getNc","getData","getNe","getBytes","toString","equals","hashCode"}) {
            check(hasMethod(c, n), "CommandAPDU declares " + n);
        }
        // multiple public constructors must exist
        int ctorCount = 0;
        for (Constructor<?> ct : c.getConstructors()) ctorCount++;
        check(ctorCount >= 8, "CommandAPDU has >= 8 public ctors (got " + ctorCount + ")");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // case 1: header only
        javax.smartcardio.CommandAPDU c1 = new javax.smartcardio.CommandAPDU(0x00, 0xA4, 0x04, 0x00);
        check(c1.getCLA() == 0x00, "case1 CLA");
        check(c1.getINS() == 0xA4, "case1 INS");
        check(c1.getP1() == 0x04, "case1 P1");
        check(c1.getP2() == 0x00, "case1 P2");
        check(c1.getNc() == 0, "case1 Nc");
        check(c1.getNe() == 0, "case1 Ne");
        check(c1.getData().length == 0, "case1 data empty");
        check(c1.getBytes().length == 4, "case1 bytes length 4");

        // header values are masked to low 8 bits
        javax.smartcardio.CommandAPDU masked = new javax.smartcardio.CommandAPDU(0x1A5, 0x2B4, 0x3C3, 0x4D2);
        check(masked.getCLA() == 0xA5 && masked.getINS() == 0xB4 && masked.getP1() == 0xC3 && masked.getP2() == 0xD2,
                "CommandAPDU header low-8-bit masking");

        // case 3: header + data (short form)
        byte[] data3 = new byte[]{1, 2, 3};
        javax.smartcardio.CommandAPDU c3 = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, data3);
        check(c3.getNc() == 3, "case3 Nc");
        check(arrayEquals(c3.getData(), data3), "case3 data round-trip");
        check(c3.getNe() == 0, "case3 Ne 0");
        check(c3.getBytes().length == 4 + 1 + 3, "case3 bytes length 8");
        check(c3.getBytes()[4] == 3, "case3 Lc byte");

        // case 3: null/empty data -> case 1
        javax.smartcardio.CommandAPDU c3n = new javax.smartcardio.CommandAPDU(0, 1, 2, 3, (byte[]) null);
        check(c3n.getNc() == 0 && c3n.getNe() == 0 && c3n.getBytes().length == 4, "case3 null data -> case1");

        // case 2: header + Ne (short form)
        javax.smartcardio.CommandAPDU c2a = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, 255);
        check(c2a.getNe() == 255 && c2a.getBytes().length == 5 && c2a.getBytes()[4] == (byte) 255, "case2s Ne=255");
        javax.smartcardio.CommandAPDU c2b = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, 256);
        check(c2b.getNe() == 256 && c2b.getBytes().length == 5 && c2b.getBytes()[4] == 0, "case2s Ne=256 encodes 0x00");

        // case 2: extended form Ne=65536
        javax.smartcardio.CommandAPDU c2e = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, 65536);
        check(c2e.getNe() == 65536, "case2e Ne=65536");
        check(c2e.getBytes().length == 7, "case2e bytes length 7");
        check(c2e.getBytes()[4] == 0 && c2e.getBytes()[5] == 0 && c2e.getBytes()[6] == 0, "case2e Le encoded 00 00");

        // case 3: extended form (data length > 255)
        byte[] big = new byte[256];
        for (int i = 0; i < big.length; i++) big[i] = (byte) i;
        javax.smartcardio.CommandAPDU c3e = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, big);
        check(c3e.getNc() == 256, "case3e Nc 256");
        check(arrayEquals(c3e.getData(), big), "case3e data round-trip");
        check(c3e.getBytes().length == 4 + 3 + 256, "case3e bytes length 263");
        check(c3e.getBytes()[4] == 0 && c3e.getBytes()[5] == 1 && c3e.getBytes()[6] == 0, "case3e Lc encoded 01 00");

        // case 4: header + data + Ne (short form)
        javax.smartcardio.CommandAPDU c4 = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, new byte[]{9, 8, 7, 6, 5}, 10);
        check(c4.getNc() == 5 && c4.getNe() == 10, "case4 Nc/Ne");
        check(arrayEquals(c4.getData(), new byte[]{9, 8, 7, 6, 5}), "case4 data");
        check(c4.getBytes().length == 4 + 2 + 5, "case4 bytes length 11");
        check(c4.getBytes()[4] == 5 && c4.getBytes()[10] == 10, "case4 Lc/Le bytes");

        // case 4: extended form (data 256 + Ne 65536)
        javax.smartcardio.CommandAPDU c4e = new javax.smartcardio.CommandAPDU(0, 0x20, 0, 0, big, 65536);
        check(c4e.getNc() == 256 && c4e.getNe() == 65536, "case4e Nc/Ne");
        check(c4e.getBytes().length == 4 + 5 + 256, "case4e bytes length 265");

        // byte[] constructor round-trip (C1 from JDK CommandAPDUTest)
        byte[] c1bytes = hex("00 A4 04 00 07 A0 00 00 00 62 81 01 00");
        javax.smartcardio.CommandAPDU fromBytes = new javax.smartcardio.CommandAPDU(c1bytes);
        check(fromBytes.getCLA() == 0x00 && fromBytes.getINS() == 0xA4 && fromBytes.getP1() == 0x04 && fromBytes.getP2() == 0x00,
                "byte[] ctor header");
        check(fromBytes.getNc() == 7, "byte[] ctor Nc=7");
        check(fromBytes.getNe() == 256, "byte[] ctor Ne=256 (Le 0x00)");
        check(arrayEquals(fromBytes.getData(), hex("A0 00 00 00 62 81 01")), "byte[] ctor data");
        check(arrayEquals(fromBytes.getBytes(), c1bytes), "byte[] ctor getBytes round-trip");

        // round-trip: encode then decode
        javax.smartcardio.CommandAPDU rt = new javax.smartcardio.CommandAPDU(new javax.smartcardio.CommandAPDU(0, 0x30, 1, 2, big, 65536).getBytes());
        check(rt.getNc() == 256 && rt.getNe() == 65536 && arrayEquals(rt.getData(), big), "CommandAPDU encode/decode round-trip");

        // offset constructor
        byte[] padded = new byte[3 + c1bytes.length];
        System.arraycopy(c1bytes, 0, padded, 3, c1bytes.length);
        javax.smartcardio.CommandAPDU off = new javax.smartcardio.CommandAPDU(padded, 3, c1bytes.length);
        check(off.getNc() == 7 && off.getNe() == 256 && arrayEquals(off.getBytes(), c1bytes), "offset ctor");

        // ByteBuffer constructor advances position to limit
        ByteBuffer bb = ByteBuffer.wrap(c1bytes);
        javax.smartcardio.CommandAPDU fromBB = new javax.smartcardio.CommandAPDU(bb);
        check(bb.position() == bb.limit(), "ByteBuffer ctor consumes to limit");
        check(fromBB.getNc() == 7 && fromBB.getNe() == 256, "ByteBuffer ctor parse");

        // defensive copy of input
        byte[] input = new byte[]{0, 0x20, 0, 0, 5, 1, 2, 3, 4, 5};
        javax.smartcardio.CommandAPDU dc = new javax.smartcardio.CommandAPDU(input);
        input[5] = 99;
        check(dc.getData()[0] == 1, "CommandAPDU defensive copy of input");

        // defensive copy of output
        byte[] out = dc.getBytes();
        out[0] = 99;
        check(dc.getBytes()[0] == 0, "CommandAPDU defensive copy of output");

        // equals / hashCode / toString
        javax.smartcardio.CommandAPDU eq1 = new javax.smartcardio.CommandAPDU(0, 1, 2, 3, new byte[]{4, 5});
        javax.smartcardio.CommandAPDU eq2 = new javax.smartcardio.CommandAPDU(0, 1, 2, 3, new byte[]{4, 5});
        check(eq1.equals(eq2) && eq2.equals(eq1), "CommandAPDU equals");
        check(eq1.hashCode() == eq2.hashCode(), "CommandAPDU hashCode");
        check(!eq1.equals(new javax.smartcardio.CommandAPDU(0, 1, 2, 3)), "CommandAPDU not equals");
        check(eq1.toString() != null, "CommandAPDU toString");

        // exceptions
        assertThrows(NullPointerException.class, () -> new javax.smartcardio.CommandAPDU((byte[]) null), "null byte[] NPE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(new byte[]{0, 1, 2}), "short byte[] IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(new byte[]{0, 0, 0, 0, 4, 1}), "invalid APDU length IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(new byte[]{1, 2, 3, 4}, -1, 4), "negative offset IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(0, 0, 0, 0, -1), "negative Ne IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(0, 0, 0, 0, 65537), "Ne > 65536 IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.CommandAPDU(0, 0, 0, 0, new byte[65536]), "dataLength > 65535 IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
