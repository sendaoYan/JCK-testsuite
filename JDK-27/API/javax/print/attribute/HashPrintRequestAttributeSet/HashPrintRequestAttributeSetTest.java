/*
 * @test
 * @summary javax.print.attribute.HashPrintRequestAttributeSet conformance: white-box structure + black-box behavior.
 * @run main HashPrintRequestAttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class HashPrintRequestAttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.HashPrintRequestAttributeSet");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(HashAttributeSet.class.isAssignableFrom(c), "extends HashAttributeSet");
        check(PrintRequestAttributeSet.class.isAssignableFrom(c), "implements PrintRequestAttributeSet");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashPrintRequestAttributeSet s = new HashPrintRequestAttributeSet();
        check(s.isEmpty() && s.size() == 0, "empty");
        check(s.add(new Copies(2)), "add returns changed");
        check(!s.add(new Copies(2)), "re-add same returns false");
        check(s.size() == 1 && s.containsKey(Copies.class) && s.containsValue(new Copies(2)), "contains");
        check(new Copies(2).equals(s.get(Copies.class)), "get");
        s.add(new JobName("j", Locale.ROOT));
        check(s.size() == 2, "two attributes");
        Attribute[] arr = s.toArray();
        check(arr.length == 2, "toArray");
        s.remove(Copies.class);
        check(s.size() == 1 && !s.containsKey(Copies.class), "remove(Class)");
        s.clear();
        check(s.isEmpty(), "clear");
        HashPrintRequestAttributeSet other = new HashPrintRequestAttributeSet(new Copies(2));
        check(!s.equals(other) && !s.equals(null) && !s.equals("x"), "equals");
        check(s.hashCode() == s.hashCode(), "hashCode");
        boolean threw = false; try { s.add(new PagesPerMinute(5)); } catch (ClassCastException e) { threw = true; }
        check(threw, "wrong category throws ClassCastException");
        check(new HashPrintRequestAttributeSet(new Copies[]{ new Copies(2) }).size() == 1, "array ctor");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
