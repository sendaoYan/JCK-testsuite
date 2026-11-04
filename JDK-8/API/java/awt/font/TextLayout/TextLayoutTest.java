/*
 * @test
 * @summary java.awt.font.TextLayout conformance: class reflection.
 * @run main TextLayoutTest
 */

public class TextLayoutTest {
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
        Class<?> c = Class.forName("java.awt.font.TextLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getJustifiedLayout"), "method getJustifiedLayout");
        check(hasMethod(c, "getBaseline"), "method getBaseline");
        check(hasMethod(c, "getBaselineOffsets"), "method getBaselineOffsets");
        check(hasMethod(c, "getAdvance"), "method getAdvance");
        check(hasMethod(c, "getVisibleAdvance"), "method getVisibleAdvance");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getLeading"), "method getLeading");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getPixelBounds"), "method getPixelBounds");
        check(hasMethod(c, "isLeftToRight"), "method isLeftToRight");
        check(hasMethod(c, "isVertical"), "method isVertical");
        check(hasMethod(c, "getCharacterCount"), "method getCharacterCount");
        check(hasMethod(c, "getCaretInfo"), "method getCaretInfo");
        check(hasMethod(c, "getNextRightHit"), "method getNextRightHit");
        check(hasMethod(c, "getNextLeftHit"), "method getNextLeftHit");
        check(hasMethod(c, "getVisualOtherHit"), "method getVisualOtherHit");
        check(hasMethod(c, "getCaretShape"), "method getCaretShape");
        check(hasMethod(c, "getCharacterLevel"), "method getCharacterLevel");
        check(hasMethod(c, "getCaretShapes"), "method getCaretShapes");
        check(hasMethod(c, "getLogicalRangesForVisualSelection"), "method getLogicalRangesForVisualSelection");
        check(hasMethod(c, "getVisualHighlightShape"), "method getVisualHighlightShape");
        check(hasMethod(c, "getLogicalHighlightShape"), "method getLogicalHighlightShape");
        check(hasMethod(c, "getBlackBoxBounds"), "method getBlackBoxBounds");
        check(hasMethod(c, "hitTestChar"), "method hitTestChar");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "getOutline"), "method getOutline");
        check(hasMethod(c, "getLayoutPath"), "method getLayoutPath");
        check(hasMethod(c, "hitToPoint"), "method hitToPoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
