/*
 * @test
 * @summary java.awt.FontMetrics conformance: abstract class reflection.
 * @run main FontMetricsTest
 */

public class FontMetricsTest {
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
        Class<?> c = Class.forName("java.awt.FontMetrics");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "getFontRenderContext"), "method getFontRenderContext");
        check(hasMethod(c, "getLeading"), "method getLeading");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMaxAscent"), "method getMaxAscent");
        check(hasMethod(c, "getMaxDescent"), "method getMaxDescent");
        check(hasMethod(c, "getMaxDecent"), "method getMaxDecent");
        check(hasMethod(c, "getMaxAdvance"), "method getMaxAdvance");
        check(hasMethod(c, "charWidth"), "method charWidth");
        check(hasMethod(c, "stringWidth"), "method stringWidth");
        check(hasMethod(c, "charsWidth"), "method charsWidth");
        check(hasMethod(c, "bytesWidth"), "method bytesWidth");
        check(hasMethod(c, "getWidths"), "method getWidths");
        check(hasMethod(c, "hasUniformLineMetrics"), "method hasUniformLineMetrics");
        check(hasMethod(c, "getLineMetrics"), "method getLineMetrics");
        check(hasMethod(c, "getStringBounds"), "method getStringBounds");
        check(hasMethod(c, "getMaxCharBounds"), "method getMaxCharBounds");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
