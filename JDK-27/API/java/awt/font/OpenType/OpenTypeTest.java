/*
 * @test
 * @summary java.awt.font.OpenType conformance: white-box structure plus black-box behavior.
 * @run main OpenTypeTest
 */

import java.awt.font.OpenType;
import java.lang.reflect.*;

public class OpenTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.OpenType");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        for (String s : new String[]{"TAG_CMAP","TAG_HEAD","TAG_NAME","TAG_GLYF","TAG_MAXP"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + s);
        }
        check(hasMethod(c, "getVersion"), "getVersion()");
        check(hasMethod(c, "getFontTable", int.class), "getFontTable(int)");
        check(hasMethod(c, "getFontTable", String.class), "getFontTable(String)");
        check(hasMethod(c, "getFontTable", int.class, int.class, int.class), "getFontTable(int,int,int)");
        check(hasMethod(c, "getFontTableSize", int.class), "getFontTableSize(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(OpenType.TAG_CMAP != OpenType.TAG_HEAD, "distinct tag constants");

        // anonymous implementation
        OpenType ot = new OpenType() {
            public int getVersion() { return 0x00010000; }
            public byte[] getFontTable(int tag) { return new byte[]{1, 2, 3}; }
            public byte[] getFontTable(String tag) { return new byte[]{4, 5}; }
            public byte[] getFontTable(int tag, int off, int len) { return new byte[]{6}; }
            public byte[] getFontTable(String tag, int off, int len) { return new byte[]{7}; }
            public int getFontTableSize(int tag) { return 3; }
            public int getFontTableSize(String tag) { return 2; }
        };
        check(ot.getVersion() == 0x00010000, "getVersion");
        check(ot.getFontTable(OpenType.TAG_CMAP).length == 3, "getFontTable(int)");
        check(ot.getFontTable("cmap").length == 2, "getFontTable(String)");
        check(ot.getFontTable(OpenType.TAG_CMAP, 0, 2).length == 1, "getFontTable(int,off,len)");
        check(ot.getFontTable("cmap", 0, 1).length == 1, "getFontTable(String,off,len)");
        check(ot.getFontTableSize(OpenType.TAG_CMAP) == 3, "getFontTableSize(int)");
        check(ot.getFontTableSize("cmap") == 2, "getFontTableSize(String)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
