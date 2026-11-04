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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
