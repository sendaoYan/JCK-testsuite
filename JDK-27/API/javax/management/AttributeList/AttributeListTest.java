/*
 * @test
 * @summary javax.management.AttributeList conformance: white-box structure + black-box behavior.
 * @run main AttributeListTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class AttributeListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeList");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == java.util.ArrayList.class, "extends ArrayList");
        check(hasMethod(c, "asList"), "has asList");
        check(hasMethod(c, "add"), "has add");
        check(hasMethod(c, "addAll"), "has addAll");
        check(hasMethod(c, "set"), "has set");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributeList list = new AttributeList();
        check(list.isEmpty() && list.size() == 0, "starts empty");

        list.add(new Attribute("a", 1));
        list.add(new Attribute("b", 2));
        check(list.size() == 2, "size after add");
        check(list.get(0).equals(new Attribute("a", 1)), "get(0)");
        check(list.get(1).equals(new Attribute("b", 2)), "get(1)");

        // add(Object) returns boolean true (cast forces the Object overload)
        check(list.add((Object) new Attribute("c", 3)), "add(Object) returns true");
        list.add(1, new Attribute("x", 99));
        check(list.get(1).equals(new Attribute("x", 99)), "add(index, attribute)");

        // set(int, Attribute) replaces in place
        list.set(0, new Attribute("A", 10));
        check(list.get(0).equals(new Attribute("A", 10)), "set(int,Attribute)");

        // copy constructors
        List<Attribute> plain = new ArrayList<Attribute>();
        plain.add(new Attribute("p", 1));
        AttributeList fromList = new AttributeList(plain);
        check(fromList.size() == 1 && fromList.get(0).equals(new Attribute("p", 1)), "copy from List<Attribute>");
        AttributeList fromAttrList = new AttributeList(fromList);
        check(fromAttrList.equals(fromList), "copy from AttributeList");

        // addAll(AttributeList)
        AttributeList other = new AttributeList();
        other.add(new Attribute("q", 7));
        check(fromAttrList.addAll(other), "addAll(AttributeList) returns true");
        check(fromAttrList.size() == 2, "size after addAll");

        // asList() exposes typed view with same elements
        List<Attribute> view = fromAttrList.asList();
        check(view.size() == 2, "asList size");
        check(view.get(0) instanceof Attribute, "asList elements typed");

        // type-safety enforced once asList() has been called
        AttributeList typed = new AttributeList();
        typed.add(new Attribute("t", 1));
        typed.asList();
        boolean threw = false;
        try { typed.add("not-an-attribute"); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "type-safe add(Object) rejects non-Attribute");
        check(typed.size() == 1, "rejected element not added");

        // set(int, Object) type-safe rejection
        boolean threwSet = false;
        try { typed.set(0, "not-an-attribute"); } catch (IllegalArgumentException ex) { threwSet = true; }
        check(threwSet, "type-safe set(int,Object) rejects non-Attribute");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
