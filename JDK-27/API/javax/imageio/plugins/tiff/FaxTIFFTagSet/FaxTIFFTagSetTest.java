/*
 * @test
 * @summary javax.imageio.plugins.tiff.FaxTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main FaxTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.FaxTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class FaxTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.FaxTIFFTagSet");
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
        FaxTIFFTagSet s = FaxTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == FaxTIFFTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() == 3, "exactly three fax tags");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        check(s.getTag("BadFaxLines").getNumber() == 326, "BadFaxLines number 326");
        check(s.getTag("CleanFaxData").getNumber() == 327, "CleanFaxData number 327");
        check(s.getTag("ConsecutiveBadFaxLines").getNumber() == 328, "ConsecutiveBadFaxLines number 328");

        check(s.getTag(326).getName().equals("BadFaxLines"), "getTag(326) resolves BadFaxLines");
        check(s.getTag(327).getName().equals("CleanFaxData"), "getTag(327) resolves CleanFaxData");
        check(s.getTag(328).getName().equals("ConsecutiveBadFaxLines"), "getTag(328) resolves ConsecutiveBadFaxLines");

        TIFFTag clean = s.getTag("CleanFaxData");
        check(clean.hasValueNames(), "CleanFaxData has value names");
        check(clean.getValueName(FaxTIFFTagSet.CLEAN_FAX_DATA_NO_ERRORS) != null, "CleanFaxData NO_ERRORS value name");
        check(clean.getValueName(FaxTIFFTagSet.CLEAN_FAX_DATA_ERRORS_CORRECTED) != null, "CleanFaxData CORRECTED value name");
    }

    // ---- Part 3 (deepened): constants, types, value names, integration ----
    static void testDeepened() throws Exception {
        // exact constant values (probed via javap -constants)
        check(FaxTIFFTagSet.TAG_BAD_FAX_LINES == 326, "TAG_BAD_FAX_LINES 326");
        check(FaxTIFFTagSet.TAG_CLEAN_FAX_DATA == 327, "TAG_CLEAN_FAX_DATA 327");
        check(FaxTIFFTagSet.TAG_CONSECUTIVE_BAD_LINES == 328, "TAG_CONSECUTIVE_BAD_LINES 328");
        check(FaxTIFFTagSet.CLEAN_FAX_DATA_NO_ERRORS == 0, "CLEAN_FAX_DATA_NO_ERRORS 0");
        check(FaxTIFFTagSet.CLEAN_FAX_DATA_ERRORS_CORRECTED == 1, "CLEAN_FAX_DATA_ERRORS_CORRECTED 1");
        check(FaxTIFFTagSet.CLEAN_FAX_DATA_ERRORS_UNCORRECTED == 2, "CLEAN_FAX_DATA_ERRORS_UNCORRECTED 2");

        FaxTIFFTagSet s = FaxTIFFTagSet.getInstance();
        // all three tags are SHORT-valued with count 1 (probed);
        // BadFaxLines/ConsecutiveBadFaxLines additionally accept LONG
        String[] names = { "BadFaxLines", "CleanFaxData", "ConsecutiveBadFaxLines" };
        for (String n : names) {
            TIFFTag t = s.getTag(n);
            check(t != null, n + " resolves");
            check(t.isDataTypeOK(TIFFTag.TIFF_SHORT), n + " allows SHORT");
            check(t.getCount() == 1, n + " count 1");
            check(!t.isIFDPointer(), n + " not IFD pointer");
            check(t.getTagSet() == null, n + " no tag set back-reference");
        }
        check(s.getTag("BadFaxLines").isDataTypeOK(TIFFTag.TIFF_LONG), "BadFaxLines allows LONG");
        check(s.getTag("ConsecutiveBadFaxLines").isDataTypeOK(TIFFTag.TIFF_LONG),
              "ConsecutiveBadFaxLines allows LONG");
        check(!s.getTag("CleanFaxData").isDataTypeOK(TIFFTag.TIFF_LONG), "CleanFaxData rejects LONG");
        // value names on CleanFaxData (probed strings)
        TIFFTag clean = s.getTag("CleanFaxData");
        check(clean.hasValueNames(), "CleanFaxData has value names");
        check(clean.getValueName(FaxTIFFTagSet.CLEAN_FAX_DATA_NO_ERRORS).equals("No errors"),
              "0 -> No errors");
        check(clean.getValueName(FaxTIFFTagSet.CLEAN_FAX_DATA_ERRORS_CORRECTED).equals("Errors corrected"),
              "1 -> Errors corrected");
        check(clean.getValueName(FaxTIFFTagSet.CLEAN_FAX_DATA_ERRORS_UNCORRECTED).equals("Errors uncorrected"),
              "2 -> Errors uncorrected");
        check(clean.getValueName(3) == null, "3 -> null");
        check(java.util.Arrays.equals(clean.getNamedValues(), new int[]{0, 1, 2}),
              "named values [0,1,2]");
        // the other two carry no value names
        check(!s.getTag("BadFaxLines").hasValueNames(), "BadFaxLines no value names");
        check(!s.getTag("ConsecutiveBadFaxLines").hasValueNames(), "ConsecutiveBadFaxLines no value names");
        // boundary lookups
        check(s.getTag(325) == null && s.getTag(329) == null, "neighbors null");
        check(s.getTag("BadFaxLine") == null, "singular name null");
        // number -> name round trip for all three
        check(s.getTag(326).getName().equals("BadFaxLines"), "326 -> BadFaxLines");
        check(s.getTag(327).getName().equals("CleanFaxData"), "327 -> CleanFaxData");
        check(s.getTag(328).getName().equals("ConsecutiveBadFaxLines"), "328 -> ConsecutiveBadFaxLines");
        // sorted view boundaries
        check(s.getTagNumbers().first() == 326 && s.getTagNumbers().last() == 328, "numbers 326..328");
        // Fax set is one of the four default allowed sets of TIFFImageReadParam (probed)
        javax.imageio.plugins.tiff.TIFFImageReadParam p =
            new javax.imageio.plugins.tiff.TIFFImageReadParam();
        boolean hasFax = false;
        for (javax.imageio.plugins.tiff.TIFFTagSet ts : p.getAllowedTagSets())
            if (ts == s) hasFax = true;
        check(hasFax, "Fax is a default allowed tag set");
        // SHORT field round-trip through the tag definition
        javax.imageio.plugins.tiff.TIFFField f =
            new javax.imageio.plugins.tiff.TIFFField(s.getTag("BadFaxLines"),
                TIFFTag.TIFF_SHORT, 2, new char[]{3, 4});
        check(f.getAsInt(1) == 4, "fax SHORT field value");
        check(f.getValueAsString(0).equals("3"), "fax SHORT value string");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
