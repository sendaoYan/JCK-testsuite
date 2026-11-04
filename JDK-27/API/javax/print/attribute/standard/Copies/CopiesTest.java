/*
 * @test
 * @summary javax.print.attribute.standard.Copies conformance: white-box structure + black-box behavior.
 * @run main CopiesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class CopiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Copies");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(IntegerSyntax.class.isAssignableFrom(c), "extends IntegerSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Copies a = new Copies(5);
        check(a.getValue() == 5, "getValue");
        check(a.getCategory() == Copies.class, "getCategory");
        check("copies".equals(a.getName()), "getName");
        check("5".equals(a.toString()), "toString");
        check(a.equals(new Copies(5)), "equals same value");
        check(a.hashCode() == 5, "hashCode == value");
        check(!a.equals(new Copies(6)), "different value unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(new Copies(1).getValue() == 1, "lower bound ok");
        boolean threw = false; try { new Copies(0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "Copies(0) throws IllegalArgumentException");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
