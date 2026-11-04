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

    // ---- Part 3 (deepened): exact-exception probe helper ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + " expected exactly " + expected.getName() + " but got " + t.getClass().getName());
        }
    }

    // ---- Part 3 (deepened): data type constants, size table, boundary probes ----
    static void testDeepened() throws Exception {
        // exact data type constant values from TIFF 6.0 spec field types
        check(TIFFTag.TIFF_BYTE == 1, "TIFF_BYTE == 1");
        check(TIFFTag.TIFF_ASCII == 2, "TIFF_ASCII == 2");
        check(TIFFTag.TIFF_SHORT == 3, "TIFF_SHORT == 3");
        check(TIFFTag.TIFF_LONG == 4, "TIFF_LONG == 4");
        check(TIFFTag.TIFF_RATIONAL == 5, "TIFF_RATIONAL == 5");
        check(TIFFTag.TIFF_SBYTE == 6, "TIFF_SBYTE == 6");
        check(TIFFTag.TIFF_UNDEFINED == 7, "TIFF_UNDEFINED == 7");
        check(TIFFTag.TIFF_SSHORT == 8, "TIFF_SSHORT == 8");
        check(TIFFTag.TIFF_SLONG == 9, "TIFF_SLONG == 9");
        check(TIFFTag.TIFF_SRATIONAL == 10, "TIFF_SRATIONAL == 10");
        check(TIFFTag.TIFF_FLOAT == 11, "TIFF_FLOAT == 11");
        check(TIFFTag.TIFF_DOUBLE == 12, "TIFF_DOUBLE == 12");
        check(TIFFTag.TIFF_IFD_POINTER == 13, "TIFF_IFD_POINTER == 13");
        check(TIFFTag.MIN_DATATYPE == 1, "MIN_DATATYPE == 1");
        check(TIFFTag.MAX_DATATYPE == 13, "MAX_DATATYPE == 13");

        // exact byte size of every data type (probed on JDK-27)
        int[] sizes = {1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 4};
        for (int i = 1; i <= 13; i++) {
            check(TIFFTag.getSizeOfType(i) == sizes[i - 1], "getSizeOfType(" + i + ") == " + sizes[i - 1]);
        }
        // out-of-range getSizeOfType -> IllegalArgumentException (probed)
        expectExact(IllegalArgumentException.class, () -> TIFFTag.getSizeOfType(0), "getSizeOfType(0)");
        expectExact(IllegalArgumentException.class, () -> TIFFTag.getSizeOfType(14), "getSizeOfType(14)");
        expectExact(IllegalArgumentException.class, () -> TIFFTag.getSizeOfType(-1), "getSizeOfType(-1)");

        // out-of-range isDataTypeOK -> IllegalArgumentException (probed)
        TIFFTag probe = new TIFFTag("Probe", 500, 1 << TIFFTag.TIFF_SHORT);
        expectExact(IllegalArgumentException.class, () -> probe.isDataTypeOK(0), "isDataTypeOK(0)");
        expectExact(IllegalArgumentException.class, () -> probe.isDataTypeOK(14), "isDataTypeOK(14)");
        expectExact(IllegalArgumentException.class, () -> probe.isDataTypeOK(-1), "isDataTypeOK(-1)");
        // in-range boundaries are legal queries
        check(probe.isDataTypeOK(1) == false, "isDataTypeOK(1) legal query false");
        check(probe.isDataTypeOK(3), "isDataTypeOK(3) legal query true");

        // all-types mask accepts every type
        int all = 0;
        for (int i = 1; i <= 13; i++) all |= (1 << i);
        TIFFTag allTag = new TIFFTag("All", 501, all, -1);
        for (int i = 1; i <= 13; i++) {
            check(allTag.isDataTypeOK(i), "all-types tag accepts type " + i);
        }
        check(allTag.getDataTypes() == all, "all-types dataTypes mask exact");

        // dataTypes == 0 is a legal (empty) mask: no type is OK (probed: no exception)
        TIFFTag zero = new TIFFTag("Zero", 502, 0);
        check(zero.getDataTypes() == 0, "empty dataTypes mask stored");
        for (int i = 1; i <= 13; i++) {
            check(!zero.isDataTypeOK(i), "empty-mask tag rejects type " + i);
        }
        check(zero.getCount() == -1, "3-arg ctor default count -1");

        // bit 0 of the mask is accepted (ignored by validation); bit 14 and above rejected
        TIFFTag bit0 = new TIFFTag("Bit0", 503, 1 | (1 << TIFFTag.TIFF_SHORT));
        check(bit0.getDataTypes() == (1 | (1 << TIFFTag.TIFF_SHORT)), "bit0 mask stored verbatim");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFTag("B14", 504, 1 << 14), "dataTypes bit 14 rejected");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFTag("BNeg", 505, -1), "negative dataTypes rejected");

        // tag number 0 is legal (used by ExifGPSTagSet GPSVersionID)
        check(new TIFFTag("ZeroNum", 0, 1 << TIFFTag.TIFF_BYTE).getNumber() == 0, "tag number 0 legal");

        // 4-arg ctor count semantics: any count (0, negative) stored verbatim (probed: no exception)
        check(new TIFFTag("C0", 506, all, 0).getCount() == 0, "count 0 stored verbatim");
        check(new TIFFTag("CN", 507, all, -5).getCount() == -5, "negative count stored verbatim");

        // constructor null/invalid exceptions are exact (probed on JDK-27)
        expectExact(NullPointerException.class,
            () -> new TIFFTag(null, 1, 1 << TIFFTag.TIFF_SHORT), "null name exact NPE");
        expectExact(NullPointerException.class,
            () -> new TIFFTag(null, 1, 1 << TIFFTag.TIFF_SHORT, 1), "null name 4-arg exact NPE");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFTag("Neg", -1, 1 << TIFFTag.TIFF_SHORT), "negative number exact IAE");
        expectExact(IllegalArgumentException.class,
            () -> new TIFFTag("Bad", 1, -1), "negative dataTypes exact IAE");
        expectExact(NullPointerException.class,
            () -> new TIFFTag("P", 1, (TIFFTagSet) null), "null tagSet exact NPE");

        // IFD-pointer ctor sets exactly LONG | IFD_POINTER and count 1 (probed)
        TIFFTagSet empty = new TIFFTagSet(new ArrayList<TIFFTag>());
        TIFFTag ifd = new TIFFTag("Ptr", 34665, empty);
        check(ifd.getDataTypes() == ((1 << TIFFTag.TIFF_LONG) | (1 << TIFFTag.TIFF_IFD_POINTER)),
              "IFD ctor dataTypes == LONG|IFD_POINTER");
        check(ifd.isIFDPointer(), "IFD ctor isIFDPointer");
        check(ifd.isDataTypeOK(TIFFTag.TIFF_LONG) && ifd.isDataTypeOK(TIFFTag.TIFF_IFD_POINTER),
              "IFD ctor allows LONG and IFD_POINTER");
        check(!ifd.isDataTypeOK(TIFFTag.TIFF_SHORT), "IFD ctor rejects SHORT");
        check(ifd.getCount() == 1, "IFD ctor count 1");

        // a plain tag whose mask includes IFD_POINTER keeps tagSet null but is an IFD pointer
        TIFFTag plainPtr = new TIFFTag("PlainPtr", 508, 1 << TIFFTag.TIFF_IFD_POINTER);
        check(plainPtr.isIFDPointer(), "mask-IFD tag is pointer");
        check(plainPtr.getTagSet() == null, "mask-IFD tag has null tag set");

        // non-IFD tags report isIFDPointer false
        check(!probe.isIFDPointer(), "SHORT-only tag not IFD pointer");

        // subclass probe: TIFFTag is extensible through its public constructors
        class SubTag extends TIFFTag {
            SubTag() { super("SubTag", 509, 1 << TIFFTag.TIFF_SHORT, 1); }
        }
        SubTag sub = new SubTag();
        check(sub instanceof TIFFTag, "subclass instance of TIFFTag");
        check(sub.getName().equals("SubTag"), "subclass name");
        check(sub.getNumber() == 509, "subclass number");
        check(sub.isDataTypeOK(TIFFTag.TIFF_SHORT), "subclass data type query");

        // value-name surface for a tag built without names (probed: null returns, no exception)
        check(probe.hasValueNames() == false, "probe has no value names");
        check(probe.getValueName(0) == null, "getValueName(0) null");
        check(probe.getValueName(1) == null, "getValueName(1) null");
        check(probe.getNamedValues() == null, "getNamedValues null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
