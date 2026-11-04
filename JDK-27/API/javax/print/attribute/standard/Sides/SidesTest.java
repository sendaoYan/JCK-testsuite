/*
 * @test
 * @summary javax.print.attribute.standard.Sides conformance: white-box structure + black-box behavior.
 * @run main SidesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SidesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Sides");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Sides.ONE_SIDED.getCategory() == Sides.class, "getCategory");
        check("sides".equals(Sides.ONE_SIDED.getName()), "getName");
        check(Sides.ONE_SIDED.getValue() == 0, "ONE_SIDED value == 0");
        check("one-sided".equals(Sides.ONE_SIDED.toString()), "ONE_SIDED toString");
        check(Sides.TWO_SIDED_LONG_EDGE.getValue() == 1, "TWO_SIDED_LONG_EDGE value == 1");
        check("two-sided-long-edge".equals(Sides.TWO_SIDED_LONG_EDGE.toString()), "TWO_SIDED_LONG_EDGE toString");
        check(Sides.TWO_SIDED_SHORT_EDGE.getValue() == 2, "TWO_SIDED_SHORT_EDGE value == 2");
        check("two-sided-short-edge".equals(Sides.TWO_SIDED_SHORT_EDGE.toString()), "TWO_SIDED_SHORT_EDGE toString");
        check(Sides.DUPLEX.getValue() == 1, "DUPLEX value == 1");
        check("two-sided-long-edge".equals(Sides.DUPLEX.toString()), "DUPLEX toString");
        check(Sides.TUMBLE.getValue() == 2, "TUMBLE value == 2");
        check("two-sided-short-edge".equals(Sides.TUMBLE.toString()), "TUMBLE toString");
        check(Sides.ONE_SIDED.clone() == Sides.ONE_SIDED, "clone returns singleton");
        check(Sides.ONE_SIDED.hashCode() == 0, "hashCode == value");
        check(Sides.ONE_SIDED.equals(Sides.ONE_SIDED), "equals self");
        check(!Sides.ONE_SIDED.equals(Sides.TWO_SIDED_LONG_EDGE), "distinct constants unequal");
        check(!Sides.ONE_SIDED.equals(null) && !Sides.ONE_SIDED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
