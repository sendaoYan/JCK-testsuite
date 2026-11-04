/*
 * @test
 * @summary java.awt.image.ShortLookupTable conformance: white-box structure + black-box behavior.
 * @run main ShortLookupTableTest
 */

import java.awt.image.ShortLookupTable;
import java.awt.image.LookupTable;
import java.lang.reflect.*;

public class ShortLookupTableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ShortLookupTable", false, ShortLookupTableTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == LookupTable.class, "extends LookupTable");
        check(hasMethod(c, "getTable"), "getTable()");
        check(hasMethod(c, "lookupPixel", int[].class, int[].class), "lookupPixel(int[],int[])");
        check(hasMethod(c, "lookupPixel", short[].class, short[].class), "lookupPixel(short[],short[])");
    }

    static void testBlackBox() {
        short[][] table = new short[3][65536];
        for (int i = 0; i < 65536; i++) {
            table[0][i] = (short) (i % 1000);
            table[1][i] = (short) ((i + 1) % 1000);
            table[2][i] = (short) ((i * 2) % 1000);
        }
        ShortLookupTable lut = new ShortLookupTable(0, table);
        check(lut.getNumComponents() == 3, "3 components");
        check(lut.getOffset() == 0, "offset 0");
        short[][] got = lut.getTable();
        check(got.length == 3 && got[0].length == 65536, "table dims");

        int[] res = lut.lookupPixel(new int[] { 3, 4, 5 }, null);
        check(res[0] == 3 && res[1] == 5 && res[2] == 10, "lookupPixel(int[]) maps bands");

        // single-band short table applies to all channels; masks input with 0xffff
        short[] single = new short[65536];
        for (int i = 0; i < 65536; i++) single[i] = (short) (i % 5000);
        ShortLookupTable lut1 = new ShortLookupTable(0, single);
        check(lut1.getNumComponents() == 1, "single component");
        int[] r1 = lut1.lookupPixel(new int[] { 7, 65541 }, null);
        check(r1[0] == 7, "single table lookup 7");
        check(r1[1] == 5, "single table masks 65541&0xffff=5");

        short[] sres = lut1.lookupPixel(new short[] { 3, 4, 5 }, null);
        check(sres[0] == 3 && sres[1] == 4 && sres[2] == 5, "lookupPixel(short[])");

        ShortLookupTable offLut = new ShortLookupTable(1, single);
        check(offLut.getOffset() == 1, "offset 1");
        int[] ro = offLut.lookupPixel(new int[] { 9 }, null);
        check(ro[0] == 8, "offset lookup 9-1 -> table[8]");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += ShortLookupTableDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ShortLookupTableDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.ShortLookupTable");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.LookupTable"), "superclass java.awt.image.LookupTable");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTable"), "method getTable");
        check(hasMethod(c, "lookupPixel"), "method lookupPixel");
    }

    static void testSingleArray() {
        short[] data = new short[] { 100, 300, 500, 700 };
        ShortLookupTable t = new ShortLookupTable(0, data);
        check(t.getNumComponents() == 1, "1 component");
        check(t.getOffset() == 0, "offset 0");
        check(t.getTable().length == 1 && t.getTable()[0] == data, "getTable returns wrapped array");

        // short lookup supports values above 255 (unlike ByteLookupTable).
        short[] dst = t.lookupPixel(new short[] { 0, 1, 2, 3 }, null);
        check(dst[0] == 100 && dst[1] == 300 && dst[3] == 700, "short lookup exact above 255");
        short[] provided = new short[4];
        check(t.lookupPixel(new short[] { 0, 1, 2, 3 }, provided) == provided, "short lookup reuses dst");

        // int lookup through the abstract surface.
        int[] idst = t.lookupPixel(new int[] { 1, 3 }, null);
        check(idst[0] == 300 && idst[1] == 700, "int lookup exact");
        int[] ireused = new int[2];
        check(t.lookupPixel(new int[] { 0, 2 }, ireused) == ireused && ireused[1] == 500,
              "int lookup reuses dst");
    }

    static void testMultiArray() {
        short[][] data = new short[][] { new short[] { 1000, 2000 }, new short[] { 10, 20 } };
        ShortLookupTable t = new ShortLookupTable(0, data);
        check(t.getNumComponents() == 2, "2 components");
        check(t.getTable()[0][1] == 2000 && t.getTable()[1][1] == 20, "getTable equal content");
        int[] out = t.lookupPixel(new int[] { 1, 1 }, null);
        check(out[0] == 2000 && out[1] == 20, "multi-component lookup");
        short[] sout = t.lookupPixel(new short[] { 0, 1 }, null);
        check(sout[0] == 1000 && sout[1] == 20, "multi-component short lookup");
    }

    static void testOffset() {
        ShortLookupTable t = new ShortLookupTable(5, new short[] { 9000, 10000 });
        check(t.getOffset() == 5, "offset recorded");
        int[] out = t.lookupPixel(new int[] { 5, 6 }, null);
        check(out[0] == 9000 && out[1] == 10000, "offset shifts domain");
    }

    static void testErrors() {
        boolean threw = false;
        try { new ShortLookupTable(0, (short[]) null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null table -> NPE");
        threw = false;
        try { new ShortLookupTable(-1, new short[4]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative offset -> IAE");
        threw = false;
        try { new ShortLookupTable(0, new short[4]).lookupPixel((short[]) null, null); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "null src -> NPE");
        threw = false;
        try { new ShortLookupTable(0, new short[4]).lookupPixel(new short[] { 99 }, null); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "src beyond table -> AIOOBE");
        threw = false;
        try { new ShortLookupTable(0, new short[4]).lookupPixel(new int[] { 4 }, null); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "int src beyond table -> AIOOBE");
        // More source components than table rows is legal; every component is mapped.
        int[] many = new ShortLookupTable(0, new short[][] { new short[] { 500, 600, 700, 800 } })
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
