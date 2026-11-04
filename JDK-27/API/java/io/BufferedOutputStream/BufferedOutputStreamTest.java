/*
 * @test
 * @summary java.io.BufferedOutputStream conformance: white-box structure (reflection) + black-box
 *          behavior (write/flush/close auto-flush, small-buffer correctness).
 * @run main BufferedOutputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 带缓冲的输出流，write 先入缓冲，flush/close 或缓冲满时刷到底层。
 * 2. 场景: BufferedOutputStream(OutputStream, smallBuffer) 写单字节/批量/off-len。
 * 3. 组合: 以小缓冲写大块字节 -> close 自动 flush -> 读回底层字节逐字节比对。
 * 4. 不变量: flush() 后底层立即可见；close() 隐含 flush；小缓冲满时自动刷写不丢字节。
 * 5. 边界: write(int) 只写低 8 位。
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class BufferedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.BufferedOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        hasMethod(c, "write", int.class);
        hasMethod(c, "flush");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(sink);
        bos.write(65);
        bos.write(new byte[]{66, 67}, 0, 2);
        bos.write(new byte[]{68, 69, 70}, 1, 2);
        bos.flush();
        check(Arrays.equals(sink.toByteArray(), new byte[]{65, 66, 67, 69, 70}), "write + flush");

        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 5);
        ByteArrayOutputStream small = new ByteArrayOutputStream();
        BufferedOutputStream sbos = new BufferedOutputStream(small, 64);
        sbos.write(data, 0, data.length);
        sbos.close();
        check(Arrays.equals(small.toByteArray(), data), "small buffer auto-flush + close");

        ByteArrayOutputStream bits = new ByteArrayOutputStream();
        BufferedOutputStream bbos = new BufferedOutputStream(bits);
        bbos.write(0x1FF); // 511 -> low byte 0xFF
        bbos.close();
        check(bits.toByteArray().length == 1 && bits.toByteArray()[0] == (byte) 0xFF,
              "write(int) low 8 bits");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
