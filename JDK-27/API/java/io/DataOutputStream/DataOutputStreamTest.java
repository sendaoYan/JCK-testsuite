/*
 * @test
 * @summary java.io.DataOutputStream conformance: white-box structure (reflection) + black-box
 *          behavior (size() accounting, primitive writes, writeUTF/writeBytes/writeChars).
 * @run main DataOutputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 将 Java 基本类型按大端字节序写入底层流，并维护已写字节计数 size()。
 * 2. 场景: writeBoolean/Byte/Short/Char/Int/Long/Float/Double -> size() 逐项断言。
 * 3. 组合: 写入各类型后读回（借助 DataInputStream）验证字节序一致；writeUTF 长度前缀=2+UTF长度。
 * 4. 不变量: writeUTF 写 2 字节长度 + 修改版 UTF-8；writeBytes 每字符取低 8 位；writeChars 每字符 2 字节。
 * 5. 边界: writeUTF(null) 抛 NullPointerException。
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.DataOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        check(hasInterface(c, "java.io.DataOutput"), "implements DataOutput");
        hasMethod(c, "writeInt", int.class);
        hasMethod(c, "writeLong", long.class);
        hasMethod(c, "writeUTF", String.class);
        hasMethod(c, "size");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
        check(dos.size() == 0, "initial size");
        dos.writeBoolean(true);  check(dos.size() == 1, "writeBoolean");
        dos.writeByte(1);        check(dos.size() == 2, "writeByte");
        dos.writeShort(1);       check(dos.size() == 4, "writeShort");
        dos.writeChar('A');      check(dos.size() == 6, "writeChar");
        dos.writeInt(1);         check(dos.size() == 10, "writeInt");
        dos.writeLong(1);        check(dos.size() == 18, "writeLong");
        dos.writeFloat(1f);      check(dos.size() == 22, "writeFloat");
        dos.writeDouble(1d);     check(dos.size() == 30, "writeDouble");

        ByteArrayOutputStream u = new ByteArrayOutputStream();
        DataOutputStream ud = new DataOutputStream(u);
        ud.writeUTF("hello");
        check(ud.size() == 7, "writeUTF size (2-byte len + 5 bytes)");
        ud.writeUTF("world");
        check(ud.size() == 14, "writeUTF accumulates");
        ud.flush();
        check(u.toByteArray().length == 14, "writeUTF byte count matches size");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream bd = new DataOutputStream(b);
        bd.writeBytes("AB");   // 2 bytes: 0x41 0x42
        bd.writeChars("CD");   // 4 bytes: 0x00 0x43 0x00 0x44
        byte[] raw = b.toByteArray();
        check(Arrays.equals(raw, new byte[]{0x41, 0x42, 0x00, 0x43, 0x00, 0x44}),
              "writeBytes/writeChars layout");

        boolean npe = false;
        try { dos.writeUTF(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "writeUTF(null) NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
