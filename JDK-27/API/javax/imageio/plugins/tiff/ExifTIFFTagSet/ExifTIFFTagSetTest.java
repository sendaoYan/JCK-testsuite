/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class ExifTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.ExifTIFFTagSet");
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
        ExifTIFFTagSet s = ExifTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == ExifTIFFTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() > 40, "Exif TIFF has many tag names");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        boolean allResolve = true;
        for (String name : names) {
            TIFFTag t = s.getTag(name);
            if (t == null || !t.getName().equals(name)) { allResolve = false; break; }
        }
        check(allResolve, "every Exif TIFF tag name resolves");

        // exact Exif TIFF tag numbers
        check(s.getTag("InteroperabilityIFD").getNumber() == 40965, "InteroperabilityIFD number 40965");
        check(s.getTag("ExifVersion").getNumber() == 36864, "ExifVersion number 36864");
        check(s.getTag("ColorSpace").getNumber() == 40961, "ColorSpace number 40961");

        // mnemonic value names on ColorSpace
        TIFFTag cs = s.getTag("ColorSpace");
        check(cs.hasValueNames(), "ColorSpace has value names");
        check(cs.getValueName(ExifTIFFTagSet.COLOR_SPACE_SRGB) != null, "ColorSpace value name for sRGB");

        // ExposureProgram has mnemonic names too
        TIFFTag ep = s.getTag("ExposureProgram");
        check(ep.hasValueNames(), "ExposureProgram has value names");
        check(ep.getValueName(ExifTIFFTagSet.EXPOSURE_PROGRAM_MANUAL) != null, "ExposureProgram value name for Manual");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
