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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: unmodifiable/synchronized views over all 5 set interfaces + verify helpers ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.HashDocAttributeSet doc = new javax.print.attribute.HashDocAttributeSet();
        doc.add(javax.print.attribute.standard.Chromaticity.MONOCHROME);
        javax.print.attribute.DocAttributeSet docUn = javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.DocAttributeSet) doc);
        try { docUn.clear(); throw new AssertionError("expected UnmodifiableSetException doc"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        javax.print.attribute.HashPrintRequestAttributeSet req = new javax.print.attribute.HashPrintRequestAttributeSet();
        req.add(new javax.print.attribute.standard.Copies(1));
        javax.print.attribute.PrintRequestAttributeSet reqUn = javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.PrintRequestAttributeSet) req);
        try { reqUn.clear(); throw new AssertionError("expected UnmodifiableSetException req"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        javax.print.attribute.HashPrintJobAttributeSet job = new javax.print.attribute.HashPrintJobAttributeSet();
        job.add(new javax.print.attribute.standard.Copies(1));
        javax.print.attribute.PrintJobAttributeSet jobUn = javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.PrintJobAttributeSet) job);
        try { jobUn.clear(); throw new AssertionError("expected UnmodifiableSetException job"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        javax.print.attribute.HashPrintServiceAttributeSet svc = new javax.print.attribute.HashPrintServiceAttributeSet();
        svc.add(new javax.print.attribute.standard.PrinterName("p", null));
        javax.print.attribute.PrintServiceAttributeSet svcUn = javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.PrintServiceAttributeSet) svc);
        try { svcUn.clear(); throw new AssertionError("expected UnmodifiableSetException svc"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        // synchronized views remain live + writable
        javax.print.attribute.AttributeSet sync = javax.print.attribute.AttributeSetUtilities.synchronizedView((javax.print.attribute.AttributeSet) doc);
        sync.add(javax.print.attribute.standard.Chromaticity.COLOR);
        check(doc.containsKey(javax.print.attribute.standard.Chromaticity.class), "sync view writes through");
        // unmodifiable view of typed set is rejected at runtime when wrong-typed attributes added
        javax.print.attribute.AttributeSet plain = javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.AttributeSet) doc);
        try { plain.add(new javax.print.attribute.standard.JobName("x", null)); throw new AssertionError("expected UnmodifiableSetException through view"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        check(plain.containsKey(javax.print.attribute.standard.Chromaticity.class), "plain view reads source category");
        check(javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(new javax.print.attribute.standard.Copies(2), javax.print.attribute.standard.Copies.class) instanceof javax.print.attribute.standard.Copies, "verifyAttributeValue pass-through");
        check(javax.print.attribute.AttributeSetUtilities.verifyAttributeCategory(javax.print.attribute.standard.Copies.class, javax.print.attribute.standard.Copies.class) == javax.print.attribute.standard.Copies.class, "verifyAttributeCategory pass-through");
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(new javax.print.attribute.standard.JobName("x", null), javax.print.attribute.standard.Copies.class); throw new AssertionError("expected CCE"); }
        catch (ClassCastException e) { passed++; }
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeCategory(javax.print.attribute.standard.JobName.class, javax.print.attribute.standard.Copies.class); throw new AssertionError("expected CCE category"); }
        catch (ClassCastException e) { passed++; }
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(null, javax.print.attribute.standard.Copies.class); throw new AssertionError("expected NPE"); }
        catch (NullPointerException e) { passed++; }
        try { javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.AttributeSet) null); throw new AssertionError("expected NPE unmod"); }
        catch (NullPointerException e) { passed++; }
        try { javax.print.attribute.AttributeSetUtilities.synchronizedView((javax.print.attribute.AttributeSet) null); throw new AssertionError("expected NPE sync"); }
        catch (NullPointerException e) { passed++; }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
