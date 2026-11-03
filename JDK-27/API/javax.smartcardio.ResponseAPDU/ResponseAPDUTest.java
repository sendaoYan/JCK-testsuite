/*
 * @test
 * @summary javax.smartcardio.ResponseAPDU conformance: white-box structure + black-box behavior.
 * @run main ResponseAPDUTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ResponseAPDUTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.ResponseAPDU");
        check(Modifier.isFinal(c.getModifiers()), "ResponseAPDU is final");
        check(!c.isInterface(), "ResponseAPDU not interface");
        check(Serializable.class.isAssignableFrom(c), "ResponseAPDU implements Serializable");
        Constructor<?> ctor = c.getConstructor(byte[].class);
        check(ctor != null, "ResponseAPDU(byte[]) ctor present");
        for (String n : new String[]{"getNr","getData","getSW1","getSW2","getSW","getBytes","toString","equals","hashCode"}) {
            check(hasMethod(c, n), "ResponseAPDU declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // data + 2-byte SW
        javax.smartcardio.ResponseAPDU r = new javax.smartcardio.ResponseAPDU(new byte[]{0x11, 0x22, (byte) 0x90, 0x00});
        check(r.getNr() == 2, "ResponseAPDU getNr");
        check(Arrays.equals(r.getData(), new byte[]{0x11, 0x22}), "ResponseAPDU getData");
        check(r.getSW1() == 0x90, "ResponseAPDU getSW1");
        check(r.getSW2() == 0x00, "ResponseAPDU getSW2");
        check(r.getSW() == 0x9000, "ResponseAPDU getSW");
        check(Arrays.equals(r.getBytes(), new byte[]{0x11, 0x22, (byte) 0x90, 0x00}), "ResponseAPDU getBytes round-trip");
        check("ResponseAPDU: 4 bytes, SW=9000".equals(r.toString()), "ResponseAPDU toString");

        // SW-only (no body)
        javax.smartcardio.ResponseAPDU swOnly = new javax.smartcardio.ResponseAPDU(new byte[]{(byte) 0x6A, (byte) 0x82});
        check(swOnly.getNr() == 0, "SW-only getNr 0");
        check(swOnly.getData().length == 0, "SW-only data empty");
        check(swOnly.getSW1() == 0x6A && swOnly.getSW2() == 0x82 && swOnly.getSW() == 0x6A82, "SW-only SW bytes");

        // high bytes -> SW computed via (SW1<<8)|SW2
        javax.smartcardio.ResponseAPDU high = new javax.smartcardio.ResponseAPDU(new byte[]{(byte) 0xFF, (byte) 0xFF});
        check(high.getSW() == 0xFFFF && high.getSW1() == 255 && high.getSW2() == 255, "ResponseAPDU high SW");

        // multi-byte data with longer body (R1 from JDK ResponseAPDUTest)
        byte[] r1 = new byte[]{0x07, (byte) 0xA0, 0x00, 0x00, 0x00, 0x62, (byte) 0x81, 0x01, 0x04, 0x01,
                0x00, 0x00, 0x24, 0x05, 0x00, 0x0B, 0x04, (byte) 0xB0, 0x25, (byte) 0x90, 0x00};
        javax.smartcardio.ResponseAPDU longR = new javax.smartcardio.ResponseAPDU(r1);
        check(longR.getNr() == r1.length - 2, "long ResponseAPDU getNr");
        check(longR.getSW() == 0x9000, "long ResponseAPDU SW");
        check(Arrays.equals(longR.getData(), Arrays.copyOf(r1, r1.length - 2)), "long ResponseAPDU data");
        check(Arrays.equals(longR.getBytes(), r1), "long ResponseAPDU getBytes round-trip");

        // defensive copy of input
        byte[] input = new byte[]{1, 2, 3, (byte) 0x90, 0x00};
        javax.smartcardio.ResponseAPDU dc = new javax.smartcardio.ResponseAPDU(input);
        input[0] = 99;
        check(dc.getData()[0] == 1, "ResponseAPDU defensive copy of input");

        // defensive copy of output
        byte[] out = dc.getBytes();
        out[0] = 99;
        check(dc.getBytes()[0] == 1, "ResponseAPDU defensive copy of output");

        // equals / hashCode
        javax.smartcardio.ResponseAPDU e1 = new javax.smartcardio.ResponseAPDU(new byte[]{1, 2, (byte) 0x90, 0x00});
        javax.smartcardio.ResponseAPDU e2 = new javax.smartcardio.ResponseAPDU(new byte[]{1, 2, (byte) 0x90, 0x00});
        check(e1.equals(e2) && e2.equals(e1), "ResponseAPDU equals");
        check(e1.hashCode() == e2.hashCode(), "ResponseAPDU hashCode");
        check(!e1.equals(new javax.smartcardio.ResponseAPDU(new byte[]{1, 2, (byte) 0x6A, (byte) 0x82})), "ResponseAPDU not equals");
        check(!e1.equals(null) && !e1.equals("x"), "ResponseAPDU not equals non-ResponseAPDU");

        // exceptions
        assertThrows(NullPointerException.class, () -> new javax.smartcardio.ResponseAPDU(null), "null NPE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.ResponseAPDU(new byte[0]), "empty IAE");
        assertThrows(IllegalArgumentException.class, () -> new javax.smartcardio.ResponseAPDU(new byte[]{0x01}), "1-byte IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
