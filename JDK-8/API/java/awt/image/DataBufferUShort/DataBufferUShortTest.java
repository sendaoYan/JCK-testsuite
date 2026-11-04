/*
 * @test
 * @summary java.awt.image.DataBufferUShort conformance: white-box structure + black-box contract.
 * @run main DataBufferUShortTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;

public class DataBufferUShortTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferUShort");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        }

    static void testBehavior() {
        DataBufferUShort db = new DataBufferUShort(4);
        check(db.getDataType() == DataBuffer.TYPE_USHORT, "data type ushort");
        check(db.getSize() == 4, "size");
        db.setElem(0, -1);
        check(db.getElem(0) == 65535, "ushort masks to unsigned");
        db.setElem(1, 65535);
        check(db.getElem(1) == 65535, "max ushort round-trip");
        db.setElem(2, 65536);
        check(db.getElem(2) == 0, "overflow wraps to 0");
        db.setElem(3, 1000);
        check(db.getElem(3) == 1000, "in-range round-trip");
        check(db.getElemFloat(1) == 65535.0f, "getElemFloat");
        check(db.getElemDouble(1) == 65535.0, "getElemDouble");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElem(-1, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElem(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferUShort two = new DataBufferUShort(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElem(1, 1, 70000);
        check(two.getElem(1, 1) == 4464, "banked overflow wraps");
        check(two.getElem(0, 1) == 0, "banks independent");
        short[] backing = new short[] { 1, 2, 3 };
        DataBufferUShort view = new DataBufferUShort(backing, 3);
        check(view.getData() == backing, "getData live reference");
        DataBufferUShort off = new DataBufferUShort(backing, 2, 1);
        check(off.getElem(0) == 2, "offset shifts access");
        DataBufferUShort zero = new DataBufferUShort(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferUShort(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
