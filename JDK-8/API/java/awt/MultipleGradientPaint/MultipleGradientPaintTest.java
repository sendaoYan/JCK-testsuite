/*
 * @test
 * @summary java.awt.MultipleGradientPaint conformance: abstract class reflection.
 * @run main MultipleGradientPaintTest
 */

public class MultipleGradientPaintTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.MultipleGradientPaint");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Paint"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFractions"), "method getFractions");
        check(hasMethod(c, "getColors"), "method getColors");
        check(hasMethod(c, "getCycleMethod"), "method getCycleMethod");
        check(hasMethod(c, "getColorSpace"), "method getColorSpace");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
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
