/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFField conformance: white-box structure + black-box behavior.
 * @run main TIFFFieldTest
 */

import java.lang.reflect.Modifier;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFFieldTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.lang.Cloneable"),
              "implements Cloneable");
        check(c.getConstructors().length == 4, "four public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        int shortAndLong = (1 << TIFFTag.TIFF_SHORT) | (1 << TIFFTag.TIFF_LONG);
        TIFFTag tagA = new TIFFTag("MyTag", 300, shortAndLong);

        // single-value constructor selects SHORT for small value
        TIFFField small = new TIFFField(tagA, 42L);
        check(small.getTag() == tagA, "small field tag");
        check(small.getTagNumber() == 300, "small field tag number");
        check(small.getType() == TIFFTag.TIFF_SHORT, "small value stored as SHORT");
        check(small.getCount() == 1, "small field count 1");
        check(small.getAsInt(0) == 42, "small getAsInt");
        check(small.getAsLong(0) == 42L, "small getAsLong");
        check(small.getValueAsString(0).equals("42"), "small getValueAsString");

        // single-value constructor selects LONG for large value
        TIFFField big = new TIFFField(tagA, 70000L);
        check(big.getType() == TIFFTag.TIFF_LONG, "large value stored as LONG");
        check(big.getAsLong(0) == 70000L, "big getAsLong");
        check(big.getAsFloat(0) == 70000.0f, "big getAsFloat");
        check(big.getAsDouble(0) == 70000.0, "big getAsDouble");

        // direct construction with SHORT char[] data
        TIFFField shorts = new TIFFField(tagA, TIFFTag.TIFF_SHORT, 3, new char[]{10, 20, 30});
        check(shorts.getCount() == 3, "shorts count 3");
        check(shorts.getType() == TIFFTag.TIFF_SHORT, "shorts type SHORT");
        check(shorts.getData() instanceof char[], "shorts data is char[]");
        check(shorts.getAsChars()[1] == 20, "shorts getAsChars");
        check(shorts.getAsInts().length == 3 && shorts.getAsInts()[2] == 30, "shorts getAsInts");
        check(shorts.getAsInt(1) == 20, "shorts getAsInt(index)");
        check(shorts.isIntegral(), "SHORT field is integral");

        // 3-arg constructor creates zero array of the right type
        TIFFField empty = new TIFFField(tagA, TIFFTag.TIFF_LONG, 2);
        check(empty.getCount() == 2, "3-arg ctor count");
        check(empty.getAsLongs().length == 2 && empty.getAsLongs()[0] == 0L, "3-arg ctor zero long[]");

        // ASCII field
        TIFFTag asciiTag = new TIFFTag("ImageDescription", 270, 1 << TIFFTag.TIFF_ASCII);
        TIFFField ascii = new TIFFField(asciiTag, TIFFTag.TIFF_ASCII, 1, new String[]{"hello"});
        check(ascii.getCount() == 1, "ascii count");
        check(ascii.getAsString(0).equals("hello"), "ascii getAsString");
        check(ascii.getValueAsString(0).equals("hello"), "ascii getValueAsString");
        check(!ascii.isIntegral(), "ASCII field not integral");

        // type name round-trip
        check(TIFFField.getTypeName(TIFFTag.TIFF_LONG).equals("Long"), "getTypeName LONG");
        check(TIFFField.getTypeName(TIFFTag.TIFF_SHORT).equals("Short"), "getTypeName SHORT");
        check(TIFFField.getTypeName(TIFFTag.TIFF_IFD_POINTER).equals("IFDPointer"), "getTypeName IFDPointer");
        check(TIFFField.getTypeByName("Long") == TIFFTag.TIFF_LONG, "getTypeByName Long");
        check(TIFFField.getTypeByName("Short") == TIFFTag.TIFF_SHORT, "getTypeByName Short");
        check(TIFFField.getTypeByName("NoSuchType") == -1, "getTypeByName unknown -1");

        // createArrayForType
        check(TIFFField.createArrayForType(TIFFTag.TIFF_SHORT, 3) instanceof char[], "createArray SHORT");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_DOUBLE, 3) instanceof double[], "createArray DOUBLE");
        check(TIFFField.createArrayForType(TIFFTag.TIFF_IFD_POINTER, 1) instanceof long[], "createArray IFD_POINTER");

        // IFD pointer field with directory
        TIFFTag ifdTag = new TIFFTag("ExifIFDPointer", 34665,
                (1 << TIFFTag.TIFF_LONG) | (1 << TIFFTag.TIFF_IFD_POINTER), 1);
        TIFFDirectory dir = new TIFFDirectory(new TIFFTagSet[]{ BaselineTIFFTagSet.getInstance() }, null);
        TIFFField ifdField = new TIFFField(ifdTag, TIFFTag.TIFF_LONG, 12345L, dir);
        check(ifdField.hasDirectory(), "IFD field has directory");
        check(ifdField.getDirectory() == dir, "IFD field directory reference");
        check(ifdField.getAsLong(0) == 12345L, "IFD field offset");
        check(ifdField.getCount() == 1, "IFD field count 1");

        // invalid construction
        expectThrows(NullPointerException.class, () -> new TIFFField(null, 42L), "null tag (value ctor)");
        expectThrows(IllegalArgumentException.class, () -> new TIFFField(tagA, -1L), "negative value");
        expectThrows(IllegalArgumentException.class,
            () -> new TIFFField(tagA, TIFFTag.TIFF_BYTE, 1, new byte[]{1}), "BYTE not allowed for tagA");
        expectThrows(IllegalArgumentException.class,
            () -> new TIFFField(tagA, TIFFTag.TIFF_SHORT, 2, new char[]{1}), "wrong data array length");
        expectThrows(IllegalArgumentException.class, () -> TIFFField.getTypeName(99), "getTypeName out of range");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
