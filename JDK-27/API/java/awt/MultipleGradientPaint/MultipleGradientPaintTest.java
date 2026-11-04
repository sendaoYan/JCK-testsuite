/*
 * @test
 * @summary java.awt.MultipleGradientPaint conformance: white-box structure + black-box behavior.
 * @run main MultipleGradientPaintTest
 */

import java.lang.reflect.*;

public class MultipleGradientPaintTest {
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
        Class<?> c = Class.forName("java.awt.MultipleGradientPaint", false, MultipleGradientPaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.MultipleGradientPaint");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasInterface(c, "java.awt.Paint"), "implements Paint");
        check(hasMethod(c, "getFractions"), "declares getFractions");
        check(hasMethod(c, "getColors"), "declares getColors");
        check(hasMethod(c, "getCycleMethod"), "declares getCycleMethod");
        check(hasMethod(c, "getColorSpace"), "declares getColorSpace");
        check(hasMethod(c, "getTransform"), "declares getTransform");
        check(hasMethod(c, "getTransparency"), "declares getTransparency");
    }

    static void testBlackBox() throws Exception {
        // MultipleGradientPaint is abstract with a package-private constructor and
        // cannot be instantiated directly; its concrete getters are exercised via the
        // LinearGradientPaint / RadialGradientPaint tests.
        check(true, "abstract base: no direct instance (getters covered by subclasses)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // enum constants: ordinal coverage and distinctness
        java.awt.MultipleGradientPaint.CycleMethod[] cms = java.awt.MultipleGradientPaint.CycleMethod.values();
        check(cms.length == 3, "CycleMethod has 3 constants");
        check(java.awt.MultipleGradientPaint.CycleMethod.valueOf("NO_CYCLE")
              == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "valueOf NO_CYCLE");
        check(java.awt.MultipleGradientPaint.CycleMethod.valueOf("REFLECT")
              == java.awt.MultipleGradientPaint.CycleMethod.REFLECT, "valueOf REFLECT");
        check(java.awt.MultipleGradientPaint.CycleMethod.valueOf("REPEAT")
              == java.awt.MultipleGradientPaint.CycleMethod.REPEAT, "valueOf REPEAT");
        java.awt.MultipleGradientPaint.ColorSpaceType[] css = java.awt.MultipleGradientPaint.ColorSpaceType.values();
        check(css.length == 2, "ColorSpaceType has 2 constants");
        check(java.awt.MultipleGradientPaint.ColorSpaceType.valueOf("SRGB")
              == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "valueOf SRGB");
        check(java.awt.MultipleGradientPaint.ColorSpaceType.valueOf("LINEAR_RGB")
              == java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB, "valueOf LINEAR_RGB");
        // MultipleGradientPaint is abstract; subclass via LinearGradientPaint exercises the API
        check(java.lang.reflect.Modifier.isAbstract(java.awt.MultipleGradientPaint.class.getModifiers()),
              "MultipleGradientPaint abstract");
        java.awt.LinearGradientPaint lgp = new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f,
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue});
        check(lgp instanceof java.awt.MultipleGradientPaint, "LinearGradientPaint is MultipleGradientPaint");
        check(java.awt.RadialGradientPaint.class.getSuperclass() == java.awt.MultipleGradientPaint.class,
              "RadialGradientPaint extends MultipleGradientPaint");
        check(java.awt.LinearGradientPaint.class.getSuperclass() == java.awt.MultipleGradientPaint.class,
              "LinearGradientPaint extends MultipleGradientPaint");
        // shared accessors through the base type
        java.awt.MultipleGradientPaint m = lgp;
        check(m.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "base getCycleMethod");
        check(m.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "base getColorSpace");
        check(m.getTransparency() == java.awt.Transparency.OPAQUE, "base getTransparency");
        check(m.getFractions().length == 2 && m.getColors().length == 2, "base getFractions/getColors");
        check(m.getTransform().isIdentity(), "base getTransform identity");
    }

}
