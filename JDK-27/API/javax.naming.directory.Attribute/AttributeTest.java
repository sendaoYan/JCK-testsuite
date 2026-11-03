/*
 * @test
 * @summary javax.naming.directory.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
