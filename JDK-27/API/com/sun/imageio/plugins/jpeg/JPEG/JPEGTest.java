/*
 * @test
 * @summary com.sun.imageio.plugins.jpeg.JPEG conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.jpeg:+open
 * @run main JPEGTest
 */

import java.util.*;
import java.lang.reflect.*;

public class JPEGTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.jpeg.JPEG", false, JPEGTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.jpeg.JPEG".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(ifaces(c)), "interfaces");
        check("".equals(methodNames(c)), "public/protected methods");
        check("ADOBE_IMPOSSIBLE;ADOBE_UNKNOWN;ADOBE_YCC;ADOBE_YCCK;APP0;APP1;APP10;APP11;APP12;APP13;APP14;APP15;APP2;APP3;APP4;APP5;APP6;APP7;APP8;APP9;COM;DAC;DEFAULT_QUALITY;DENSITY_UNIT_ASPECT_RATIO;DENSITY_UNIT_DOTS_CM;DENSITY_UNIT_DOTS_INCH;DHP;DHT;DNL;DQT;DRI;EOI;EXP;JCS_CMYK;JCS_GRAYSCALE;JCS_RGB;JCS_UNKNOWN;JCS_YCCK;JCS_YCbCr;JPG;NUM_DENSITY_UNIT;NUM_JCS_CODES;RESTART_RANGE;RST0;RST1;RST2;RST3;RST4;RST5;RST6;RST7;SOF0;SOF1;SOF10;SOF11;SOF13;SOF14;SOF15;SOF2;SOF3;SOF5;SOF6;SOF7;SOF9;SOI;SOS;TEM;nativeImageMetadataFormatClassName;nativeImageMetadataFormatName;nativeStreamMetadataFormatClassName;nativeStreamMetadataFormatName;vendor;version".equals(fieldNames(c)), "public/protected fields");
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
