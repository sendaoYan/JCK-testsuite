/*
 * @test
 * @summary javax.print.attribute.AttributeSet conformance: white-box structure + black-box behavior.
 * @run main AttributeSetTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.AttributeSet");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "containsValue"), "declares containsValue");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "size"), "declares size");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(hasMethod(c, "clear"), "declares clear");
        check(hasMethod(c, "isEmpty"), "declares isEmpty");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashAttributeSet s = new HashAttributeSet();
        s.add(new Copies(2));
        check(s.size() == 1 && s.containsKey(Copies.class) && s.containsValue(new Copies(2)), "AttributeSet contract");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
