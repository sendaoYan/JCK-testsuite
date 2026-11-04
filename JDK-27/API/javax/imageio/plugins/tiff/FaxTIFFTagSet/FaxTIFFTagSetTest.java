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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
