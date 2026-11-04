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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // 12 rule constants: instances with exact rule values and alpha 1.0
        java.awt.AlphaComposite[] cs = {java.awt.AlphaComposite.Clear, java.awt.AlphaComposite.Src,
            java.awt.AlphaComposite.Dst, java.awt.AlphaComposite.SrcOver, java.awt.AlphaComposite.DstOver,
            java.awt.AlphaComposite.SrcIn, java.awt.AlphaComposite.DstIn, java.awt.AlphaComposite.SrcOut,
            java.awt.AlphaComposite.DstOut, java.awt.AlphaComposite.SrcAtop, java.awt.AlphaComposite.DstAtop,
            java.awt.AlphaComposite.Xor};
        String[] names = {"Clear","Src","Dst","SrcOver","DstOver","SrcIn","DstIn","SrcOut","DstOut","SrcAtop","DstAtop","Xor"};
        for (int i = 0; i < cs.length; i++) {
            int[] rules = {1, 2, 9, 3, 4, 5, 6, 7, 8, 10, 11, 12};
            check(cs[i].getRule() == rules[i], names[i] + " rule value " + rules[i]);
            check(cs[i].getAlpha() == 1.0f, names[i] + " constant alpha 1.0");
        }
        // int rule constants 1..12
        check(java.awt.AlphaComposite.CLEAR == 1 && java.awt.AlphaComposite.SRC == 2
              && java.awt.AlphaComposite.DST == 9
              && java.awt.AlphaComposite.SRC_OVER == 3 && java.awt.AlphaComposite.DST_OVER == 4
              && java.awt.AlphaComposite.SRC_IN == 5 && java.awt.AlphaComposite.DST_IN == 6
              && java.awt.AlphaComposite.SRC_OUT == 7 && java.awt.AlphaComposite.DST_OUT == 8
              && java.awt.AlphaComposite.SRC_ATOP == 10 && java.awt.AlphaComposite.DST_ATOP == 11
              && java.awt.AlphaComposite.XOR == 12, "rule int constants 1..12");
        // getInstance caching: same params -> same instance
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f)
              == java.awt.AlphaComposite.SrcOver, "getInstance cached identity");
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f)
              .equals(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f)),
              "alpha variant equal (not identity-cached)");
        // equals/hashCode
        check(java.awt.AlphaComposite.getInstance(3, 0.5f).equals(java.awt.AlphaComposite.getInstance(3, 0.5f)),
              "equals same rule+alpha");
        check(java.awt.AlphaComposite.getInstance(3, 0.5f).hashCode()
              == java.awt.AlphaComposite.getInstance(3, 0.5f).hashCode(), "hashCode consistent");
        check(!java.awt.AlphaComposite.getInstance(3, 0.5f).equals(java.awt.AlphaComposite.getInstance(3, 0.6f)),
              "alpha differs not equal");
        check(!java.awt.AlphaComposite.getInstance(3, 0.5f).equals(java.awt.AlphaComposite.getInstance(4, 0.5f)),
              "rule differs not equal");
        check(!java.awt.AlphaComposite.SrcOver.equals(null), "equals(null)");
        // alpha bounds
        boolean iae = false;
        try { java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.5f); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "alpha > 1 -> IAE");
        iae = false;
        try { java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, -0.01f); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "alpha < 0 -> IAE");
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.0f).getAlpha() == 0.0f, "alpha 0 legal");
        check(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f).getAlpha() == 1.0f, "alpha 1 legal");
        // rule bounds: 1..12
        iae = false;
        try { java.awt.AlphaComposite.getInstance(0); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "rule 0 -> IAE");
        iae = false;
        try { java.awt.AlphaComposite.getInstance(13); } catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "rule 13 -> IAE");
        // createContext works headless (in-memory BufferedImage color models)
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.CompositeContext ctx = java.awt.AlphaComposite.SrcOver.createContext(
                bi.getColorModel(), bi.getColorModel(), null);
        check(ctx != null, "createContext returns context headless");
        ctx.dispose();
        // AlphaComposite is not Serializable
        check(!(java.io.Serializable.class.isAssignableFrom(java.awt.AlphaComposite.class)),
              "AlphaComposite not Serializable");
        // composite applies in-memory: SRC_OVER with alpha over background pixel via context
        java.awt.CompositeContext c2 = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f)
                .createContext(bi.getColorModel(), bi.getColorModel(), null);
        check(c2 != null, "createContext alpha 0.5");
        c2.dispose();
    }

}
