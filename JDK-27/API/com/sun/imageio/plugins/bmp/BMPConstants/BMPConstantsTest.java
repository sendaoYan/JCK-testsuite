/*
 * @test
 * @summary com.sun.imageio.plugins.bmp.BMPConstants conformance: internal interface reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.bmp:+open
 * @run main BMPConstantsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BMPConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.bmp.BMPConstants", false, BMPConstantsTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.bmp.BMPConstants".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("".equals(ifaces(c)), "interfaces");
        check("".equals(methodNames(c)), "public/protected methods");
        check("BI_BITFIELDS;BI_JPEG;BI_PNG;BI_RGB;BI_RLE4;BI_RLE8;LCS_CALIBRATED_RGB;LCS_WINDOWS_COLOR_SPACE;LCS_sRGB;PROFILE_EMBEDDED;PROFILE_LINKED;VERSION_2;VERSION_3;VERSION_3_EXT;VERSION_3_NT;VERSION_4;VERSION_5".equals(fieldNames(c)), "public/protected fields");
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
