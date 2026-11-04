/*
 * @test
 * @summary javax.imageio.plugins.tiff.GeoTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main GeoTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class GeoTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.GeoTIFFTagSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.plugins.tiff.TIFFTagSet"), "extends TIFFTagSet");
    }

    static void testBlackBox() throws Exception {
        GeoTIFFTagSet s = GeoTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == GeoTIFFTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() == 6, "exactly six GeoTIFF tags");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        check(s.getTag("ModelPixelScaleTag").getNumber() == 33550, "ModelPixelScaleTag number 33550");
        check(s.getTag("ModelTransformationTag").getNumber() == 34264, "ModelTransformationTag number 34264");
        check(s.getTag("GeoKeyDirectoryTag").getNumber() == 34735, "GeoKeyDirectoryTag number 34735");

        check(s.getTag(33550).getName().equals("ModelPixelScaleTag"), "getTag(33550) resolves ModelPixelScaleTag");
        check(s.getTag(34264).getName().equals("ModelTransformationTag"), "getTag(34264) resolves ModelTransformationTag");
        check(s.getTag(34735).getName().equals("GeoKeyDirectoryTag"), "getTag(34735) resolves GeoKeyDirectoryTag");
    }

    // ---- Part 3 (deepened): constants, data types, integration ----
    static void testDeepened() throws Exception {
        // exact constant values (probed via javap -constants)
        check(GeoTIFFTagSet.TAG_MODEL_PIXEL_SCALE == 33550, "TAG_MODEL_PIXEL_SCALE 33550");
        check(GeoTIFFTagSet.TAG_MODEL_TRANSFORMATION == 34264, "TAG_MODEL_TRANSFORMATION 34264");
        check(GeoTIFFTagSet.TAG_MODEL_TIE_POINT == 33922, "TAG_MODEL_TIE_POINT 33922");
        check(GeoTIFFTagSet.TAG_GEO_KEY_DIRECTORY == 34735, "TAG_GEO_KEY_DIRECTORY 34735");
        check(GeoTIFFTagSet.TAG_GEO_DOUBLE_PARAMS == 34736, "TAG_GEO_DOUBLE_PARAMS 34736");
        check(GeoTIFFTagSet.TAG_GEO_ASCII_PARAMS == 34737, "TAG_GEO_ASCII_PARAMS 34737");

        GeoTIFFTagSet s = GeoTIFFTagSet.getInstance();
        // exactly six tags, numbers sorted ascending 33550 < 33922 < 34264 < 34735 < 34736 < 34737
        check(s.getTagNumbers().size() == 6, "six tags");
        Integer[] nums = s.getTagNumbers().toArray(new Integer[0]);
        check(nums[0] == 33550 && nums[1] == 33922 && nums[2] == 34264
              && nums[3] == 34735 && nums[4] == 34736 && nums[5] == 34737,
              "ascending tag numbers exact");
        check(s.getTagNumbers().first() == 33550 && s.getTagNumbers().last() == 34737,
              "first/last boundaries");

        // data type contracts (probed): DOUBLE for geometry, SHORT for keys, ASCII for text
        check(s.getTag("ModelPixelScaleTag").isDataTypeOK(TIFFTag.TIFF_DOUBLE),
              "ModelPixelScaleTag DOUBLE");
        check(!s.getTag("ModelPixelScaleTag").isDataTypeOK(TIFFTag.TIFF_SHORT),
              "ModelPixelScaleTag rejects SHORT");
        check(s.getTag("ModelTransformationTag").isDataTypeOK(TIFFTag.TIFF_DOUBLE),
              "ModelTransformationTag DOUBLE");
        check(s.getTag("ModelTiepointTag").isDataTypeOK(TIFFTag.TIFF_DOUBLE),
              "ModelTiepointTag DOUBLE");
        check(s.getTag("GeoKeyDirectoryTag").isDataTypeOK(TIFFTag.TIFF_SHORT),
              "GeoKeyDirectoryTag SHORT");
        check(!s.getTag("GeoKeyDirectoryTag").isDataTypeOK(TIFFTag.TIFF_DOUBLE),
              "GeoKeyDirectoryTag rejects DOUBLE");
        check(s.getTag("GeoDoubleParamsTag").isDataTypeOK(TIFFTag.TIFF_DOUBLE),
              "GeoDoubleParamsTag DOUBLE");
        check(s.getTag("GeoAsciiParamsTag").isDataTypeOK(TIFFTag.TIFF_ASCII),
              "GeoAsciiParamsTag ASCII");
        // all six have undefined count and no value names (probed)
        for (String n : new String[]{ "ModelPixelScaleTag", "ModelTransformationTag", "ModelTiepointTag",
                                      "GeoKeyDirectoryTag", "GeoDoubleParamsTag", "GeoAsciiParamsTag" }) {
            TIFFTag t = s.getTag(n);
            check(t.getCount() == -1, n + " count undefined");
            check(!t.hasValueNames(), n + " no value names");
            check(!t.isIFDPointer(), n + " not IFD pointer");
        }
        // canonical name pairings both directions
        check(s.getTag(33550).getName().equals("ModelPixelScaleTag"), "33550 name");
        check(s.getTag(33922).getName().equals("ModelTiepointTag"), "33922 name");
        check(s.getTag(34264).getName().equals("ModelTransformationTag"), "34264 name");
        check(s.getTag(34735).getName().equals("GeoKeyDirectoryTag"), "34735 name");
        check(s.getTag(34736).getName().equals("GeoDoubleParamsTag"), "34736 name");
        check(s.getTag(34737).getName().equals("GeoAsciiParamsTag"), "34737 name");
        // boundary lookups
        check(s.getTag(33549) == null && s.getTag(34738) == null, "neighbors null");
        check(s.getTag("ModelTiePoint") == null, "misspelled name null");
        // GeoTIFF is one of the four default allowed sets of TIFFImageReadParam (probed)
        javax.imageio.plugins.tiff.TIFFImageReadParam p =
            new javax.imageio.plugins.tiff.TIFFImageReadParam();
        boolean hasGeo = false;
        for (javax.imageio.plugins.tiff.TIFFTagSet ts : p.getAllowedTagSets())
            if (ts == s) hasGeo = true;
        check(hasGeo, "GeoTIFF is a default allowed tag set");
        // a DOUBLE field through the tag definition round-trips
        javax.imageio.plugins.tiff.TIFFField f =
            new javax.imageio.plugins.tiff.TIFFField(s.getTag("ModelPixelScaleTag"),
                TIFFTag.TIFF_DOUBLE, 3, new double[]{2.0, 2.0, 0.0});
        check(f.getAsDouble(0) == 2.0 && f.getAsDouble(2) == 0.0, "pixel scale doubles");
        check(f.getValueAsString(0).equals("2.0"), "pixel scale value string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
