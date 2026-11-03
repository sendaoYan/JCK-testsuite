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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
