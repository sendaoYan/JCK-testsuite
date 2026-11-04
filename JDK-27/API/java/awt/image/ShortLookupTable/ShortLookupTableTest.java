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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
