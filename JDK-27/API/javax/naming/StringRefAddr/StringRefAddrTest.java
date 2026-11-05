/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: white-box structure + black-box behavior.
 * @run main StringRefAddrTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.BinaryRefAddr;
import javax.naming.Reference;

public class StringRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = StringRefAddr.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == RefAddr.class, "extends RefAddr");
        Constructor<?> ct = c.getDeclaredConstructor(String.class, String.class);
        check(Modifier.isPublic(ct.getModifiers()), "public ctor(String,String)");
        for (String m : new String[] {"getContent", "equals", "hashCode", "toString"})
            check(method(c, m), "inherits/overrides " + m);
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringRefAddr a = new StringRefAddr("addrType", "content");
        check(a.getType().equals("addrType"), "type");
        check(a.getContent() instanceof String, "content type");
        check(((String) a.getContent()).equals("content"), "content value");

        // value semantics: equals compares type + string content
        check(a.equals(new StringRefAddr("addrType", "content")), "equals same");
        check(!a.equals(new StringRefAddr("addrType", "other")), "content mismatch");
        check(!a.equals(new StringRefAddr("other", "content")), "type mismatch");
        check(a.hashCode() == new StringRefAddr("addrType", "content").hashCode(), "hashCode equal pair");
        check(a.toString().equals("Type: addrType\nContent: content") || a.toString().startsWith("Type: addrType"),
                "toString shape");

        // null content is legal (JDK-4347817 regression)
        StringRefAddr n = new StringRefAddr("T", null);
        check(n.getContent() == null, "null content preserved");
        check(n.toString() != null, "toString null-safe");
        check(n.equals(new StringRefAddr("T", null)), "null equals null");
        check(!n.equals(a), "null vs non-null");

        // empty-string content differs from null
        StringRefAddr e = new StringRefAddr("T", "");
        check(!e.equals(n), "empty != null content");

        // same instance used twice in a Reference stays addressable by type
        javax.naming.Reference ref = new javax.naming.Reference("C", a);
        check(ref.get("addrType") == a, "retrieved by type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSraContentForms();
        dSraEquality();
        dSraSerialization();
        System.out.println("deep: StringRefAddr contracts OK");
    }

    static void dSraContentForms() {
        StringRefAddr s = new StringRefAddr("addr", "value");
        check("addr".equals(s.getType()), "getType");
        check("value".equals(s.getContent()), "getContent returns the string");
        Object content = s.getContent();
        check(content instanceof String, "getContent is String form");
        StringRefAddr empty = new StringRefAddr("addr", "");
        check("".equals(empty.getContent()), "empty string content preserved");
        StringRefAddr nullContent = new StringRefAddr("addr", null);
        check(nullContent.getContent() == null, "null content stored as null");
        StringRefAddr nullType = new StringRefAddr(null, "v");
        check(nullType.getType() == null, "null type preserved");
        boolean nullTypeNpe = false;
        try { nullType.equals(new StringRefAddr("addr", "v")); }
        catch (NullPointerException x) { nullTypeNpe = true; }
        check(nullTypeNpe, "equals from null-type instance -> NPE (compareTo on null addrType)");
    }

    static void dSraEquality() {
        StringRefAddr a = new StringRefAddr("t", "c");
        StringRefAddr b = new StringRefAddr("t", "c");
        check(a.equals(b), "equal type and content");
        check(a.hashCode() == b.hashCode(), "hashCode contract");
        check(!a.equals(new StringRefAddr("T", "c")), "type comparison is case-sensitive");
        check(!a.equals(new StringRefAddr("t", "C")), "content comparison is case-sensitive");
        check(!a.equals(null), "null never equal");
        check(!a.equals(new BinaryRefAddr("t", new byte[] { 99 })), "string vs binary distinct");
    }

    static void dSraSerialization() throws Exception {
        StringRefAddr s = new StringRefAddr("t", "c");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(s); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            StringRefAddr back = (StringRefAddr) oi.readObject();
            check(back.equals(s) && "c".equals(back.getContent()), "round trip equal");
        }
    }

}
