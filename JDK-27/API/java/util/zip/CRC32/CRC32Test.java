/*
 * @test
 * @summary java.util.zip.CRC32 conformance: white-box structure + black-box checksum behavior.
 * @run main CRC32Test
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CRC32Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.CRC32");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !c.isEnum() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Checksum.class.isAssignableFrom(c), "implements Checksum");

        c.getMethod("update", int.class);
        c.getMethod("update", byte[].class, int.class, int.class);
        c.getMethod("update", ByteBuffer.class);
        c.getMethod("reset");
        c.getMethod("getValue");
        check(true, "update/reset/getValue methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] nine = "123456789".getBytes("UTF-8");

        CRC32 c = new CRC32();
        check(c.getValue() == 0L, "initial value 0");
        c.update(nine, 0, nine.length);
        check(c.getValue() == 0xCBF43926L, "crc32('123456789') == 0xCBF43926");
        c.reset();
        check(c.getValue() == 0L, "reset -> 0");

        CRC32 d = new CRC32();
        for (byte b : nine) d.update(b);
        check(d.getValue() == 0xCBF43926L, "update(int) byte-wise");

        CRC32 e = new CRC32();
        e.update(ByteBuffer.wrap(nine));
        check(e.getValue() == 0xCBF43926L, "update(ByteBuffer)");

        CRC32 f = new CRC32();
        f.update(new byte[0], 0, 0);
        check(f.getValue() == 0L, "empty input leaves 0");

        CRC32 g = new CRC32();
        g.update(nine, 0, 4);
        g.update(nine, 4, nine.length - 4);
        check(g.getValue() == 0xCBF43926L, "incremental update equals whole");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
