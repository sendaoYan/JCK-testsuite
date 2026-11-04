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

    /** Version snapshot (JDK-8): ByteOrder is a final class with exactly two public
     *  constants; the enum surface (values/valueOf) only exists from JDK 9 on. */
    static void testVersionSurface() throws Exception {
        Class<?> c = Class.forName("java.nio.ByteOrder");
        check(!c.isEnum(), "JDK-8 ByteOrder is a class, not an enum (became an enum in JDK 9+)");
        check(!hasMethod(c, "values"), "JDK-8 ByteOrder has no values() (enum surface, JDK 9+)");
        check(!hasMethod(c, "valueOf", String.class),
                "JDK-8 ByteOrder has no valueOf(String) (enum surface, JDK 9+)");
        check(c.getFields().length == 2, "exactly two public fields: BIG_ENDIAN, LITTLE_ENDIAN");
        check(c.getField("BIG_ENDIAN").getType() == ByteOrder.class
                && c.getField("LITTLE_ENDIAN").getType() == ByteOrder.class,
                "both constants are typed ByteOrder");
    }

    /** Endianness contract: host order, factory defaults, order switching, views. */
    static void testEndiannessContract() {
        // This host is x86_64, so the native order is little-endian.
        check(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN,
                "nativeOrder() == LITTLE_ENDIAN on x86_64");

        // Default BIG_ENDIAN for every byte-buffer factory.
        check(ByteBuffer.allocateDirect(8).order() == ByteOrder.BIG_ENDIAN,
                "direct buffer default order is BIG_ENDIAN");
        check(ByteBuffer.wrap(new byte[8]).order() == ByteOrder.BIG_ENDIAN,
                "wrapped buffer default order is BIG_ENDIAN");
        // Implementation-stable (not promised by the javadoc): slice/duplicate/
        // asReadOnlyBuffer reset the order to the BIG_ENDIAN default instead of
        // carrying the source's explicit order over; verified on heap and direct
        // buffers on both JDK-8 and JDK-27.
        ByteBuffer dup = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).duplicate();
        check(dup.order() == ByteOrder.BIG_ENDIAN,
                "duplicate resets the order to BIG_ENDIAN (implementation-stable)");
        check(ByteBuffer.allocateDirect(8).order(ByteOrder.LITTLE_ENDIAN).slice().order()
                == ByteOrder.BIG_ENDIAN, "slice of a direct buffer resets the order too");

        // order() never rewrites bytes; it only changes their interpretation.
        ByteBuffer mixed = ByteBuffer.allocate(8);
        mixed.putLong(0x0102030405060708L);
        mixed.flip();
        mixed.order(ByteOrder.LITTLE_ENDIAN);
        check(mixed.getLong() == 0x0807060504030201L,
                "switching order reinterprets the same bytes");
        mixed.rewind();
        mixed.order(ByteOrder.BIG_ENDIAN);
        check(mixed.getLong() == 0x0102030405060708L, "switching back restores the value");

        // Typed views track the creating byte buffer's explicit order, not the host order.
        ByteBuffer be = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        check(be.asCharBuffer().order() == ByteOrder.BIG_ENDIAN
                && be.asIntBuffer().order() == ByteOrder.BIG_ENDIAN,
                "views inherit the explicit BIG_ENDIAN order even on a little-endian host");
        ByteBuffer le = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        check(le.asCharBuffer().order() == ByteOrder.LITTLE_ENDIAN
                && le.asLongBuffer().order() == ByteOrder.LITTLE_ENDIAN,
                "views inherit the explicit LITTLE_ENDIAN order");

        // Native-order equivalence: the same long laid out under native vs BIG_ENDIAN.
        ByteBuffer nativeLayout = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder());
        nativeLayout.putLong(0x0102030405060708L);
        nativeLayout.flip();
        ByteBuffer beLayout = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        beLayout.putLong(0x0102030405060708L);
        beLayout.flip();
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            check(nativeLayout.get(0) == 0x08 && beLayout.get(0) == 0x01,
                    "native LE layout stores the low byte first");
        } else {
            check(nativeLayout.get(0) == 0x01 && beLayout.get(0) == 0x01,
                    "on a big-endian host both layouts agree");
        }

        // Cross-type equality and null safety.
        check(!ByteOrder.BIG_ENDIAN.equals(null), "equals(null) is false");
        check(!ByteOrder.BIG_ENDIAN.equals("BIG_ENDIAN"), "equals across types is false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testVersionSurface();
        testEndiannessContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
