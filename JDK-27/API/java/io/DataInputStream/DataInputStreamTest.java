/*
 * @test
 * @summary java.io.DataInputStream conformance: white-box structure (reflection) + black-box
 *          behavior (primitive roundtrip, unsigned reads, UTF strings, EOFException).
 * @run main DataInputStreamTest
 *
 * 黑盒设计案:
 * 1. 契约: 从底层字节流读取 Java 基本类型与 UTF 字符串（大端字节序），EOF 时抛 EOFException。
 * 2. 场景: DataOutputStream 写各类型 -> 本类 readXxx 读回 -> 逐值断言。
 * 3. 组合: 单条字节数组链：writeBoolean/Byte/Short/Char/Int/Long/Float/Double/writeUTF -> 顺序读回；writeUTF 非 ASCII 往返。
 * 4. 不变量: readUnsignedByte/readUnsignedShort 还原 0..255 / 0..65535；readFully 填满缓冲。
 * 5. 边界: 流末尾 readInt()/readByte() 抛 EOFException。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class DataInputStreamTest {
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
        Class<?> c = Class.forName("java.io.DataInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterInputStream.class, "extends FilterInputStream");
        check(hasInterface(c, "java.io.DataInput"), "implements DataInput");
        hasMethod(c, "readInt");
        hasMethod(c, "readLong");
        hasMethod(c, "readUTF");
        hasMethod(c, "readFully", byte[].class);
        hasMethod(c, "readUnsignedByte");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeBoolean(true);
        dos.writeByte(7);
        dos.writeShort(0x1234);
        dos.writeChar('A');
        dos.writeInt(0x12345678);
        dos.writeLong(0x123456789ABCDEFL);
        dos.writeFloat(1.5f);
        dos.writeDouble(2.5d);
        dos.writeUTF("hello");
        dos.writeUTF("héllo 世界");
        dos.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readBoolean(), "readBoolean");
        check(dis.readByte() == 7, "readByte");
        check(dis.readShort() == 0x1234, "readShort");
        check(dis.readChar() == 'A', "readChar");
        check(dis.readInt() == 0x12345678, "readInt");
        check(dis.readLong() == 0x123456789ABCDEFL, "readLong");
        check(dis.readFloat() == 1.5f, "readFloat");
        check(dis.readDouble() == 2.5d, "readDouble");
        check("hello".equals(dis.readUTF()), "readUTF");
        check("héllo 世界".equals(dis.readUTF()), "readUTF non-ASCII");

        ByteArrayOutputStream u = new ByteArrayOutputStream();
        DataOutputStream ud = new DataOutputStream(u);
        ud.writeByte(200);
        ud.writeShort(0xABCD);
        DataInputStream ui = new DataInputStream(new ByteArrayInputStream(u.toByteArray()));
        check(ui.readUnsignedByte() == 200, "readUnsignedByte");
        check(ui.readUnsignedShort() == 0xABCD, "readUnsignedShort");

        ByteArrayOutputStream f = new ByteArrayOutputStream();
        DataOutputStream fd = new DataOutputStream(f);
        fd.write(new byte[]{1, 2, 3, 4});
        DataInputStream fi = new DataInputStream(new ByteArrayInputStream(f.toByteArray()));
        byte[] full = new byte[4];
        fi.readFully(full);
        check(Arrays.equals(full, new byte[]{1, 2, 3, 4}), "readFully");

        DataInputStream si = new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        check(si.skipBytes(2) == 2 && si.read() == 3, "skipBytes");

        DataInputStream eof = new DataInputStream(new ByteArrayInputStream(new byte[]{1}));
        check(eof.readByte() == 1, "read last byte");
        boolean threw = false;
        try { eof.readInt(); } catch (EOFException e) { threw = true; }
        check(threw, "readInt at EOF throws EOFException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
