/*
 * @test
 * @summary javax.naming.directory.Attributes conformance: interface hierarchy and members.
 * @run main AttributesTest
 */



import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
public class AttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attributes");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        expIfs.add("java.lang.Cloneable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("get"), "has method get");
        check(names.contains("getAll"), "has method getAll");
        check(names.contains("getIDs"), "has method getIDs");        testDeepContracts();
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
