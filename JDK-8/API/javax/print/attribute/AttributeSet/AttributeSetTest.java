/*
 * @test
 * @summary javax.print.attribute.AttributeSet conformance: interface.
 * @run main AttributeSetTest
 */

import java.lang.reflect.*;


public class AttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.AttributeSet");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "containsValue"), "declares containsValue");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "size"), "declares size");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(hasMethod(c, "clear"), "declares clear");
        check(hasMethod(c, "isEmpty"), "declares isEmpty");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full method surface of AttributeSet via reflection + behavior on Hash implementer ----
    static void testContractDeep() throws Exception {
        Class<?> c = javax.print.attribute.AttributeSet.class;
        check(c.isInterface(), "is interface");
        String[] names = { "add", "addAll", "clear", "containsKey", "containsValue", "equals", "get", "hashCode", "isEmpty", "remove", "size", "toArray" };
        for (String n : names) { boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) { found = true; break; }
            check(found, "method " + n); }
        check(c.getMethod("add", javax.print.attribute.Attribute.class).getReturnType() == boolean.class, "add returns boolean");
        check(c.getMethod("get", Class.class).getReturnType() == javax.print.attribute.Attribute.class, "get returns Attribute");
        check(c.getMethod("toArray").getReturnType() == javax.print.attribute.Attribute[].class, "toArray returns Attribute[]");
        javax.print.attribute.AttributeSet s = new javax.print.attribute.HashAttributeSet();
        check(s.isEmpty() && s.size() == 0 && s.toArray().length == 0, "fresh set empty");
        check(s.add(new javax.print.attribute.standard.Copies(2)) && s.size() == 1, "add first true");
        check(s.add(new javax.print.attribute.standard.Copies(5)), "same-category different value: add true (replacement)");
        check(s.size() == 1, "still one attribute after replacement");
        javax.print.attribute.AttributeSet self = new javax.print.attribute.HashAttributeSet();
        self.add(new javax.print.attribute.standard.Copies(3));
        check(!self.add(new javax.print.attribute.standard.Copies(3)), "identical value re-add false");
        check(((javax.print.attribute.standard.Copies) s.get(javax.print.attribute.standard.Copies.class)).getValue() == 5, "category-keyed replacement");
        check(s.containsValue(new javax.print.attribute.standard.Copies(5)), "containsValue");
        check(!s.containsValue(new javax.print.attribute.standard.Copies(99)), "absent value false");
        check(s.containsKey(javax.print.attribute.standard.Copies.class), "containsKey");
        check(s.get(javax.print.attribute.standard.Copies.class) != null, "get present");
        javax.print.attribute.AttributeSet t = new javax.print.attribute.HashAttributeSet();
        t.add(new javax.print.attribute.standard.Copies(5));
        check(s.equals(t) && s.hashCode() == t.hashCode(), "equals+hashCode by content");
        check(s.addAll(t) == false, "addAll same content false");
        javax.print.attribute.AttributeSet u = new javax.print.attribute.HashAttributeSet();
        u.add(new javax.print.attribute.standard.JobName("j", null));
        check(s.addAll(u), "addAll disjoint true");
        check(s.size() == 2, "size after union");
        check(s.remove(new javax.print.attribute.standard.Copies(5)), "remove present true");
        check(!s.remove(new javax.print.attribute.standard.Copies(5)), "remove absent false");
        s.clear();
        check(s.isEmpty(), "clear");
        try { s.add(null); throw new AssertionError("expected NPE add(null)"); }
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
