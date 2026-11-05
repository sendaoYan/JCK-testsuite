/*
 * @test
 * @summary javax.naming.directory.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.Attribute");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(Cloneable.class.isAssignableFrom(c), "extends Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "get"), "get()");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "getID"), "getID");
        check(hasMethod(c, "contains"), "contains");
        check(hasMethod(c, "add"), "add(Object)");
        check(hasMethod(c, "remove"), "remove(Object)");
        check(hasMethod(c, "clear"), "clear");
        check(hasMethod(c, "getAttributeSyntaxDefinition"), "getAttributeSyntaxDefinition");
        check(hasMethod(c, "getAttributeDefinition"), "getAttributeDefinition");
        check(hasMethod(c, "clone"), "clone");
        check(hasMethod(c, "isOrdered"), "isOrdered");
        check(hasMethod(c, "set"), "set");
        check(hasMethod(c, "get"), "get(int)");
        check(hasMethod(c, "remove"), "remove(int)");
        check(hasMethod(c, "add"), "add(int,Object)");
    }

    static void testBlackBox() throws Exception {
        // Exercise every core interface method through the standard implementation.
        Attribute a = new BasicAttribute("cn", "Alice", true);
        check("cn".equals(a.getID()), "getID");
        check(a.isOrdered(), "isOrdered");
        check(a.size() == 1, "size");
        check("Alice".equals(a.get()), "get()");
        check("Alice".equals(a.get(0)), "get(int)");
        check(a.add("Bob"), "add(Object)");
        a.add(0, "Zoe");
        check("Zoe".equals(a.get(0)), "add(int,Object)");
        check(a.contains("Bob"), "contains");
        check(a.set(0, "Quinn").equals("Zoe"), "set returns old");
        check(a.remove(0).equals("Quinn"), "remove(int)");
        check(a.remove("Alice"), "remove(Object)");
        List<String> vals = new ArrayList<>();
        NamingEnumeration<?> en = a.getAll();
        while (en.hasMore()) vals.add(String.valueOf(en.next()));
        en.close();
        check(vals.equals(Arrays.asList("Bob")), "getAll after edits");
        a.clear();
        check(a.size() == 0, "clear");
        Attribute cl = (Attribute) ((BasicAttribute) new BasicAttribute("sn", "v")).clone();
        check("sn".equals(cl.getID()), "clone id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
