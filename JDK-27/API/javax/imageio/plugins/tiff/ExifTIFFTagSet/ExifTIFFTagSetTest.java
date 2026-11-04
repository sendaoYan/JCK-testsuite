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

    // ---- Part 3 (deepened): exact tag number constants (Exif 2.3 spec) ----
    static void testTagConstants() throws Exception {
        int[][] tags = {
            { ExifTIFFTagSet.TAG_EXIF_VERSION, 36864 },
            { ExifTIFFTagSet.TAG_FLASHPIX_VERSION, 40960 },
            { ExifTIFFTagSet.TAG_COLOR_SPACE, 40961 },
            { ExifTIFFTagSet.TAG_COMPONENTS_CONFIGURATION, 37121 },
            { ExifTIFFTagSet.TAG_COMPRESSED_BITS_PER_PIXEL, 37122 },
            { ExifTIFFTagSet.TAG_PIXEL_X_DIMENSION, 40962 },
            { ExifTIFFTagSet.TAG_PIXEL_Y_DIMENSION, 40963 },
            { ExifTIFFTagSet.TAG_MAKER_NOTE, 37500 },
            { ExifTIFFTagSet.TAG_MARKER_NOTE, 37500 },
            { ExifTIFFTagSet.TAG_USER_COMMENT, 37510 },
            { ExifTIFFTagSet.TAG_RELATED_SOUND_FILE, 40964 },
            { ExifTIFFTagSet.TAG_DATE_TIME_ORIGINAL, 36867 },
            { ExifTIFFTagSet.TAG_DATE_TIME_DIGITIZED, 36868 },
            { ExifTIFFTagSet.TAG_SUB_SEC_TIME, 37520 },
            { ExifTIFFTagSet.TAG_SUB_SEC_TIME_ORIGINAL, 37521 },
            { ExifTIFFTagSet.TAG_SUB_SEC_TIME_DIGITIZED, 37522 },
            { ExifTIFFTagSet.TAG_EXPOSURE_TIME, 33434 },
            { ExifTIFFTagSet.TAG_F_NUMBER, 33437 },
            { ExifTIFFTagSet.TAG_EXPOSURE_PROGRAM, 34850 },
            { ExifTIFFTagSet.TAG_SPECTRAL_SENSITIVITY, 34852 },
            { ExifTIFFTagSet.TAG_ISO_SPEED_RATINGS, 34855 },
            { ExifTIFFTagSet.TAG_OECF, 34856 },
            { ExifTIFFTagSet.TAG_SHUTTER_SPEED_VALUE, 37377 },
            { ExifTIFFTagSet.TAG_APERTURE_VALUE, 37378 },
            { ExifTIFFTagSet.TAG_BRIGHTNESS_VALUE, 37379 },
            { ExifTIFFTagSet.TAG_EXPOSURE_BIAS_VALUE, 37380 },
            { ExifTIFFTagSet.TAG_MAX_APERTURE_VALUE, 37381 },
            { ExifTIFFTagSet.TAG_SUBJECT_DISTANCE, 37382 },
            { ExifTIFFTagSet.TAG_METERING_MODE, 37383 },
            { ExifTIFFTagSet.TAG_LIGHT_SOURCE, 37384 },
            { ExifTIFFTagSet.TAG_FLASH, 37385 },
            { ExifTIFFTagSet.TAG_FOCAL_LENGTH, 37386 },
            { ExifTIFFTagSet.TAG_SUBJECT_AREA, 37396 },
            { ExifTIFFTagSet.TAG_FLASH_ENERGY, 41483 },
            { ExifTIFFTagSet.TAG_SPATIAL_FREQUENCY_RESPONSE, 41484 },
            { ExifTIFFTagSet.TAG_FOCAL_PLANE_X_RESOLUTION, 41486 },
            { ExifTIFFTagSet.TAG_FOCAL_PLANE_Y_RESOLUTION, 41487 },
            { ExifTIFFTagSet.TAG_FOCAL_PLANE_RESOLUTION_UNIT, 41488 },
            { ExifTIFFTagSet.TAG_SUBJECT_LOCATION, 41492 },
            { ExifTIFFTagSet.TAG_EXPOSURE_INDEX, 41493 },
            { ExifTIFFTagSet.TAG_SENSING_METHOD, 41495 },
            { ExifTIFFTagSet.TAG_FILE_SOURCE, 41728 },
            { ExifTIFFTagSet.TAG_SCENE_TYPE, 41729 },
            { ExifTIFFTagSet.TAG_CFA_PATTERN, 41730 },
            { ExifTIFFTagSet.TAG_CUSTOM_RENDERED, 41985 },
            { ExifTIFFTagSet.TAG_EXPOSURE_MODE, 41986 },
            { ExifTIFFTagSet.TAG_WHITE_BALANCE, 41987 },
            { ExifTIFFTagSet.TAG_DIGITAL_ZOOM_RATIO, 41988 },
            { ExifTIFFTagSet.TAG_FOCAL_LENGTH_IN_35MM_FILM, 41989 },
            { ExifTIFFTagSet.TAG_SCENE_CAPTURE_TYPE, 41990 },
            { ExifTIFFTagSet.TAG_GAIN_CONTROL, 41991 },
            { ExifTIFFTagSet.TAG_CONTRAST, 41992 },
            { ExifTIFFTagSet.TAG_SATURATION, 41993 },
            { ExifTIFFTagSet.TAG_SHARPNESS, 41994 },
            { ExifTIFFTagSet.TAG_DEVICE_SETTING_DESCRIPTION, 41995 },
            { ExifTIFFTagSet.TAG_SUBJECT_DISTANCE_RANGE, 41996 },
            { ExifTIFFTagSet.TAG_IMAGE_UNIQUE_ID, 42016 },
        };
        ExifTIFFTagSet s = ExifTIFFTagSet.getInstance();
        for (int i = 0; i < tags.length; i++) {
            check(tags[i][0] == tags[i][1], "TAG constant " + tags[i][1] + " exact");
            TIFFTag t = s.getTag(tags[i][1]);
            check(t != null, "tag " + tags[i][1] + " registered");
            check(t.getNumber() == tags[i][1], "tag " + tags[i][1] + " number self-consistent");
        }
        // MAKER_NOTE and MARKER_NOTE are aliases for the same number (probed)
        check(ExifTIFFTagSet.TAG_MAKER_NOTE == ExifTIFFTagSet.TAG_MARKER_NOTE,
              "MakerNote/MarkerNote alias");
        // exact set size (probed: 57 tags)
        check(s.getTagNumbers().size() == 57, "exif set holds 57 tags");
        // GPSInfoIFDPointer (34853) constant exists but is NOT a member of this set (probed)
        check(ExifTIFFTagSet.TAG_GPS_INFO_IFD_POINTER == 34853, "GPS pointer constant exact");
        check(s.getTag(34853) == null, "GPS pointer tag absent from Exif set");
        check(ExifTIFFTagSet.TAG_INTEROPERABILITY_IFD_POINTER == 40965, "interop pointer constant exact");
        // string constants (probed)
        check(ExifTIFFTagSet.EXIF_VERSION_2_1.equals("0210"), "EXIF_VERSION_2_1 0210");
        check(ExifTIFFTagSet.EXIF_VERSION_2_2.equals("0220"), "EXIF_VERSION_2_2 0220");
    }

    // ---- Part 3 (deepened): enumeration value constants and value names ----
    static void testValueConstants() throws Exception {
        int[][] vals = {
            { ExifTIFFTagSet.COLOR_SPACE_SRGB, 1 },
            { ExifTIFFTagSet.COLOR_SPACE_UNCALIBRATED, 65535 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_DOES_NOT_EXIST, 0 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_Y, 1 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_CB, 2 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_CR, 3 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_R, 4 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_G, 5 },
            { ExifTIFFTagSet.COMPONENTS_CONFIGURATION_B, 6 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_NOT_DEFINED, 0 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_MANUAL, 1 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_NORMAL_PROGRAM, 2 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_APERTURE_PRIORITY, 3 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_SHUTTER_PRIORITY, 4 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_CREATIVE_PROGRAM, 5 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_ACTION_PROGRAM, 6 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_PORTRAIT_MODE, 7 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_LANDSCAPE_MODE, 8 },
            { ExifTIFFTagSet.EXPOSURE_PROGRAM_MAX_RESERVED, 255 },
            { ExifTIFFTagSet.METERING_MODE_UNKNOWN, 0 },
            { ExifTIFFTagSet.METERING_MODE_AVERAGE, 1 },
            { ExifTIFFTagSet.METERING_MODE_CENTER_WEIGHTED_AVERAGE, 2 },
            { ExifTIFFTagSet.METERING_MODE_SPOT, 3 },
            { ExifTIFFTagSet.METERING_MODE_MULTI_SPOT, 4 },
            { ExifTIFFTagSet.METERING_MODE_PATTERN, 5 },
            { ExifTIFFTagSet.METERING_MODE_PARTIAL, 6 },
            { ExifTIFFTagSet.METERING_MODE_MIN_RESERVED, 7 },
            { ExifTIFFTagSet.METERING_MODE_MAX_RESERVED, 254 },
            { ExifTIFFTagSet.METERING_MODE_OTHER, 255 },
            { ExifTIFFTagSet.LIGHT_SOURCE_UNKNOWN, 0 },
            { ExifTIFFTagSet.LIGHT_SOURCE_DAYLIGHT, 1 },
            { ExifTIFFTagSet.LIGHT_SOURCE_FLUORESCENT, 2 },
            { ExifTIFFTagSet.LIGHT_SOURCE_TUNGSTEN, 3 },
            { ExifTIFFTagSet.LIGHT_SOURCE_FLASH, 4 },
            { ExifTIFFTagSet.LIGHT_SOURCE_FINE_WEATHER, 9 },
            { ExifTIFFTagSet.LIGHT_SOURCE_D55, 20 },
            { ExifTIFFTagSet.LIGHT_SOURCE_D65, 21 },
            { ExifTIFFTagSet.LIGHT_SOURCE_D75, 22 },
            { ExifTIFFTagSet.LIGHT_SOURCE_D50, 23 },
            { ExifTIFFTagSet.LIGHT_SOURCE_OTHER, 255 },
            { ExifTIFFTagSet.FLASH_DID_NOT_FIRE, 0 },
            { ExifTIFFTagSet.FLASH_FIRED, 1 },
            { ExifTIFFTagSet.FLASH_STROBE_RETURN_LIGHT_NOT_DETECTED, 5 },
            { ExifTIFFTagSet.FLASH_STROBE_RETURN_LIGHT_DETECTED, 7 },
            { ExifTIFFTagSet.FLASH_MASK_FIRED, 1 },
            { ExifTIFFTagSet.FLASH_MASK_RETURN_NOT_DETECTED, 4 },
            { ExifTIFFTagSet.FLASH_MASK_RETURN_DETECTED, 6 },
            { ExifTIFFTagSet.FLASH_MASK_MODE_FLASH_FIRING, 8 },
            { ExifTIFFTagSet.FLASH_MASK_MODE_FLASH_SUPPRESSION, 16 },
            { ExifTIFFTagSet.FLASH_MASK_MODE_AUTO, 24 },
            { ExifTIFFTagSet.FLASH_MASK_FUNCTION_NOT_PRESENT, 32 },
            { ExifTIFFTagSet.FLASH_MASK_RED_EYE_REDUCTION, 64 },
            { ExifTIFFTagSet.FOCAL_PLANE_RESOLUTION_UNIT_NONE, 1 },
            { ExifTIFFTagSet.FOCAL_PLANE_RESOLUTION_UNIT_INCH, 2 },
            { ExifTIFFTagSet.FOCAL_PLANE_RESOLUTION_UNIT_CENTIMETER, 3 },
            { ExifTIFFTagSet.SENSING_METHOD_NOT_DEFINED, 1 },
            { ExifTIFFTagSet.SENSING_METHOD_TRILINEAR_SENSOR, 7 },
            { ExifTIFFTagSet.SENSING_METHOD_COLOR_SEQUENTIAL_LINEAR_SENSOR, 8 },
            { ExifTIFFTagSet.FILE_SOURCE_DSC, 3 },
            { ExifTIFFTagSet.SCENE_TYPE_DSC, 1 },
            { ExifTIFFTagSet.CUSTOM_RENDERED_NORMAL, 0 },
            { ExifTIFFTagSet.CUSTOM_RENDERED_CUSTOM, 1 },
            { ExifTIFFTagSet.EXPOSURE_MODE_AUTO_EXPOSURE, 0 },
            { ExifTIFFTagSet.EXPOSURE_MODE_MANUAL_EXPOSURE, 1 },
            { ExifTIFFTagSet.EXPOSURE_MODE_AUTO_BRACKET, 2 },
            { ExifTIFFTagSet.WHITE_BALANCE_AUTO, 0 },
            { ExifTIFFTagSet.WHITE_BALANCE_MANUAL, 1 },
            { ExifTIFFTagSet.SCENE_CAPTURE_TYPE_STANDARD, 0 },
            { ExifTIFFTagSet.SCENE_CAPTURE_TYPE_LANDSCAPE, 1 },
            { ExifTIFFTagSet.SCENE_CAPTURE_TYPE_PORTRAIT, 2 },
            { ExifTIFFTagSet.SCENE_CAPTURE_TYPE_NIGHT_SCENE, 3 },
            { ExifTIFFTagSet.GAIN_CONTROL_NONE, 0 },
            { ExifTIFFTagSet.GAIN_CONTROL_HIGH_GAIN_DOWN, 4 },
            { ExifTIFFTagSet.CONTRAST_NORMAL, 0 },
            { ExifTIFFTagSet.CONTRAST_SOFT, 1 },
            { ExifTIFFTagSet.CONTRAST_HARD, 2 },
            { ExifTIFFTagSet.SATURATION_NORMAL, 0 },
            { ExifTIFFTagSet.SATURATION_LOW, 1 },
            { ExifTIFFTagSet.SATURATION_HIGH, 2 },
            { ExifTIFFTagSet.SHARPNESS_NORMAL, 0 },
            { ExifTIFFTagSet.SHARPNESS_SOFT, 1 },
            { ExifTIFFTagSet.SHARPNESS_HARD, 2 },
            { ExifTIFFTagSet.SUBJECT_DISTANCE_RANGE_UNKNOWN, 0 },
            { ExifTIFFTagSet.SUBJECT_DISTANCE_RANGE_MACRO, 1 },
            { ExifTIFFTagSet.SUBJECT_DISTANCE_RANGE_CLOSE_VIEW, 2 },
            { ExifTIFFTagSet.SUBJECT_DISTANCE_RANGE_DISTANT_VIEW, 3 },
        };
        for (int i = 0; i < vals.length; i++) {
            check(vals[i][0] == vals[i][1], "exif value constant " + vals[i][1] + " exact");
        }
        // value-name lookups (probed)
        ExifTIFFTagSet s = ExifTIFFTagSet.getInstance();
        check(s.getTag("MeteringMode").getValueName(0).equals("Unknown"), "MeteringMode 0 Unknown");
        check(s.getTag("MeteringMode").getValueName(5).equals("Pattern"), "MeteringMode 5 Pattern");
        check(s.getTag("MeteringMode").getValueName(6) != null, "MeteringMode 6 named");
        check(s.getTag("LightSource").getValueName(21).equals("D65"), "LightSource 21 D65");
        check(s.getTag("LightSource").getValueName(1).equals("Daylight"), "LightSource 1 Daylight");
        check(s.getTag("ExposureProgram").getValueName(1) != null, "ExposureProgram 1 named");
        check(s.getTag("ColorSpace").getValueName(ExifTIFFTagSet.COLOR_SPACE_SRGB) != null,
              "ColorSpace sRGB named");
        check(s.getTag("Flash").hasValueNames(), "Flash has value names");
        // unknown values yield null
        check(s.getTag("MeteringMode").getValueName(12345) == null, "MeteringMode 12345 null");
        // plain tags carry no value names
        check(!s.getTag("ExposureTime").hasValueNames(), "ExposureTime no value names");
        check(s.getTag("UserComment").getValueName(0) == null, "UserComment value name null");
    }

    // ---- Part 3 (deepened): per-tag data types and IFD pointers ----
    static void testTagTypes() throws Exception {
        ExifTIFFTagSet s = ExifTIFFTagSet.getInstance();
        check(s.getTag("ExposureTime").isDataTypeOK(TIFFTag.TIFF_RATIONAL), "ExposureTime RATIONAL");
        check(!s.getTag("ExposureTime").isDataTypeOK(TIFFTag.TIFF_LONG), "ExposureTime rejects LONG");
        check(s.getTag("FNumber").isDataTypeOK(TIFFTag.TIFF_RATIONAL), "FNumber RATIONAL");
        check(s.getTag("ExifVersion").isDataTypeOK(TIFFTag.TIFF_UNDEFINED), "ExifVersion UNDEFINED");
        check(!s.getTag("ExifVersion").isDataTypeOK(TIFFTag.TIFF_ASCII), "ExifVersion rejects ASCII");
        check(s.getTag("DateTimeOriginal").isDataTypeOK(TIFFTag.TIFF_ASCII), "DateTimeOriginal ASCII");
        check(s.getTag("DateTimeOriginal").getNumber() == 36867, "DateTimeOriginal 36867");
        check(s.getTag("MakerNote").isDataTypeOK(TIFFTag.TIFF_UNDEFINED), "MakerNote UNDEFINED");
        check(s.getTag("ISOSpeedRatings").isDataTypeOK(TIFFTag.TIFF_SHORT), "ISOSpeedRatings SHORT");
        check(s.getTag("ImageUniqueID").getNumber() == 42016, "ImageUniqueID 42016");
        // InteroperabilityIFD is an IFD pointer tag (probed)
        TIFFTag interop = s.getTag("InteroperabilityIFD");
        check(interop.getNumber() == 40965, "InteropIFD 40965");
        check(interop.isIFDPointer(), "InteropIFD is IFD pointer");
        check(s.getTag(40965) == interop, "40965 resolves InteropIFD");
        // the set's smallest tag number is 33434 (ExposureTime) (probed)
        check(s.getTagNumbers().first() == 33434, "exif smallest number 33434");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTagConstants();
        testValueConstants();
        testTagTypes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
