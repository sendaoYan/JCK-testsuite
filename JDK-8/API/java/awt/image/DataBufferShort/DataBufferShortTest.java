/*
 * @test
 * @summary java.awt.image.DataBufferShort conformance: white-box structure + black-box contract.
 * @run main DataBufferShortTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;

public class DataBufferShortTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferShort");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        }

    static void testBehavior() {
        DataBufferShort db = new DataBufferShort(4);
        check(db.getDataType() == DataBuffer.TYPE_SHORT, "data type short");
        check(db.getSize() == 4, "size");
        check(db.getNumBanks() == 1, "single bank");
        db.setElem(0, -1);
        check(db.getElem(0) == -1, "short keeps sign");
        db.setElem(1, 32767);
        check(db.getElem(1) == 32767, "max short round-trip");
        db.setElem(2, -32768);
        check(db.getElem(2) == -32768, "min short round-trip");
        db.setElem(3, 32768);
        check(db.getElem(3) == -32768, "overflow wraps to min short");
        check(db.getElemFloat(1) == 32767.0f, "getElemFloat");
        check(db.getElemDouble(1) == 32767.0, "getElemDouble");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.getElem(-1); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferShort two = new DataBufferShort(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElem(1, 1, -5);
        check(two.getElem(1, 1) == -5, "banked round-trip");
        check(two.getElem(0, 1) == 0, "banks independent");
        short[] backing = new short[] { 1, -2, 3 };
        DataBufferShort view = new DataBufferShort(backing, 3);
        check(view.getData() == backing, "getData live reference");
        backing[0] = -9;
        check(view.getElem(0) == -9, "external write visible");
        DataBufferShort off = new DataBufferShort(backing, 2, 1);
        check(off.getElem(0) == -2, "offset shifts access");
        DataBufferShort zero = new DataBufferShort(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferShort(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
