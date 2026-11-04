/*
 * @test
 * @summary com.sun.imageio.plugins.bmp.BMPMetadata conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.bmp:+open
 * @run main BMPMetadataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BMPMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.bmp.BMPMetadata", false, BMPMetadataTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.bmp.BMPMetadata".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "javax.imageio.metadata.IIOMetadata".equals(c.getSuperclass().getName()), "superclass");
        check("com.sun.imageio.plugins.bmp.BMPConstants".equals(ifaces(c)), "interfaces");
        check("getAsTree;getStandardChromaNode;getStandardCompressionNode;getStandardDataNode;getStandardDimensionNode;isReadOnly;mergeTree;reset;setFromTree".equals(methodNames(c)), "public/protected methods");
        check("alphaMask;bitsPerPixel;blue;blueMask;blueX;blueY;blueZ;bmpVersion;colorSpace;colorsImportant;colorsUsed;compression;gammaBlue;gammaGreen;gammaRed;green;greenMask;greenX;greenY;greenZ;height;imageSize;intent;nativeMetadataFormatName;palette;paletteSize;red;redMask;redX;redY;redZ;width;xPixelsPerMeter;yPixelsPerMeter".equals(fieldNames(c)), "public/protected fields");
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
