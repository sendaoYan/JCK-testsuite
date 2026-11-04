/*
 * @test
 * @summary java.awt.AlphaComposite conformance: rules and alpha.
 * @run main AlphaCompositeTest
 */


import java.awt.AlphaComposite;
public class AlphaCompositeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        AlphaComposite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        check(a.getRule() == AlphaComposite.SRC_OVER, "rule");
        check(a.getAlpha() == 0.5f, "alpha");
        check(a.equals(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)), "equals");
        AlphaComposite def = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        check(def.getAlpha() == 1.0f, "default alpha 1.0");
        check(AlphaComposite.SrcOver.getRule() == AlphaComposite.SRC_OVER, "SrcOver constant");
        check(AlphaComposite.Xor.getRule() == AlphaComposite.XOR, "Xor constant");
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
