/*
 * @test
 * @summary java.awt.image.DataBuffer conformance: white-box structure + black-box behavior.
 * @run main DataBufferTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.lang.reflect.*;

public class DataBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }
    static boolean hasField(Class<?> c, String name) {
        try { c.getField(name); return true; } catch (NoSuchFieldException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DataBuffer", false, DataBufferTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasField(c, "TYPE_BYTE"), "TYPE_BYTE");
        check(hasField(c, "TYPE_USHORT"), "TYPE_USHORT");
        check(hasField(c, "TYPE_SHORT"), "TYPE_SHORT");
        check(hasField(c, "TYPE_INT"), "TYPE_INT");
        check(hasField(c, "TYPE_FLOAT"), "TYPE_FLOAT");
        check(hasField(c, "TYPE_DOUBLE"), "TYPE_DOUBLE");
        check(hasField(c, "TYPE_UNDEFINED"), "TYPE_UNDEFINED");
        check(hasMethod(c, "getDataTypeSize", int.class), "getDataTypeSize(int)");
        check(hasMethod(c, "getElem", int.class, int.class), "getElem(int,int)");
        check(hasMethod(c, "setElem", int.class, int.class, int.class), "setElem(int,int,int)");
        check(hasMethod(c, "getElemFloat", int.class), "getElemFloat(int)");
        check(hasMethod(c, "getElemDouble", int.class, int.class), "getElemDouble(int,int)");
    }

    // ---- Part 2: black-box (behavioral, via concrete DataBufferInt) ----
    static void testBlackBox() {
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE) == 8, "BYTE size 8");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_USHORT) == 16, "USHORT size 16");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_SHORT) == 16, "SHORT size 16");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_INT) == 32, "INT size 32");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_FLOAT) == 32, "FLOAT size 32");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_DOUBLE) == 64, "DOUBLE size 64");
        boolean threw = false;
        try { DataBuffer.getDataTypeSize(DataBuffer.TYPE_UNDEFINED); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getDataTypeSize(TYPE_UNDEFINED) throws");

        DataBufferInt db = new DataBufferInt(8);
        check(db.getDataType() == DataBuffer.TYPE_INT, "INT data type");
        check(db.getSize() == 8, "size 8");
        check(db.getOffset() == 0, "default offset 0");
        check(db.getNumBanks() == 1, "one bank");
        check(db.getOffsets().length == 1 && db.getOffsets()[0] == 0, "offsets [0]");

        db.setElem(3, 77);
        check(db.getElem(3) == 77, "getElem/setElem round-trip");
        check(db.getElem(0) == 0, "untouched elem 0");
        check(db.getElemFloat(3) == 77.0f, "getElemFloat promotes int");
        check(db.getElemDouble(3) == 77.0, "getElemDouble promotes int");
        db.setElemFloat(1, 2.5f);
        check(db.getElem(1) == 2, "setElemFloat truncates to 2");
        db.setElemDouble(2, 9.99);
        check(db.getElem(2) == 9, "setElemDouble truncates to 9");

        // Two-bank DataBufferInt exercises bank/offset dispatch.
        DataBufferInt db2 = new DataBufferInt(new int[][] { { 10, 20, 30 }, { 40, 50, 60 } }, 3, new int[] { 0, 0 });
        check(db2.getNumBanks() == 2, "two banks");
        check(db2.getSize() == 3, "two-bank size 3");
        check(db2.getElem(0) == 10 && db2.getElem(1) == 20 && db2.getElem(2) == 30, "bank 0 values");
        db2.setElem(1, 1, 55);
        check(db2.getElem(1, 1) == 55, "bank 1 setElem/getElem");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
