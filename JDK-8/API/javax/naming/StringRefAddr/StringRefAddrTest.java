/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: full contract coverage.
 *          type/content/toString, equals/hashCode (type + content), null
 *          content handling, and serialization round-trip.
 * @run main StringRefAddrTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.naming.StringRefAddr;
import javax.naming.BinaryRefAddr;
import javax.naming.RefAddr;

public class StringRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static StringRefAddr roundTrip(StringRefAddr a) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (StringRefAddr) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        StringRefAddr a = new StringRefAddr("url", "http://x/");
        check("url".equals(a.getType()), "getType");
        check("http://x/".equals(a.getContent()), "getContent");
        check(a.toString() != null, "toString");
    }

    static void testEqualsHashCode() {
        StringRefAddr a = new StringRefAddr("t", "v");
        StringRefAddr b = new StringRefAddr("t", "v");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(new StringRefAddr("t", "w")), "different content");
        check(!a.equals(new StringRefAddr("u", "v")), "different type");
        check(!a.equals(null) && !a.equals("t"), "equals null/non-RefAddr");
    }

    static void testNullContent() {
        StringRefAddr a = new StringRefAddr("t", null);
        check(a.getContent() == null, "null content getContent");
        check(a.toString() != null, "null content toString");
        StringRefAddr b = new StringRefAddr("t", null);
        check(a.equals(b), "null content equals");
    }

    static void testSerialization() throws Exception {
        StringRefAddr a = new StringRefAddr("url", "http://x/");
        StringRefAddr back = roundTrip(a);
        check(back.equals(a) && "http://x/".equals(back.getContent()), "round trip");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEqualsHashCode();
        testNullContent();
        testSerialization();
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
