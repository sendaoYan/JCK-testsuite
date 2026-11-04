/*
 * @test
 * @summary java.awt.image.DataBufferByte conformance: white-box structure + black-box contract.
 * @run main DataBufferByteTest
 */

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

public class DataBufferByteTest {
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
        Class<?> c = Class.forName("java.awt.image.DataBufferByte");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.DataBuffer"), "superclass java.awt.image.DataBuffer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getBankData"), "method getBankData");
        check(hasMethod(c, "getElem"), "method getElem");
        check(hasMethod(c, "setElem"), "method setElem");
        }

    static void testBehavior() {
        DataBufferByte db = new DataBufferByte(4);
        check(db.getDataType() == DataBuffer.TYPE_BYTE, "data type byte");
        check(db.getSize() == 4, "size");
        check(db.getOffset() == 0, "default offset 0");
        check(db.getNumBanks() == 1, "default single bank");
        check(db.getOffsets().length == 1 && db.getOffsets()[0] == 0, "offsets array");

        // Element round-trip with unsigned narrowing: values wrap modulo 256.
        db.setElem(0, 300);
        check(db.getElem(0) == 44, "setElem(300) narrows to 44");
        db.setElem(1, -1);
        check((db.getElem(1) & 0xff) == 255, "setElem(-1) wraps to 255");
        db.setElem(2, 127);
        check(db.getElem(2) == 127, "positive round-trip");
        check(db.getElemFloat(2) == 127.0f, "getElemFloat");
        check(db.getElemDouble(2) == 127.0, "getElemDouble");
        db.setElemFloat(3, 1.75f);
        check(db.getElem(3) == 1, "setElemFloat narrows via int cast");

        // Out-of-range indices -> AIOOBE.
        boolean threw = false;
        try { db.getElem(4); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(size) -> AIOOBE");
        threw = false;
        try { db.getElem(-1); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getElem(-1) -> AIOOBE");
        threw = false;
        try { db.setElem(4, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "setElem(size) -> AIOOBE");
    }

    static void testConstructors() {
        // Banked allocation.
        DataBufferByte two = new DataBufferByte(3, 2);
        check(two.getNumBanks() == 2, "banked ctor banks");
        check(two.getSize() == 3, "banked ctor size");
        two.setElem(1, 1, 100);
        check(two.getElem(1, 1) == 100, "banked round-trip");
        check(two.getElem(0, 1) == 0, "banks independent");
        check(two.getOffsets().length == 2, "banked offsets length");

        // User array (single bank): live view, not a copy.
        byte[] backing = new byte[] { 1, 2, 3 };
        DataBufferByte view = new DataBufferByte(backing, 3);
        check(view.getData() == backing, "getData returns live reference");
        backing[0] = 99;
        check(view.getElem(0) == 99, "external write visible through buffer");

        // Offset ctor shifts element indexing inside the bank.
        DataBufferByte off = new DataBufferByte(backing, 2, 1);
        check(off.getOffset() == 1, "offset recorded");
        check(off.getElem(0) == 2, "offset shifts element access");
        check(off.getSize() == 2, "size limited by ctor");

        // Banked user arrays with per-bank offsets.
        DataBufferByte mb = new DataBufferByte(new byte[][] { new byte[3], new byte[3] }, 1, new int[] { 0, 1 });
        check(mb.getNumBanks() == 2, "multi-bank arrays");
        mb.setElem(1, 1, 100);
        check(mb.getElem(1, 1) == 100, "multi-bank set/get");
        check(mb.getBankData().length == 2, "getBankData banks");
        check(mb.getBankData()[1] == mb.getData(1), "getBankData bank matches getData");
        // JDK-8 accepts zero size (empty buffer) and rejects negative with NegativeArraySizeException.
        DataBufferByte zero = new DataBufferByte(0);
        check(zero.getSize() == 0, "zero size accepted on JDK-8");
        boolean threw = false;
        try { new DataBufferByte(-1); } catch (NegativeArraySizeException e) { threw = true; }
        check(threw, "negative size -> NegativeArraySizeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBehavior();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
