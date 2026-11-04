/*
 * @test
 * @summary javax.imageio.plugins.tiff.BaselineTIFFTagSet conformance: white-box structure + black-box behavior.
 * @run main BaselineTIFFTagSetTest
 */

import java.lang.reflect.Modifier;
import java.util.SortedSet;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFTag;

public class BaselineTIFFTagSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.BaselineTIFFTagSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.plugins.tiff.TIFFTagSet"), "extends TIFFTagSet");
        check(c.getField("TAG_IMAGE_WIDTH") != null, "has TAG_IMAGE_WIDTH");
        check(c.getField("TAG_COMPRESSION") != null, "has TAG_COMPRESSION");
        check(c.getField("TAG_PHOTOMETRIC_INTERPRETATION") != null, "has TAG_PHOTOMETRIC_INTERPRETATION");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BaselineTIFFTagSet s = BaselineTIFFTagSet.getInstance();
        check(s != null, "getInstance non-null");
        check(s == BaselineTIFFTagSet.getInstance(), "getInstance singleton");

        // exact tag numbers from TIFF 6.0 baseline spec
        int[][] known = {
            { BaselineTIFFTagSet.TAG_IMAGE_WIDTH, 256 },
            { BaselineTIFFTagSet.TAG_IMAGE_LENGTH, 257 },
            { BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE, 258 },
            { BaselineTIFFTagSet.TAG_COMPRESSION, 259 },
            { BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION, 262 },
            { BaselineTIFFTagSet.TAG_STRIP_OFFSETS, 273 },
            { BaselineTIFFTagSet.TAG_ROWS_PER_STRIP, 278 },
            { BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS, 279 },
        };
        for (int i = 0; i < known.length; i++) {
            check(known[i][0] == known[i][1], "constant equals spec value for index " + i);
        }

        // name -> tag lookup
        TIFFTag w = s.getTag("ImageWidth");
        check(w != null, "getTag(\"ImageWidth\") non-null");
        check(w.getNumber() == 256, "ImageWidth number 256");
        check(w.getCount() == 1, "ImageWidth count 1");
        check(w.isDataTypeOK(TIFFTag.TIFF_SHORT) && w.isDataTypeOK(TIFFTag.TIFF_LONG),
              "ImageWidth allows SHORT and LONG");

        TIFFTag bps = s.getTag("BitsPerSample");
        check(bps != null && bps.getNumber() == 258, "BitsPerSample number 258");

        // number -> tag lookup
        check(s.getTag(256) == w, "getTag(256) resolves ImageWidth");
        check(s.getTag(259) != null && s.getTag(259).getNumber() == 259, "getTag(259) resolves Compression");
        check(s.getTag(262) != null && s.getTag(262).getNumber() == 262, "getTag(262) resolves PhotometricInterpretation");
        check(s.getTag(99999) == null, "unknown tag number null");

        // tag names and numbers sets
        SortedSet<String> names = s.getTagNames();
        SortedSet<Integer> numbers = s.getTagNumbers();
        check(names.size() > 40, "baseline has many named tags");
        check(names.contains("ImageWidth") && names.contains("Compression"), "tag names contains ImageWidth/Compression");
        check(numbers.contains(Integer.valueOf(256)) && numbers.contains(Integer.valueOf(279)),
              "tag numbers contains 256 and 279");
        check(numbers.size() == names.size(), "tag numbers size equals tag names size");

        // every name resolves to a non-null tag with the same number key
        boolean allResolve = true;
        for (String name : names) {
            TIFFTag t = s.getTag(name);
            if (t == null || !t.getName().equals(name)) { allResolve = false; break; }
        }
        check(allResolve, "every named tag resolves");

        // compression tag has mnemonic value names
        TIFFTag comp = s.getTag("Compression");
        check(comp.hasValueNames(), "Compression has value names");
        check(comp.getValueName(BaselineTIFFTagSet.COMPRESSION_NONE) != null,
              "Compression value name for NONE");
        check(comp.getNamedValues() != null && comp.getNamedValues().length > 0,
              "Compression named values non-empty");
    }

    // ---- Part 3 (deepened): full tag number constant table (TIFF 6.0 spec) ----
    static void testTagConstants() throws Exception {
        int[][] tags = {
            { BaselineTIFFTagSet.TAG_NEW_SUBFILE_TYPE, 254 },
            { BaselineTIFFTagSet.TAG_SUBFILE_TYPE, 255 },
            { BaselineTIFFTagSet.TAG_IMAGE_WIDTH, 256 },
            { BaselineTIFFTagSet.TAG_IMAGE_LENGTH, 257 },
            { BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE, 258 },
            { BaselineTIFFTagSet.TAG_COMPRESSION, 259 },
            { BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION, 262 },
            { BaselineTIFFTagSet.TAG_THRESHHOLDING, 263 },
            { BaselineTIFFTagSet.TAG_CELL_WIDTH, 264 },
            { BaselineTIFFTagSet.TAG_CELL_LENGTH, 265 },
            { BaselineTIFFTagSet.TAG_FILL_ORDER, 266 },
            { BaselineTIFFTagSet.TAG_DOCUMENT_NAME, 269 },
            { BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION, 270 },
            { BaselineTIFFTagSet.TAG_MAKE, 271 },
            { BaselineTIFFTagSet.TAG_MODEL, 272 },
            { BaselineTIFFTagSet.TAG_STRIP_OFFSETS, 273 },
            { BaselineTIFFTagSet.TAG_ORIENTATION, 274 },
            { BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL, 277 },
            { BaselineTIFFTagSet.TAG_ROWS_PER_STRIP, 278 },
            { BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS, 279 },
            { BaselineTIFFTagSet.TAG_MIN_SAMPLE_VALUE, 280 },
            { BaselineTIFFTagSet.TAG_MAX_SAMPLE_VALUE, 281 },
            { BaselineTIFFTagSet.TAG_X_RESOLUTION, 282 },
            { BaselineTIFFTagSet.TAG_Y_RESOLUTION, 283 },
            { BaselineTIFFTagSet.TAG_PLANAR_CONFIGURATION, 284 },
            { BaselineTIFFTagSet.TAG_PAGE_NAME, 285 },
            { BaselineTIFFTagSet.TAG_X_POSITION, 286 },
            { BaselineTIFFTagSet.TAG_Y_POSITION, 287 },
            { BaselineTIFFTagSet.TAG_FREE_OFFSETS, 288 },
            { BaselineTIFFTagSet.TAG_FREE_BYTE_COUNTS, 289 },
            { BaselineTIFFTagSet.TAG_GRAY_RESPONSE_UNIT, 290 },
            { BaselineTIFFTagSet.TAG_GRAY_RESPONSE_CURVE, 291 },
            { BaselineTIFFTagSet.TAG_T4_OPTIONS, 292 },
            { BaselineTIFFTagSet.TAG_T6_OPTIONS, 293 },
            { BaselineTIFFTagSet.TAG_RESOLUTION_UNIT, 296 },
            { BaselineTIFFTagSet.TAG_PAGE_NUMBER, 297 },
            { BaselineTIFFTagSet.TAG_TRANSFER_FUNCTION, 301 },
            { BaselineTIFFTagSet.TAG_SOFTWARE, 305 },
            { BaselineTIFFTagSet.TAG_DATE_TIME, 306 },
            { BaselineTIFFTagSet.TAG_ARTIST, 315 },
            { BaselineTIFFTagSet.TAG_HOST_COMPUTER, 316 },
            { BaselineTIFFTagSet.TAG_PREDICTOR, 317 },
            { BaselineTIFFTagSet.TAG_WHITE_POINT, 318 },
            { BaselineTIFFTagSet.TAG_PRIMARY_CHROMATICITES, 319 },
            { BaselineTIFFTagSet.TAG_COLOR_MAP, 320 },
            { BaselineTIFFTagSet.TAG_HALFTONE_HINTS, 321 },
            { BaselineTIFFTagSet.TAG_TILE_WIDTH, 322 },
            { BaselineTIFFTagSet.TAG_TILE_LENGTH, 323 },
            { BaselineTIFFTagSet.TAG_TILE_OFFSETS, 324 },
            { BaselineTIFFTagSet.TAG_TILE_BYTE_COUNTS, 325 },
            { BaselineTIFFTagSet.TAG_INK_SET, 332 },
            { BaselineTIFFTagSet.TAG_INK_NAMES, 333 },
            { BaselineTIFFTagSet.TAG_NUMBER_OF_INKS, 334 },
            { BaselineTIFFTagSet.TAG_DOT_RANGE, 336 },
            { BaselineTIFFTagSet.TAG_TARGET_PRINTER, 337 },
            { BaselineTIFFTagSet.TAG_EXTRA_SAMPLES, 338 },
            { BaselineTIFFTagSet.TAG_SAMPLE_FORMAT, 339 },
            { BaselineTIFFTagSet.TAG_S_MIN_SAMPLE_VALUE, 340 },
            { BaselineTIFFTagSet.TAG_S_MAX_SAMPLE_VALUE, 341 },
            { BaselineTIFFTagSet.TAG_TRANSFER_RANGE, 342 },
            { BaselineTIFFTagSet.TAG_JPEG_TABLES, 347 },
            { BaselineTIFFTagSet.TAG_JPEG_PROC, 512 },
            { BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT, 513 },
            { BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, 514 },
            { BaselineTIFFTagSet.TAG_JPEG_RESTART_INTERVAL, 515 },
            { BaselineTIFFTagSet.TAG_JPEG_LOSSLESS_PREDICTORS, 517 },
            { BaselineTIFFTagSet.TAG_JPEG_POINT_TRANSFORMS, 518 },
            { BaselineTIFFTagSet.TAG_JPEG_Q_TABLES, 519 },
            { BaselineTIFFTagSet.TAG_JPEG_DC_TABLES, 520 },
            { BaselineTIFFTagSet.TAG_JPEG_AC_TABLES, 521 },
            { BaselineTIFFTagSet.TAG_Y_CB_CR_COEFFICIENTS, 529 },
            { BaselineTIFFTagSet.TAG_Y_CB_CR_SUBSAMPLING, 530 },
            { BaselineTIFFTagSet.TAG_Y_CB_CR_POSITIONING, 531 },
            { BaselineTIFFTagSet.TAG_REFERENCE_BLACK_WHITE, 532 },
            { BaselineTIFFTagSet.TAG_COPYRIGHT, 33432 },
            { BaselineTIFFTagSet.TAG_ICC_PROFILE, 34675 },
        };
        BaselineTIFFTagSet s = BaselineTIFFTagSet.getInstance();
        for (int i = 0; i < tags.length; i++) {
            check(tags[i][0] == tags[i][1], "TAG constant " + tags[i][1] + " exact");
            // every constant resolves to a registered non-null tag whose number matches
            TIFFTag t = s.getTag(tags[i][1]);
            check(t != null, "tag " + tags[i][1] + " registered");
            check(t.getNumber() == tags[i][1], "tag " + tags[i][1] + " number self-consistent");
            check(t.getName() != null, "tag " + tags[i][1] + " has a name");
        }
        // exact size of the shipped set (probed: 76 tags)
        check(s.getTagNumbers().size() == 76, "baseline set holds 76 tags");

        // tag number -> canonical name pairings (probed)
        String[][] named = {
            { "256", "ImageWidth" }, { "257", "ImageLength" }, { "258", "BitsPerSample" },
            { "259", "Compression" }, { "262", "PhotometricInterpretation" }, { "273", "StripOffsets" },
            { "274", "Orientation" }, { "277", "SamplesPerPixel" }, { "278", "RowsPerStrip" },
            { "279", "StripByteCounts" }, { "282", "XResolution" }, { "283", "YResolution" },
            { "296", "ResolutionUnit" }, { "305", "Software" }, { "306", "DateTime" },
            { "322", "TileWidth" }, { "323", "TileLength" }, { "324", "TileOffsets" },
            { "339", "SampleFormat" }, { "33432", "Copyright" }, { "34675", "ICC Profile" },
        };
        for (String[] pair : named) {
            check(s.getTag(Integer.parseInt(pair[0])).getName().equals(pair[1]),
                  "tag " + pair[0] + " is " + pair[1]);
        }
    }

    // ---- Part 3 (deepened): enumeration value constants and value names ----
    static void testValueConstants() throws Exception {
        // value constant exact values (probed via javap -constants)
        int[][] vals = {
            { BaselineTIFFTagSet.NEW_SUBFILE_TYPE_REDUCED_RESOLUTION, 1 },
            { BaselineTIFFTagSet.NEW_SUBFILE_TYPE_SINGLE_PAGE, 2 },
            { BaselineTIFFTagSet.NEW_SUBFILE_TYPE_TRANSPARENCY, 4 },
            { BaselineTIFFTagSet.SUBFILE_TYPE_FULL_RESOLUTION, 1 },
            { BaselineTIFFTagSet.SUBFILE_TYPE_REDUCED_RESOLUTION, 2 },
            { BaselineTIFFTagSet.SUBFILE_TYPE_SINGLE_PAGE, 3 },
            { BaselineTIFFTagSet.COMPRESSION_NONE, 1 },
            { BaselineTIFFTagSet.COMPRESSION_CCITT_RLE, 2 },
            { BaselineTIFFTagSet.COMPRESSION_CCITT_T_4, 3 },
            { BaselineTIFFTagSet.COMPRESSION_CCITT_T_6, 4 },
            { BaselineTIFFTagSet.COMPRESSION_LZW, 5 },
            { BaselineTIFFTagSet.COMPRESSION_OLD_JPEG, 6 },
            { BaselineTIFFTagSet.COMPRESSION_JPEG, 7 },
            { BaselineTIFFTagSet.COMPRESSION_ZLIB, 8 },
            { BaselineTIFFTagSet.COMPRESSION_PACKBITS, 32773 },
            { BaselineTIFFTagSet.COMPRESSION_DEFLATE, 32946 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO, 0 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO, 1 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_RGB, 2 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_PALETTE_COLOR, 3 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_TRANSPARENCY_MASK, 4 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_CMYK, 5 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_Y_CB_CR, 6 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_CIELAB, 8 },
            { BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_ICCLAB, 9 },
            { BaselineTIFFTagSet.THRESHHOLDING_NONE, 1 },
            { BaselineTIFFTagSet.THRESHHOLDING_ORDERED_DITHER, 2 },
            { BaselineTIFFTagSet.THRESHHOLDING_RANDOMIZED_DITHER, 3 },
            { BaselineTIFFTagSet.FILL_ORDER_LEFT_TO_RIGHT, 1 },
            { BaselineTIFFTagSet.FILL_ORDER_RIGHT_TO_LEFT, 2 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_TOP_COLUMN_0_LEFT, 1 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_TOP_COLUMN_0_RIGHT, 2 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_BOTTOM_COLUMN_0_RIGHT, 3 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_BOTTOM_COLUMN_0_LEFT, 4 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_LEFT_COLUMN_0_TOP, 5 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_RIGHT_COLUMN_0_TOP, 6 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_RIGHT_COLUMN_0_BOTTOM, 7 },
            { BaselineTIFFTagSet.ORIENTATION_ROW_0_LEFT_COLUMN_0_BOTTOM, 8 },
            { BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY, 1 },
            { BaselineTIFFTagSet.PLANAR_CONFIGURATION_PLANAR, 2 },
            { BaselineTIFFTagSet.GRAY_RESPONSE_UNIT_TENTHS, 1 },
            { BaselineTIFFTagSet.GRAY_RESPONSE_UNIT_HUNDRED_THOUSANDTHS, 5 },
            { BaselineTIFFTagSet.T4_OPTIONS_2D_CODING, 1 },
            { BaselineTIFFTagSet.T4_OPTIONS_UNCOMPRESSED, 2 },
            { BaselineTIFFTagSet.T4_OPTIONS_EOL_BYTE_ALIGNED, 4 },
            { BaselineTIFFTagSet.T6_OPTIONS_UNCOMPRESSED, 2 },
            { BaselineTIFFTagSet.RESOLUTION_UNIT_NONE, 1 },
            { BaselineTIFFTagSet.RESOLUTION_UNIT_INCH, 2 },
            { BaselineTIFFTagSet.RESOLUTION_UNIT_CENTIMETER, 3 },
            { BaselineTIFFTagSet.PREDICTOR_NONE, 1 },
            { BaselineTIFFTagSet.PREDICTOR_HORIZONTAL_DIFFERENCING, 2 },
            { BaselineTIFFTagSet.INK_SET_CMYK, 1 },
            { BaselineTIFFTagSet.INK_SET_NOT_CMYK, 2 },
            { BaselineTIFFTagSet.EXTRA_SAMPLES_UNSPECIFIED, 0 },
            { BaselineTIFFTagSet.EXTRA_SAMPLES_ASSOCIATED_ALPHA, 1 },
            { BaselineTIFFTagSet.EXTRA_SAMPLES_UNASSOCIATED_ALPHA, 2 },
            { BaselineTIFFTagSet.SAMPLE_FORMAT_UNSIGNED_INTEGER, 1 },
            { BaselineTIFFTagSet.SAMPLE_FORMAT_SIGNED_INTEGER, 2 },
            { BaselineTIFFTagSet.SAMPLE_FORMAT_FLOATING_POINT, 3 },
            { BaselineTIFFTagSet.SAMPLE_FORMAT_UNDEFINED, 4 },
            { BaselineTIFFTagSet.JPEG_PROC_BASELINE, 1 },
            { BaselineTIFFTagSet.JPEG_PROC_LOSSLESS, 14 },
            { BaselineTIFFTagSet.Y_CB_CR_POSITIONING_CENTERED, 1 },
            { BaselineTIFFTagSet.Y_CB_CR_POSITIONING_COSITED, 2 },
        };
        for (int i = 0; i < vals.length; i++) {
            check(vals[i][0] == vals[i][1], "value constant " + vals[i][1] + " exact");
        }

        BaselineTIFFTagSet s = BaselineTIFFTagSet.getInstance();
        // value-name lookups (probed strings)
        TIFFTag comp = s.getTag("Compression");
        check(comp.getValueName(BaselineTIFFTagSet.COMPRESSION_NONE).equals("Uncompressed"),
              "Compression 1 -> Uncompressed");
        check(comp.getValueName(BaselineTIFFTagSet.COMPRESSION_PACKBITS).equals("PackBits"),
              "Compression 32773 -> PackBits");
        check(comp.getValueName(BaselineTIFFTagSet.COMPRESSION_CCITT_T_4) != null,
              "Compression 3 named");
        check(comp.getValueName(12345) == null, "Compression 12345 unnamed -> null");
        int[] nv = comp.getNamedValues();
        check(java.util.Arrays.equals(nv, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 32773, 32946}),
              "Compression named values exact");
        // Orientation: values 1..7 named in this implementation, value 8 has no name (probed)
        TIFFTag orient = s.getTag("Orientation");
        for (int v = 1; v <= 7; v++) {
            check(orient.getValueName(v) != null, "Orientation " + v + " named");
        }
        check(orient.getValueName(1).equals("Row 0=Top, Column 0=Left"), "Orientation 1 string");
        check(orient.getValueName(0) == null, "Orientation 0 null");
        check(orient.getValueName(9) == null, "Orientation 9 null");
        // more value-name tables
        check(s.getTag("PhotometricInterpretation").getValueName(2).equals("RGB"), "Photometric 2 RGB");
        check(s.getTag("SubfileType").getValueName(1).equals("FullResolution"), "SubfileType 1");
        check(s.getTag("NewSubfileType").getValueName(4).equals("Transparency"), "NewSubfileType 4");
        check(s.getTag("PlanarConfiguration").getValueName(1).equals("Chunky"), "Planar 1 Chunky");
        check(s.getTag("PlanarConfiguration").getValueName(2).equals("Planar"), "Planar 2 Planar");
        check(s.getTag("Predictor").getValueName(2).equals("Horizontal Differencing"), "Predictor 2");
        check(s.getTag("ResolutionUnit").getValueName(2).equals("Inch"), "ResolutionUnit 2 Inch");
        check(s.getTag("FillOrder").getValueName(1).equals("LeftToRight"), "FillOrder 1");
        check(s.getTag("InkSet").getValueName(1).equals("CMYK"), "InkSet 1 CMYK");
        check(s.getTag("ExtraSamples").getValueName(0).equals("Unspecified"), "ExtraSamples 0");
        check(s.getTag("SampleFormat").getValueName(1).equals("Unsigned Integer"), "SampleFormat 1");
        // no value names on plain tags
        check(!s.getTag("ImageWidth").hasValueNames(), "ImageWidth has no value names");
        check(s.getTag("ImageWidth").getValueName(1) == null, "ImageWidth value name null");
    }

    // ---- Part 3 (deepened): per-tag data type and count contracts ----
    static void testTagTypes() throws Exception {
        BaselineTIFFTagSet s = BaselineTIFFTagSet.getInstance();
        TIFFTag iw = s.getTag("ImageWidth");
        check(iw.isDataTypeOK(TIFFTag.TIFF_SHORT) && iw.isDataTypeOK(TIFFTag.TIFF_LONG),
              "ImageWidth SHORT|LONG");
        check(!iw.isDataTypeOK(TIFFTag.TIFF_BYTE) && !iw.isDataTypeOK(TIFFTag.TIFF_RATIONAL),
              "ImageWidth rejects BYTE/RATIONAL");
        check(iw.getCount() == 1, "ImageWidth count 1");
        TIFFTag xres = s.getTag("XResolution");
        check(xres.isDataTypeOK(TIFFTag.TIFF_RATIONAL) && !xres.isDataTypeOK(TIFFTag.TIFF_LONG),
              "XResolution RATIONAL only");
        check(xres.getCount() == 1, "XResolution count 1");
        check(s.getTag("DateTime").isDataTypeOK(TIFFTag.TIFF_ASCII), "DateTime ASCII");
        check(!s.getTag("DateTime").isDataTypeOK(TIFFTag.TIFF_SHORT), "DateTime rejects SHORT");
        check(s.getTag("DocumentName").isDataTypeOK(TIFFTag.TIFF_ASCII), "DocumentName ASCII");
        check(s.getTag("Software").isDataTypeOK(TIFFTag.TIFF_ASCII), "Software ASCII");
        check(s.getTag("BitsPerSample").isDataTypeOK(TIFFTag.TIFF_SHORT), "BitsPerSample SHORT");
        check(s.getTag("BitsPerSample").getCount() == -1, "BitsPerSample count undefined");
        check(s.getTag("StripOffsets").isDataTypeOK(TIFFTag.TIFF_SHORT)
              && s.getTag("StripOffsets").isDataTypeOK(TIFFTag.TIFF_LONG), "StripOffsets SHORT|LONG");
        check(s.getTag("StripOffsets").getCount() == -1, "StripOffsets count undefined");
        check(s.getTag("ICC Profile").isDataTypeOK(TIFFTag.TIFF_UNDEFINED), "ICC Profile UNDEFINED");
        check(s.getTag("TileWidth").isDataTypeOK(TIFFTag.TIFF_LONG), "TileWidth LONG");
        check(s.getTag("TileWidth").getCount() == 1, "TileWidth count 1");
        // baseline tags carry no tag set back-reference and none is an IFD pointer
        boolean anyPointer = false;
        for (Integer n : s.getTagNumbers()) {
            TIFFTag t = s.getTag(n.intValue());
            if (t.isIFDPointer()) anyPointer = true;
        }
        check(!anyPointer, "no baseline tag is an IFD pointer");
        // every baseline tag reaches this set through TIFFTagSet lookup only (getTagSet null)
        check(s.getTag("ImageWidth").getTagSet() == null, "baseline tag getTagSet null");
    }

    // ---- Part 3 (deepened): the tag set drives a real TIFF encode/decode ----
    static void testCodecIntegration() throws Exception {
        int w = 8, h = 4;
        java.awt.image.BufferedImage src =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                src.setRGB(x, y, ((x * 30 + y * 60) & 0xFF) << 16);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        boolean ok = javax.imageio.ImageIO.write(src, "tiff", baos);
        check(ok, "baseline tag set: TIFF write ok");
        javax.imageio.stream.ImageInputStream iis =
            javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()));
        javax.imageio.ImageReader reader = javax.imageio.ImageIO.getImageReaders(iis).next();
        try {
            reader.setInput(iis, true, false);
            javax.imageio.metadata.IIOMetadata meta = reader.getImageMetadata(0);
            javax.imageio.plugins.tiff.TIFFDirectory dir =
                javax.imageio.plugins.tiff.TIFFDirectory.createFromMetadata(meta);
            // the tags actually written by the codec are baseline tags with baseline numbers
            // (probed: 256..296; StripOffsets/StripByteCounts are layout-dependent, checked > 0)
            int[][] expect = {
                { 256, w }, { 257, h }, { 258, 8 }, { 259, 1 },
                { 262, 1 }, { 277, 1 }, { 278, h },
            };
            for (int[] e : expect) {
                javax.imageio.plugins.tiff.TIFFField f = dir.getTIFFField(e[0]);
                check(f != null, "codec wrote tag " + e[0]);
                if (f != null) {
                    check(f.getAsInt(0) == e[1], "tag " + e[0] + " value " + e[1]);
                    check(f.getTag().getName()
                          .equals(BaselineTIFFTagSet.getInstance().getTag(e[0]).getName()),
                          "tag " + e[0] + " resolves to baseline definition");
                }
            }
            // strip layout tags: positive values, correct tag identity
            javax.imageio.plugins.tiff.TIFFField off = dir.getTIFFField(273);
            javax.imageio.plugins.tiff.TIFFField cnt = dir.getTIFFField(279);
            check(off != null && off.getAsLong(0) > 0, "StripOffsets positive");
            check(cnt != null && cnt.getAsLong(0) == (long) w * h, "StripByteCounts equals w*h");
            check(off.getTag().getName().equals("StripOffsets"), "273 is StripOffsets");
            check(cnt.getTag().getName().equals("StripByteCounts"), "279 is StripByteCounts");
            javax.imageio.plugins.tiff.TIFFField res = dir.getTIFFField(296);
            check(res != null && res.getAsInt(0) == 1, "ResolutionUnit 1 written");
            check(res.getTag().getName().equals("ResolutionUnit"), "296 is ResolutionUnit");
        } finally {
            reader.dispose();
            iis.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTagConstants();
        testValueConstants();
        testTagTypes();
        testCodecIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
