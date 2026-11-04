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
        passed += DataBufferIntDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class DataBufferIntDeepContract {
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
        check(off.getOffset() == 1 && off.getElem(0) == 2, "offset shifts access");// JDK-27 validates the size argument directly in the constructor.
        try { new DataBufferInt(0); check(false, "size 0 rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "size 0 -> IAE (JDK-27 validates size)"); }
        try { new DataBufferInt(-1); check(false, "negative size rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "negative size -> IAE (JDK-27)"); }
    }



    static int run() throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        return passed;
    }
}
