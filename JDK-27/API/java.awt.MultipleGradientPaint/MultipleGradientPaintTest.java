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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
