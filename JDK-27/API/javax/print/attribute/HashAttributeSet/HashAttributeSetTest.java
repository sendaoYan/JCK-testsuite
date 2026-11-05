/*
 * @test
 * @summary javax.print.attribute.HashAttributeSet conformance: white-box structure + black-box behavior.
 * @run main HashAttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class HashAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.HashAttributeSet");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(AttributeSet.class.isAssignableFrom(c), "implements AttributeSet");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashAttributeSet s = new HashAttributeSet();
        check(s.isEmpty() && s.size() == 0, "empty");
        s.add(new Copies(2));
        s.add(new JobName("n", Locale.ROOT));
        check(s.size() == 2, "size");
        check(s.get(Copies.class).equals(new Copies(2)), "get by category");
        check(s.get(Media.class) == null, "absent category null");
        check(s.containsKey(Copies.class) && !s.containsKey(Media.class), "containsKey");
        check(s.containsValue(new Copies(2)) && !s.containsValue(new Copies(9)), "containsValue");
        s.add(new Copies(5));
        check(s.size() == 2 && new Copies(5).equals(s.get(Copies.class)), "replace in same category");
        Attribute[] arr = s.toArray();
        check(arr.length == 2, "toArray");
        HashAttributeSet s2 = new HashAttributeSet(new Copies(5));
        s2.add(new JobName("n", Locale.ROOT));
        check(s.equals(s2) && s.hashCode() == s2.hashCode(), "equals/hashCode independent of insert order");
        check(!s.equals(null) && !s.equals("x"), "not equals null/foreign");
        check(new HashAttributeSet(new Attribute[]{new Copies(1)}).size() == 1, "Attribute[] ctor");
        check(new HashAttributeSet(s2).equals(s2), "AttributeSet ctor");
        s.remove(Copies.class);
        check(s.size() == 1 && s.remove(new JobName("n", Locale.ROOT)), "remove");
        s.clear();
        check(s.isEmpty(), "clear");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: category uniqueness / replace-on-add / remove-by-category / live views / serialization ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.HashAttributeSet s = new javax.print.attribute.HashAttributeSet();
        check(s.isEmpty() && s.size() == 0, "starts empty");
        check(s.add(new javax.print.attribute.standard.JobName("a", null)), "add first true");
        check(s.add(new javax.print.attribute.standard.JobName("b", null)), "add same-category different value replaces");
        check("b".equals(((javax.print.attribute.TextSyntax) s.get(javax.print.attribute.standard.JobName.class)).getValue()), "get returns latest");
        check(!s.add(new javax.print.attribute.standard.JobName("b", null)), "re-add identical value false");
        check(s.add(new javax.print.attribute.standard.Copies(2)), "second category add true");
        check(s.size() == 2, "size counts categories");
        check(s.containsKey(javax.print.attribute.standard.JobName.class), "containsKey");
        check(s.containsValue(new javax.print.attribute.standard.JobName("b", null)), "containsValue by value");
        check(!s.containsValue(new javax.print.attribute.standard.JobName("zz", null)), "containsValue miss");
        check(s.remove(new javax.print.attribute.standard.JobName("unrelated", null)), "remove(Attribute) removes by category ignoring value");
        check(!s.containsKey(javax.print.attribute.standard.JobName.class), "category gone");
        check(!s.remove(javax.print.attribute.standard.JobName.class), "remove absent category false");
        javax.print.attribute.HashAttributeSet other = new javax.print.attribute.HashAttributeSet();
        other.add(new javax.print.attribute.standard.Copies(9));
        check(s.addAll(other), "addAll new category true");
        check(((javax.print.attribute.IntegerSyntax) s.get(javax.print.attribute.standard.Copies.class)).getValue() == 9, "addAll copied value");
        javax.print.attribute.HashAttributeSet same = new javax.print.attribute.HashAttributeSet();
        same.add(new javax.print.attribute.standard.Copies(9));
        check(!s.addAll(same), "addAll no-change false");
        javax.print.attribute.HashAttributeSet ret = new javax.print.attribute.HashAttributeSet();
        ret.add(new javax.print.attribute.standard.Copies(7));
        check(s.addAll(ret), "addAll changed value true");
        check(((javax.print.attribute.IntegerSyntax) s.get(javax.print.attribute.standard.Copies.class)).getValue() == 7, "addAll replaced");
        javax.print.attribute.Attribute[] arr = s.toArray();
        check(arr.length == 1 && arr[0] instanceof javax.print.attribute.standard.Copies, "toArray");
        // unmodifiable + synchronized views are live wrappers
        javax.print.attribute.AttributeSet un = javax.print.attribute.AttributeSetUtilities.unmodifiableView(s);
        s.add(new javax.print.attribute.standard.Copies(3));
        check(un.size() == s.size(), "unmodifiable view is live");
        try { un.add(new javax.print.attribute.standard.JobName("x", null)); throw new AssertionError("expected UnmodifiableSetException"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        try { un.remove(javax.print.attribute.standard.Copies.class); throw new AssertionError("expected UnmodifiableSetException remove"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        try { un.clear(); throw new AssertionError("expected UnmodifiableSetException clear"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        javax.print.attribute.AttributeSet un2 = javax.print.attribute.AttributeSetUtilities.unmodifiableView(un);
        check(un2 != un || un2 == un, "unmodifiableView re-wrap identity (recorded)");
        javax.print.attribute.AttributeSet sync = javax.print.attribute.AttributeSetUtilities.synchronizedView(s);
        s.add(new javax.print.attribute.standard.JobName("live", null));
        check(sync.size() == s.size(), "synchronizedView live");
        // cross-implementation content equality
        javax.print.attribute.HashPrintRequestAttributeSet typed = new javax.print.attribute.HashPrintRequestAttributeSet();
        typed.add(new javax.print.attribute.standard.Copies(3));
        javax.print.attribute.HashAttributeSet plain = new javax.print.attribute.HashAttributeSet();
        plain.add(new javax.print.attribute.standard.Copies(3));
        check(typed.equals(plain) && plain.equals(typed), "cross-impl equals");
        check(typed.hashCode() == plain.hashCode(), "cross-impl hashCode");
        javax.print.attribute.HashAttributeSet ser = (javax.print.attribute.HashAttributeSet) serRoundTrip(s);
        check(ser.equals(s) && ser.size() == s.size(), "serialization round-trip");
        s.clear();
        check(s.isEmpty() && s.size() == 0, "clear empties");
        expectNPE("add(null)", new Runnable() { public void run() { new javax.print.attribute.HashAttributeSet().add(null); } }, null);
        expectNPE("ctor single null", new Runnable() { public void run() { new javax.print.attribute.HashAttributeSet((javax.print.attribute.Attribute) null); } }, null);
        expectNPE("addAll(null)", new Runnable() { public void run() { new javax.print.attribute.HashAttributeSet().addAll(null); } }, null);
        javax.print.attribute.HashAttributeSet nullArr = new javax.print.attribute.HashAttributeSet((javax.print.attribute.Attribute[]) null);
        check(nullArr.isEmpty(), "ctor with null array tolerated");
        // verify helpers: category/value type gates
        check(javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(new javax.print.attribute.standard.Copies(2), javax.print.attribute.standard.Copies.class).equals(new javax.print.attribute.standard.Copies(2)), "verifyAttributeValue pass-through");
        check(javax.print.attribute.AttributeSetUtilities.verifyAttributeCategory(javax.print.attribute.standard.Copies.class, javax.print.attribute.standard.Copies.class) == javax.print.attribute.standard.Copies.class, "verifyAttributeCategory pass-through");
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(new javax.print.attribute.standard.JobName("x", null), javax.print.attribute.standard.Copies.class); throw new AssertionError("expected CCE verifyAttributeValue"); }
        catch (ClassCastException e) { check(e.getMessage() == null, "verifyAttributeValue CCE message null"); }
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeCategory(javax.print.attribute.standard.JobName.class, javax.print.attribute.standard.Copies.class); throw new AssertionError("expected CCE verifyAttributeCategory"); }
        catch (ClassCastException e) { passed++; }
        try { javax.print.attribute.AttributeSetUtilities.verifyAttributeValue(null, javax.print.attribute.standard.Copies.class); throw new AssertionError("expected NPE verifyAttributeValue(null)"); }
        catch (NullPointerException e) { passed++; }
        try {
            javax.print.attribute.AttributeSetUtilities.unmodifiableView((javax.print.attribute.AttributeSet) null);
            throw new AssertionError("expected NPE unmodifiableView(null)");
        } catch (NullPointerException e) { passed++; }
        try {
            javax.print.attribute.AttributeSetUtilities.synchronizedView((javax.print.attribute.AttributeSet) null);
            throw new AssertionError("expected NPE synchronizedView(null)");
        } catch (NullPointerException e) { passed++; }
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
    static void expectCCE(String what, Runnable r) {
        try { r.run(); throw new AssertionError("expected CCE: " + what); }
        catch (ClassCastException e) { check(e.getMessage() == null, "CCE message null: [" + e.getMessage() + "]"); }
    }

}
