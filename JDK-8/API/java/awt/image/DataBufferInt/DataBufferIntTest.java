/*
 * @test
 * @summary java.awt.image.DataBufferInt conformance: white-box structure + black-box contract.
 * @run main DataBufferIntTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

public class DataBufferIntTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferInt");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        }

    static void testBehavior() {
        DataBufferInt db = new DataBufferInt(4);
        check(db.getDataType() == DataBuffer.TYPE_INT, "data type int");
        check(db.getSize() == 4, "size");
        db.setElem(0, 42);
        check(db.getElem(0) == 42, "set/get elem");
        db.setElem(1, -1);
        check(db.getElem(1) == -1, "negative int preserved");
        db.setElem(2, Integer.MIN_VALUE);
        check(db.getElem(2) == Integer.MIN_VALUE, "min int round-trip");
        db.setElem(3, Integer.MAX_VALUE);
        check(db.getElem(3) == Integer.MAX_VALUE, "max int round-trip");
        check(db.getElemFloat(0) == 42.0f, "getElemFloat");
        check(db.getElemDouble(0) == 42.0, "getElemDouble");
        db.setElemFloat(1, 2.5f);
        check(db.getElem(1) == 2, "setElemFloat truncates");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.getElem(-1); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferInt two = new DataBufferInt(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElem(1, 1, 7);
        check(two.getElem(1, 1) == 7, "banked round-trip");
        check(two.getElem(0, 1) == 0, "banks independent");
        int[] backing = { 1, 2, 3, 4 };
        DataBufferInt db2 = new DataBufferInt(backing, 4);
        check(db2.getElem(3) == 4, "backing array");
        check(db2.getData() == backing, "getData live reference");
        backing[0] = 50;
        check(db2.getElem(0) == 50, "external write visible");
        DataBufferInt off = new DataBufferInt(backing, 3, 1);
        check(off.getOffset() == 1 && off.getElem(0) == 2, "offset shifts access");
        DataBufferInt zero = new DataBufferInt(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferInt(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
