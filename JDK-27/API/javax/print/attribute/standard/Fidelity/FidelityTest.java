/*
 * @test
 * @summary javax.print.attribute.standard.Fidelity conformance: white-box structure + black-box behavior.
 * @run main FidelityTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class FidelityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Fidelity");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Fidelity.FIDELITY_TRUE.getCategory() == Fidelity.class, "getCategory");
        check("ipp-attribute-fidelity".equals(Fidelity.FIDELITY_TRUE.getName()), "getName");
        check(Fidelity.FIDELITY_TRUE.getValue() == 0, "FIDELITY_TRUE value == 0");
        check("true".equals(Fidelity.FIDELITY_TRUE.toString()), "FIDELITY_TRUE toString");
        check(Fidelity.FIDELITY_FALSE.getValue() == 1, "FIDELITY_FALSE value == 1");
        check("false".equals(Fidelity.FIDELITY_FALSE.toString()), "FIDELITY_FALSE toString");
        check(Fidelity.FIDELITY_TRUE.clone() == Fidelity.FIDELITY_TRUE, "clone returns singleton");
        check(Fidelity.FIDELITY_TRUE.hashCode() == 0, "hashCode == value");
        check(Fidelity.FIDELITY_TRUE.equals(Fidelity.FIDELITY_TRUE), "equals self");
        check(!Fidelity.FIDELITY_TRUE.equals(Fidelity.FIDELITY_FALSE), "distinct constants unequal");
        check(!Fidelity.FIDELITY_TRUE.equals(null) && !Fidelity.FIDELITY_TRUE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
