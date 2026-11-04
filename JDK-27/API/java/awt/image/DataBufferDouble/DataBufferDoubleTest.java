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
        passed += DataBufferDoubleDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class DataBufferDoubleDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferDouble");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        check(hasMethod(c, "getElemFloat"), "method getElemFloat");
        check(hasMethod(c, "setElemFloat"), "method setElemFloat");
        check(hasMethod(c, "getElemDouble"), "method getElemDouble");
        check(hasMethod(c, "setElemDouble"), "method setElemDouble");
        }

    static void testBehavior() {
        DataBufferDouble db = new DataBufferDouble(4);
        check(db.getDataType() == DataBuffer.TYPE_DOUBLE, "data type double");
        check(db.getSize() == 4, "size");
        check(db.getNumBanks() == 1, "single bank");
        db.setElemDouble(0, 1.5);
        check(db.getElemDouble(0) == 1.5, "set/get elem");
        check(db.getElemDouble(0) == 1.5, "getElemDouble widens");
        // The int-typed view truncates toward zero.
        check(db.getElem(0) == 1, "getElem truncates fractional part");
        db.setElem(1, 7);
        check(db.getElemDouble(1) == 7.0, "setElem stores exact int");
        check(db.getElemFloat(1) == 7.0f, "getElemFloat of int value");
        db.setElemDouble(2, 2.75);
        check(db.getElemDouble(2) == 2.75, "setElemDouble round-trip");
        db.setElemFloat(3, -0.5f);
        check(db.getElem(3) == 0, "negative fraction truncates toward zero");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElemDouble(-1, 0.0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElemDouble(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferDouble two = new DataBufferDouble(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElemDouble(1, 2, 9.5);
        check(two.getElemDouble(1, 2) == 9.5, "banked round-trip");
        check(two.getElemDouble(0, 2) == 0.0, "banks independent");
        double[] backing = new double[] { 1.25, 2.5 };
        DataBufferDouble view = new DataBufferDouble(backing, 2);
        check(view.getData() == backing, "getData live reference");
        backing[0] = -3.75;
        check(view.getElemDouble(0) == -3.75, "external write visible");
        DataBufferDouble off = new DataBufferDouble(backing, 1, 1);
        check(off.getElemDouble(0) == 2.5, "offset shifts access");// JDK-27 validates the size argument directly in the constructor.
        try { new DataBufferDouble(0); check(false, "size 0 rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "size 0 -> IAE (JDK-27 validates size)"); }
        try { new DataBufferDouble(-1); check(false, "negative size rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "negative size -> IAE (JDK-27)"); }
    }



    static int run() throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        return passed;
    }
}
