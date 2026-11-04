/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.NA conformance: media size constants.
 * @run main NATest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class NATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "NA");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_LETTER = c.getDeclaredField("LETTER");
        check(java.lang.reflect.Modifier.isStatic(f_LETTER.getModifiers()), "LETTER is static");
        check(f_LETTER.getType() == javax.print.attribute.standard.MediaSize.class, "LETTER type MediaSize");
        check(f_LETTER.get(null) != null, "LETTER non-null");
        java.lang.reflect.Field f_LEGAL = c.getDeclaredField("LEGAL");
        check(java.lang.reflect.Modifier.isStatic(f_LEGAL.getModifiers()), "LEGAL is static");
        check(f_LEGAL.getType() == javax.print.attribute.standard.MediaSize.class, "LEGAL type MediaSize");
        check(f_LEGAL.get(null) != null, "LEGAL non-null");
        java.lang.reflect.Field f_NA_5X7 = c.getDeclaredField("NA_5X7");
        check(java.lang.reflect.Modifier.isStatic(f_NA_5X7.getModifiers()), "NA_5X7 is static");
        check(f_NA_5X7.getType() == javax.print.attribute.standard.MediaSize.class, "NA_5X7 type MediaSize");
        check(f_NA_5X7.get(null) != null, "NA_5X7 non-null");
        java.lang.reflect.Field f_NA_8X10 = c.getDeclaredField("NA_8X10");
        check(java.lang.reflect.Modifier.isStatic(f_NA_8X10.getModifiers()), "NA_8X10 is static");
        check(f_NA_8X10.getType() == javax.print.attribute.standard.MediaSize.class, "NA_8X10 type MediaSize");
        check(f_NA_8X10.get(null) != null, "NA_8X10 non-null");
        java.lang.reflect.Field f_NA_NUMBER_9_ENVELOPE = c.getDeclaredField("NA_NUMBER_9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_9_ENVELOPE.getModifiers()), "NA_NUMBER_9_ENVELOPE is static");
        check(f_NA_NUMBER_9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_9_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_9_ENVELOPE.get(null) != null, "NA_NUMBER_9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_10_ENVELOPE = c.getDeclaredField("NA_NUMBER_10_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_10_ENVELOPE.getModifiers()), "NA_NUMBER_10_ENVELOPE is static");
        check(f_NA_NUMBER_10_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_10_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_10_ENVELOPE.get(null) != null, "NA_NUMBER_10_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_11_ENVELOPE = c.getDeclaredField("NA_NUMBER_11_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_11_ENVELOPE.getModifiers()), "NA_NUMBER_11_ENVELOPE is static");
        check(f_NA_NUMBER_11_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_11_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_11_ENVELOPE.get(null) != null, "NA_NUMBER_11_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_12_ENVELOPE = c.getDeclaredField("NA_NUMBER_12_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_12_ENVELOPE.getModifiers()), "NA_NUMBER_12_ENVELOPE is static");
        check(f_NA_NUMBER_12_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_12_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_12_ENVELOPE.get(null) != null, "NA_NUMBER_12_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_NUMBER_14_ENVELOPE = c.getDeclaredField("NA_NUMBER_14_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_NUMBER_14_ENVELOPE.getModifiers()), "NA_NUMBER_14_ENVELOPE is static");
        check(f_NA_NUMBER_14_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_NUMBER_14_ENVELOPE type MediaSize");
        check(f_NA_NUMBER_14_ENVELOPE.get(null) != null, "NA_NUMBER_14_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_6X9_ENVELOPE = c.getDeclaredField("NA_6X9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_6X9_ENVELOPE.getModifiers()), "NA_6X9_ENVELOPE is static");
        check(f_NA_6X9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_6X9_ENVELOPE type MediaSize");
        check(f_NA_6X9_ENVELOPE.get(null) != null, "NA_6X9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_7X9_ENVELOPE = c.getDeclaredField("NA_7X9_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_7X9_ENVELOPE.getModifiers()), "NA_7X9_ENVELOPE is static");
        check(f_NA_7X9_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_7X9_ENVELOPE type MediaSize");
        check(f_NA_7X9_ENVELOPE.get(null) != null, "NA_7X9_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_9x11_ENVELOPE = c.getDeclaredField("NA_9x11_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_9x11_ENVELOPE.getModifiers()), "NA_9x11_ENVELOPE is static");
        check(f_NA_9x11_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_9x11_ENVELOPE type MediaSize");
        check(f_NA_9x11_ENVELOPE.get(null) != null, "NA_9x11_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_9x12_ENVELOPE = c.getDeclaredField("NA_9x12_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_9x12_ENVELOPE.getModifiers()), "NA_9x12_ENVELOPE is static");
        check(f_NA_9x12_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_9x12_ENVELOPE type MediaSize");
        check(f_NA_9x12_ENVELOPE.get(null) != null, "NA_9x12_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10x13_ENVELOPE = c.getDeclaredField("NA_10x13_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10x13_ENVELOPE.getModifiers()), "NA_10x13_ENVELOPE is static");
        check(f_NA_10x13_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10x13_ENVELOPE type MediaSize");
        check(f_NA_10x13_ENVELOPE.get(null) != null, "NA_10x13_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10x14_ENVELOPE = c.getDeclaredField("NA_10x14_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10x14_ENVELOPE.getModifiers()), "NA_10x14_ENVELOPE is static");
        check(f_NA_10x14_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10x14_ENVELOPE type MediaSize");
        check(f_NA_10x14_ENVELOPE.get(null) != null, "NA_10x14_ENVELOPE non-null");
        java.lang.reflect.Field f_NA_10X15_ENVELOPE = c.getDeclaredField("NA_10X15_ENVELOPE");
        check(java.lang.reflect.Modifier.isStatic(f_NA_10X15_ENVELOPE.getModifiers()), "NA_10X15_ENVELOPE is static");
        check(f_NA_10X15_ENVELOPE.getType() == javax.print.attribute.standard.MediaSize.class, "NA_10X15_ENVELOPE type MediaSize");
        check(f_NA_10X15_ENVELOPE.get(null) != null, "NA_10X15_ENVELOPE non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
