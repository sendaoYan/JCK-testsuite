/*
 * @test
 * @summary javax.naming.directory.BasicAttributes conformance: white-box structure + black-box behavior.
 * @run main BasicAttributesTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicAttributesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.BasicAttributes");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface(), "not interface");
        check(Attributes.class.isAssignableFrom(c), "implements Attributes");
        check(hasMethod(c, "isCaseIgnored"), "isCaseIgnored");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "get"), "get");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "getIDs"), "getIDs");
        check(hasMethod(c, "put"), "put");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "clone"), "clone");
        check(hasMethod(c, "equals"), "equals");
        check(hasMethod(c, "hashCode"), "hashCode");
        check(hasMethod(c, "toString"), "toString");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == boolean.class), "ctor(boolean)");
    }

    static void testBlackBox() throws Exception {
        // case-sensitive default
        BasicAttributes as = new BasicAttributes();
        check(!as.isCaseIgnored(), "default case-sensitive");
        check(as.size() == 0, "empty size");

        as.put("cn", "Alice");
        check(as.size() == 1, "size 1");
        Attribute a = as.get("cn");
        check(a != null, "get cn");
        check("Alice".equals(a.get()), "get value");
        check(as.get("CN") == null, "case-sensitive get CN null");

        as.put("CN", "Bob");
        check(as.size() == 2, "size 2 distinct case");
        check("Bob".equals(as.get("CN").get()), "get CN");

        // put(Attribute)
        as.put(new BasicAttribute("sn", "Smith"));
        check(as.size() == 3, "size 3 after put(Attribute)");
        check("Smith".equals(as.get("sn").get()), "get sn");

        // getIDs / getAll
        List<String> ids = new ArrayList<>();
        NamingEnumeration<String> idEn = as.getIDs();
        while (idEn.hasMore()) ids.add(idEn.next());
        idEn.close();
        Collections.sort(ids);
        check(ids.equals(Arrays.asList("CN", "cn", "sn")), "getIDs sorted");

        List<String> all = new ArrayList<>();
        NamingEnumeration<? extends Attribute> allEn = as.getAll();
        while (allEn.hasMore()) all.add(allEn.next().getID());
        allEn.close();
        Collections.sort(all);
        check(all.equals(Arrays.asList("CN", "cn", "sn")), "getAll sorted ids");

        // remove
        check(as.remove("sn") != null, "remove sn");
        check(as.size() == 2, "size 2 after remove");
        check(as.remove("nope") == null, "remove absent null");

        // case-ignored attributes
        BasicAttributes ci = new BasicAttributes(true);
        check(ci.isCaseIgnored(), "case-ignored");
        ci.put("cn", "Alice");
        check(ci.get("CN") != null, "case-ignored get CN");
        check("Alice".equals(ci.get("CN").get()), "case-ignored value");
        check(ci.size() == 1, "case-ignored size 1");

        // equals / hashCode / clone
        BasicAttributes b1 = new BasicAttributes("cn", "v");
        BasicAttributes b2 = new BasicAttributes("cn", "v");
        check(b1.equals(b2), "equals same content");
        check(b1.hashCode() == b2.hashCode(), "hashCode same");
        BasicAttributes b3 = new BasicAttributes("cn", "different");
        check(!b1.equals(b3), "equals different value false");

        BasicAttributes cl = (BasicAttributes) b1.clone();
        check(cl != b1, "clone distinct");
        check(cl.equals(b1), "clone equals");
        check("v".equals(cl.get("cn").get()), "clone value");

        // toString
        check(as.toString().contains("cn"), "toString contains id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
