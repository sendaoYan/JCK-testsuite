/*
 * @test
 * @summary javax.naming.directory.SearchResult conformance: name/object/attributes.
 * @run main SearchResultTest
 */



import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
public class SearchResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes attrs = new javax.naming.directory.BasicAttributes("cn", "John");
        javax.naming.directory.SearchResult sr = new javax.naming.directory.SearchResult("cn=John", "value", attrs);
        check("cn=John".equals(sr.getName()), "getName");
        check("value".equals(sr.getObject()), "getObject");
        check(sr.getAttributes() == attrs, "getAttributes");
        javax.naming.directory.BasicAttributes attrs2 = new javax.naming.directory.BasicAttributes("sn", "Doe");
        sr.setAttributes(attrs2);
        check(sr.getAttributes() == attrs2, "setAttributes");
        check(sr instanceof javax.naming.Binding, "is Binding");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSrConstructors();
        dSrAttributes();
        dSrInheritance();
        dSrToString();
        dSrSerialization();
        System.out.println("deep: SearchResult contracts OK");
    }

    static void dSrConstructors() {
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v"));
        SearchResult a = new SearchResult("name", "obj", at);
        check("name".equals(a.getName()), "name recorded");
        check("obj".equals(a.getObject()), "object recorded");
        check(a.getAttributes() == at, "attributes identity");
        check(a.isRelative(), "default relative");
        SearchResult b = new SearchResult("name", "obj", at, false);
        check(!b.isRelative(), "absolute form");
        SearchResult c = new SearchResult("name", "java.lang.String", "obj", at);
        check("java.lang.String".equals(c.getClassName()), "explicit class name");
        check(c.getObject() instanceof String, "object type independent of class name");
        SearchResult d = new SearchResult("name", "java.lang.Integer", "obj", at, true);
        check(d.isRelative() && "java.lang.Integer".equals(d.getClassName()), "5-arg form");
        // null object and null attributes are both legal
        SearchResult n = new SearchResult("n", null, null);
        check(n.getObject() == null && n.getAttributes() == null, "nulls legal");
    }

    static void dSrAttributes() {
        SearchResult r = new SearchResult("n", "o", new BasicAttributes());
        BasicAttributes replacement = new BasicAttributes();
        replacement.put(new BasicAttribute("k", "v2"));
        r.setAttributes(replacement);
        check(r.getAttributes() == replacement, "setAttributes replaces");
        r.setAttributes(null);
        check(r.getAttributes() == null, "setAttributes(null) allowed");
    }

    static void dSrInheritance() {
        BasicAttributes at = new BasicAttributes();
        SearchResult r = new SearchResult("nm", "obj", at, false);
        NameClassPair asPair = r;
        check("nm".equals(asPair.getName()), "NameClassPair name via inheritance");
        check(asPair.isRelative() == false, "isRelative inherited");
        asPair.setName("renamed");
        check("renamed".equals(r.getName()), "setName visible through subclass");
        // class name defaults to object's class when absent
        SearchResult auto = new SearchResult("n", Integer.valueOf(5), at);
        check("java.lang.Integer".equals(auto.getClassName()), "className defaults to object class");
        auto.setClassName("custom");
        check("custom".equals(auto.getClassName()), "setClassName");
    }

    static void dSrToString() {
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v"));
        String s = new SearchResult("nm", "obj", at).toString();
        check(s.contains("nm"), "toString contains name");
        check(s.contains("cn"), "toString contains attribute id");
    }

    static void dSrSerialization() throws Exception {
        BasicAttributes at = new BasicAttributes();
        at.put(new BasicAttribute("cn", "v"));
        SearchResult r = new SearchResult("nm", "payload", at, false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(r); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            SearchResult back = (SearchResult) ois.readObject();
            check("nm".equals(back.getName()), "round-trip name");
            check("payload".equals(back.getObject()), "round-trip object");
            check(!back.isRelative(), "round-trip relative flag");
            check("v".equals(back.getAttributes().get("cn").get()), "round-trip attributes");
        }
    }

}
