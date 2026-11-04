/*
 * @test
 * @summary javax.imageio.ImageTypeSpecifier conformance: white-box structure + black-box behavior.
 * @run main ImageTypeSpecifierTest
 */

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.lang.reflect.Modifier;
import javax.imageio.ImageTypeSpecifier;

public class ImageTypeSpecifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, Act a, String msg) {
        try {
            a.run();
            check(false, msg + ": expected " + expected.getSimpleName() + " but nothing thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName());
        }
    }
    static final ColorSpace SRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTypeSpecifier");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 2, "two public constructors");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("createPacked"), "method createPacked");
        check(dm.contains("createInterleaved"), "method createInterleaved");
        check(dm.contains("createBanded"), "method createBanded");
        check(dm.contains("createGrayscale"), "method createGrayscale");
        check(dm.contains("createIndexed"), "method createIndexed");
        check(dm.contains("createFromBufferedImageType"), "method createFromBufferedImageType");
        check(dm.contains("createFromRenderedImage"), "method createFromRenderedImage");
        check(dm.contains("getBufferedImageType"), "method getBufferedImageType");
        check(dm.contains("getNumComponents"), "method getNumComponents");
        check(dm.contains("getNumBands"), "method getNumBands");
        check(dm.contains("getBitsPerBand"), "method getBitsPerBand");
        check(dm.contains("getSampleModel"), "method getSampleModel");
        check(dm.contains("getColorModel"), "method getColorModel");
        check(dm.contains("createBufferedImage"), "method createBufferedImage");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // factory from a BufferedImage type
        ImageTypeSpecifier rgb = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        check(rgb.getBufferedImageType() == BufferedImage.TYPE_INT_RGB, "INT_RGB type");
        check(rgb.getNumComponents() == 3, "INT_RGB 3 components");
        check(rgb.getNumBands() == 3, "INT_RGB 3 bands");
        check(rgb.getBitsPerBand(0) == 8, "INT_RGB 8 bits/band");
        check(rgb.getBitsPerBand(1) == 8 && rgb.getBitsPerBand(2) == 8, "INT_RGB all bands 8 bits");
        check(rgb.getSampleModel() != null, "INT_RGB sample model non-null");
        check(rgb.getColorModel() != null, "INT_RGB color model non-null");
        BufferedImage rgbImg = rgb.createBufferedImage(4, 4);
        check(rgbImg.getWidth() == 4 && rgbImg.getHeight() == 4, "INT_RGB createBufferedImage dims");
        check(rgbImg.getType() == BufferedImage.TYPE_INT_RGB, "INT_RGB createBufferedImage type");

        // factory from a rendered image (ARGB)
        BufferedImage argb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ImageTypeSpecifier fromImg = ImageTypeSpecifier.createFromRenderedImage(argb);
        check(fromImg.getBufferedImageType() == BufferedImage.TYPE_INT_ARGB, "ARGB type");
        check(fromImg.getNumComponents() == 4, "ARGB 4 components");
        check(fromImg.getNumBands() == 4, "ARGB 4 bands");
        check(fromImg.getBitsPerBand(0) == 8, "ARGB 8 bits/band");
        check(fromImg.createBufferedImage(2, 2).getType() == BufferedImage.TYPE_INT_ARGB, "ARGB createBufferedImage type");

        // constructor taking a ColorModel and SampleModel stores them by reference
        ColorModel cm = argb.getColorModel();
        SampleModel sm = argb.getSampleModel();
        ImageTypeSpecifier cmSm = new ImageTypeSpecifier(cm, sm);
        check(cmSm.getColorModel() == cm, "ColorModel identity");
        check(cmSm.getSampleModel() == sm, "SampleModel identity");
        check(cmSm.getNumBands() == sm.getNumBands(), "numBands from SampleModel");
        check(cmSm.getNumComponents() == 4, "numComponents from ColorModel");

        // constructor from a RenderedImage stores the image's ColorModel/SampleModel
        ImageTypeSpecifier fromCtor = new ImageTypeSpecifier(argb);
        check(fromCtor.getColorModel() == argb.getColorModel(), "ctor stores image ColorModel");
        check(fromCtor.getSampleModel() == argb.getSampleModel(), "ctor stores image SampleModel");
        check(fromCtor.equals(cmSm), "RenderedImage ctor equals (ColorModel,SampleModel) ctor");
        check(fromCtor.getNumBands() == 4 && fromCtor.getNumComponents() == 4, "ctor bands/components");

        // getSampleModel(w,h) resizes the layout
        SampleModel sm23 = rgb.getSampleModel(2, 3);
        check(sm23.getWidth() == 2 && sm23.getHeight() == 3, "getSampleModel(2,3) dims");

        // grayscale factory
        ImageTypeSpecifier gray = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false);
        check(gray.getNumComponents() == 1, "grayscale 1 component");
        check(gray.getNumBands() == 1, "grayscale 1 band");
        check(gray.getBitsPerBand(0) == 8, "grayscale 8 bits/band");
        check(gray.getBufferedImageType() == BufferedImage.TYPE_BYTE_GRAY, "grayscale BYTE_GRAY type");

        // equals/hashCode contract
        ImageTypeSpecifier same = ImageTypeSpecifier.createFromRenderedImage(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB));
        check(fromImg.equals(same), "equals for same layout");
        check(fromImg.hashCode() == same.hashCode(), "hashCode consistent with equals");
        check(!fromImg.equals(null), "equals(null) false");
        check(!fromImg.equals(rgb), "ARGB not equal INT_RGB");
    }

    // ---- Part 3 (deep): factory negative contracts (all probed to IAE) ----
    static void testFactoryNegatives() {
        BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        expectExact(IllegalArgumentException.class,
                    () -> new ImageTypeSpecifier((ColorModel) null, (SampleModel) null),
                    "ctor(null, null)");
        expectExact(IllegalArgumentException.class,
                    () -> new ImageTypeSpecifier(bi.getColorModel(), null),
                    "ctor(cm, null sm)");
        expectExact(IllegalArgumentException.class,
                    () -> new ImageTypeSpecifier(null, bi.getSampleModel()),
                    "ctor(null cm, sm)");
        expectExact(IllegalArgumentException.class,
                    () -> new ImageTypeSpecifier((java.awt.image.RenderedImage) null),
                    "ctor((RenderedImage) null)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createFromRenderedImage(null),
                    "createFromRenderedImage(null)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createFromBufferedImageType(-1),
                    "createFromBufferedImageType(-1)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createFromBufferedImageType(999),
                    "createFromBufferedImageType(999)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_CUSTOM),
                    "createFromBufferedImageType(TYPE_CUSTOM)");
        // createPacked
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createPacked(null, 0xFF0000, 0xFF00, 0xFF, 0, DataBuffer.TYPE_INT, false),
                    "createPacked(null colorspace)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createPacked(SRGB, 0xFF00FF, 0xFF00, 0xFF, 0, DataBuffer.TYPE_INT, false),
                    "createPacked(noncontiguous mask)");
        // createInterleaved
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createInterleaved(SRGB, null, DataBuffer.TYPE_BYTE, false, false),
                    "createInterleaved(null offsets)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createInterleaved(SRGB, new int[]{0}, DataBuffer.TYPE_BYTE, false, false),
                    "createInterleaved(offset count != cs components)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createInterleaved(null, new int[]{0, 1, 2}, DataBuffer.TYPE_BYTE, false, false),
                    "createInterleaved(null colorspace)");
        // createBanded
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createBanded(SRGB, null, null, DataBuffer.TYPE_BYTE, false, false),
                    "createBanded(null banks)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createBanded(SRGB, new int[]{0}, new int[]{0, 1}, DataBuffer.TYPE_BYTE, false, false),
                    "createBanded(bank/offset length mismatch)");
        // createGrayscale: bits must be 1/2/4/8/16 and match dataType capacity
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createGrayscale(0, DataBuffer.TYPE_BYTE, false),
                    "createGrayscale(bits=0)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createGrayscale(12, DataBuffer.TYPE_USHORT, false),
                    "createGrayscale(bits=12)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createGrayscale(17, DataBuffer.TYPE_BYTE, false),
                    "createGrayscale(bits=17)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_INT, false),
                    "createGrayscale(bad dataType)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createGrayscale(16, DataBuffer.TYPE_BYTE, false),
                    "createGrayscale(16 bits in byte)");
        // createIndexed: LUT lengths must be 2^bits, bits in 1..8
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createIndexed(new byte[2], new byte[16], new byte[16], new byte[16],
                                                           4, DataBuffer.TYPE_BYTE),
                    "createIndexed(LUT length mismatch)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createIndexed(new byte[8], new byte[8], new byte[8], new byte[8],
                                                           3, DataBuffer.TYPE_BYTE),
                    "createIndexed(bits=3)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createIndexed(new byte[0], new byte[0], new byte[0], new byte[0],
                                                           0, DataBuffer.TYPE_BYTE),
                    "createIndexed(bits=0)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageTypeSpecifier.createIndexed(null, new byte[2], new byte[2], new byte[2],
                                                           1, DataBuffer.TYPE_BYTE),
                    "createIndexed(null LUT)");
        // query bounds
        ImageTypeSpecifier rgb = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        expectExact(IllegalArgumentException.class, () -> rgb.getBitsPerBand(3),
                    "getBitsPerBand(band=numBands)");
        expectExact(IllegalArgumentException.class, () -> rgb.getBitsPerBand(-1),
                    "getBitsPerBand(-1)");
        expectExact(IllegalArgumentException.class, () -> rgb.getSampleModel(0, 0),
                    "getSampleModel(0,0)");
        expectExact(IllegalArgumentException.class, () -> rgb.getSampleModel(-1, 1),
                    "getSampleModel(-1,1)");
        expectExact(IllegalArgumentException.class, () -> rgb.getSampleModel(1, -1),
                    "getSampleModel(1,-1)");
        expectExact(IllegalArgumentException.class, () -> rgb.createBufferedImage(0, 0),
                    "createBufferedImage(0,0)");
        expectExact(IllegalArgumentException.class, () -> rgb.createBufferedImage(-1, 1),
                    "createBufferedImage(-1,1)");
        expectExact(IllegalArgumentException.class, () -> rgb.createBufferedImage(1, -1),
                    "createBufferedImage(1,-1)");
    }

    // ---- Part 3: createPacked matrix (probed values) ----
    static void testCreatePackedMatrix() {
        ImageTypeSpecifier argb = ImageTypeSpecifier.createPacked(SRGB, 0xFF0000, 0xFF00, 0xFF,
                                                                  0xFF000000, DataBuffer.TYPE_INT, false);
        check(argb.getBufferedImageType() == BufferedImage.TYPE_INT_ARGB, "packed with alpha -> TYPE_INT_ARGB");
        check(argb.getNumComponents() == 4, "packed argb 4 components");
        check(argb.getNumBands() == 4, "packed argb 4 bands");
        check(argb.getBitsPerBand(0) == 8 && argb.getBitsPerBand(1) == 8 && argb.getBitsPerBand(2) == 8,
              "packed rgb bands 8 bits");
        check(argb.getBitsPerBand(3) == 8, "packed alpha band 8 bits");
        ImageTypeSpecifier rgb = ImageTypeSpecifier.createPacked(SRGB, 0xFF0000, 0xFF00, 0xFF,
                                                                 0, DataBuffer.TYPE_INT, false);
        check(rgb.getNumComponents() == 3, "packed without alpha -> 3 components");
        check(rgb.getNumBands() == 3, "packed without alpha -> 3 bands");
        BufferedImage bi = rgb.createBufferedImage(3, 3);
        check(bi.getType() == BufferedImage.TYPE_INT_RGB, "packed rgb createBufferedImage is TYPE_INT_RGB");
        check(argb.createBufferedImage(3, 3).getType() == BufferedImage.TYPE_INT_ARGB,
              "packed argb createBufferedImage is TYPE_INT_ARGB");
        // premultiplied flag survives into the ColorModel
        ImageTypeSpecifier premul = ImageTypeSpecifier.createPacked(SRGB, 0xFF0000, 0xFF00, 0xFF,
                                                                    0xFF000000, DataBuffer.TYPE_INT, true);
        check(premul.getColorModel().isAlphaPremultiplied(), "packed isAlphaPremultiplied=true visible");
        check(!argb.getColorModel().isAlphaPremultiplied(), "packed default not premultiplied");
        check(!rgb.equals(argb), "packed rgb != packed argb");
        check(rgb.equals(ImageTypeSpecifier.createPacked(SRGB, 0xFF0000, 0xFF00, 0xFF,
                                                         0, DataBuffer.TYPE_INT, false)),
              "packed cross-instance equals");
    }

    // ---- Part 3: createInterleaved / createBanded (TYPE_CUSTOM producers) ----
    static void testInterleavedAndBanded() {
        ImageTypeSpecifier il = ImageTypeSpecifier.createInterleaved(SRGB, new int[]{0, 1, 2},
                                                                     DataBuffer.TYPE_BYTE, false, false);
        check(il.getBufferedImageType() == BufferedImage.TYPE_CUSTOM, "interleaved -> TYPE_CUSTOM");
        check(il.getNumComponents() == 3, "interleaved 3 components");
        check(il.getNumBands() == 3, "interleaved 3 bands");
        check(il.getBitsPerBand(0) == 8, "interleaved byte bands 8 bits");
        ImageTypeSpecifier ilA = ImageTypeSpecifier.createInterleaved(SRGB, new int[]{0, 1, 2, 3},
                                                                      DataBuffer.TYPE_BYTE, true, false);
        check(ilA.getNumComponents() == 4, "interleaved with alpha 4 components");
        check(ilA.getNumBands() == 4, "interleaved with alpha 4 bands");
        check(ilA.getColorModel().hasAlpha(), "interleaved alpha visible in ColorModel");
        check(!il.getColorModel().hasAlpha(), "interleaved no-alpha ColorModel");
        check(il.createBufferedImage(2, 2) != null, "interleaved createBufferedImage works");
        check(il.getSampleModel(4, 5).getWidth() == 4 && il.getSampleModel(4, 5).getHeight() == 5,
              "interleaved getSampleModel(w,h) resizes");
        ImageTypeSpecifier band = ImageTypeSpecifier.createBanded(SRGB, new int[]{0, 0, 0},
                                                                  new int[]{0, 1, 2}, DataBuffer.TYPE_BYTE,
                                                                  false, false);
        check(band.getBufferedImageType() == BufferedImage.TYPE_CUSTOM, "banded -> TYPE_CUSTOM");
        check(band.getNumComponents() == 3, "banded 3 components");
        check(band.getNumBands() == 3, "banded 3 bands");
        ImageTypeSpecifier bandA = ImageTypeSpecifier.createBanded(SRGB, new int[]{0, 0, 0, 0},
                                                                   new int[]{0, 1, 2, 3}, DataBuffer.TYPE_BYTE,
                                                                   true, false);
        check(bandA.getNumComponents() == 4, "banded with alpha 4 components");
        check(bandA.getNumBands() == 4, "banded with alpha 4 bands");
        check(!band.equals(bandA), "banded alpha != banded no-alpha");
        check(band.equals(ImageTypeSpecifier.createBanded(SRGB, new int[]{0, 0, 0},
                                                          new int[]{0, 1, 2}, DataBuffer.TYPE_BYTE, false, false)),
              "banded cross-instance equals");
    }

    // ---- Part 3: createGrayscale full matrix (probed) ----
    static void testCreateGrayscaleMatrix() {
        ImageTypeSpecifier g1 = ImageTypeSpecifier.createGrayscale(1, DataBuffer.TYPE_BYTE, false);
        check(g1.getBitsPerBand(0) == 1, "grayscale 1-bit depth");
        check(g1.getBufferedImageType() == BufferedImage.TYPE_BYTE_BINARY, "grayscale 1-bit -> BYTE_BINARY");
        ImageTypeSpecifier g8 = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false);
        check(g8.getBufferedImageType() == BufferedImage.TYPE_BYTE_GRAY, "grayscale 8-bit -> BYTE_GRAY");
        check(g8.getNumComponents() == 1, "grayscale 8-bit 1 component");
        check(g8.getNumBands() == 1, "grayscale 8-bit 1 band");
        ImageTypeSpecifier g16 = ImageTypeSpecifier.createGrayscale(16, DataBuffer.TYPE_USHORT, false);
        check(g16.getBufferedImageType() == BufferedImage.TYPE_USHORT_GRAY, "grayscale 16-bit -> USHORT_GRAY");
        check(g16.getBitsPerBand(0) == 16, "grayscale 16-bit depth");
        check(g16.getNumComponents() == 1, "grayscale 16-bit 1 component");
        // signed 16-bit short variant
        ImageTypeSpecifier g16s = ImageTypeSpecifier.createGrayscale(16, DataBuffer.TYPE_SHORT, true);
        check(g16s.getBitsPerBand(0) == 16, "signed grayscale 16-bit depth");
        // 4-arg overload ALWAYS adds an alpha band (4th arg is isAlphaPremultiplied only)
        ImageTypeSpecifier g8a = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false, true);
        check(g8a.getNumComponents() == 2, "4-arg grayscale has alpha: 2 components");
        check(g8a.getNumBands() == 2, "4-arg grayscale has alpha: 2 bands");
        check(g8a.getColorModel().hasAlpha(), "4-arg grayscale alpha visible");
        check(g8a.getColorModel().isAlphaPremultiplied(), "4-arg grayscale premultiplied=true honored");
        ImageTypeSpecifier g8b = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false, false);
        check(g8b.getNumComponents() == 2 && g8b.getNumBands() == 2,
              "4-arg grayscale keeps alpha band even with premultiplied=false");
        check(g8b.getColorModel().hasAlpha(), "4-arg no-premultiply still has alpha");
        check(!g8b.getColorModel().isAlphaPremultiplied(), "4-arg premultiplied=false honored");
        check(!g8a.equals(g8b), "premultiplied grayscale != non-premultiplied grayscale");
        check(!g8.equals(g8b), "3-arg (no alpha) != 4-arg (with alpha)");
        check(!g8.equals(g16), "8-bit gray != 16-bit gray");
    }

    // ---- Part 3: createIndexed matrix (probed) ----
    static void testCreateIndexedMatrix() {
        ImageTypeSpecifier idx1 = ImageTypeSpecifier.createIndexed(
            new byte[]{0x10, 0x20}, new byte[]{0x30, 0x40}, new byte[]{0x50, 0x60},
            new byte[]{0x00, (byte) 0xFF}, 1, DataBuffer.TYPE_BYTE);
        check(idx1.getBufferedImageType() == BufferedImage.TYPE_BYTE_BINARY, "indexed 1-bit -> BYTE_BINARY");
        check(idx1.getNumComponents() == 4, "indexed with alpha LUT 4 components");
        check(idx1.getNumBands() == 1, "indexed 1 band");
        check(idx1.getBitsPerBand(0) == 1, "indexed 1-bit depth");
        ImageTypeSpecifier idx4 = ImageTypeSpecifier.createIndexed(
            new byte[16], new byte[16], new byte[16], new byte[16], 4, DataBuffer.TYPE_BYTE);
        check(idx4.getBufferedImageType() == BufferedImage.TYPE_BYTE_BINARY, "indexed 4-bit -> BYTE_BINARY");
        check(idx4.getBitsPerBand(0) == 4, "indexed 4-bit depth");
        ImageTypeSpecifier idx8 = ImageTypeSpecifier.createIndexed(
            new byte[256], new byte[256], new byte[256], new byte[256], 8, DataBuffer.TYPE_BYTE);
        check(idx8.getBufferedImageType() == BufferedImage.TYPE_BYTE_INDEXED, "indexed 8-bit -> BYTE_INDEXED");
        check(idx8.getBitsPerBand(0) == 8, "indexed 8-bit depth");
        check(idx8.getNumComponents() == 4, "indexed 8-bit with alpha 4 components");
        // null alpha LUT means opaque palette
        ImageTypeSpecifier noA = ImageTypeSpecifier.createIndexed(
            new byte[]{0x10, 0x20}, new byte[]{0x30, 0x40}, new byte[]{0x50, 0x60},
            null, 1, DataBuffer.TYPE_BYTE);
        check(noA.getNumComponents() == 3, "indexed without alpha LUT 3 components");
        check(noA.getNumBands() == 1, "indexed no-alpha 1 band");
        check(!noA.getColorModel().hasAlpha(), "indexed no-alpha ColorModel");
        check(idx1.createBufferedImage(3, 2) != null, "indexed createBufferedImage works");
        check(!idx1.equals(noA), "indexed alpha != indexed opaque");
    }

    // ---- Part 3: every BufferedImage.TYPE_* round-trips (probed 13/13) ----
    static void testFullBufferedTypeMatrix() {
        int[] types = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB,
                       BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR,
                       BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
                       BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB,
                       BufferedImage.TYPE_USHORT_555_RGB, BufferedImage.TYPE_BYTE_GRAY,
                       BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY,
                       BufferedImage.TYPE_BYTE_INDEXED};
        int[] comps = {3, 4, 4, 3, 3, 4, 4, 3, 3, 1, 1, 3, 3};
        int[] bands = {3, 4, 4, 3, 3, 4, 4, 3, 3, 1, 1, 1, 1};
        int[] bpb = {8, 8, 8, 8, 8, 8, 8, 5, 5, 8, 16, 1, 8};
        for (int i = 0; i < types.length; i++) {
            ImageTypeSpecifier fromType = ImageTypeSpecifier.createFromBufferedImageType(types[i]);
            check(fromType.getBufferedImageType() == types[i],
                  "createFromBufferedImageType(" + types[i] + ") type round-trip");
            check(fromType.getNumComponents() == comps[i], "type " + types[i] + " components");
            check(fromType.getNumBands() == bands[i], "type " + types[i] + " bands");
            check(fromType.getBitsPerBand(0) == bpb[i], "type " + types[i] + " bits per band");
            check(fromType.getColorModel() != null, "type " + types[i] + " color model non-null");
            check(fromType.getSampleModel() != null, "type " + types[i] + " sample model non-null");
            BufferedImage img = new BufferedImage(3, 3, types[i]);
            ImageTypeSpecifier fromImg = ImageTypeSpecifier.createFromRenderedImage(img);
            check(fromImg.getBufferedImageType() == types[i],
                  "createFromRenderedImage type " + types[i] + " type preserved");
            if (types[i] == BufferedImage.TYPE_BYTE_INDEXED) {
                // JDK-9+ changed the default TYPE_BYTE_INDEXED palette so the image-built
                // IndexColorModel differs from the canonical one (probed: equals false here,
                // while JDK 8 reported true)
                check(!fromImg.equals(fromType),
                      "type 13 image palette differs from canonical (JDK-9+ behavior)");
            } else {
                check(fromImg.equals(fromType), "type " + types[i] + " image-based equals type-based");
                check(fromImg.hashCode() == fromType.hashCode(), "type " + types[i] + " hashCodes agree");
            }
            BufferedImage re = fromImg.createBufferedImage(3, 3);
            check(re != null && re.getWidth() == 3 && re.getHeight() == 3,
                  "type " + types[i] + " createBufferedImage dims");
            check(re.getType() == types[i], "type " + types[i] + " createBufferedImage type round-trip");
        }
    }

    // ---- Part 3: equals/hashCode across independent instances ----
    static void testEqualsHashCodeMatrix() {
        ImageTypeSpecifier a = ImageTypeSpecifier.createFromRenderedImage(
            new BufferedImage(4, 4, BufferedImage.TYPE_3BYTE_BGR));
        ImageTypeSpecifier b = ImageTypeSpecifier.createFromRenderedImage(
            new BufferedImage(9, 7, BufferedImage.TYPE_3BYTE_BGR));
        check(a.equals(b), "equals ignores source image dimensions");
        check(a.hashCode() == b.hashCode(), "equal specifiers share hashCode");
        ImageTypeSpecifier c = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR);
        check(a.equals(c), "equals across factory kinds");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("not a specifier"), "equals(non-specifier) false");
        ImageTypeSpecifier d = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_BGR);
        check(!a.equals(d), "3BYTE_BGR != INT_BGR");
        check(!a.equals(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)),
              "BGR != RGB family");
        // grayscale vs indexed with same band count stay distinct
        ImageTypeSpecifier gray = ImageTypeSpecifier.createGrayscale(8, DataBuffer.TYPE_BYTE, false);
        ImageTypeSpecifier idx = ImageTypeSpecifier.createIndexed(
            new byte[256], new byte[256], new byte[256], null, 8, DataBuffer.TYPE_BYTE);
        check(!gray.equals(idx), "grayscale != opaque indexed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryNegatives();
        testCreatePackedMatrix();
        testInterleavedAndBanded();
        testCreateGrayscaleMatrix();
        testCreateIndexedMatrix();
        testFullBufferedTypeMatrix();
        testEqualsHashCodeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
