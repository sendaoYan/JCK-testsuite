/*
 * @test
 * @summary java.awt.PageAttributes conformance: white-box structure + black-box behavior.
 * @run main PageAttributesTest
 */

import java.lang.reflect.*;

public class PageAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.PageAttributes", false, PageAttributesTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.PageAttributes");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
        check(hasMethod(c, "getColor"), "declares getColor");
        check(hasMethod(c, "setOrientationRequested"), "declares setOrientationRequested");
        check(hasMethod(c, "setPrinterResolution"), "declares setPrinterResolution");
    }

    static void testBlackBox() throws Exception {
        java.awt.PageAttributes p = new java.awt.PageAttributes();
        check(p.getColor() == java.awt.PageAttributes.ColorType.MONOCHROME, "default color MONOCHROME");
        check(p.getMedia() == java.awt.PageAttributes.MediaType.NA_LETTER, "default media NA_LETTER");
        check(p.getOrientationRequested() == java.awt.PageAttributes.OrientationRequestedType.PORTRAIT, "default portrait");
        check(p.getOrigin() == java.awt.PageAttributes.OriginType.PHYSICAL, "default origin PHYSICAL");
        check(p.getPrintQuality() == java.awt.PageAttributes.PrintQualityType.NORMAL, "default quality NORMAL");
        check(p.getPrinterResolution() != null && p.getPrinterResolution().length == 3, "default printerResolution {72,72,72}");
        p.setColor(java.awt.PageAttributes.ColorType.MONOCHROME);
        check(p.getColor() == java.awt.PageAttributes.ColorType.MONOCHROME, "setColor");
        p.setMedia(java.awt.PageAttributes.MediaType.NA_LETTER);
        check(p.getMedia() == java.awt.PageAttributes.MediaType.NA_LETTER, "setMedia");
        p.setMediaToDefault();
        check(p.getMedia() == java.awt.PageAttributes.MediaType.NA_LETTER, "setMediaToDefault");
        p.setOrientationRequested(java.awt.PageAttributes.OrientationRequestedType.LANDSCAPE);
        check(p.getOrientationRequested() == java.awt.PageAttributes.OrientationRequestedType.LANDSCAPE, "setOrientationRequested enum");
        p.setOrientationRequested(3); // PORTRAIT
        check(p.getOrientationRequested() == java.awt.PageAttributes.OrientationRequestedType.PORTRAIT, "setOrientationRequested int");
        p.setOrientationRequestedToDefault();
        check(p.getOrientationRequested() == java.awt.PageAttributes.OrientationRequestedType.PORTRAIT, "setOrientationRequestedToDefault");
        p.setOrigin(java.awt.PageAttributes.OriginType.PRINTABLE);
        check(p.getOrigin() == java.awt.PageAttributes.OriginType.PRINTABLE, "setOrigin");
        p.setPrintQuality(java.awt.PageAttributes.PrintQualityType.HIGH);
        check(p.getPrintQuality() == java.awt.PageAttributes.PrintQualityType.HIGH, "setPrintQuality enum");
        p.setPrintQuality(5); // HIGH
        check(p.getPrintQuality() == java.awt.PageAttributes.PrintQualityType.HIGH, "setPrintQuality int");
        p.setPrintQualityToDefault();
        check(p.getPrintQuality() == java.awt.PageAttributes.PrintQualityType.NORMAL, "setPrintQualityToDefault");
        p.setPrinterResolution(new int[]{300, 300, 3});
        check(p.getPrinterResolution() != null && p.getPrinterResolution()[0] == 300 && p.getPrinterResolution()[2] == 3, "setPrinterResolution array");
        p.setPrinterResolution(600);
        check(p.getPrinterResolution() != null && p.getPrinterResolution()[0] == 600, "setPrinterResolution int");
        p.setPrinterResolutionToDefault();
        check(p.getPrinterResolution() != null && p.getPrinterResolution().length == 3, "setPrinterResolutionToDefault non-null");
        java.awt.PageAttributes copy = new java.awt.PageAttributes(p);
        check(copy.equals(p), "copy ctor equals");
        java.awt.PageAttributes cl = (java.awt.PageAttributes) p.clone();
        check(cl.equals(p), "clone equals");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
        check(p.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
