/*
 * @test
 * @summary com.sun.imageio.plugins.tiff.TIFFDecompressor conformance: internal abstract class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.tiff:+open
 * @run main TIFFDecompressorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TIFFDecompressorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.tiff.TIFFDecompressor", false, TIFFDecompressorTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.tiff.TIFFDecompressor".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("beginDecoding;createRawImage;decode;decodeRaw;getRawImageType;getRawImageTypeSpecifier;setActiveSrcHeight;setActiveSrcMinX;setActiveSrcMinY;setActiveSrcWidth;setBitsPerSample;setByteCount;setColorConverter;setColorMap;setCompression;setDestinationBands;setDstHeight;setDstMinX;setDstMinY;setDstWidth;setDstXOffset;setDstYOffset;setExtraSamples;setImage;setMetadata;setOffset;setPhotometricInterpretation;setPlanar;setPlanarBand;setReader;setSampleFormat;setSamplesPerPixel;setSourceBands;setSourceXOffset;setSourceYOffset;setSrcHeight;setSrcMinX;setSrcMinY;setSrcWidth;setStream;setSubsampleX;setSubsampleY".equals(methodNames(c)), "public/protected methods");
        check("activeSrcHeight;activeSrcMinX;activeSrcMinY;activeSrcWidth;bitsPerSample;byteCount;colorConverter;colorMap;compression;destinationBands;dstHeight;dstMinX;dstMinY;dstWidth;dstXOffset;dstYOffset;extraSamples;image;metadata;offset;photometricInterpretation;planar;planarBand;rawImage;reader;sampleFormat;samplesPerPixel;sourceBands;sourceXOffset;sourceYOffset;srcHeight;srcMinX;srcMinY;srcWidth;stream;subsampleX;subsampleY".equals(fieldNames(c)), "public/protected fields");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String ifaces(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return String.join(";", s);
    }

    static String methodNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return String.join(";", s);
    }

    static String fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            int fm = f.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(f.getName());
        }
        return String.join(";", s);
    }

    static String enumConstants(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
        return String.join(";", s);
    }
}
