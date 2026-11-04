/*
 * @test
 * @summary java.awt.image.DataBuffer conformance: white-box structure + black-box behavior.
 * @run main DataBufferTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.lang.reflect.*;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;

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
        passed += DataBufferDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class DataBufferDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
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
        Class<?> c = Class.forName("java.awt.image.DataBuffer");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDataTypeSize"), "method getDataTypeSize");
        check(hasMethod(c, "getDataType"), "method getDataType");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getOffsets"), "method getOffsets");
        check(hasMethod(c, "getNumBanks"), "method getNumBanks");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        check(hasMethod(c, "getElemFloat"), "method getElemFloat");
        check(hasMethod(c, "setElemFloat"), "method setElemFloat");
        check(hasMethod(c, "getElemDouble"), "method getElemDouble");
        check(hasMethod(c, "setElemDouble"), "method setElemDouble");
    }

    static void testTypeConstants() {
        check(DataBuffer.TYPE_BYTE == 0, "TYPE_BYTE == 0");
        check(DataBuffer.TYPE_USHORT == 1, "TYPE_USHORT == 1");
        check(DataBuffer.TYPE_SHORT == 2, "TYPE_SHORT == 2");
        check(DataBuffer.TYPE_INT == 3, "TYPE_INT == 3");
        check(DataBuffer.TYPE_FLOAT == 4, "TYPE_FLOAT == 4");
        check(DataBuffer.TYPE_DOUBLE == 5, "TYPE_DOUBLE == 5");
        check(DataBuffer.TYPE_UNDEFINED == 32, "TYPE_UNDEFINED == 32");
        // getDataTypeSize is exact for every defined type.
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE) == 8, "size of byte");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_USHORT) == 16, "size of ushort");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_SHORT) == 16, "size of short");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_INT) == 32, "size of int");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_FLOAT) == 32, "size of float");
        check(DataBuffer.getDataTypeSize(DataBuffer.TYPE_DOUBLE) == 64, "size of double");
        boolean threw = false;
        try { DataBuffer.getDataTypeSize(DataBuffer.TYPE_UNDEFINED); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getDataTypeSize(UNDEFINED) -> IAE");
        threw = false;
        try { DataBuffer.getDataTypeSize(99); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getDataTypeSize(99) -> IAE");
    }

    static void testEachConcreteType() {
        DataBuffer[] bufs = {
            new DataBufferByte(2), new DataBufferUShort(2), new DataBufferShort(2),
            new DataBufferInt(2), new DataBufferFloat(2), new DataBufferDouble(2) };
        int[] types = { DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, DataBuffer.TYPE_SHORT,
            DataBuffer.TYPE_INT, DataBuffer.TYPE_FLOAT, DataBuffer.TYPE_DOUBLE };
        String[] names = { "BYTE", "USHORT", "SHORT", "INT", "FLOAT", "DOUBLE" };
        for (int i = 0; i < bufs.length; i++) {
            check(bufs[i].getDataType() == types[i], names[i] + " reports its data type");
            check(bufs[i].getSize() == 2, names[i] + " size");
            check(bufs[i].getOffset() == 0, names[i] + " offset 0");
            check(bufs[i].getNumBanks() == 1, names[i] + " single bank");
            // Bank-0 element access routes through the two-arg variant.
            bufs[i].setElem(0, 1);
            check(bufs[i].getElem(0) == 1, names[i] + " set/get through base type");
            // Float view of any element is the int value widened.
            check(bufs[i].getElemFloat(0) == 1.0f, names[i] + " getElemFloat");
            check(bufs[i].getElemDouble(0) == 1.0, names[i] + " getElemDouble");
            bufs[i].setElem(1, 2);
            check(bufs[i].getElem(1) == 2, names[i] + " second element round-trip");
        }
    }

    static void testTypedNarrowing() {
        // The int-typed view narrows per the storage type.
        DataBufferByte b = new DataBufferByte(1);
        b.setElem(0, 0x1FF);
        check(b.getElem(0) == 0xFF, "byte buffer wraps modulo 256");
        DataBufferUShort u = new DataBufferUShort(1);
        u.setElem(0, 0x1FFFF);
        check(u.getElem(0) == 0xFFFF, "ushort buffer wraps modulo 65536");
        DataBufferShort s = new DataBufferShort(1);
        s.setElem(0, 0x7FFF + 1);
        check(s.getElem(0) == Short.MIN_VALUE, "short buffer wraps to min");
        DataBufferInt i = new DataBufferInt(1);
        i.setElem(0, -1);
        check(i.getElem(0) == -1, "int buffer keeps full range");
        DataBufferFloat f = new DataBufferFloat(1);
        f.setElemFloat(0, 2.5f);
        check(f.getElem(0) == 2, "float buffer truncates on int view");
        DataBufferDouble d = new DataBufferDouble(1);
        d.setElemDouble(0, -2.5);
        check(d.getElem(0) == -2, "double buffer truncates toward zero");
    }



    static int run() throws Exception {
        testWhiteBox();
        testTypeConstants();
        testEachConcreteType();
        testTypedNarrowing();
        return passed;
    }
}
