/*
 * @test
 * @summary java.awt.image.DataBufferFloat conformance: white-box structure + black-box contract.
 * @run main DataBufferFloatTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;

public class DataBufferFloatTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferFloat");
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
        DataBufferFloat db = new DataBufferFloat(4);
        check(db.getDataType() == DataBuffer.TYPE_FLOAT, "data type float");
        check(db.getSize() == 4, "size");
        check(db.getNumBanks() == 1, "single bank");
        db.setElemFloat(0, 1.5f);
        check(db.getElemFloat(0) == 1.5f, "set/get elemf");
        check(db.getElemDouble(0) == 1.5, "getElemDouble widens");
        // The int-typed view truncates toward zero.
        check(db.getElem(0) == 1, "getElem truncates fractional part");
        db.setElem(1, 7);
        check(db.getElemFloat(1) == 7.0f, "setElem stores exact int");
        check(db.getElemFloat(1) == 7.0f, "getElemFloat of int value");
        db.setElemDouble(2, 2.75);
        check(db.getElemDouble(2) == 2.75, "setElemDouble round-trip");
        db.setElemFloat(3, -0.5f);
        check(db.getElem(3) == 0, "negative fraction truncates toward zero");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElemFloat(-1, 0.0f); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElemFloat(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferFloat two = new DataBufferFloat(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElemFloat(1, 2, 9.5f);
        check(two.getElemFloat(1, 2) == 9.5f, "banked round-trip");
        check(two.getElemFloat(0, 2) == 0.0f, "banks independent");
        float[] backing = new float[] { 1.25f, 2.5f };
        DataBufferFloat view = new DataBufferFloat(backing, 2);
        check(view.getData() == backing, "getData live reference");
        backing[0] = -3.75f;
        check(view.getElemFloat(0) == -3.75f, "external write visible");
        DataBufferFloat off = new DataBufferFloat(backing, 1, 1);
        check(off.getElemFloat(0) == 2.5f, "offset shifts access");
        DataBufferFloat zero = new DataBufferFloat(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferFloat(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
