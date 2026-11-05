/*
 * @test
 * @summary javax.naming.directory.BasicAttribute conformance: white-box structure + black-box behavior.
 * @run main BasicAttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class BasicAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.BasicAttribute");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Attribute.class.isAssignableFrom(c), "implements Attribute");
        check(hasMethod(c, "getID"), "getID");
        check(hasMethod(c, "getAll"), "getAll");
        check(hasMethod(c, "get"), "get");
        check(hasMethod(c, "size"), "size");
        check(hasMethod(c, "contains"), "contains");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "clear"), "clear");
        check(hasMethod(c, "isOrdered"), "isOrdered");
        check(hasMethod(c, "get"), "get(int)");
        check(hasMethod(c, "remove"), "remove(int)");
        check(hasMethod(c, "set"), "set");
        check(hasMethod(c, "clone"), "clone");
        check(hasMethod(c, "equals"), "equals");
        check(hasMethod(c, "hashCode"), "hashCode");
        check(hasMethod(c, "toString"), "toString");
        check(hasMethod(c, "getAttributeSyntaxDefinition"), "getAttributeSyntaxDefinition");
        check(hasMethod(c, "getAttributeDefinition"), "getAttributeDefinition");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class), "ctor(String,boolean)");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == Object.class && x.getParameterTypes()[2] == boolean.class), "ctor(String,Object,boolean)");
    }

    static void testBlackBox() throws Exception {
        // ordered attribute lifecycle
        BasicAttribute a = new BasicAttribute("cn", "Alice", true);
        check("cn".equals(a.getID()), "id");
        check(a.isOrdered(), "ordered");
        check(a.size() == 1, "size 1");
        check("Alice".equals(a.get()), "get first");
        check(a.get(0).equals("Alice"), "get(0)");

        a.add("Bob");
        check(a.size() == 2, "size 2 after add");
        check(a.get(1).equals("Bob"), "get(1)");
        check(a.get().equals("Alice"), "get still first");

        a.add(0, "Zoe");
        check(a.size() == 3, "size 3 after add(0)");
        check(a.get(0).equals("Zoe"), "add at 0");

        check(a.contains("Zoe"), "contains Zoe");
        check(!a.contains("Nobody"), "not contains Nobody");

        check(a.set(0, "Quinn").equals("Zoe"), "set returns old");
        check(a.get(0).equals("Quinn"), "set new value");

        check(a.remove(0).equals("Quinn"), "remove(int) returns value");
        check(a.size() == 2, "size after remove(int)");

        check(a.remove("Alice"), "remove(Object) true");
        check(!a.remove("Alice"), "remove(Object) false when absent");

        a.clear();
        check(a.size() == 0, "clear empties");

        // unordered attribute
        BasicAttribute u = new BasicAttribute("cn", "x", false);
        check(!u.isOrdered(), "unordered");
        check(!u.add("x"), "unordered duplicate add returns false");
        check(u.size() == 1, "unordered size stays 1");
        try {
            u.add(0, "x");
            check(false, "unordered add(int,dup) should throw");
        } catch (IllegalStateException expected) {
            check(true, "unordered add(int,dup) throws IllegalStateException");
        }

        // index bounds
        try {
            a.get(99);
            check(false, "get(99) should throw");
        } catch (IndexOutOfBoundsException expected) {
            check(true, "get(99) throws IndexOutOfBoundsException");
        }

        // schema methods default to OperationNotSupportedException
        try {
            a.getAttributeSyntaxDefinition();
            check(false, "getAttributeSyntaxDefinition should throw");
        } catch (OperationNotSupportedException expected) {
            check(true, "getAttributeSyntaxDefinition throws OperationNotSupportedException");
        }
        try {
            a.getAttributeDefinition();
            check(false, "getAttributeDefinition should throw");
        } catch (OperationNotSupportedException expected) {
            check(true, "getAttributeDefinition throws OperationNotSupportedException");
        }

        // equals / hashCode / clone
        BasicAttribute e1 = new BasicAttribute("cn", "v1");
        e1.add("v2");
        BasicAttribute e2 = new BasicAttribute("cn", "v1");
        e2.add("v2");
        check(e1.equals(e2), "equals same id+values");
        check(e1.hashCode() == e2.hashCode(), "hashCode same");
        BasicAttribute e3 = new BasicAttribute("sn", "v1");
        e3.add("v2");
        check(!e1.equals(e3), "equals different id false");

        BasicAttribute cl = (BasicAttribute) e1.clone();
        check(cl != e1, "clone distinct instance");
        check(cl.equals(e1), "clone equals original");
        check("cn".equals(cl.getID()), "clone id");
        check(cl.size() == 2, "clone size");

        // getAll enumeration
        List<String> all = new ArrayList<>();
        NamingEnumeration<?> en = e1.getAll();
        while (en.hasMore()) all.add(String.valueOf(en.next()));
        en.close();
        check(all.equals(Arrays.asList("v1", "v2")), "getAll ordered values");

        // toString
        check(a.toString().contains("cn"), "toString contains id");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBaConstructors();
        dBaValues();
        dBaOrdering();
        dBaEquality();
        dBaNegative();
        dBaCloneAndSerialize();
        System.out.println("deep: BasicAttribute contracts OK");
    }

    static void dBaConstructors() throws Exception {
        check("cn".equals(new BasicAttribute("cn").getID()), "id-only ctor");
        check(new BasicAttribute("cn").size() == 0, "id-only ctor empty");
        check(!new BasicAttribute("cn").isOrdered(), "default unordered");
        check(new BasicAttribute("cn", true).isOrdered(), "ordered flag ctor");
        check(!new BasicAttribute("cn", "v", true).isOrdered() == false, "value+ordered flag");
        BasicAttribute withVal = new BasicAttribute("cn", "v");
        check(withVal.size() == 1 && "v".equals(withVal.get()), "single value ctor");
        // null value is recorded as an explicit null value
        BasicAttribute nullVal = new BasicAttribute("cn", null);
        check(nullVal.size() == 1 && nullVal.get() == null, "null value recorded");
    }

    static void dBaValues() throws Exception {
        BasicAttribute a = new BasicAttribute("cn");
        check(a.add("a"), "first add true");
        check(a.add("a") == false, "unordered duplicate add rejected");
        check(a.add("b"), "distinct add true");
        check(a.size() == 2, "size counts distinct only");
        check(a.contains("a") && a.contains("b"), "contains");
        check(!a.contains("c"), "contains absent");
        check("a".equals(a.get()), "get() returns first value");
        check("b".equals(a.get(1)), "get(index)");
        check(a.remove("a"), "remove by value");
        check(!a.remove("a"), "remove absent value false");
        check(a.size() == 1, "size after remove");
        // array values compare element-wise
        BasicAttribute arr = new BasicAttribute("photo");
        byte[] b1 = {1, 2, 3}, b2 = {1, 2, 3};
        arr.add(b1);
        check(arr.contains(b2), "array equality element-wise for contains");
        check(!arr.contains(new byte[]{1, 2}), "different-length array not equal");
        check(arr.remove(b2), "remove by equal (not identical) array");
        // enumeration of values
        BasicAttribute e = new BasicAttribute("m");
        e.add("x"); e.add("y");
        java.util.List<Object> seen = new java.util.ArrayList<>();
        for (NamingEnumeration<?> en = e.getAll(); en.hasMore(); ) seen.add(en.next());
        check(seen.size() == 2 && seen.contains("x") && seen.contains("y"), "getAll enumerates");
        e.clear();
        check(e.size() == 0, "clear empties");
        check(e.getAll().hasMore() == false, "getAll empty after clear");
    }

    static void dBaOrdering() throws Exception {
        BasicAttribute o = new BasicAttribute("cn", true);
        o.add("a"); o.add("b"); o.add("a");
        check(o.size() == 3, "ordered attribute keeps duplicates");
        o.add(1, "mid");
        check("mid".equals(o.get(1)), "insert at index");
        check("a".equals(o.get(0)) && "b".equals(o.get(2)), "shifted values");
        Object prev = o.set(0, "z");
        check("a".equals(prev), "set returns previous value");
        check("z".equals(o.get(0)), "set replaces in place");
        Object removed = o.remove(1);
        check("mid".equals(removed), "remove(int) returns removed value");
        // ordered add at index allows duplicates
        o.add(0, "z");
        check(o.size() == 4 && "z".equals(o.get(0)), "duplicate insert OK when ordered");
        // unordered add(int)/set reject duplicates with ISE
        BasicAttribute u = new BasicAttribute("cn");
        u.add("only");
        boolean ise1 = false, ise2 = false;
        try { u.add(0, "only"); }
        catch (IllegalStateException ex) {
            ise1 = true;
            check("Cannot add duplicate to unordered attribute".equals(ex.getMessage()),
                    "ISE message for add(int)");
        }
        try { u.set(0, "only"); }
        catch (IllegalStateException ex) {
            ise2 = true;
            check("Cannot add duplicate to unordered attribute".equals(ex.getMessage()),
                    "ISE message for set");
        }
        check(ise1 && ise2, "unordered duplicate mutation rejected");
        // out-of-range index -> ArrayIndexOutOfBoundsException
        boolean aioobe = false;
        try { u.get(5); } catch (ArrayIndexOutOfBoundsException ex) { aioobe = true; }
        check(aioobe, "get(out of range)");
    }

    static void dBaEquality() {
        BasicAttribute a = new BasicAttribute("cn"); a.add("x"); a.add("y");
        BasicAttribute b = new BasicAttribute("cn"); b.add("y"); b.add("x");
        check(a.equals(b) && b.equals(a), "unordered equality ignores order");
        check(a.hashCode() == b.hashCode(), "hashCode agrees across order");
        check(!a.equals(new BasicAttribute("sn")), "different id not equal");
        check(!a.equals(new BasicAttribute("cn")), "different size not equal");
        // id comparison is case sensitive
        BasicAttribute lower = new BasicAttribute("cn", "v");
        BasicAttribute upper = new BasicAttribute("CN", "v");
        check(!lower.equals(upper), "id comparison case-sensitive");
        // ordered vs unordered with same values are not equal
        BasicAttribute ord = new BasicAttribute("cn", true);
        ord.add("x"); ord.add("y");
        check(!a.equals(ord), "ordered flag participates in equality");
        // ordered equality is position sensitive
        BasicAttribute o1 = new BasicAttribute("cn", true);
        o1.add("x"); o1.add("y");
        BasicAttribute o2 = new BasicAttribute("cn", true);
        o2.add("y"); o2.add("x");
        check(!o1.equals(o2), "ordered equality respects order");
        check(o1.equals(new BasicAttribute("cn", true) {{ add("x"); add("y"); }}),
                "ordered equal contents");
        // null values compare equal
        BasicAttribute n1 = new BasicAttribute("cn", null);
        BasicAttribute n2 = new BasicAttribute("cn", null);
        check(n1.equals(n2), "null values equal");
        // byte[] values compare element-wise
        BasicAttribute p1 = new BasicAttribute("photo", new byte[]{1, 2});
        BasicAttribute p2 = new BasicAttribute("photo", new byte[]{1, 2});
        check(p1.equals(p2), "array values equal element-wise");
        // equals with byte[] vs different content
        BasicAttribute p3 = new BasicAttribute("photo", new byte[]{1, 3});
        check(!p1.equals(p3), "array values differ");
        check(!a.equals(null) && !a.equals("cn"), "non-Attribute never equal");
    }

    static void dBaNegative() throws Exception {
        BasicAttribute empty = new BasicAttribute("cn");
        boolean nsee = false;
        try { empty.get(); }
        catch (java.util.NoSuchElementException e) {
            nsee = true;
            check(e.getMessage() != null && e.getMessage().contains("has no value"),
                    "empty get() message mentions attribute");
        }
        check(nsee, "get() on empty -> NoSuchElementException");
        // schema accessors are unsupported by default
        boolean osn1 = false, osn2 = false;
        try { empty.getAttributeDefinition(); }
        catch (OperationNotSupportedException e) { osn1 = true; }
        try { empty.getAttributeSyntaxDefinition(); }
        catch (OperationNotSupportedException e) { osn2 = true; }
        check(osn1 && osn2, "schema methods unsupported by default");
        // remove(int) out of range
        boolean aioobe = false;
        try { empty.remove(0); }
        catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "remove(int) out of range");
        // toString shape
        BasicAttribute t = new BasicAttribute("cn"); t.add("v1"); t.add("v2");
        check(t.toString().startsWith("cn: "), "toString starts with id");
        check(t.toString().contains("v1") && t.toString().contains("v2"), "toString lists values");
    }

    static void dBaCloneAndSerialize() throws Exception {
        BasicAttribute a = new BasicAttribute("cn", true);
        a.add("v1"); a.add("v2");
        BasicAttribute clone = (BasicAttribute) a.clone();
        check(a.equals(clone), "clone equals original");
        clone.add("v3");
        check(a.size() == 2 && clone.size() == 3, "clone structurally independent");
        check(clone.isOrdered() == a.isOrdered(), "clone preserves ordered flag");
        // serialization round-trip (custom wire format)
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(a); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            BasicAttribute back = (BasicAttribute) ois.readObject();
            check(back.isOrdered(), "ordered flag survives serialization");
            check(back.size() == 2 && back.contains("v1") && back.contains("v2"),
                    "values survive serialization");
            check(a.equals(back), "round-trip equals original");
        }
    }

}
