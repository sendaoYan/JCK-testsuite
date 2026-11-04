/*
 * @test
 * @summary javax.imageio.plugins.tiff.BaselineTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main BaselineTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class BaselineTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.BaselineTIFFTagSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.plugins.tiff.TIFFTagSet"), "extends TIFFTagSet");
        check(c.getField("TAG_IMAGE_WIDTH") != null, "has TAG_IMAGE_WIDTH");
        check(c.getField("TAG_COMPRESSION") != null, "has TAG_COMPRESSION");
        check(c.getField("TAG_PHOTOMETRIC_INTERPRETATION") != null, "has TAG_PHOTOMETRIC_INTERPRETATION");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BaselineTIFFTagSet s = BaselineTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == BaselineTIFFTagSet.getInstance(), "getInstance singleton");

        // exact tag numbers from TIFF 6.0 baseline spec
        int[][] known = {
            { BaselineTIFFTagSet.TAG_IMAGE_WIDTH, 256 },
            { BaselineTIFFTagSet.TAG_IMAGE_LENGTH, 257 },
            { BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE, 258 },
            { BaselineTIFFTagSet.TAG_COMPRESSION, 259 },
            { BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION, 262 },
            { BaselineTIFFTagSet.TAG_STRIP_OFFSETS, 273 },
            { BaselineTIFFTagSet.TAG_ROWS_PER_STRIP, 278 },
            { BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS, 279 },
        };
        for (int i = 0; i < known.length; i++) {
            check(known[i][0] == known[i][1], "constant equals spec value for index " + i);
        }

        // name -> tag lookup
        TIFFTag w = s.getTag("ImageWidth");
        check(w != null, "getTag(\"ImageWidth\") non-null");
        check(w.getNumber() == 256, "ImageWidth number 256");
        check(w.getCount() == 1, "ImageWidth count 1");
        check(w.isDataTypeOK(TIFFTag.TIFF_SHORT) && w.isDataTypeOK(TIFFTag.TIFF_LONG),
              "ImageWidth allows SHORT and LONG");

        TIFFTag bps = s.getTag("BitsPerSample");
        check(bps != null && bps.getNumber() == 258, "BitsPerSample number 258");

        // number -> tag lookup
        check(s.getTag(256) == w, "getTag(256) resolves ImageWidth");
        check(s.getTag(259) != null && s.getTag(259).getNumber() == 259, "getTag(259) resolves Compression");
        check(s.getTag(262) != null && s.getTag(262).getNumber() == 262, "getTag(262) resolves PhotometricInterpretation");
        check(s.getTag(99999) == null, "unknown tag number null");

        // tag names and numbers sets
        SortedSet<String> names = s.getTagNames();
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(names.size() > 40, "baseline has many named tags");
        check(names.contains("ImageWidth") && names.contains("Compression"), "tag names contains ImageWidth/Compression");
        check(numbers.contains(Integer.valueOf(256)) && numbers.contains(Integer.valueOf(279)),
              "tag numbers contains 256 and 279");
        check(numbers.size() == names.size(), "tag numbers size equals tag names size");

        // every name resolves to a non-null tag with the same number key
        boolean allResolve = true;
        for (String name : names) {
            TIFFTag t = s.getTag(name);
            if (t == null || !t.getName().equals(name)) { allResolve = false; break; }
        }
        check(allResolve, "every named tag resolves");

        // compression tag has mnemonic value names
        TIFFTag comp = s.getTag("Compression");
        check(comp.hasValueNames(), "Compression has value names");
        check(comp.getValueName(BaselineTIFFTagSet.COMPRESSION_NONE) != null,
              "Compression value name for NONE");
        check(comp.getNamedValues() != null && comp.getNamedValues().length > 0,
              "Compression named values non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
