/*
 * @test
 * @summary java.awt.image.DataBufferByte conformance: white-box structure + black-box behavior.
 * @run main DataBufferByteTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.lang.reflect.*;

public class DataBufferByteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBufferByte", false, DataBufferByteTest.class.getClassLoader());
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
        check(hasMethod(c, "setElem", int.class, int.class, int.class), "setElem(int,int,int)");
    }

    static void testBlackBox() {
        DataBufferByte db = new DataBufferByte(8);
        check(db.getDataType() == DataBuffer.TYPE_BYTE, "BYTE type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0 && db.getNumBanks() == 1, "offset 0 one bank");
        check(db.getData().length == 8, "getData length 8");
        db.setElem(3, 200);
        check(db.getElem(3) == 200, "getElem/setElem 200");
        check(db.getData()[3] == (byte) 200, "backing array byte 200");

        // byte storage is unsigned on read: (byte)-56 == 200
        db.setElem(4, -56);
        check(db.getElem(4) == 200, "unsigned read -56 -> 200");

        DataBufferByte fromArray = new DataBufferByte(new byte[] { 1, 2, 3, 4 }, 4);
        check(fromArray.getSize() == 4, "array ctor size 4");
        check(fromArray.getElem(0) == 1 && fromArray.getElem(3) == 4, "array ctor values");
        fromArray.setElem(1, 99);
        check(fromArray.getElem(1) == 99, "array ctor setElem");

        DataBufferByte banks = new DataBufferByte(new byte[][] { { 10, 20 }, { 30, 40 } }, 2);
        check(banks.getNumBanks() == 2, "two banks");
        check(banks.getElem(0) == 10 && banks.getElem(1) == 20, "bank 0");
        check(banks.getElem(1, 0) == 30 && banks.getElem(1, 1) == 40, "bank 1");
        banks.setElem(1, 1, 77);
        check(banks.getElem(1, 1) == 77, "bank 1 setElem");
        check(banks.getData(1)[1] == 77, "getData(1) reflects write");
        check(banks.getBankData().length == 2, "getBankData length 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
