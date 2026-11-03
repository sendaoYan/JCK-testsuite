/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifParentTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifParentTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class ExifParentTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.ExifParentTIFFTagSet");
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
        ExifParentTIFFTagSet s = ExifParentTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == ExifParentTIFFTagSet.getInstance(), "getInstance singleton");

        SortedSet<String> names = s.getTagNames();
        check(names.size() == 2, "exactly two parent IFD pointer tags");
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(numbers.size() == names.size(), "names and numbers same cardinality");

        TIFFTag exif = s.getTag("ExifIFDPointer");
        check(exif != null, "ExifIFDPointer non-null");
        check(exif.getNumber() == 34665, "ExifIFDPointer number 34665");
        check(exif.isIFDPointer(), "ExifIFDPointer is IFD pointer");

        TIFFTag gps = s.getTag("GPSInfoIFDPointer");
        check(gps != null, "GPSInfoIFDPointer non-null");
        check(gps.getNumber() == 34853, "GPSInfoIFDPointer number 34853");
        check(gps.isIFDPointer(), "GPSInfoIFDPointer is IFD pointer");

        check(s.getTag(34665) == exif, "getTag(34665) resolves ExifIFDPointer");
        check(s.getTag(34853) == gps, "getTag(34853) resolves GPSInfoIFDPointer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
