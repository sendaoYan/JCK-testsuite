/*
 * @test
 * @summary javax.imageio.stream.IIOByteBuffer conformance: behavioral.
 * @run main IIOByteBufferTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class IIOByteBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] data = new byte[10];
        javax.imageio.stream.IIOByteBuffer b = new javax.imageio.stream.IIOByteBuffer(data, 2, 4);
        check(b.getData() == data, "getData");
        check(b.getOffset() == 2, "offset");
        check(b.getLength() == 4, "length");
        byte[] d2 = new byte[5];
        b.setData(d2); b.setOffset(1); b.setLength(3);
        check(b.getData() == d2 && b.getOffset() == 1 && b.getLength() == 3, "setters");
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: structural contract ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.IIOByteBuffer");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "plain class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object directly");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        check(!Serializable.class.isAssignableFrom(c), "not Serializable");
        check(!Cloneable.class.isAssignableFrom(c), "not Cloneable");
        check(!Comparable.class.isAssignableFrom(c), "not Comparable");

        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "single public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 3 && ps[0] == byte[].class && ps[1] == int.class && ps[2] == int.class,
              "constructor (byte[], int, int)");

        // exact exported method surface: 3 get/set pairs, nothing else
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 6, "exactly six methods declared");
        check(names.contains("getData") && names.contains("setData"), "data accessors");
        check(names.contains("getOffset") && names.contains("setOffset"), "offset accessors");
        check(names.contains("getLength") && names.contains("setLength"), "length accessors");
        check(c.getMethod("getData").getReturnType() == byte[].class, "getData returns byte[]");
        check(c.getMethod("setData", byte[].class).getReturnType() == void.class, "setData(byte[]) void");
        check(c.getMethod("getOffset").getReturnType() == int.class, "getOffset returns int");
        check(c.getMethod("setOffset", int.class).getReturnType() == void.class, "setOffset(int) void");
        check(c.getMethod("getLength").getReturnType() == int.class, "getLength returns int");
        check(c.getMethod("setLength", int.class).getReturnType() == void.class, "setLength(int) void");
        // the newLength accessors some other buffer classes expose do not exist here
        boolean hasNewLength = false;
        for (Method m : c.getMethods()) {
            if (m.getName().contains("NewLength")) hasNewLength = true;
        }
        check(!hasNewLength, "no getNewLength/setNewLength methods");
        // all accessors are plain public instance methods (no static, no varargs)
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()),
                  "method " + m.getName() + " is public instance");
            check(m.getExceptionTypes().length == 0, "method " + m.getName() + " throws nothing");
        }
    }

    // ---- Part 3: value contract (holder semantics, no validation) ----
    static void testBlackBox() throws Exception {
        // constructor stores all three arguments verbatim
        byte[] d = new byte[8];
        IIOByteBuffer b = new IIOByteBuffer(d, 3, 5);
        check(b.getData() == d, "ctor data reference");
        check(b.getOffset() == 3, "ctor offset");
        check(b.getLength() == 5, "ctor length");

        // zero-length and zero-offset are ordinary values
        IIOByteBuffer z = new IIOByteBuffer(d, 0, 0);
        check(z.getOffset() == 0 && z.getLength() == 0, "zero offset/length accepted");
        z.setLength(8);
        check(z.getLength() == 8, "setLength grows holder");

        // null data tolerated at construction and via setData
        IIOByteBuffer n = new IIOByteBuffer(null, 0, 0);
        check(n.getData() == null, "ctor null data kept");
        n.setLength(4);
        check(n.getLength() == 4, "length independent of null data");
        IIOByteBuffer n2 = new IIOByteBuffer(d, 0, 1);
        n2.setData(null);
        check(n2.getData() == null, "setData(null) kept");

        // no range validation: any int survives a round trip
        IIOByteBuffer v = new IIOByteBuffer(d, 0, 0);
        v.setOffset(-5);
        check(v.getOffset() == -5, "negative offset preserved");
        v.setOffset(Integer.MIN_VALUE);
        check(v.getOffset() == Integer.MIN_VALUE, "MIN_VALUE offset preserved");
        v.setOffset(Integer.MAX_VALUE);
        check(v.getOffset() == Integer.MAX_VALUE, "MAX_VALUE offset preserved");
        v.setLength(-1);
        check(v.getLength() == -1, "negative length preserved");
        v.setLength(Integer.MIN_VALUE);
        check(v.getLength() == Integer.MIN_VALUE, "MIN_VALUE length preserved");
        v.setLength(Integer.MAX_VALUE);
        check(v.getLength() == Integer.MAX_VALUE, "MAX_VALUE length preserved");
        check(v.getData() == d, "data untouched by offset/length writes");

        // length may exceed the array size: the holder does not cross-check
        IIOByteBuffer big = new IIOByteBuffer(new byte[2], 0, 100);
        check(big.getLength() == 100, "length larger than data kept");

        // independence of two instances
        byte[] d1 = new byte[1];
        byte[] d2b = new byte[1];
        IIOByteBuffer i1 = new IIOByteBuffer(d1, 1, 1);
        IIOByteBuffer i2 = new IIOByteBuffer(d2b, 2, 2);
        i1.setOffset(9);
        check(i2.getOffset() == 2 && i2.getLength() == 2 && i2.getData() == d2b,
              "sibling instance unaffected");

        // integration with ImageInputStream.readBytes: fresh array each call, offset reset
        MemoryCacheImageInputStream in = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(new byte[]{10, 20, 30, 40}));
        try {
            IIOByteBuffer rb = new IIOByteBuffer(d, 2, 4);
            in.readBytes(rb, 2);
            check(rb.getOffset() == 0, "readBytes resets offset to 0");
            check(rb.getLength() == 2, "readBytes sets length read");
            check(rb.getData() != d, "readBytes installs a new array");
            check(rb.getData()[0] == 10 && rb.getData()[1] == 20, "readBytes payload start");
            byte[] first = rb.getData();
            in.readBytes(rb, 2);
            check(rb.getData() != first, "readBytes replaces array on reuse");
            check(rb.getData()[0] == 30 && rb.getData()[1] == 40, "readBytes continues stream");
            in.readBytes(rb, 2); // at EOF: length becomes -1, no exception
            check(rb.getLength() == -1, "readBytes at EOF reports length -1");
            check(rb.getData().length == 2, "readBytes at EOF still supplies array of requested size");
        } finally {
            try { in.close(); } catch (IOException ignore) {}
        }
    }
}
