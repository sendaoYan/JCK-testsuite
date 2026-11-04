/*
 * @test
 * @summary java.io.ByteArrayInputStream conformance: white-box structure (reflection) + black-box
 *          behavior (reads, skip, mark/reset, readAllBytes/readNBytes/transferTo).
 * @run main ByteArrayInputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 内存字节数组上的 InputStream，支持顺序读、批量读、skip、mark/reset，available 报告剩余字节，EOF 返回 -1。
 * 2. 场景: 构造(byte[])与(byte[],off,len) -> read()/read(byte[],off,len) -> available -> skip -> mark/reset。
 * 3. 组合: 写 ByteArrayOutputStream -> toByteArray -> 回灌本类 -> readAllBytes 全量比对；transferTo 拷贝到另一输出流。
 * 4. 不变量: markSupported()==true；未 mark 时 reset() 回到起点；read(byte[],off,len) 精确落在 offset/len 上。
 * 5. 边界: 读到末尾 read()==-1；read(byte[]) 在末尾返回 -1；close() 为 no-op 不抛异常。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ByteArrayInputStreamTest {
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
        Class<?> c = Class.forName("java.io.ByteArrayInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.InputStream.class, "extends InputStream");
        hasMethod(c, "read");
        hasMethod(c, "readAllBytes");
        hasMethod(c, "readNBytes", byte[].class, int.class, int.class);
        hasMethod(c, "transferTo", java.io.OutputStream.class);
        hasMethod(c, "markSupported");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        byte[] data = {1, 2, 3, 4, 5};
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(in.available() == 5, "available");
        check(in.markSupported(), "markSupported");
        check(in.read() == 1 && in.read() == 2, "read()");
        byte[] buf = new byte[4];
        check(in.read(buf, 1, 2) == 2 && buf[0] == 0 && buf[1] == 3 && buf[2] == 4 && buf[3] == 0,
              "read(buf,off,len)");
        check(in.skip(1) == 1, "skip");
        check(in.read() == -1, "EOF read()");
        check(in.read(buf) == -1, "EOF read(byte[])");

        ByteArrayInputStream m = new ByteArrayInputStream(new byte[]{10, 20, 30});
        check(m.read() == 10, "pre-mark read");
        m.mark(0);
        check(m.read() == 20 && m.read() == 30, "reads after mark");
        m.reset();
        check(m.read() == 20, "reset to mark position");

        ByteArrayInputStream r = new ByteArrayInputStream(new byte[]{7, 8});
        check(r.read() == 7, "read before unmarked reset");
        r.reset();
        check(r.read() == 7, "unmarked reset returns to 0");

        ByteArrayInputStream sub = new ByteArrayInputStream(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        check(sub.available() == 3 && sub.read() == 8 && sub.read() == 7 && sub.read() == 6 && sub.read() == -1,
              "offset/length constructor");

        byte[] big = new byte[1000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) (i * 7);
        ByteArrayOutputStream src = new ByteArrayOutputStream();
        src.write(big, 0, big.length);
        ByteArrayInputStream full = new ByteArrayInputStream(src.toByteArray());
        check(Arrays.equals(full.readAllBytes(), big), "readAllBytes");

        ByteArrayInputStream copyIn = new ByteArrayInputStream(big);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        check(copyIn.transferTo(dst) == big.length, "transferTo count");
        check(Arrays.equals(dst.toByteArray(), big), "transferTo content");

        ByteArrayInputStream nb = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        byte[] nbuf = new byte[3];
        check(nb.readNBytes(nbuf, 0, 3) == 3 && Arrays.equals(nbuf, new byte[]{1, 2, 3}), "readNBytes");
        check(nb.readNBytes(nbuf, 0, 3) == 1 && nbuf[0] == 4, "readNBytes partial at end");

        ByteArrayInputStream c = new ByteArrayInputStream(new byte[]{1});
        c.close();
        check(c.read() == 1, "close is no-op");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
