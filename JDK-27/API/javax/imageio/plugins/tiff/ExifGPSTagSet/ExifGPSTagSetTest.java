/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifGPSTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifGPSTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifGPSTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class ExifGPSTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.ExifGPSTagSet");
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
        ExifGPSTagSet s = ExifGPSTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == ExifGPSTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() > 10, "has many GPS tag names");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        boolean allResolve = true;
        for (String name : names) {
            TIFFTag t = s.getTag(name);
            if (t == null || !t.getName().equals(name)) { allResolve = false; break; }
        }
        check(allResolve, "every GPS tag name resolves");

        // exact Exif GPS tag numbers
        check(s.getTag(ExifGPSTagSet.TAG_GPS_VERSION_ID).getNumber() == 0, "GPSVersionID number 0");
        check(s.getTag("GPSLatitudeRef").getNumber() == 1, "GPSLatitudeRef number 1");
        check(s.getTag("GPSLatitude").getNumber() == 2, "GPSLatitude number 2");
        check(s.getTag("GPSLongitudeRef").getNumber() == 3, "GPSLongitudeRef number 3");
        check(s.getTag("GPSLongitude").getNumber() == 4, "GPSLongitude number 4");
        check(s.getTag("GPSAltitude").getNumber() == 6, "GPSAltitude number 6");

        // mnemonic value names on GPSAltitudeRef
        TIFFTag altRef = s.getTag("GPSAltitudeRef");
        check(altRef.hasValueNames(), "GPSAltitudeRef has value names");
        check("Sea level".equals(altRef.getValueName(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL)),
              "GPSAltitudeRef value name for sea level");
        check("Sea level reference (negative value)".equals(
              altRef.getValueName(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL_REFERENCE)),
              "GPSAltitudeRef value name for sea level reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
