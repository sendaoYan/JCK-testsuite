/*
 * @test
 * @summary java.nio.ByteOrder conformance: white-box structure (final class, extends
 *          Object, two constants, no public constructor) plus black-box toString,
 *          nativeOrder, and endianness effect on ByteBuffer multibyte access.
 * @run main ByteOrderTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteOrderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.ByteOrder");
        check(Modifier.isFinal(c.getModifiers()), "ByteOrder is final");
        check(c.getSuperclass() == Object.class, "ByteOrder extends Object");
        check(c.getConstructors().length == 0, "no public constructor");
        check(hasMethod(c, "nativeOrder"), "has nativeOrder()");
        check(hasMethod(c, "toString"), "has toString()");
    }

    static void testBlackBox() {
        check("BIG_ENDIAN".equals(ByteOrder.BIG_ENDIAN.toString()), "BIG_ENDIAN toString");
        check("LITTLE_ENDIAN".equals(ByteOrder.LITTLE_ENDIAN.toString()), "LITTLE_ENDIAN toString");
        check(ByteOrder.BIG_ENDIAN != ByteOrder.LITTLE_ENDIAN, "distinct constants");
        check(ByteOrder.BIG_ENDIAN.equals(ByteOrder.BIG_ENDIAN), "identity equals self");
        check(!ByteOrder.BIG_ENDIAN.equals(ByteOrder.LITTLE_ENDIAN), "identity equals distinct");

        ByteOrder nativeOrder = ByteOrder.nativeOrder();
        check(nativeOrder == ByteOrder.BIG_ENDIAN || nativeOrder == ByteOrder.LITTLE_ENDIAN,
              "nativeOrder is one of two constants");
        check(nativeOrder == ByteOrder.nativeOrder(), "nativeOrder stable");

        ByteBuffer bb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        check(bb.order() == ByteOrder.LITTLE_ENDIAN, "order getter");
        bb.putInt(0x01020304);
        bb.flip();
        check(bb.get() == 0x04 && bb.get() == 0x03 && bb.get() == 0x02 && bb.get() == 0x01,
              "little-endian byte layout");

        bb.rewind();
        bb.order(ByteOrder.BIG_ENDIAN);
        check(bb.getInt() == 0x04030201, "reinterpret same bytes as big-endian");

        ByteBuffer big = ByteBuffer.allocate(4);
        check(big.order() == ByteOrder.BIG_ENDIAN, "default order is BIG_ENDIAN");
        big.putInt(0x01020304);
        big.flip();
        check(big.get() == 0x01 && big.get() == 0x02 && big.get() == 0x03 && big.get() == 0x04,
              "big-endian byte layout");

        check(ByteOrder.LITTLE_ENDIAN.hashCode() == ByteOrder.LITTLE_ENDIAN.hashCode(),
              "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
