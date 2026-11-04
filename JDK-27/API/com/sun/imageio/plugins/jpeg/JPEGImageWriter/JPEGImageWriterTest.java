/*
 * @test
 * @summary com.sun.imageio.plugins.jpeg.JPEGImageWriter conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.jpeg:+open
 * @run main JPEGImageWriterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class JPEGImageWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageWriter", false, JPEGImageWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.jpeg.JPEGImageWriter".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "javax.imageio.ImageWriter".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("abort;canWriteRasters;canWriteSequence;clearAbortRequest;convertImageMetadata;convertStreamMetadata;dispose;endWriteSequence;getDefaultImageMetadata;getDefaultStreamMetadata;getDefaultWriteParam;getNumThumbnailsSupported;getPreferredThumbnailSizes;prepareWriteSequence;reset;setOutput;write;writeToSequence".equals(methodNames(c)), "public/protected methods");
        check("WARNING_DEST_IGNORED;WARNING_DEST_METADATA_ADOBE_MISMATCH;WARNING_DEST_METADATA_COMP_MISMATCH;WARNING_DEST_METADATA_JFIF_MISMATCH;WARNING_FORCING_JFIF;WARNING_IGNORING_THUMBS;WARNING_ILLEGAL_THUMBNAIL;WARNING_IMAGE_METADATA_ADOBE_MISMATCH;WARNING_IMAGE_METADATA_JFIF_MISMATCH;WARNING_METADATA_ADJUSTED_FOR_THUMB;WARNING_METADATA_NOT_JPEG_FOR_RASTER;WARNING_NO_BANDS_ON_INDEXED;WARNING_NO_GRAY_THUMB_AS_INDEXED;WARNING_NO_RGB_THUMB_AS_INDEXED;WARNING_STREAM_METADATA_IGNORED;WARNING_THUMB_CLIPPED".equals(fieldNames(c)), "public/protected fields");
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
