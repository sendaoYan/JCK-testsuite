/*
 * @test
 * @summary java.awt.image.DataBufferDouble conformance: white-box structure + black-box contract.
 * @run main DataBufferDoubleTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;

public class DataBufferDoubleTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferDouble");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        check(hasMethod(c, "getElemFloat"), "method getElemFloat");
        check(hasMethod(c, "setElemFloat"), "method setElemFloat");
        check(hasMethod(c, "getElemDouble"), "method getElemDouble");
        check(hasMethod(c, "setElemDouble"), "method setElemDouble");
        }

    static void testBehavior() {
        DataBufferDouble db = new DataBufferDouble(4);
        check(db.getDataType() == DataBuffer.TYPE_DOUBLE, "data type double");
        check(db.getSize() == 4, "size");
        check(db.getNumBanks() == 1, "single bank");
        db.setElemDouble(0, 1.5);
        check(db.getElemDouble(0) == 1.5, "set/get elem");
        check(db.getElemDouble(0) == 1.5, "getElemDouble widens");
        // The int-typed view truncates toward zero.
        check(db.getElem(0) == 1, "getElem truncates fractional part");
        db.setElem(1, 7);
        check(db.getElemDouble(1) == 7.0, "setElem stores exact int");
        check(db.getElemFloat(1) == 7.0f, "getElemFloat of int value");
        db.setElemDouble(2, 2.75);
        check(db.getElemDouble(2) == 2.75, "setElemDouble round-trip");
        db.setElemFloat(3, -0.5f);
        check(db.getElem(3) == 0, "negative fraction truncates toward zero");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElemDouble(-1, 0.0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElemDouble(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferDouble two = new DataBufferDouble(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElemDouble(1, 2, 9.5);
        check(two.getElemDouble(1, 2) == 9.5, "banked round-trip");
        check(two.getElemDouble(0, 2) == 0.0, "banks independent");
        double[] backing = new double[] { 1.25, 2.5 };
        DataBufferDouble view = new DataBufferDouble(backing, 2);
        check(view.getData() == backing, "getData live reference");
        backing[0] = -3.75;
        check(view.getElemDouble(0) == -3.75, "external write visible");
        DataBufferDouble off = new DataBufferDouble(backing, 1, 1);
        check(off.getElemDouble(0) == 2.5, "offset shifts access");
        DataBufferDouble zero = new DataBufferDouble(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferDouble(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
