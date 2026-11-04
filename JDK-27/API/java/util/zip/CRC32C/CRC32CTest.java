/*
 * @test
 * @summary java.util.zip.CRC32C conformance: white-box structure + black-box checksum behavior.
 * @run main CRC32CTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;

public class CRC32CTest {
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
        Class<?> c = clazz("java.util.zip.CRC32C");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(!c.isInterface() && !c.isEnum() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
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

        CRC32C c = new CRC32C();
        check(c.getValue() == 0L, "initial value 0");
        c.update(nine, 0, nine.length);
        check(c.getValue() == 0xE3069283L, "crc32c('123456789') == 0xE3069283");
        c.reset();
        check(c.getValue() == 0L, "reset -> 0");

        CRC32C d = new CRC32C();
        for (byte b : nine) d.update(b);
        check(d.getValue() == 0xE3069283L, "update(int) byte-wise");

        CRC32C e = new CRC32C();
        e.update(ByteBuffer.wrap(nine));
        check(e.getValue() == 0xE3069283L, "update(ByteBuffer)");

        CRC32C f = new CRC32C();
        f.update(new byte[0], 0, 0);
        check(f.getValue() == 0L, "empty input leaves 0");

        CRC32C g = new CRC32C();
        g.update(nine, 0, 4);
        g.update(nine, 4, nine.length - 4);
        check(g.getValue() == 0xE3069283L, "incremental update equals whole");
    }


    // ===================== 契约深化：null/越界/ByteBuffer 位置与只读/分块一致性/长输入 =====================
    static void testJdk27Additions() {
        byte[] nine = "123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try {
            new CRC32C().update((byte[]) null, 0, 1);
            check(false, "null array NPE");
        } catch (NullPointerException e) { check(true, "null array NPE"); }
        try {
            new CRC32C().update(nine, -1, 1);
            check(false, "negative offset");
        } catch (ArrayIndexOutOfBoundsException e) { check(true, "negative offset AIOOBE"); }
        try {
            new CRC32C().update(nine, 0, 10);
            check(false, "len past end");
        } catch (ArrayIndexOutOfBoundsException e) { check(true, "len past end AIOOBE"); }
        try {
            new CRC32C().update((java.nio.ByteBuffer) null);
            check(false, "null ByteBuffer NPE");
        } catch (NullPointerException e) { check(true, "null ByteBuffer NPE"); }
        // ByteBuffer 位置/边界
        java.nio.ByteBuffer padded = java.nio.ByteBuffer.allocate(11);
        padded.put((byte) 'x').put(nine).put((byte) 'y').flip();
        padded.position(1).limit(10);
        CRC32C pos = new CRC32C();
        pos.update(padded);
        check(pos.getValue() == 0xE3069283L, "ByteBuffer respects position/limit");
        // 只读 buffer 同样可用
        java.nio.ByteBuffer ro = java.nio.ByteBuffer.wrap(nine).asReadOnlyBuffer();
        CRC32C r = new CRC32C();
        r.update(ro);
        check(r.getValue() == 0xE3069283L, "read-only ByteBuffer");
        // 分块 == 整体（含大输入跨内部块边界）
        java.util.Random rnd = new java.util.Random(42);
        byte[] big = new byte[70000];
        rnd.nextBytes(big);
        CRC32C whole = new CRC32C();
        whole.update(big);
        CRC32C chunked = new CRC32C();
        for (int i = 0; i < big.length; i += 9999) {
            chunked.update(big, i, Math.min(9999, big.length - i));
        }
        check(whole.getValue() == chunked.getValue(), "chunked equals whole over 70000 bytes");
        // getValue 返回 long 且 reset 归零
        check(whole.getValue() >= 0 && whole.getValue() <= 0xFFFFFFFFL, "value within unsigned 32 range");
        whole.reset();
        check(whole.getValue() == 0L, "reset to 0");
        // 同一数据 CRC32C 与 CRC32 不同
        java.util.zip.CRC32 c32 = new java.util.zip.CRC32();
        c32.update(nine);
        CRC32C cc = new CRC32C();
        cc.update(nine);
        check(c32.getValue() != cc.getValue(), "CRC32 != CRC32C for same input");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();        testJdk27Additions();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
