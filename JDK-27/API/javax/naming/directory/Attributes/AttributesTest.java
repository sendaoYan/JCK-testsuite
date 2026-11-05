/*
 * @test
 * @summary javax.naming.directory.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
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

public class AttributesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attributes");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(Cloneable.class.isAssignableFrom(c), "extends Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "isCaseIgnored"), "isCaseIgnored");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "get"), "get");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "getIDs"), "getIDs");
        check(hasMethod(c, "put"), "put(String,Object)");
        check(hasMethod(c, "put"), "put(Attribute)");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testBlackBox() throws Exception {
        // Exercise the contract through BasicAttributes.
        Attributes as = new BasicAttributes(false);
        check(!as.isCaseIgnored(), "isCaseIgnored");
        as.put("cn", "Alice");
        check(as.size() == 1, "size");
        Attribute a = as.get("cn");
        check(a != null, "get");
        check("Alice".equals(a.get()), "get value");
        as.put(new BasicAttribute("sn", "Smith"));
        check(as.size() == 2, "put(Attribute) size");
        List<String> ids = new ArrayList<>();
        NamingEnumeration<String> idEn = as.getIDs();
        while (idEn.hasMore()) ids.add(idEn.next());
        idEn.close();
        Collections.sort(ids);
        check(ids.equals(Arrays.asList("cn", "sn")), "getIDs");
        List<String> got = new ArrayList<>();
        NamingEnumeration<? extends Attribute> all = as.getAll();
        while (all.hasMore()) got.add(all.next().getID());
        all.close();
        Collections.sort(got);
        check(got.equals(Arrays.asList("cn", "sn")), "getAll");
        check(as.remove("sn") != null, "remove");
        Attributes cl = (Attributes) ((BasicAttributes) as).clone();
        check(cl.size() == 1, "clone size");
        check(cl.get("cn") != null, "clone get");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dAsInterfaceSurface();
        dAsCustomImplementation();
        dAsPolymorphic();
        System.out.println("deep: Attributes contracts OK");
    }

    /** Minimal hand-written implementation proves the interface is complete. */
    static class DeepAttrs implements Attributes {
        final java.util.SortedMap<String, Attribute> map = new java.util.TreeMap<>();
        final boolean ignoreCase;
        DeepAttrs(boolean ignoreCase) { this.ignoreCase = ignoreCase; }
        private String key(String id) { return ignoreCase ? id.toLowerCase(java.util.Locale.ENGLISH) : id; }
        public boolean isCaseIgnored() { return ignoreCase; }
        public int size() { return map.size(); }
        public Attribute get(String attrID) { return map.get(key(attrID)); }
        public Attribute put(Attribute attr) { return map.put(key(attr.getID()), attr); }
        public Attribute put(String attrID, Object val) {
            javax.naming.directory.BasicAttribute a = new javax.naming.directory.BasicAttribute(attrID, val);
            return map.put(key(attrID), a);
        }
        public NamingEnumeration<String> getIDs() {
            return new NamingEnumeration<String>() {
                final java.util.Iterator<String> it = new java.util.ArrayList<>(map.keySet()).iterator();
                public String next() { return it.next(); }
                public boolean hasMore() { return it.hasNext(); }
                public void close() { }
                public String nextElement() { return it.next(); }
                public boolean hasMoreElements() { return it.hasNext(); }
            };
        }
        public Attribute remove(String attrID) { return map.remove(key(attrID)); }
        public NamingEnumeration<? extends Attribute> getAll() {
            return new NamingEnumeration<Attribute>() {
                final java.util.Iterator<Attribute> it = map.values().iterator();
                public Attribute next() { return it.next(); }
                public boolean hasMore() { return it.hasNext(); }
                public void close() { }
                public Attribute nextElement() { return it.next(); }
                public boolean hasMoreElements() { return it.hasNext(); }
            };
        }
        public Object clone() {
            DeepAttrs c = new DeepAttrs(ignoreCase);
            for (Attribute a : map.values()) c.put((Attribute) ((BasicAttribute) a).clone());
            return c;
        }
    }

    static void dAsInterfaceSurface() throws Exception {
        DeepAttrs a = new DeepAttrs(false);
        check(a.size() == 0, "starts empty");
        check(a.isCaseIgnored() == false, "case flag");
        check(a.get("cn") == null, "absent get null");
        Attribute cn = new BasicAttribute("cn", "v");
        check(a.put(cn) == null, "first put null");
        check(a.put(new BasicAttribute("cn", "v2")) == cn, "replace returns old");
        check(a.size() == 1, "size");
        check(a.remove("cn") != null, "remove returns attribute");
        check(a.remove("cn") == null, "second remove null");
        int n = 0;
        for (NamingEnumeration<? extends Attribute> en = a.getAll(); en.hasMore(); ) { en.next(); n++; }
        check(n == 0, "empty enumeration");
    }

    static void dAsCustomImplementation() throws Exception {
        DeepAttrs a = new DeepAttrs(true);
        a.put(new BasicAttribute("Case", "v"));
        check(a.get("case") != null && a.get("CASE") != null, "case-insensitive custom impl");
        check(a.size() == 1, "one logical id");
        a.put(new BasicAttribute("OTHER", "w"));
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (NamingEnumeration<? extends Attribute> en = a.getAll(); en.hasMore(); )
            ids.add(en.next().getID());
        java.util.Collections.sort(ids);
        check(ids.equals(java.util.Arrays.asList("Case", "OTHER")), "getAll content");
        Attributes c = (Attributes) a.clone();
        check(c.size() == 2 && c.isCaseIgnored(), "clone contract");
        c.remove("case");
        check(a.size() == 2, "clone independent of original");
    }

    static void dAsPolymorphic() throws Exception {
        Attributes basic = new BasicAttributes();
        basic.put(new BasicAttribute("k", "v"));
        Attributes custom = new DeepAttrs(false);
        custom.put(new BasicAttribute("k", "v"));
        check(basic.size() == custom.size(), "same size");
        check(basic.get("k").get().equals(custom.get("k").get()), "same value");
        check(basic.get("k").getID().equals(custom.get("k").getID()), "same id");
    }

}
