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

    // ---- Part 3 (deepened): exact tag number constants (Exif GPS 2.3 spec) ----
    static void testTagConstants() throws Exception {
        int[][] tags = {
            { ExifGPSTagSet.TAG_GPS_VERSION_ID, 0 },
            { ExifGPSTagSet.TAG_GPS_LATITUDE_REF, 1 },
            { ExifGPSTagSet.TAG_GPS_LATITUDE, 2 },
            { ExifGPSTagSet.TAG_GPS_LONGITUDE_REF, 3 },
            { ExifGPSTagSet.TAG_GPS_LONGITUDE, 4 },
            { ExifGPSTagSet.TAG_GPS_ALTITUDE_REF, 5 },
            { ExifGPSTagSet.TAG_GPS_ALTITUDE, 6 },
            { ExifGPSTagSet.TAG_GPS_TIME_STAMP, 7 },
            { ExifGPSTagSet.TAG_GPS_SATELLITES, 8 },
            { ExifGPSTagSet.TAG_GPS_STATUS, 9 },
            { ExifGPSTagSet.TAG_GPS_MEASURE_MODE, 10 },
            { ExifGPSTagSet.TAG_GPS_DOP, 11 },
            { ExifGPSTagSet.TAG_GPS_SPEED_REF, 12 },
            { ExifGPSTagSet.TAG_GPS_SPEED, 13 },
            { ExifGPSTagSet.TAG_GPS_TRACK_REF, 14 },
            { ExifGPSTagSet.TAG_GPS_TRACK, 15 },
            { ExifGPSTagSet.TAG_GPS_IMG_DIRECTION_REF, 16 },
            { ExifGPSTagSet.TAG_GPS_IMG_DIRECTION, 17 },
            { ExifGPSTagSet.TAG_GPS_MAP_DATUM, 18 },
            { ExifGPSTagSet.TAG_GPS_DEST_LATITUDE_REF, 19 },
            { ExifGPSTagSet.TAG_GPS_DEST_LATITUDE, 20 },
            { ExifGPSTagSet.TAG_GPS_DEST_LONGITUDE_REF, 21 },
            { ExifGPSTagSet.TAG_GPS_DEST_LONGITUDE, 22 },
            { ExifGPSTagSet.TAG_GPS_DEST_BEARING_REF, 23 },
            { ExifGPSTagSet.TAG_GPS_DEST_BEARING, 24 },
            { ExifGPSTagSet.TAG_GPS_DEST_DISTANCE_REF, 25 },
            { ExifGPSTagSet.TAG_GPS_DEST_DISTANCE, 26 },
            { ExifGPSTagSet.TAG_GPS_PROCESSING_METHOD, 27 },
            { ExifGPSTagSet.TAG_GPS_AREA_INFORMATION, 28 },
            { ExifGPSTagSet.TAG_GPS_DATE_STAMP, 29 },
            { ExifGPSTagSet.TAG_GPS_DIFFERENTIAL, 30 },
        };
        ExifGPSTagSet s = ExifGPSTagSet.getInstance();
        for (int i = 0; i < tags.length; i++) {
            check(tags[i][0] == tags[i][1], "TAG constant " + tags[i][1] + " exact");
            TIFFTag t = s.getTag(tags[i][1]);
            check(t != null, "tag " + tags[i][1] + " registered");
            check(t.getNumber() == tags[i][1], "tag " + tags[i][1] + " number self-consistent");
            check(t.getName() != null, "tag " + tags[i][1] + " named");
        }
        // every tag from 0 to 30 present: exactly 31 tags (probed)
        check(s.getTagNumbers().size() == 31, "gps set holds 31 tags");
        check(s.getTagNumbers().first() == 0 && s.getTagNumbers().last() == 30,
              "gps numbers span 0..30");
        // string constants (probed)
        check(ExifGPSTagSet.GPS_VERSION_2_2.equals("2200"), "GPS_VERSION_2_2 2200");
        check(ExifGPSTagSet.LATITUDE_REF_NORTH.equals("N"), "LATITUDE_REF_NORTH N");
        check(ExifGPSTagSet.LATITUDE_REF_SOUTH.equals("S"), "LATITUDE_REF_SOUTH S");
        check(ExifGPSTagSet.LONGITUDE_REF_EAST.equals("E"), "LONGITUDE_REF_EAST E");
        check(ExifGPSTagSet.LONGITUDE_REF_WEST.equals("W"), "LONGITUDE_REF_WEST W");
        check(ExifGPSTagSet.STATUS_MEASUREMENT_IN_PROGRESS.equals("A"), "STATUS A");
        check(ExifGPSTagSet.STATUS_MEASUREMENT_INTEROPERABILITY.equals("V"), "STATUS V");
        check(ExifGPSTagSet.MEASURE_MODE_2D.equals("2"), "MEASURE_MODE_2D 2");
        check(ExifGPSTagSet.MEASURE_MODE_3D.equals("3"), "MEASURE_MODE_3D 3");
        check(ExifGPSTagSet.SPEED_REF_KILOMETERS_PER_HOUR.equals("K"), "SPEED_REF K");
        check(ExifGPSTagSet.SPEED_REF_MILES_PER_HOUR.equals("M"), "SPEED_REF M");
        check(ExifGPSTagSet.SPEED_REF_KNOTS.equals("N"), "SPEED_REF N");
        check(ExifGPSTagSet.DIRECTION_REF_TRUE.equals("T"), "DIRECTION_REF_TRUE T");
        check(ExifGPSTagSet.DIRECTION_REF_MAGNETIC.equals("M"), "DIRECTION_REF_MAGNETIC M");
        check(ExifGPSTagSet.DEST_DISTANCE_REF_KILOMETERS.equals("K"), "DEST_DISTANCE_REF K");
        check(ExifGPSTagSet.DEST_DISTANCE_REF_MILES.equals("M"), "DEST_DISTANCE_REF M");
        check(ExifGPSTagSet.DEST_DISTANCE_REF_KNOTS.equals("N"), "DEST_DISTANCE_REF N");
        // numeric value constants (probed)
        check(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL == 0, "ALTITUDE_REF_SEA_LEVEL 0");
        check(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL_REFERENCE == 1, "ALTITUDE_REF_REFERENCE 1");
        check(ExifGPSTagSet.DIFFERENTIAL_CORRECTION_NONE == 0, "DIFFERENTIAL_NONE 0");
        check(ExifGPSTagSet.DIFFERENTIAL_CORRECTION_APPLIED == 1, "DIFFERENTIAL_APPLIED 1");
    }

    // ---- Part 3 (deepened): canonical names, data types, value names ----
    static void testTagDetails() throws Exception {
        ExifGPSTagSet s = ExifGPSTagSet.getInstance();
        // canonical name pairings (probed)
        String[][] named = {
            { "0", "GPSVersionID" }, { "1", "GPSLatitudeRef" }, { "2", "GPSLatitude" },
            { "3", "GPSLongitudeRef" }, { "4", "GPSLongitude" }, { "5", "GPSAltitudeRef" },
            { "6", "GPSAltitude" }, { "7", "GPSTimeStamp" }, { "16", "GPSImgDirectionRef" },
            { "18", "GPSMapDatum" }, { "23", "GPSDestBearingRef" }, { "27", "GPSProcessingMethod" },
            { "29", "GPSDateStamp" }, { "30", "GPSDifferential" },
        };
        for (String[] pair : named) {
            check(s.getTag(Integer.parseInt(pair[0])).getName().equals(pair[1]),
                  "tag " + pair[0] + " is " + pair[1]);
        }
        // data type contracts (probed)
        check(s.getTag("GPSVersionID").isDataTypeOK(TIFFTag.TIFF_BYTE), "GPSVersionID BYTE");
        check(!s.getTag("GPSVersionID").isDataTypeOK(TIFFTag.TIFF_SHORT), "GPSVersionID rejects SHORT");
        check(s.getTag("GPSVersionID").getCount() == -1, "GPSVersionID count undefined");
        check(s.getTag("GPSLatitudeRef").isDataTypeOK(TIFFTag.TIFF_ASCII), "GPSLatitudeRef ASCII");
        check(s.getTag("GPSLatitude").isDataTypeOK(TIFFTag.TIFF_RATIONAL), "GPSLatitude RATIONAL");
        check(s.getTag("GPSAltitude").isDataTypeOK(TIFFTag.TIFF_RATIONAL), "GPSAltitude RATIONAL");
        check(!s.getTag("GPSAltitude").isDataTypeOK(TIFFTag.TIFF_LONG), "GPSAltitude rejects LONG");
        check(s.getTag("GPSTimeStamp").isDataTypeOK(TIFFTag.TIFF_RATIONAL), "GPSTimeStamp RATIONAL");
        check(s.getTag("GPSDestBearingRef").isDataTypeOK(TIFFTag.TIFF_ASCII), "GPSDestBearingRef ASCII");
        check(s.getTag("GPSDestBearingRef").getNumber() == 23, "GPSDestBearingRef 23");
        // value names on GPSAltitudeRef (probed strings)
        TIFFTag altRef = s.getTag("GPSAltitudeRef");
        check(altRef.hasValueNames(), "GPSAltitudeRef has value names");
        check(altRef.getValueName(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL).equals("Sea level"),
              "altitude ref 0 Sea level");
        check(altRef.getValueName(ExifGPSTagSet.ALTITUDE_REF_SEA_LEVEL_REFERENCE)
              .equals("Sea level reference (negative value)"), "altitude ref 1 long name");
        check(altRef.getValueName(2) == null, "altitude ref 2 null");
        check(java.util.Arrays.equals(altRef.getNamedValues(), new int[]{0, 1}),
              "altitude ref named values [0,1]");
        // plain GPS tags carry no value names
        check(!s.getTag("GPSLatitude").hasValueNames(), "GPSLatitude no value names");
        check(!s.getTag("GPSMapDatum").hasValueNames(), "GPSMapDatum no value names");
        // unknown tag numbers miss
        check(s.getTag(31) == null, "gps tag 31 null");
        check(s.getTag(-1) == null, "gps tag -1 null");
        check(s.getTag("NoSuchGpsTag") == null, "gps unknown name null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTagConstants();
        testTagDetails();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
