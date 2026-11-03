/*
 * @test
 * @summary java.awt.image.DataBufferInt conformance: white-box structure + black-box behavior.
 * @run main DataBufferIntTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.lang.reflect.*;

public class DataBufferIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferInt", false, DataBufferIntTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isFinal(m), "final class");
        check(c.getSuperclass() == DataBuffer.class, "extends DataBuffer");
        check(hasMethod(c, "getData"), "getData()");
        check(hasMethod(c, "getData", int.class), "getData(int)");
        check(hasMethod(c, "getBankData"), "getBankData()");
        check(hasMethod(c, "getElem", int.class), "getElem(int)");
        check(hasMethod(c, "getElem", int.class, int.class), "getElem(int,int)");
        check(hasMethod(c, "setElem", int.class, int.class), "setElem(int,int)");
    }

    static void testBlackBox() {
        DataBufferInt db = new DataBufferInt(8);
        check(db.getDataType() == DataBuffer.TYPE_INT, "INT type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0 && db.getNumBanks() == 1, "offset 0 one bank");
        check(db.getData().length == 8, "getData length 8");
        db.setElem(3, 0x7FFFFFFF);
        check(db.getElem(3) == 0x7FFFFFFF, "max int round-trip");
        db.setElem(2, -12345);
        check(db.getElem(2) == -12345, "negative int round-trip");

        int[] backing = new int[] { 5, 6, 7, 8 };
        DataBufferInt fromArray = new DataBufferInt(backing, 4);
        check(fromArray.getSize() == 4, "array ctor size 4");
        check(fromArray.getData() == backing, "getData returns same array");
        check(fromArray.getElem(0) == 5 && fromArray.getElem(3) == 8, "array ctor values");
        fromArray.setElem(1, 999);
        check(backing[1] == 999, "setElem mutates backing array");

        DataBufferInt banks = new DataBufferInt(new int[][] { { 1, 2, 3 }, { 4, 5, 6 } }, 3);
        check(banks.getNumBanks() == 2, "two banks");
        check(banks.getElem(0) == 1 && banks.getElem(2) == 3, "bank 0");
        check(banks.getElem(1, 0) == 4 && banks.getElem(1, 2) == 6, "bank 1");
        banks.setElem(1, 1, 42);
        check(banks.getElem(1, 1) == 42, "bank 1 setElem");
        check(banks.getBankData().length == 2, "getBankData length 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
