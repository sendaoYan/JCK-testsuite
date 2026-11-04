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
        passed += DataBufferFloatDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class DataBufferFloatDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferFloat");
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
        DataBufferFloat db = new DataBufferFloat(4);
        check(db.getDataType() == DataBuffer.TYPE_FLOAT, "data type float");
        check(db.getSize() == 4, "size");
        check(db.getNumBanks() == 1, "single bank");
        db.setElemFloat(0, 1.5f);
        check(db.getElemFloat(0) == 1.5f, "set/get elemf");
        check(db.getElemDouble(0) == 1.5, "getElemDouble widens");
        // The int-typed view truncates toward zero.
        check(db.getElem(0) == 1, "getElem truncates fractional part");
        db.setElem(1, 7);
        check(db.getElemFloat(1) == 7.0f, "setElem stores exact int");
        check(db.getElemFloat(1) == 7.0f, "getElemFloat of int value");
        db.setElemDouble(2, 2.75);
        check(db.getElemDouble(2) == 2.75, "setElemDouble round-trip");
        db.setElemFloat(3, -0.5f);
        check(db.getElem(3) == 0, "negative fraction truncates toward zero");
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.setElemFloat(-1, 0.0f); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElemFloat(-1) -> AIOOBE");
    }

    static void testConstructors() {
        DataBufferFloat two = new DataBufferFloat(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        two.setElemFloat(1, 2, 9.5f);
        check(two.getElemFloat(1, 2) == 9.5f, "banked round-trip");
        check(two.getElemFloat(0, 2) == 0.0f, "banks independent");
        float[] backing = new float[] { 1.25f, 2.5f };
        DataBufferFloat view = new DataBufferFloat(backing, 2);
        check(view.getData() == backing, "getData live reference");
        backing[0] = -3.75f;
        check(view.getElemFloat(0) == -3.75f, "external write visible");
        DataBufferFloat off = new DataBufferFloat(backing, 1, 1);
        check(off.getElemFloat(0) == 2.5f, "offset shifts access");// JDK-27 validates the size argument directly in the constructor.
        try { new DataBufferFloat(0); check(false, "size 0 rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "size 0 -> IAE (JDK-27 validates size)"); }
        try { new DataBufferFloat(-1); check(false, "negative size rejected on JDK-27"); }
        catch (IllegalArgumentException e) { check(true, "negative size -> IAE (JDK-27)"); }
    }



    static int run() throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        return passed;
    }
}
