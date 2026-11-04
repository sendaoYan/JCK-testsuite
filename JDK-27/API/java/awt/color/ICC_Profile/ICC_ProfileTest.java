/*
 * @test
 * @summary java.awt.color.ICC_Profile conformance: white-box structure plus black-box behavior.
 * @run main ICC_ProfileTest
 */

import java.awt.color.*;
import java.io.*;
import java.lang.reflect.*;

public class ICC_ProfileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.color.ICC_Profile");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        String[] sigs = {"icSigHead","icSigAToB0Tag","icSigProfileDescriptionTag","icSigRgbData","icSigGrayData"};
        for (String s : sigs) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + s);
        }
        check(hasMethod(c, "getInstance", int.class), "getInstance(int)");
        check(hasMethod(c, "getInstance", byte[].class), "getInstance(byte[])");
        check(hasMethod(c, "getInstance", String.class), "getInstance(String)");
        check(hasMethod(c, "getInstance", InputStream.class), "getInstance(InputStream)");
        check(hasMethod(c, "getData"), "getData()");
        check(hasMethod(c, "getData", int.class), "getData(int)");
        check(hasMethod(c, "setData", int.class, byte[].class), "setData(int,byte[])");
        check(hasMethod(c, "write", OutputStream.class), "write(OutputStream)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ICC_Profile srgb = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
        check(srgb.getNumComponents() == 3, "sRGB 3 components");
        check(srgb.getColorSpaceType() == ColorSpace.TYPE_RGB, "sRGB colorSpaceType TYPE_RGB");
        check(srgb.getPCSType() == ColorSpace.TYPE_XYZ, "sRGB PCS TYPE_XYZ");
        check(srgb.getProfileClass() == ICC_Profile.CLASS_DISPLAY, "sRGB CLASS_DISPLAY");
        check(srgb.getMajorVersion() == 2, "major version 2");
        check(srgb.getMinorVersion() >= 0, "minor version non-negative");

        // getData / getData(tag)
        byte[] all = srgb.getData();
        check(all != null && all.length > 0, "getData non-empty");
        byte[] head = srgb.getData(ICC_Profile.icSigHead);
        check(head != null && head.length == 128, "header 128 bytes");

        // byte[] round-trip
        ICC_Profile srgb2 = ICC_Profile.getInstance(all);
        check(srgb2.getNumComponents() == 3 && srgb2.getColorSpaceType() == ColorSpace.TYPE_RGB,
              "byte[] round-trip");

        // write(OutputStream) reproduces getData
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        srgb.write(bos);
        byte[] written = bos.toByteArray();
        check(written.length == all.length, "write(OutputStream) same length as getData");

        // gray profile
        ICC_Profile gray = ICC_Profile.getInstance(ColorSpace.CS_GRAY);
        check(gray.getNumComponents() == 1 && gray.getColorSpaceType() == ColorSpace.TYPE_GRAY,
              "gray profile components/type");

        // unknown color space throws IllegalArgumentException
        boolean threw = false;
        try { ICC_Profile.getInstance(12345); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "getInstance(unknown) throws IllegalArgumentException");

        // getInstance on the predefined ids is idempotent (same cached instance)
        check(ICC_Profile.getInstance(ColorSpace.CS_sRGB) == ICC_Profile.getInstance(ColorSpace.CS_sRGB),
              "getInstance(CS_sRGB) idempotent (==)");
        check(ICC_Profile.getInstance(ColorSpace.CS_GRAY) == ICC_Profile.getInstance(ColorSpace.CS_GRAY),
              "getInstance(CS_GRAY) idempotent (==)");

        // profile class constants and per-space classes
        check(ICC_Profile.CLASS_INPUT == 0 && ICC_Profile.CLASS_DISPLAY == 1 && ICC_Profile.CLASS_OUTPUT == 2
              && ICC_Profile.CLASS_DEVICELINK == 3 && ICC_Profile.CLASS_COLORSPACECONVERSION == 4
              && ICC_Profile.CLASS_ABSTRACT == 5 && ICC_Profile.CLASS_NAMEDCOLOR == 6,
              "CLASS_* constant values");
        check(ICC_Profile.getInstance(ColorSpace.CS_GRAY).getProfileClass() == ICC_Profile.CLASS_DISPLAY,
              "gray profile class CLASS_DISPLAY");
        ICC_Profile ciexyzP = ICC_Profile.getInstance(ColorSpace.CS_CIEXYZ);
        check(ciexyzP.getColorSpaceType() == ColorSpace.TYPE_XYZ && ciexyzP.getPCSType() == ColorSpace.TYPE_XYZ,
              "CS_CIEXYZ profile colorSpaceType/PCSType");

        // getData for an unknown tag signature returns null (absent tag)
        check(srgb.getData(0x7FFFFFFF) == null, "getData(unknown tag) returns null");

        // malformed profile bytes and null file name
        boolean garbageThrew = false;
        try { ICC_Profile.getInstance(new byte[]{1, 2, 3}); }
        catch (IllegalArgumentException e) { garbageThrew = true; }
        check(garbageThrew, "getInstance(garbage bytes) throws IllegalArgumentException");
        boolean nullNameThrew = false;
        try { ICC_Profile.getInstance((String) null); }
        catch (NullPointerException e) { nullNameThrew = true; }
        check(nullNameThrew, "getInstance((String) null) throws NullPointerException");

        // byte-derived (non built-in) profiles are distinct objects and writable
        ICC_Profile copy = ICC_Profile.getInstance(all);
        check(copy != srgb, "byte-derived profile is a distinct instance");
        byte[] copyHead = copy.getData(ICC_Profile.icSigHead);
        copyHead[8] = 0x21;
        copy.setData(ICC_Profile.icSigHead, copyHead);
        check(copy.getData(ICC_Profile.icSigHead)[8] == 0x21, "setData(header) read-back on byte-derived profile");
        check(copy.getMajorVersion() == 0x21, "getMajorVersion reflects raw header byte after setData");

        // built-in profiles are shared: mutating them is forbidden (JDK 9+ protection of the shared cache)
        boolean builtinThrew = false;
        try { srgb.setData(ICC_Profile.icSigHead, srgb.getData(ICC_Profile.icSigHead)); }
        catch (IllegalArgumentException e) { builtinThrew = true; }
        check(builtinThrew, "setData on built-in profile throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
