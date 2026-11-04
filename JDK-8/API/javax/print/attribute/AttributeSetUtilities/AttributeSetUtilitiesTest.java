/*
 * @test
 * @summary javax.print.attribute.AttributeSetUtilities conformance: attribute set utilities.
 * @run main AttributeSetUtilitiesTest
 */

import java.lang.reflect.*;


public class AttributeSetUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        javax.print.attribute.HashAttributeSet base = new javax.print.attribute.HashAttributeSet();
        javax.print.attribute.AttributeSet uv = javax.print.attribute.AttributeSetUtilities.unmodifiableView(base);
        check(uv != null, "unmodifiableView non-null");
        boolean threwU = false;
        try { uv.add(new javax.print.attribute.standard.Copies(2)); } catch (javax.print.attribute.UnmodifiableSetException ex) { threwU = true; }
        check(threwU, "unmodifiable add throws UnmodifiableSetException");
        javax.print.attribute.AttributeSet sv = javax.print.attribute.AttributeSetUtilities.synchronizedView(base);
        check(sv != null, "synchronizedView non-null");
        check(javax.print.attribute.AttributeSetUtilities.verifyAttributeCategory(javax.print.attribute.standard.Copies.class, javax.print.attribute.standard.Copies.class) == javax.print.attribute.standard.Copies.class, "verifyAttributeCategory");
        javax.print.attribute.Attribute va = javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(new javax.print.attribute.standard.Copies(2), javax.print.attribute.standard.Copies.class);
        check(va instanceof javax.print.attribute.standard.Copies, "verifyAttributeValue");
        javax.print.attribute.AttributeSetUtilities.verifyCategoryForValue(javax.print.attribute.standard.Copies.class, new javax.print.attribute.standard.Copies(2));
        System.out.println("PASS (" + passed + " assertions)");
    }
}
