/*
 * @test
 * @summary javax.imageio.plugins.tiff.ExifParentTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main ExifParentTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.ExifTIFFTagSet;
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

    // ---- Part 3 (deepened): constants, IFD pointer contracts, integration ----
    static void testDeepened() throws Exception {
        // exact constant values (probed via javap -constants)
        check(ExifParentTIFFTagSet.TAG_EXIF_IFD_POINTER == 34665, "TAG_EXIF_IFD_POINTER 34665");
        check(ExifParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER == 34853, "TAG_GPS_INFO_IFD_POINTER 34853");

        ExifParentTIFFTagSet s = ExifParentTIFFTagSet.getInstance();
        TIFFTag exif = s.getTag("ExifIFDPointer");
        TIFFTag gps = s.getTag("GPSInfoIFDPointer");

        // both tags allow exactly LONG and IFD_POINTER (probed)
        check(exif.isDataTypeOK(TIFFTag.TIFF_LONG), "ExifIFDPointer allows LONG");
        check(exif.isDataTypeOK(TIFFTag.TIFF_IFD_POINTER), "ExifIFDPointer allows IFD_POINTER");
        check(!exif.isDataTypeOK(TIFFTag.TIFF_SHORT), "ExifIFDPointer rejects SHORT");
        check(gps.isDataTypeOK(TIFFTag.TIFF_LONG), "GPSInfoIFDPointer allows LONG");
        check(gps.isDataTypeOK(TIFFTag.TIFF_IFD_POINTER), "GPSInfoIFDPointer allows IFD_POINTER");
        check(!gps.isDataTypeOK(TIFFTag.TIFF_SHORT), "GPSInfoIFDPointer rejects SHORT");
        check(exif.getCount() == 1 && gps.getCount() == 1, "both pointers count 1");
        check(exif.isIFDPointer() && gps.isIFDPointer(), "both are IFD pointers");
        check(!exif.hasValueNames() && !gps.hasValueNames(), "no value names on pointers");

        // boundary lookups
        check(s.getTag(34664) == null && s.getTag(34666) == null, "neighbors of 34665 null");
        check(s.getTag(34852) == null && s.getTag(34854) == null, "neighbors of 34853 null");
        check(s.getTag("ExifIFD") == null, "getTag(ExifIFD) null");

        // both constants appear in the shipped default allowed tag sets of TIFFImageReadParam
        javax.imageio.plugins.tiff.TIFFImageReadParam p =
            new javax.imageio.plugins.tiff.TIFFImageReadParam();
        boolean hasExifParent = false;
        for (javax.imageio.plugins.tiff.TIFFTagSet ts : p.getAllowedTagSets())
            if (ts == s) hasExifParent = true;
        check(hasExifParent, "ExifParent is a default allowed tag set");

        // a field pointing at a nested directory: tag, type, count, offset, directory
        javax.imageio.plugins.tiff.TIFFDirectory nested =
            new javax.imageio.plugins.tiff.TIFFDirectory(
                new javax.imageio.plugins.tiff.TIFFTagSet[]{ s }, null);
        javax.imageio.plugins.tiff.TIFFField f =
            new javax.imageio.plugins.tiff.TIFFField(exif, TIFFTag.TIFF_IFD_POINTER, 16L, nested);
        check(f.hasDirectory() && f.getDirectory() == nested, "pointer field holds directory");
        check(f.getAsLong(0) == 16L, "pointer field offset");
        check(f.getType() == TIFFTag.TIFF_IFD_POINTER, "pointer field type 13");
        // nested node renders as TIFFIFD (probed via getAsNativeNode name)
        org.w3c.dom.Node n = f.getAsNativeNode();
        check(n.getNodeName().equals("TIFFIFD"), "directory field node name TIFFIFD");
        // LONG-typed pointer field with a directory also renders as TIFFIFD (probed)
        javax.imageio.plugins.tiff.TIFFField f2 =
            new javax.imageio.plugins.tiff.TIFFField(exif, TIFFTag.TIFF_LONG, 32L, nested);
        check(f2.getAsNativeNode().getNodeName().equals("TIFFIFD"), "LONG pointer node TIFFIFD");
        // GPS pointer number equals ExifTIFFTagSet's GPS pointer constant (spec consistency)
        check(ExifParentTIFFTagSet.TAG_GPS_INFO_IFD_POINTER == ExifTIFFTagSet.TAG_GPS_INFO_IFD_POINTER,
              "GPS pointer number consistent with ExifTIFFTagSet");
        // sorted view contains exactly the two pointer numbers in order
        Integer[] parentNums = s.getTagNumbers().toArray(new Integer[0]);
        check(parentNums.length == 2 && parentNums[0] == 34665 && parentNums[1] == 34853,
              "parent numbers [34665, 34853]");
        // param integration: removing the default then re-adding works (probed)
        javax.imageio.plugins.tiff.TIFFImageReadParam p2 =
            new javax.imageio.plugins.tiff.TIFFImageReadParam();
        p2.removeAllowedTagSet(s);
        check(!p2.getAllowedTagSets().contains(s), "param remove parent set");
        p2.addAllowedTagSet(s);
        check(p2.getAllowedTagSets().contains(s), "param re-add parent set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
