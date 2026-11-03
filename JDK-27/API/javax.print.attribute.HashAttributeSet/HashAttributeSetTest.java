/*
 * @test
 * @summary javax.print.attribute.HashAttributeSet conformance: white-box structure + black-box behavior.
 * @run main HashAttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class HashAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.HashAttributeSet");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(AttributeSet.class.isAssignableFrom(c), "implements AttributeSet");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashAttributeSet s = new HashAttributeSet();
        check(s.isEmpty() && s.size() == 0, "empty");
        s.add(new Copies(2));
        s.add(new JobName("n", Locale.ROOT));
        check(s.size() == 2, "size");
        check(s.get(Copies.class).equals(new Copies(2)), "get by category");
        check(s.get(Media.class) == null, "absent category null");
        check(s.containsKey(Copies.class) && !s.containsKey(Media.class), "containsKey");
        check(s.containsValue(new Copies(2)) && !s.containsValue(new Copies(9)), "containsValue");
        s.add(new Copies(5));
        check(s.size() == 2 && new Copies(5).equals(s.get(Copies.class)), "replace in same category");
        Attribute[] arr = s.toArray();
        check(arr.length == 2, "toArray");
        HashAttributeSet s2 = new HashAttributeSet(new Copies(5));
        s2.add(new JobName("n", Locale.ROOT));
        check(s.equals(s2) && s.hashCode() == s2.hashCode(), "equals/hashCode independent of insert order");
        check(!s.equals(null) && !s.equals("x"), "not equals null/foreign");
        check(new HashAttributeSet(new Attribute[]{new Copies(1)}).size() == 1, "Attribute[] ctor");
        check(new HashAttributeSet(s2).equals(s2), "AttributeSet ctor");
        s.remove(Copies.class);
        check(s.size() == 1 && s.remove(new JobName("n", Locale.ROOT)), "remove");
        s.clear();
        check(s.isEmpty(), "clear");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
