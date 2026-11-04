/*
 * @test
 * @summary javax.naming.directory.BasicAttribute conformance: white-box structure + black-box behavior.
 * @run main BasicAttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
