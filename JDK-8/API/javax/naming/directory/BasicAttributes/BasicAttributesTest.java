/*
 * @test
 * @summary javax.naming.directory.BasicAttributes conformance: put/get/getIDs.
 * @run main BasicAttributesTest
 */



import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
public class BasicAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes as = new javax.naming.directory.BasicAttributes();
        check(as.size() == 0, "empty");
        check(!as.isCaseIgnored(), "case-sensitive by default");
        as.put("cn", "John");
        as.put("sn", "Doe");
        check(as.size() == 2, "size");
        check("John".equals(as.get("cn").get()), "get");
        javax.naming.directory.BasicAttributes as2 = new javax.naming.directory.BasicAttributes(true);
        check(as2.isCaseIgnored(), "case-insensitive flag");
        javax.naming.directory.BasicAttributes as3 = new javax.naming.directory.BasicAttributes("cn", "John");
        check("John".equals(as3.get("cn").get()), "get from ctor");
        check(as3.remove("cn") != null, "remove");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBsBasics();
        dBsCaseSensitivity();
        dBsEquality();
        dBsCloneSerialize();
        dBsNegative();
        System.out.println("deep: BasicAttributes contracts OK");
    }

    static void dBsBasics() throws Exception {
        check(!new BasicAttributes().isCaseIgnored(), "default case-sensitive");
        check(new BasicAttributes(true).isCaseIgnored(), "ignore-case flag ctor");
        BasicAttributes a = new BasicAttributes();
        check(a.size() == 0, "new set empty");
        check(a.toString().equals("No attributes"), "empty toString");
        BasicAttribute cn = new BasicAttribute("cn", "v1");
        check(a.put(cn) == null, "first put returns null");
        BasicAttribute cn2 = new BasicAttribute("cn", "v2");
        check(a.put(cn2) == cn, "second put returns previous attribute");
        check(a.size() == 1, "put replaces by id");
        check(a.get("cn") == cn2, "get returns current");
        check(a.get("missing") == null, "get absent -> null");
        check(a.remove("cn") == cn2, "remove returns attribute");
        check(a.remove("cn") == null, "remove absent -> null");
        check(a.size() == 0, "size after remove");
        // multi-attribute set enumeration and lookups
        a.put(new BasicAttribute("a", "1"));
        a.put(new BasicAttribute("b", "2"));
        a.put(new BasicAttribute("c", "3"));
        check(a.size() == 3, "size counts ids");
        java.util.Set<String> ids = new java.util.TreeSet<>();
        for (NamingEnumeration<? extends Attribute> en = a.getAll(); en.hasMore(); )
            ids.add(en.next().getID());
        check(ids.equals(new java.util.TreeSet<>(java.util.Arrays.asList("a", "b", "c"))),
                "getAll covers all ids");
        check(a.get("A") == null, "case-sensitive lookup");
        for (String id : new String[] {"a", "b", "c"}) a.remove(id);
        check(a.size() == 0 && a.get("a") == null, "removing each id empties");
    }

    static void dBsCaseSensitivity() throws Exception {
        BasicAttributes ci = new BasicAttributes(true);
        ci.put(new BasicAttribute("CaseID", "v"));
        check(ci.get("caseid") == ci.get("CaseID"), "ignore-case lookup matches");
        check(ci.get("CASEID").equals("v") || ci.get("CASEID") != null, "any case spelling hits");
        check(ci.isCaseIgnored(), "flag visible");
        BasicAttributes cs = new BasicAttributes();
        cs.put(new BasicAttribute("CaseID", "v"));
        check(cs.get("caseid") == null, "case-sensitive lookup misses");
        // replace happens under the canonical spelling of the stored id
        ci.put(new BasicAttribute("CASEID", "w"));
        check(ci.size() == 1, "ignore-case put replaces");
        check("w".equals(ci.get("caseid").get()), "replaced value visible");
    }

    static void dBsEquality() throws Exception {
        BasicAttributes a = new BasicAttributes();
        a.put(new BasicAttribute("a", "1"));
        a.put(new BasicAttribute("b", "2"));
        BasicAttributes b = new BasicAttributes();
        b.put(new BasicAttribute("b", "2"));
        b.put(new BasicAttribute("a", "1"));
        check(a.equals(b) && b.equals(a), "set equality ignores iteration order");
        check(a.hashCode() == b.hashCode(), "hashCode agrees");
        check(!a.equals(new BasicAttributes()), "different sizes not equal");
        BasicAttributes c = new BasicAttributes();
        c.put(new BasicAttribute("a", "DIFFERENT"));
        c.put(new BasicAttribute("b", "2"));
        check(!a.equals(c), "different value not equal");
        // ignore-case flag participates: CI set != CS set with same ids
        BasicAttributes ciA = new BasicAttributes(true);
        ciA.put(new BasicAttribute("a", "1"));
        BasicAttributes csA = new BasicAttributes();
        csA.put(new BasicAttribute("a", "1"));
        check(!ciA.equals(csA), "ignore-case flag participates in equality");
        // hashCode base offset: ignoreCase contributes exactly +1
        check(ciA.hashCode() == csA.hashCode() + 1,
                "hashCode differs by exactly 1 when case ignored");
        // ignore-case affects lookup, not Attribute-level equality: the attrID
        // comparison inside BasicAttribute.equals stays case-sensitive
        BasicAttributes ci1 = new BasicAttributes(true);
        ci1.put(new BasicAttribute("ID", "v"));
        BasicAttributes ci2 = new BasicAttributes(true);
        ci2.put(new BasicAttribute("id", "v"));
        check(!ci1.equals(ci2), "cross-case ids unequal (Attribute equals case-sensitive)");
        ci1.put(new BasicAttribute("id", "v2"));
        check(ci1.size() == 1, "ignore-case put with different case replaces the entry");
        check(ci1.get("ID") != null && "v2".equals(ci1.get("ID").get()),
                "ignore-case lookup finds either case");
        check(!a.equals(null) && !a.equals("x"), "non-Attributes never equal");
    }

    static void dBsCloneSerialize() throws Exception {
        BasicAttributes a = new BasicAttributes(true);
        a.put(new BasicAttribute("cn", "v"));
        BasicAttributes clone = (BasicAttributes) a.clone();
        check(clone.isCaseIgnored(), "clone preserves flag");
        clone.put(new BasicAttribute("extra", "e"));
        check(a.size() == 1 && clone.size() == 2, "clone independent");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            BasicAttributes back = (BasicAttributes) ois.readObject();
            check(back.isCaseIgnored(), "flag survives serialization");
            check(back.size() == 1 && "v".equals(back.get("cn").get()), "values survive");
            check(back.equals(a), "round-trip equals original");
            // deserialized clone still mutable
            back.put(new BasicAttribute("x", "y"));
            check(back.size() == 2, "deserialized set remains mutable");
        }
    }

    static void dBsNegative() throws Exception {
        BasicAttributes a = new BasicAttributes();
        // put(null id attribute): BasicAttribute ids are never null by construction,
        // but put(null) attribute -> NPE from getID()
        boolean npe = false;
        try { a.put(null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "put(null) -> NPE");
        // toString with content is non-empty and contains ids
        a.put(new BasicAttribute("zz", "1"));
        check(a.toString().contains("zz"), "toString contains id");
    }

}
