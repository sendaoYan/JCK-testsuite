/*
 * @test
 * @summary java.awt.AlphaComposite conformance: white-box structure + black-box behavior.
 * @run main AlphaCompositeTest
 */

import java.lang.reflect.*;

public class AlphaCompositeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.AlphaComposite", false, AlphaCompositeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.AlphaComposite");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Modifier.isFinal(m), "final class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.awt.Composite.class.isAssignableFrom(c), "implements java.awt.Composite");

        for (String f : new String[] {"CLEAR", "SRC", "DST", "SRC_OVER", "DST_OVER", "SRC_IN",
                "DST_IN", "SRC_OUT", "DST_OUT", "SRC_ATOP", "DST_ATOP", "XOR"})
            check(c.getField(f).getType() == int.class, "static int " + f);
        check(c.getField("Clear").getType() == java.awt.AlphaComposite.class, "static AlphaComposite Clear");
        check(c.getField("SrcOver").getType() == java.awt.AlphaComposite.class, "static AlphaComposite SrcOver");

        for (String name : new String[] {"getInstance", "getAlpha", "getRule", "derive",
                "createContext", "equals", "hashCode"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // rule constants
        check(java.awt.AlphaComposite.CLEAR == 1, "CLEAR == 1");
        check(java.awt.AlphaComposite.SRC == 2, "SRC == 2");
        check(java.awt.AlphaComposite.SRC_OVER == 3, "SRC_OVER == 3");
        check(java.awt.AlphaComposite.DST_OVER == 4, "DST_OVER == 4");
        check(java.awt.AlphaComposite.SRC_IN == 5, "SRC_IN == 5");
        check(java.awt.AlphaComposite.DST_IN == 6, "DST_IN == 6");
        check(java.awt.AlphaComposite.SRC_OUT == 7, "SRC_OUT == 7");
        check(java.awt.AlphaComposite.DST_OUT == 8, "DST_OUT == 8");
        check(java.awt.AlphaComposite.DST == 9, "DST == 9");
        check(java.awt.AlphaComposite.SRC_ATOP == 10, "SRC_ATOP == 10");
        check(java.awt.AlphaComposite.DST_ATOP == 11, "DST_ATOP == 11");
        check(java.awt.AlphaComposite.XOR == 12, "XOR == 12");

        // static instances correspond to rules
        check(java.awt.AlphaComposite.Clear.getRule() == java.awt.AlphaComposite.CLEAR,
              "Clear rule");
        check(java.awt.AlphaComposite.SrcOver.getRule() == java.awt.AlphaComposite.SRC_OVER,
              "SrcOver rule");
        check(java.awt.AlphaComposite.Src.getRule() == java.awt.AlphaComposite.SRC, "Src rule");
        check(java.awt.AlphaComposite.Xor.getRule() == java.awt.AlphaComposite.XOR, "Xor rule");
        check(java.awt.AlphaComposite.SrcOver.getAlpha() == 1.0f, "constant alpha 1.0");

        // getInstance(rule) returns cached instances
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER)
              == java.awt.AlphaComposite.SrcOver, "getInstance(rule) returns constant");

        // getInstance(rule, alpha)
        java.awt.AlphaComposite half =
            java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f);
        check(half.getRule() == java.awt.AlphaComposite.SRC_OVER, "getInstance(rule,alpha) rule");
        check(half.getAlpha() == 0.5f, "getInstance(rule,alpha) alpha");
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f)
              == java.awt.AlphaComposite.SrcOver, "alpha 1.0 returns cached constant");

        // derive(rule)
        check(java.awt.AlphaComposite.SrcOver.derive(java.awt.AlphaComposite.SRC_OVER)
              == java.awt.AlphaComposite.SrcOver, "derive(same rule) returns this");
        java.awt.AlphaComposite derivedRule =
            java.awt.AlphaComposite.SrcOver.derive(java.awt.AlphaComposite.SRC);
        check(derivedRule.getRule() == java.awt.AlphaComposite.SRC, "derive(different rule) new rule");
        check(derivedRule == java.awt.AlphaComposite.Src, "derive(different rule) returns constant");

        // derive(alpha)
        check(java.awt.AlphaComposite.SrcOver.derive(1.0f) == java.awt.AlphaComposite.SrcOver,
              "derive(same alpha) returns this");
        java.awt.AlphaComposite derivedAlpha = java.awt.AlphaComposite.SrcOver.derive(0.25f);
        check(derivedAlpha.getAlpha() == 0.25f, "derive(alpha) new alpha");
        check(derivedAlpha.getRule() == java.awt.AlphaComposite.SRC_OVER, "derive(alpha) keeps rule");

        // equals / hashCode
        java.awt.AlphaComposite a1 =
            java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f);
        java.awt.AlphaComposite a2 =
            java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f);
        check(a1.equals(a2), "equal composites");
        check(a1.hashCode() == a2.hashCode(), "equal hashCodes");
        check(!a1.equals(java.awt.AlphaComposite.SrcOver), "different alpha not equal");
        check(!java.awt.AlphaComposite.SrcOver.equals(java.awt.AlphaComposite.Src),
              "different rule not equal");
        check(!a1.equals(null), "not equal null");
        check(!a1.equals("composite"), "not equal unrelated type");

        // createContext
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
        java.awt.CompositeContext ctx = java.awt.AlphaComposite.SrcOver.createContext(cm, cm, null);
        check(ctx != null, "createContext non-null");
        ctx.dispose();
        check(true, "composite context dispose callable");

        // invalid rules/alpha
        boolean iaeRule = false;
        try { java.awt.AlphaComposite.getInstance(999); }
        catch (IllegalArgumentException e) { iaeRule = true; }
        check(iaeRule, "getInstance invalid rule throws");
        boolean iaeAlpha = false;
        try { java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.5f); }
        catch (IllegalArgumentException e) { iaeAlpha = true; }
        check(iaeAlpha, "getInstance alpha out of range throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
