/*
 * @test
 * @summary java.io.ByteArrayOutputStream conformance: white-box structure (reflection) + black-box
 *          behavior (write, size, reset, toByteArray copy semantics, writeTo, toString(Charset)).
 * @run main ByteArrayOutputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 内存字节数组输出流，自动扩容，提供 size/toByteArray/toString 以及 reset/writeTo。
 * 2. 场景: write(int)/write(byte[],off,len)/writeBytes -> size -> toByteArray -> toString(Charset)。
 * 3. 组合: 写入 -> toByteArray 读回并回灌 ByteArrayInputStream -> 逐字节一致；writeTo 拷到另一 ByteArrayOutputStream。
 * 4. 不变量: toByteArray() 返回新数组副本(外部修改不影响内部)；reset() 清空 size=0；自动扩容后内容完整。
 * 5. 边界: 空流 toByteArray().length==0；toString(Charset) 用固定 UTF-8 解码非 ASCII。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteArrayOutputStreamTest {
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
        Class<?> c = Class.forName("java.io.ByteArrayOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.OutputStream.class, "extends OutputStream");
        hasMethod(c, "toByteArray");
        hasMethod(c, "size");
        hasMethod(c, "reset");
        hasMethod(c, "toString", Charset.class);
        hasMethod(c, "writeTo", java.io.OutputStream.class);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        check(out.size() == 0, "initial size");
        out.write(65);
        out.write(new byte[]{66, 67}, 0, 2);
        out.writeBytes(new byte[]{68});
        check(out.size() == 4, "size after writes");
        byte[] bytes = out.toByteArray();
        check(Arrays.equals(bytes, new byte[]{65, 66, 67, 68}), "toByteArray content");
        check("ABCD".equals(out.toString(StandardCharsets.UTF_8)), "toString(Charset)");

        bytes[0] = 99;
        check(Arrays.equals(out.toByteArray(), new byte[]{65, 66, 67, 68}), "toByteArray copy semantics");

        ByteArrayOutputStream other = new ByteArrayOutputStream();
        out.writeTo(other);
        check(Arrays.equals(other.toByteArray(), new byte[]{65, 66, 67, 68}), "writeTo");

        out.reset();
        check(out.size() == 0 && out.toByteArray().length == 0, "reset");

        ByteArrayOutputStream big = new ByteArrayOutputStream(8);
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 31);
        big.write(data, 0, data.length);
        check(big.size() == data.length, "auto-grow size");
        ByteArrayInputStream in = new ByteArrayInputStream(big.toByteArray());
        check(Arrays.equals(in.readAllBytes(), data), "roundtrip to ByteArrayInputStream");

        ByteArrayOutputStream utf = new ByteArrayOutputStream();
        utf.write("héllo".getBytes(StandardCharsets.UTF_8));
        check("héllo".equals(utf.toString(StandardCharsets.UTF_8)), "UTF-8 non-ASCII");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
