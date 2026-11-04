/*
 * @test
 * @summary javax.print.attribute.Attribute conformance: white-box structure + black-box behavior.
 * @run main AttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.Attribute");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "getCategory"), "declares getCategory");
        check(hasMethod(c, "getName"), "declares getName");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Attribute a = new Copies(2);
        check(a.getCategory() == Copies.class && "copies".equals(a.getName()), "Attribute contract");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
