/*
 * @test
 * @summary javax.print.attribute.AttributeSetUtilities conformance: white-box structure + black-box behavior.
 * @run main AttributeSetUtilitiesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeSetUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.AttributeSetUtilities");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(hasMethod(c, "unmodifiableView") && hasMethod(c, "synchronizedView") && hasMethod(c, "verifyAttributeCategory") && hasMethod(c, "verifyAttributeValue") && hasMethod(c, "verifyCategoryForValue"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashAttributeSet s = new HashAttributeSet(new Copies(2));
        AttributeSet u = AttributeSetUtilities.unmodifiableView(s);
        check(u.size() == 1 && new Copies(2).equals(u.get(Copies.class)), "unmodifiableView reads");
        boolean threw = false; try { u.add(new JobName("n", Locale.ROOT)); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable add throws UnmodifiableSetException");
        threw = false; try { u.remove(Copies.class); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable remove throws");
        threw = false; try { u.clear(); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable clear throws");
        s.add(new JobName("n", Locale.ROOT));
        check(u.size() == 2, "view reflects underlying");
        AttributeSet syn = AttributeSetUtilities.synchronizedView(s);
        check(syn.size() == 2 && new Copies(2).equals(syn.get(Copies.class)), "synchronizedView reads");
        check(AttributeSetUtilities.verifyAttributeCategory(Copies.class, Attribute.class) == Copies.class, "verifyAttributeCategory ok");
        boolean threw2 = false; try { AttributeSetUtilities.verifyAttributeCategory(String.class, Attribute.class); } catch (ClassCastException e) { threw2 = true; }
        check(threw2, "verifyAttributeCategory wrong type ClassCastException");
        boolean threw3 = false; try { AttributeSetUtilities.verifyAttributeCategory(null, Attribute.class); } catch (NullPointerException e) { threw3 = true; }
        check(threw3, "verifyAttributeCategory null NPE");
        check(AttributeSetUtilities.verifyAttributeValue(new Copies(1), Attribute.class) instanceof Copies, "verifyAttributeValue ok");
        boolean threw4 = false; try { AttributeSetUtilities.verifyAttributeValue(new Copies(1), PrintServiceAttribute.class); } catch (ClassCastException e) { threw4 = true; }
        check(threw4, "verifyAttributeValue wrong interface ClassCastException");
        AttributeSetUtilities.verifyCategoryForValue(Copies.class, new Copies(1));
        boolean threw5 = false; try { AttributeSetUtilities.verifyCategoryForValue(Media.class, new Copies(1)); } catch (IllegalArgumentException e) { threw5 = true; }
        check(threw5, "verifyCategoryForValue mismatch IllegalArgumentException");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
