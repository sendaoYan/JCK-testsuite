/*
 * @test
 * @summary javax.imageio.ImageTypeSpecifier conformance: white-box structure + black-box behavior.
 * @run main ImageTypeSpecifierTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.lang.reflect.Modifier;
import javax.imageio.ImageTypeSpecifier;

public class ImageTypeSpecifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
