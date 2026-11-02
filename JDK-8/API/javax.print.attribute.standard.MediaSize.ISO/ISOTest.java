/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.ISO conformance: media size constants.
 * @run main ISOTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class ISOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "ISO");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_A0 = c.getDeclaredField("A0");
        check(java.lang.reflect.Modifier.isStatic(f_A0.getModifiers()), "A0 is static");
        check(f_A0.getType() == javax.print.attribute.standard.MediaSize.class, "A0 type MediaSize");
        check(f_A0.get(null) != null, "A0 non-null");
        java.lang.reflect.Field f_A1 = c.getDeclaredField("A1");
        check(java.lang.reflect.Modifier.isStatic(f_A1.getModifiers()), "A1 is static");
        check(f_A1.getType() == javax.print.attribute.standard.MediaSize.class, "A1 type MediaSize");
        check(f_A1.get(null) != null, "A1 non-null");
        java.lang.reflect.Field f_A2 = c.getDeclaredField("A2");
        check(java.lang.reflect.Modifier.isStatic(f_A2.getModifiers()), "A2 is static");
        check(f_A2.getType() == javax.print.attribute.standard.MediaSize.class, "A2 type MediaSize");
        check(f_A2.get(null) != null, "A2 non-null");
        java.lang.reflect.Field f_A3 = c.getDeclaredField("A3");
        check(java.lang.reflect.Modifier.isStatic(f_A3.getModifiers()), "A3 is static");
        check(f_A3.getType() == javax.print.attribute.standard.MediaSize.class, "A3 type MediaSize");
        check(f_A3.get(null) != null, "A3 non-null");
        java.lang.reflect.Field f_A4 = c.getDeclaredField("A4");
        check(java.lang.reflect.Modifier.isStatic(f_A4.getModifiers()), "A4 is static");
        check(f_A4.getType() == javax.print.attribute.standard.MediaSize.class, "A4 type MediaSize");
        check(f_A4.get(null) != null, "A4 non-null");
        java.lang.reflect.Field f_A5 = c.getDeclaredField("A5");
        check(java.lang.reflect.Modifier.isStatic(f_A5.getModifiers()), "A5 is static");
        check(f_A5.getType() == javax.print.attribute.standard.MediaSize.class, "A5 type MediaSize");
        check(f_A5.get(null) != null, "A5 non-null");
        java.lang.reflect.Field f_A6 = c.getDeclaredField("A6");
        check(java.lang.reflect.Modifier.isStatic(f_A6.getModifiers()), "A6 is static");
        check(f_A6.getType() == javax.print.attribute.standard.MediaSize.class, "A6 type MediaSize");
        check(f_A6.get(null) != null, "A6 non-null");
        java.lang.reflect.Field f_A7 = c.getDeclaredField("A7");
        check(java.lang.reflect.Modifier.isStatic(f_A7.getModifiers()), "A7 is static");
        check(f_A7.getType() == javax.print.attribute.standard.MediaSize.class, "A7 type MediaSize");
        check(f_A7.get(null) != null, "A7 non-null");
        java.lang.reflect.Field f_A8 = c.getDeclaredField("A8");
        check(java.lang.reflect.Modifier.isStatic(f_A8.getModifiers()), "A8 is static");
        check(f_A8.getType() == javax.print.attribute.standard.MediaSize.class, "A8 type MediaSize");
        check(f_A8.get(null) != null, "A8 non-null");
        java.lang.reflect.Field f_A9 = c.getDeclaredField("A9");
        check(java.lang.reflect.Modifier.isStatic(f_A9.getModifiers()), "A9 is static");
        check(f_A9.getType() == javax.print.attribute.standard.MediaSize.class, "A9 type MediaSize");
        check(f_A9.get(null) != null, "A9 non-null");
        java.lang.reflect.Field f_A10 = c.getDeclaredField("A10");
        check(java.lang.reflect.Modifier.isStatic(f_A10.getModifiers()), "A10 is static");
        check(f_A10.getType() == javax.print.attribute.standard.MediaSize.class, "A10 type MediaSize");
        check(f_A10.get(null) != null, "A10 non-null");
        java.lang.reflect.Field f_B0 = c.getDeclaredField("B0");
        check(java.lang.reflect.Modifier.isStatic(f_B0.getModifiers()), "B0 is static");
        check(f_B0.getType() == javax.print.attribute.standard.MediaSize.class, "B0 type MediaSize");
        check(f_B0.get(null) != null, "B0 non-null");
        java.lang.reflect.Field f_B1 = c.getDeclaredField("B1");
        check(java.lang.reflect.Modifier.isStatic(f_B1.getModifiers()), "B1 is static");
        check(f_B1.getType() == javax.print.attribute.standard.MediaSize.class, "B1 type MediaSize");
        check(f_B1.get(null) != null, "B1 non-null");
        java.lang.reflect.Field f_B2 = c.getDeclaredField("B2");
        check(java.lang.reflect.Modifier.isStatic(f_B2.getModifiers()), "B2 is static");
        check(f_B2.getType() == javax.print.attribute.standard.MediaSize.class, "B2 type MediaSize");
        check(f_B2.get(null) != null, "B2 non-null");
        java.lang.reflect.Field f_B3 = c.getDeclaredField("B3");
        check(java.lang.reflect.Modifier.isStatic(f_B3.getModifiers()), "B3 is static");
        check(f_B3.getType() == javax.print.attribute.standard.MediaSize.class, "B3 type MediaSize");
        check(f_B3.get(null) != null, "B3 non-null");
        java.lang.reflect.Field f_B4 = c.getDeclaredField("B4");
        check(java.lang.reflect.Modifier.isStatic(f_B4.getModifiers()), "B4 is static");
        check(f_B4.getType() == javax.print.attribute.standard.MediaSize.class, "B4 type MediaSize");
        check(f_B4.get(null) != null, "B4 non-null");
        java.lang.reflect.Field f_B5 = c.getDeclaredField("B5");
        check(java.lang.reflect.Modifier.isStatic(f_B5.getModifiers()), "B5 is static");
        check(f_B5.getType() == javax.print.attribute.standard.MediaSize.class, "B5 type MediaSize");
        check(f_B5.get(null) != null, "B5 non-null");
        java.lang.reflect.Field f_B6 = c.getDeclaredField("B6");
        check(java.lang.reflect.Modifier.isStatic(f_B6.getModifiers()), "B6 is static");
        check(f_B6.getType() == javax.print.attribute.standard.MediaSize.class, "B6 type MediaSize");
        check(f_B6.get(null) != null, "B6 non-null");
        java.lang.reflect.Field f_B7 = c.getDeclaredField("B7");
        check(java.lang.reflect.Modifier.isStatic(f_B7.getModifiers()), "B7 is static");
        check(f_B7.getType() == javax.print.attribute.standard.MediaSize.class, "B7 type MediaSize");
        check(f_B7.get(null) != null, "B7 non-null");
        java.lang.reflect.Field f_B8 = c.getDeclaredField("B8");
        check(java.lang.reflect.Modifier.isStatic(f_B8.getModifiers()), "B8 is static");
        check(f_B8.getType() == javax.print.attribute.standard.MediaSize.class, "B8 type MediaSize");
        check(f_B8.get(null) != null, "B8 non-null");
        java.lang.reflect.Field f_B9 = c.getDeclaredField("B9");
        check(java.lang.reflect.Modifier.isStatic(f_B9.getModifiers()), "B9 is static");
        check(f_B9.getType() == javax.print.attribute.standard.MediaSize.class, "B9 type MediaSize");
        check(f_B9.get(null) != null, "B9 non-null");
        java.lang.reflect.Field f_B10 = c.getDeclaredField("B10");
        check(java.lang.reflect.Modifier.isStatic(f_B10.getModifiers()), "B10 is static");
        check(f_B10.getType() == javax.print.attribute.standard.MediaSize.class, "B10 type MediaSize");
        check(f_B10.get(null) != null, "B10 non-null");
        java.lang.reflect.Field f_C3 = c.getDeclaredField("C3");
        check(java.lang.reflect.Modifier.isStatic(f_C3.getModifiers()), "C3 is static");
        check(f_C3.getType() == javax.print.attribute.standard.MediaSize.class, "C3 type MediaSize");
        check(f_C3.get(null) != null, "C3 non-null");
        java.lang.reflect.Field f_C4 = c.getDeclaredField("C4");
        check(java.lang.reflect.Modifier.isStatic(f_C4.getModifiers()), "C4 is static");
        check(f_C4.getType() == javax.print.attribute.standard.MediaSize.class, "C4 type MediaSize");
        check(f_C4.get(null) != null, "C4 non-null");
        java.lang.reflect.Field f_C5 = c.getDeclaredField("C5");
        check(java.lang.reflect.Modifier.isStatic(f_C5.getModifiers()), "C5 is static");
        check(f_C5.getType() == javax.print.attribute.standard.MediaSize.class, "C5 type MediaSize");
        check(f_C5.get(null) != null, "C5 non-null");
        java.lang.reflect.Field f_C6 = c.getDeclaredField("C6");
        check(java.lang.reflect.Modifier.isStatic(f_C6.getModifiers()), "C6 is static");
        check(f_C6.getType() == javax.print.attribute.standard.MediaSize.class, "C6 type MediaSize");
        check(f_C6.get(null) != null, "C6 non-null");
        java.lang.reflect.Field f_DESIGNATED_LONG = c.getDeclaredField("DESIGNATED_LONG");
        check(java.lang.reflect.Modifier.isStatic(f_DESIGNATED_LONG.getModifiers()), "DESIGNATED_LONG is static");
        check(f_DESIGNATED_LONG.getType() == javax.print.attribute.standard.MediaSize.class, "DESIGNATED_LONG type MediaSize");
        check(f_DESIGNATED_LONG.get(null) != null, "DESIGNATED_LONG non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
