/*
 * @test
 * @summary javax.naming.directory.Attribute conformance: interface hierarchy and members.
 * @run main AttributeTest
 */



import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attribute");
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
        check(names.contains("add"), "has method add");
        check(names.contains("clear"), "has method clear");
        check(names.contains("contains"), "has method contains");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dAtInterfaceSurface();
        dAtCustomImplementation();
        dAtPolymorphic();
        System.out.println("deep: Attribute contracts OK");
    }

    /** Minimal hand-written implementation proves the interface is complete. */
    static class DeepAttr implements Attribute {
        final String id;
        final java.util.List<Object> vals = new java.util.ArrayList<>();
        DeepAttr(String id) { this.id = id; }
        public NamingEnumeration<?> getAll() {
            return new NamingEnumeration<Object>() {
                final java.util.Iterator<Object> it = vals.iterator();
                public Object next() { return it.next(); }
                public boolean hasMore() { return it.hasNext(); }
                public void close() { }
                public Object nextElement() { return it.next(); }
                public boolean hasMoreElements() { return it.hasNext(); }
            };
        }
        public Object get() { if (vals.isEmpty()) throw new java.util.NoSuchElementException(id); return vals.get(0); }
        public String getID() { return id; }
        public int size() { return vals.size(); }
        public boolean add(Object v) { return vals.add(v); }
        public boolean remove(Object v) { return vals.remove(v); }
        public void clear() { vals.clear(); }
        public boolean isOrdered() { return false; }
        public DirContext getAttributeSyntaxDefinition() throws javax.naming.NamingException { throw new OperationNotSupportedException("syntax"); }
        public DirContext getAttributeDefinition() throws javax.naming.NamingException { throw new OperationNotSupportedException("definition"); }
        public boolean contains(Object v) { return vals.contains(v); }
        public Object clone() {
            DeepAttr c = new DeepAttr(id);
            c.vals.addAll(vals);
            return c;
        }
        public Object get(int ix) { return vals.get(ix); }
        public Object remove(int ix) { return vals.remove(ix); }
        public void add(int ix, Object v) { vals.add(ix, v); }
        public Object set(int ix, Object v) { return vals.set(ix, v); }
    }

    static void dAtInterfaceSurface() throws Exception {
        DeepAttr a = new DeepAttr("cn");
        check("cn".equals(a.getID()), "getID");
        check(a.size() == 0, "size starts 0");
        check(a.add("v1") && a.add("v2"), "add grows");
        check(a.size() == 2, "size reflects adds");
        check("v1".equals(a.get()), "get returns a value");
        check(a.remove("v1"), "remove by value");
        check(!a.remove("absent"), "remove absent false");
        check(!a.isOrdered(), "custom impl reports unordered");
    }

    static void dAtCustomImplementation() throws Exception {
        DeepAttr a = new DeepAttr("k");
        a.add("x");
        java.util.List<Object> seen = new java.util.ArrayList<>();
        for (NamingEnumeration<?> en = a.getAll(); en.hasMore(); ) seen.add(en.next());
        check(seen.equals(java.util.Arrays.asList("x")), "getAll through custom impl");
        a.clear();
        check(a.size() == 0, "clear");
        boolean nsee = false;
        try { a.get(); } catch (java.util.NoSuchElementException e) { nsee = true; }
        check(nsee, "empty get throws in custom impl too");
        // schema methods declared on the interface
        boolean osn = false;
        try { a.getAttributeDefinition(); }
        catch (OperationNotSupportedException e) { osn = true; }
        check(osn, "getAttributeDefinition declared");
        osn = false;
        try { a.getAttributeSyntaxDefinition(); }
        catch (OperationNotSupportedException e) { osn = true; }
        check(osn, "getAttributeSyntaxDefinition declared");
        // equals/hashCode are Object defaults unless overridden
        check(!a.equals(new DeepAttr("k")), "Object equals default for custom impl");
    }

    static void dAtPolymorphic() throws Exception {
        // BasicAttribute and custom impl interoperate through the interface type
        Attribute basic = new BasicAttribute("cn", "shared");
        Attribute custom = new DeepAttr("cn");
        custom.add("shared");
        check(basic.size() == custom.size(), "same size across impls");
        check(basic.get().equals(custom.get()), "same value across impls");
        check(basic.getID().equals(custom.getID()), "same id across impls");
    }

}
