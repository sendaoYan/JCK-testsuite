/*
 * @test
 * @summary java.awt.image.ByteLookupTable conformance: white-box structure + black-box behavior.
 * @run main ByteLookupTableTest
 */

import java.awt.image.ByteLookupTable;
import java.awt.image.LookupTable;
import java.lang.reflect.*;

public class ByteLookupTableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ByteLookupTable", false, ByteLookupTableTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == LookupTable.class, "extends LookupTable");
        check(hasMethod(c, "getTable"), "getTable()");
        check(hasMethod(c, "lookupPixel", int[].class, int[].class), "lookupPixel(int[],int[])");
        check(hasMethod(c, "lookupPixel", byte[].class, byte[].class), "lookupPixel(byte[],byte[])");
    }

    static void testBlackBox() {
        byte[][] table = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            table[0][i] = (byte) (i & 0x7F);
            table[1][i] = (byte) ((i + 1) & 0x7F);
            table[2][i] = (byte) ((i * 2) & 0x7F);
        }
        ByteLookupTable lut = new ByteLookupTable(0, table);
        check(lut.getNumComponents() == 3, "3 components");
        check(lut.getOffset() == 0, "offset 0");
        byte[][] got = lut.getTable();
        check(got.length == 3, "table 3 bands");
        check(got[0].length == 256, "table length 256");

        int[] res = lut.lookupPixel(new int[] { 3, 4, 5 }, null);
        check(res[0] == 3 && res[1] == 5 && res[2] == 10, "lookupPixel(int[]) maps bands");

        // single-band table applies to all channels
        byte[] single = new byte[256];
        for (int i = 0; i < 256; i++) single[i] = (byte) (i & 0x7F);
        ByteLookupTable lut1 = new ByteLookupTable(0, single);
        check(lut1.getNumComponents() == 1, "single component");
        int[] r1 = lut1.lookupPixel(new int[] { 10, 130 }, null);
        check(r1[0] == 10 && r1[1] == 2, "single table lookup (130&127=2)");

        // byte[] variant
        byte[] bres = lut1.lookupPixel(new byte[] { 7, 8, 9 }, null);
        check(bres[0] == 7 && bres[1] == 8 && bres[2] == 9, "lookupPixel(byte[])");

        // offset shifts the input index
        ByteLookupTable offLut = new ByteLookupTable(1, single);
        check(offLut.getOffset() == 1, "offset 1");
        int[] ro = offLut.lookupPixel(new int[] { 5 }, null);
        check(ro[0] == 4, "offset lookup 5-1 -> table[4]");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += ByteLookupTableDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ByteLookupTableDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.ByteLookupTable");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.LookupTable"), "superclass java.awt.image.LookupTable");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTable"), "method getTable");
        check(hasMethod(c, "lookupPixel"), "method lookupPixel");
    }

    static void testSingleArray() {
        byte[] data = new byte[] { 0, 10, 20, 30, 40 };
        ByteLookupTable t = new ByteLookupTable(0, data);
        check(t.getNumComponents() == 1, "1 component");
        check(t.getOffset() == 0, "offset 0");
        check(t.getTable().length == 1 && t.getTable()[0] == data, "getTable returns wrapped array");

        // byte lookup.
        byte[] src = { 0, 1, 2, 3, 4 };
        byte[] dst = t.lookupPixel(src, null);
        check(dst.length == 5, "byte lookup length");
        check(dst[0] == 0 && dst[1] == 10 && dst[4] == 40, "byte lookup exact");
        byte[] provided = new byte[5];
        check(t.lookupPixel(src, provided) == provided, "byte lookup reuses dst");
        check(provided[2] == 20, "reused dst filled");

        // int lookup (abstract LookupTable surface).
        int[] isrc = { 0, 1, 2, 3, 4 };
        int[] idst = t.lookupPixel(isrc, null);
        check(idst[0] == 0 && idst[3] == 30, "int lookup exact");
        int[] ireused = new int[5];
        check(t.lookupPixel(isrc, ireused) == ireused && ireused[4] == 40, "int lookup reuses dst");

        // Byte values map through the unsigned byte table.
        ByteLookupTable big = new ByteLookupTable(0, new byte[256]);
        for (int i = 0; i < 256; i++) big.getTable()[0][i] = (byte) (255 - i);
        int[] inv = big.lookupPixel(new int[] { 10, 200 }, null);
        check(inv[0] == (byte) 245 && inv[1] == 55, "int lookup yields signed byte value");
    }

    static void testMultiArray() {
        byte[][] data = new byte[][] { new byte[] { 1, 2, 3 }, new byte[] { 10, 20, 30 } };
        ByteLookupTable t = new ByteLookupTable(0, data);
        check(t.getNumComponents() == 2, "2 components");
        check(t.getTable().length == 2 && t.getTable()[0][1] == 2 && t.getTable()[1][1] == 20,
              "2d getTable equal content");
        int[] out = t.lookupPixel(new int[] { 2, 2 }, null);
        check(out[0] == 3 && out[1] == 30, "multi-component lookup");
        byte[] bout = t.lookupPixel(new byte[] { 0, 1 }, null);
        check(bout[0] == 1 && bout[1] == 20, "multi-component byte lookup");
    }

    static void testOffset() {
        // The offset shifts the domain: index i maps to table[i - offset].
        ByteLookupTable t = new ByteLookupTable(2, new byte[] { (byte) 100, (byte) 200, 0, 7, 8, 9, 10, 11 });
        check(t.getOffset() == 2, "offset recorded");
        int[] out = t.lookupPixel(new int[] { 2, 3 }, null);
        check(out[0] == 100 && out[1] == (byte) 200, "offset shifts domain start (signed byte)");
        int[] hi = t.lookupPixel(new int[] { 9 }, null);
        check(hi[0] == 11, "offset shifts domain end");
    }

    static void testErrors() {
        boolean threw = false;
        try { new ByteLookupTable(0, (byte[]) null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null table -> NPE");
        threw = false;
        try { new ByteLookupTable(-1, new byte[4]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative offset -> IAE");
        threw = false;
        try { new ByteLookupTable(0, new byte[4]).lookupPixel((int[]) null, null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "null src -> NPE");
        threw = false;
        try { new ByteLookupTable(0, new byte[4]).lookupPixel(new int[] { 99 }, null); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "src beyond table -> AIOOBE");
        threw = false;
        try { new ByteLookupTable(0, new byte[4]).lookupPixel(new int[] { -1 }, null); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "negative src -> AIOOBE");
        // More source components than table rows is legal; every component is mapped.
        int[] many = new ByteLookupTable(0, new byte[][] { new byte[] { 5, 6, 7, 8 } })
                .lookupPixel(new int[] { 1, 1, 1 }, null);
        check(many.length == 3, "extra components mapped without throw");
    }



    static int run() throws Exception {
        testWhiteBox();
        testSingleArray();
        testMultiArray();
        testOffset();
        testErrors();
        return passed;
    }
}
