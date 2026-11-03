/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFTag conformance: white-box structure + black-box behavior.
 * @run main TIFFTagTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFTagTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFTag");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 3, "three public constructors");
        check(c.getField("TIFF_BYTE") != null, "has TIFF_BYTE");
        check(c.getField("TIFF_IFD_POINTER") != null, "has TIFF_IFD_POINTER");
        check(c.getField("UNKNOWN_TAG_NAME") != null, "has UNKNOWN_TAG_NAME");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 4-arg constructor (name, number, dataTypes, count)
        int longAndShort = (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG);
        TIFFTag t = new TIFFTag("ImageWidth", 256, longAndShort, 1);
        check("ImageWidth".equals(t.getName()), "name");
        check(t.getNumber() == 256, "number");
        check(t.getDataTypes() == longAndShort, "dataTypes bitmask");
        check(t.getCount() == 1, "count");
        check(t.isDataTypeOK(TIFFTag.TIFF_SHORT), "SHORT allowed");
        check(t.isDataTypeOK(TIFFTag.TIFF_LONG), "LONG allowed");
        check(!t.isDataTypeOK(TIFFTag.TIFF_BYTE), "BYTE not allowed");
        check(t.getTagSet() == null, "no tag set");
        check(!t.isIFDPointer(), "not IFD pointer");
        check(!t.hasValueNames(), "no value names");
        check(t.getValueName(0) == null, "value name null");
        check(t.getNamedValues() == null, "named values null");

        // 3-arg constructor (count undefined = -1)
        TIFFTag u = new TIFFTag("BitsPerSample", 258, 1 << TIFFTag.TIFF_SHORT);
        check(u.getCount() == -1, "3-arg ctor count undefined (-1)");
        check(u.getName().equals("BitsPerSample"), "3-arg ctor name");
        check(u.getNumber() == 258, "3-arg ctor number");

        // IFD pointer constructor (name, number, tagSet)
        TIFFTagSet inner = new TIFFTagSet(new ArrayList<TIFFTag>());
        TIFFTag ifd = new TIFFTag("ExifIFDPointer", 34665, inner);
        check(ifd.getTagSet() == inner, "IFD tag set reference");
        check(ifd.isIFDPointer(), "IFD pointer true");
        check(ifd.isDataTypeOK(TIFFTag.TIFF_LONG), "IFD pointer allows LONG");
        check(ifd.isDataTypeOK(TIFFTag.TIFF_IFD_POINTER), "IFD pointer allows IFD_POINTER");
        check(ifd.getCount() == 1, "IFD pointer count 1");

        // a tag whose dataTypes include IFD_POINTER is also an IFD pointer
        TIFFTag p = new TIFFTag("Ptr", 1, 1 << TIFFTag.TIFF_IFD_POINTER);
        check(p.isIFDPointer(), "IFD_POINTER data type makes IFD pointer");

        // getSizeOfType
        check(TIFFTag.getSizeOfType(TIFFTag.TIFF_BYTE) == 1, "sizeOfType BYTE 1");
        check(TIFFTag.getSizeOfType(TIFFTag.TIFF_SHORT) == 2, "sizeOfType SHORT 2");
        check(TIFFTag.getSizeOfType(TIFFTag.TIFF_LONG) == 4, "sizeOfType LONG 4");
        check(TIFFTag.getSizeOfType(TIFFTag.TIFF_DOUBLE) == 8, "sizeOfType DOUBLE 8");

        // data type range and constants
        check(TIFFTag.MIN_DATATYPE == TIFFTag.TIFF_BYTE, "MIN_DATATYPE == TIFF_BYTE");
        check(TIFFTag.MAX_DATATYPE == TIFFTag.TIFF_IFD_POINTER, "MAX_DATATYPE == TIFF_IFD_POINTER");
        check("UnknownTag".equals(TIFFTag.UNKNOWN_TAG_NAME), "UNKNOWN_TAG_NAME");

        // invalid constructions
        expectThrows(NullPointerException.class, () -> new TIFFTag(null, 1, 1 << TIFFTag.TIFF_SHORT), "null name");
        expectThrows(IllegalArgumentException.class, () -> new TIFFTag("Neg", -1, 1 << TIFFTag.TIFF_SHORT), "negative number");
        expectThrows(IllegalArgumentException.class, () -> new TIFFTag("Bad", 1, -1), "negative dataTypes");
        expectThrows(NullPointerException.class, () -> new TIFFTag("P", 1, (TIFFTagSet) null), "null tagSet");
        expectThrows(IllegalArgumentException.class, () -> t.isDataTypeOK(99), "isDataTypeOK out of range");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
