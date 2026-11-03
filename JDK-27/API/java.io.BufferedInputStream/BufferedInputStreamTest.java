/*
 * @test
 * @summary java.io.BufferedInputStream conformance: white-box structure (reflection) + black-box
 *          behavior (buffered reads, skip, mark/reset, large buffered roundtrip).
 * @run main BufferedInputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 带缓冲的输入流，减少底层读取次数；支持 mark/reset、skip、available 与 transferTo。
 * 2. 场景: BufferedInputStream(InputStream, smallBuffer) 读单字节/批量/skip/mark-reset。
 * 3. 组合: BufferedOutputStream 写大块字节 -> flush -> 本类以小缓冲读回 -> 逐字节比对，验证缓冲分块正确。
 * 4. 不变量: markSupported()==true；mark/reset 在小量读取内重放。
 * 5. 边界: 读到末尾 read()==-1；read(byte[],off,len) 按需返回。
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class BufferedInputStreamTest {
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
        Class<?> c = Class.forName("java.io.BufferedInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterInputStream.class, "extends FilterInputStream");
        hasMethod(c, "read");
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
        hasMethod(c, "markSupported");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        BufferedInputStream bis =
            new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}), 2);
        check(bis.markSupported(), "markSupported");
        check(bis.skip(2) == 2, "skip from fresh stream");
        check(bis.read() == 3, "read after skip");
        bis.mark(10);
        int a = bis.read();
        check(a == 4, "read after mark");
        bis.reset();
        check(bis.read() == a, "reset replays");
        check(bis.read() == 5, "read next after reset");
        check(bis.read() == -1, "EOF");

        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 13);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(sink, 64);
        bos.write(data, 0, data.length);
        bos.flush();
        BufferedInputStream reader =
            new BufferedInputStream(new ByteArrayInputStream(sink.toByteArray()), 64);
        check(Arrays.equals(reader.readAllBytes(), data), "large buffered roundtrip");

        BufferedInputStream mixed = new BufferedInputStream(new ByteArrayInputStream(data), 32);
        byte[] head = new byte[100];
        check(mixed.read(head, 0, 100) == 100, "mixed read chunk");
        check(mixed.read() == (data[100] & 0xFF), "mixed single read");
        byte[] tail = mixed.readAllBytes();
        check(tail.length == data.length - 101, "mixed remaining length");
        boolean ok = true;
        for (int i = 0; i < head.length; i++) ok &= (head[i] == data[i]);
        for (int i = 0; i < tail.length; i++) ok &= (tail[i] == data[101 + i]);
        check(ok, "mixed content correctness");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
