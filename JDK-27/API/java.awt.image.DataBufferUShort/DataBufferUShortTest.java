/*
 * @test
 * @summary java.awt.image.DataBufferUShort conformance: white-box structure + black-box behavior.
 * @run main DataBufferUShortTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.lang.reflect.*;

public class DataBufferUShortTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferUShort", false, DataBufferUShortTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final class");
        check(c.getSuperclass() == DataBuffer.class, "extends DataBuffer");
        check(hasMethod(c, "getData"), "getData()");
        check(hasMethod(c, "getData", int.class), "getData(int)");
        check(hasMethod(c, "getBankData"), "getBankData()");
        check(hasMethod(c, "getElem", int.class), "getElem(int)");
        check(hasMethod(c, "setElem", int.class, int.class), "setElem(int,int)");
    }

    static void testBlackBox() {
        DataBufferUShort db = new DataBufferUShort(8);
        check(db.getDataType() == DataBuffer.TYPE_USHORT, "USHORT type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0 && db.getNumBanks() == 1, "offset 0 one bank");
        check(db.getData().length == 8, "getData length 8");
        db.setElem(3, 65535);
        check(db.getElem(3) == 65535, "unsigned short max round-trip");
        db.setElem(2, -1);
        check(db.getElem(2) == 65535, "unsigned read -1 -> 65535");

        DataBufferUShort fromArray = new DataBufferUShort(new short[] { 1, 2, 3, 4 }, 4);
        check(fromArray.getSize() == 4, "array ctor size 4");
        check(fromArray.getElem(0) == 1 && fromArray.getElem(3) == 4, "array ctor values");
        fromArray.setElem(1, 40000);
        check(fromArray.getElem(1) == 40000, "array ctor setElem");

        DataBufferUShort banks = new DataBufferUShort(new short[][] { { 10, 20 }, { 30, 40 } }, 2);
        check(banks.getNumBanks() == 2, "two banks");
        check(banks.getElem(0) == 10 && banks.getElem(1) == 20, "bank 0");
        check(banks.getElem(1, 0) == 30 && banks.getElem(1, 1) == 40, "bank 1");
        banks.setElem(1, 1, 500);
        check(banks.getElem(1, 1) == 500, "bank 1 setElem");
        check(banks.getBankData().length == 2, "getBankData length 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
