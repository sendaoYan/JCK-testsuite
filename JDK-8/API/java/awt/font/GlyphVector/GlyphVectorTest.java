/*
 * @test
 * @summary java.awt.font.GlyphVector conformance: abstract class reflection.
 * @run main GlyphVectorTest
 */

public class GlyphVectorTest {
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
        Class<?> c = Class.forName("java.awt.font.GlyphVector");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "getFontRenderContext"), "method getFontRenderContext");
        check(hasMethod(c, "performDefaultLayout"), "method performDefaultLayout");
        check(hasMethod(c, "getNumGlyphs"), "method getNumGlyphs");
        check(hasMethod(c, "getGlyphCode"), "method getGlyphCode");
        check(hasMethod(c, "getGlyphCodes"), "method getGlyphCodes");
        check(hasMethod(c, "getGlyphCharIndex"), "method getGlyphCharIndex");
        check(hasMethod(c, "getGlyphCharIndices"), "method getGlyphCharIndices");
        check(hasMethod(c, "getLogicalBounds"), "method getLogicalBounds");
        check(hasMethod(c, "getVisualBounds"), "method getVisualBounds");
        check(hasMethod(c, "getPixelBounds"), "method getPixelBounds");
        check(hasMethod(c, "getOutline"), "method getOutline");
        check(hasMethod(c, "getGlyphOutline"), "method getGlyphOutline");
        check(hasMethod(c, "getGlyphPosition"), "method getGlyphPosition");
        check(hasMethod(c, "setGlyphPosition"), "method setGlyphPosition");
        check(hasMethod(c, "getGlyphTransform"), "method getGlyphTransform");
        check(hasMethod(c, "setGlyphTransform"), "method setGlyphTransform");
        check(hasMethod(c, "getLayoutFlags"), "method getLayoutFlags");
        check(hasMethod(c, "getGlyphPositions"), "method getGlyphPositions");
        check(hasMethod(c, "getGlyphLogicalBounds"), "method getGlyphLogicalBounds");
        check(hasMethod(c, "getGlyphVisualBounds"), "method getGlyphVisualBounds");
        check(hasMethod(c, "getGlyphPixelBounds"), "method getGlyphPixelBounds");
        check(hasMethod(c, "getGlyphMetrics"), "method getGlyphMetrics");
        check(hasMethod(c, "getGlyphJustificationInfo"), "method getGlyphJustificationInfo");
        check(hasMethod(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
