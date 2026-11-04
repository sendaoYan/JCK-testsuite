/*
 * @test
 * @summary java.awt.image.ByteLookupTable conformance: white-box structure + black-box contract.
 * @run main ByteLookupTableTest
 */

import java.awt.image.ByteLookupTable;

public class ByteLookupTableTest {
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleArray();
        testMultiArray();
        testOffset();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
