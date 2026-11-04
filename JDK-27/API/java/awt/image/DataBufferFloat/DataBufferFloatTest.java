/*
 * @test
 * @summary java.awt.image.DataBufferFloat conformance: white-box structure + black-box behavior.
 * @run main DataBufferFloatTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.lang.reflect.*;

public class DataBufferFloatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferFloat", false, DataBufferFloatTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final class");
        check(c.getSuperclass() == DataBuffer.class, "extends DataBuffer");
        check(hasMethod(c, "getData"), "getData()");
        check(hasMethod(c, "getData", int.class), "getData(int)");
        check(hasMethod(c, "getBankData"), "getBankData()");
        check(hasMethod(c, "getElemFloat", int.class), "getElemFloat(int)");
        check(hasMethod(c, "getElemFloat", int.class, int.class), "getElemFloat(int,int)");
        check(hasMethod(c, "setElemFloat", int.class, float.class), "setElemFloat(int,float)");
        check(hasMethod(c, "getElemDouble", int.class), "getElemDouble(int)");
        check(hasMethod(c, "setElemDouble", int.class, double.class), "setElemDouble(int,double)");
    }

    static void testBlackBox() {
        DataBufferFloat db = new DataBufferFloat(8);
        check(db.getDataType() == DataBuffer.TYPE_FLOAT, "FLOAT type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0 && db.getNumBanks() == 1, "offset 0 one bank");
        check(db.getData().length == 8, "getData length 8");

        db.setElemFloat(3, 2.5f);
        check(db.getElemFloat(3) == 2.5f, "setElemFloat/getElemFloat round-trip");
        check(db.getElem(3) == 2, "getElem truncates float to 2");
        db.setElemDouble(2, 7.75);
        check(db.getElemDouble(2) == 7.75, "setElemDouble/getElemDouble round-trip");
        check(db.getElemFloat(2) == 7.75f, "getElemFloat from double-backed value");
        db.setElem(1, 9);
        check(db.getElemFloat(1) == 9.0f, "setElem(int) stores float 9.0");

        DataBufferFloat fromArray = new DataBufferFloat(new float[] { 1.5f, 2.5f, 3.5f }, 3);
        check(fromArray.getSize() == 3, "array ctor size 3");
        check(fromArray.getElemFloat(0) == 1.5f && fromArray.getElemFloat(2) == 3.5f, "array ctor values");
        fromArray.setElemFloat(1, 99.25f);
        check(fromArray.getElemFloat(1) == 99.25f, "array ctor setElemFloat");

        DataBufferFloat banks = new DataBufferFloat(new float[][] { { 1f, 2f }, { 3f, 4f } }, 2);
        check(banks.getNumBanks() == 2, "two banks");
        check(banks.getElemFloat(0) == 1f && banks.getElemFloat(1) == 2f, "bank 0");
        check(banks.getElemFloat(1, 0) == 3f && banks.getElemFloat(1, 1) == 4f, "bank 1");
        banks.setElemFloat(1, 1, 8.5f);
        check(banks.getElemFloat(1, 1) == 8.5f, "bank 1 setElemFloat");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
