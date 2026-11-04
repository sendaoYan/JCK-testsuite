/*
 * @test
 * @summary javax.imageio.stream.IIOByteBuffer conformance: white-box structure + black-box behavior.
 * @run main IIOByteBufferTest
 */

import java.lang.reflect.Modifier;

import javax.imageio.stream.IIOByteBuffer;

public class IIOByteBufferTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.IIOByteBuffer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "concrete");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = new byte[10];
        IIOByteBuffer b = new IIOByteBuffer(data, 2, 4);
        check(b.getData() == data, "getData reference");
        check(b.getOffset() == 2, "getOffset");
        check(b.getLength() == 4, "getLength");
        byte[] d2 = new byte[6];
        b.setData(d2);
        b.setOffset(1);
        b.setLength(3);
        check(b.getData() == d2, "setData");
        check(b.getOffset() == 1, "setOffset");
        check(b.getLength() == 3, "setLength");
        // int holders preserve arbitrary/edge values without validation
        b.setOffset(-5);
        check(b.getOffset() == -5, "negative offset preserved");
        b.setLength(0);
        check(b.getLength() == 0, "zero length preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
