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
        passed += DataBufferUShortDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class DataBufferUShortDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferUShort");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        }

    static void testBehavior() {
        DataBufferUShort db = new DataBufferUShort(4);
        check(db.getDataType() == DataBuffer.TYPE_USHORT, "data type ushort");
        check(db.getSize() == 4, "size");
        db.setElem(0, -1);
        check(db.getElem(0) == 65535, "ushort masks to unsigned");
        db.setElem(1, 65535);
        check(db.getElem(1) == 65535, "max ushort round-trip");
        db.setElem(2, 65536);
        check(db.getElem(2) == 0, "overflow wraps to 0");
        db.setElem(3, 1000);
        check(db.getElem(3) == 1000, "in-range round-trip");
        check(db.getElemFloat(1) == 65535.0f, "getElemFloat");
        check(db.getElemDouble(1) == 65535.0, "getElemDouble");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElem(-1, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElem(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferUShort two = new DataBufferUShort(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElem(1, 1, 70000);
        check(two.getElem(1, 1) == 4464, "banked overflow wraps");
        check(two.getElem(0, 1) == 0, "banks independent");
        short[] backing = new short[] { 1, 2, 3 };
        DataBufferUShort view = new DataBufferUShort(backing, 3);
        check(view.getData() == backing, "getData live reference");
        DataBufferUShort off = new DataBufferUShort(backing, 2, 1);
        check(off.getElem(0) == 2, "offset shifts access");// JDK-27 validates the size argument directly in the constructor.
        try { new DataBufferUShort(0); check(false, "size 0 rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "size 0 -> IAE (JDK-27 validates size)"); }
        try { new DataBufferUShort(-1); check(false, "negative size rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "negative size -> IAE (JDK-27)"); }
    }



    static int run() throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        return passed;
    }
}
