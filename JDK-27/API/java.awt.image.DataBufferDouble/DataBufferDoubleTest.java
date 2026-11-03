/*
 * @test
 * @summary java.awt.image.DataBufferDouble conformance: white-box structure + black-box behavior.
 * @run main DataBufferDoubleTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.lang.reflect.*;

public class DataBufferDoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferDouble", false, DataBufferDoubleTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final class");
        check(c.getSuperclass() == DataBuffer.class, "extends DataBuffer");
        check(hasMethod(c, "getData"), "getData()");
        check(hasMethod(c, "getData", int.class), "getData(int)");
        check(hasMethod(c, "getBankData"), "getBankData()");
        check(hasMethod(c, "getElemFloat", int.class), "getElemFloat(int)");
        check(hasMethod(c, "getElemDouble", int.class), "getElemDouble(int)");
        check(hasMethod(c, "setElemDouble", int.class, double.class), "setElemDouble(int,double)");
    }

    static void testBlackBox() {
        DataBufferDouble db = new DataBufferDouble(8);
        check(db.getDataType() == DataBuffer.TYPE_DOUBLE, "DOUBLE type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0 && db.getNumBanks() == 1, "offset 0 one bank");
        check(db.getData().length == 8, "getData length 8");

        db.setElemDouble(3, 2.5);
        check(db.getElemDouble(3) == 2.5, "setElemDouble/getElemDouble round-trip");
        check(db.getElem(3) == 2, "getElem truncates double to 2");
        db.setElemFloat(2, 7.75f);
        check(db.getElemFloat(2) == 7.75f, "setElemFloat/getElemFloat round-trip");
        check(db.getElemDouble(2) == 7.75, "getElemDouble from float-backed value");
        db.setElem(1, 9);
        check(db.getElemDouble(1) == 9.0, "setElem(int) stores double 9.0");

        DataBufferDouble fromArray = new DataBufferDouble(new double[] { 1.5, 2.5, 3.5 }, 3);
        check(fromArray.getSize() == 3, "array ctor size 3");
        check(fromArray.getElemDouble(0) == 1.5 && fromArray.getElemDouble(2) == 3.5, "array ctor values");
        fromArray.setElemDouble(1, 99.25);
        check(fromArray.getElemDouble(1) == 99.25, "array ctor setElemDouble");

        DataBufferDouble banks = new DataBufferDouble(new double[][] { { 1, 2 }, { 3, 4 } }, 2);
        check(banks.getNumBanks() == 2, "two banks");
        check(banks.getElemDouble(0) == 1.0 && banks.getElemDouble(1) == 2.0, "bank 0");
        check(banks.getElemDouble(1, 0) == 3.0 && banks.getElemDouble(1, 1) == 4.0, "bank 1");
        banks.setElemDouble(1, 1, 8.5);
        check(banks.getElemDouble(1, 1) == 8.5, "bank 1 setElemDouble");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
