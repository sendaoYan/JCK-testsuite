/*
 * @test
 * @summary java.awt.ImageCapabilities conformance: white-box structure + black-box behavior.
 * @run main ImageCapabilitiesTest
 */

import java.lang.reflect.*;

public class ImageCapabilitiesTest {
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
        Class<?> c = Class.forName("java.awt.ImageCapabilities", false, ImageCapabilitiesTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.ImageCapabilities");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
    }

    static void testBlackBox() throws Exception {
        java.awt.ImageCapabilities acc = new java.awt.ImageCapabilities(true);
        check(acc.isAccelerated(), "accelerated ctor");
        check(!acc.isTrueVolatile(), "not true volatile by default");
        java.awt.ImageCapabilities plain = new java.awt.ImageCapabilities(false);
        check(!plain.isAccelerated(), "plain not accelerated");
        check(plain.isAccelerated() == false, "isAccelerated false");
        java.awt.ImageCapabilities cl = (java.awt.ImageCapabilities) acc.clone();
        check(cl != acc, "clone distinct");
        check(cl.isAccelerated() == acc.isAccelerated(), "clone equal value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCapsContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static void testCapsContract() {
        // ImageCapabilities: boolean round-trip
        java.awt.ImageCapabilities acc = new java.awt.ImageCapabilities(true);
        check(acc.isAccelerated(), "isAccelerated true ctor");
        check(!acc.isTrueVolatile(), "isTrueVolatile false default");
        java.awt.ImageCapabilities notAcc = new java.awt.ImageCapabilities(false);
        check(!notAcc.isAccelerated(), "isAccelerated false ctor");
        // ImageCapabilities does not override equals (identity semantics) - observed
        check(!acc.equals(new java.awt.ImageCapabilities(true)), "no equals override: identity semantics");
        check(!acc.equals(notAcc), "different instances never equal");
        check(!acc.equals(null), "equals(null) false");
        check(!acc.equals("accelerated"), "equals unrelated type false");
        check(acc.toString().length() > 0, "toString non-empty");
        check(!(java.io.Serializable.class.isAssignableFrom(java.awt.ImageCapabilities.class)),
              "ImageCapabilities not Serializable");
        // BufferCapabilities ctor validation: NPE on null caps
        java.awt.ImageCapabilities ic = new java.awt.ImageCapabilities(true);
        // javadoc says NPE but implementation throws IAE - locked by observation
        boolean badCap = false;
        try { new java.awt.BufferCapabilities(null, ic, null); } catch (java.lang.IllegalArgumentException e) { badCap = true; }
        check(badCap, "null front caps -> IAE (observed, javadoc says NPE)");
        badCap = false;
        try { new java.awt.BufferCapabilities(ic, null, null); } catch (java.lang.IllegalArgumentException e) { badCap = true; }
        check(badCap, "null back caps -> IAE (observed, javadoc says NPE)");
        // non-flipping caps
        java.awt.BufferCapabilities bc = new java.awt.BufferCapabilities(ic, ic, null);
        check(!bc.isPageFlipping(), "no flip contents -> isPageFlipping false");
        check(bc.getFlipContents() == null, "getFlipContents null");
        check(bc.getFrontBufferCapabilities() == ic, "front caps identity");
        check(bc.getBackBufferCapabilities() == ic, "back caps identity");
        check(!bc.isFullScreenRequired(), "isFullScreenRequired false");
        check(!bc.isMultiBufferAvailable(), "isMultiBufferAvailable false");
        check(bc.clone() != null && bc.clone() != bc, "clone returns new object");
        // BufferCapabilities does not override equals (identity semantics) - observed
        check(!bc.equals(new java.awt.BufferCapabilities(ic, ic, null)), "no equals override: identity semantics");
        check(!bc.equals(null), "equals(null) false");
        check(bc.equals(bc), "equals self true");
        // flipping with each FlipContents constant
        java.awt.BufferCapabilities.FlipContents[] fc =
                {java.awt.BufferCapabilities.FlipContents.UNDEFINED,
                 java.awt.BufferCapabilities.FlipContents.BACKGROUND,
                 java.awt.BufferCapabilities.FlipContents.PRIOR,
                 java.awt.BufferCapabilities.FlipContents.COPIED};
        String[] fn = {"UNDEFINED","BACKGROUND","PRIOR","COPIED"};
        for (int i = 0; i < fc.length; i++) {
            java.awt.BufferCapabilities b2 = new java.awt.BufferCapabilities(ic, ic, fc[i]);
            check(b2.isPageFlipping(), fn[i] + ": isPageFlipping true");
            check(b2.getFlipContents() == fc[i], fn[i] + ": getFlipContents identity");
        }
        // FlipContents toString is the lowercase name (locked observed)
        check(java.awt.BufferCapabilities.FlipContents.BACKGROUND.toString().equals("background"),
              "FlipContents BACKGROUND toString");
        check(java.awt.BufferCapabilities.FlipContents.COPIED.toString().equals("copied"),
              "FlipContents COPIED toString");
    }
}
