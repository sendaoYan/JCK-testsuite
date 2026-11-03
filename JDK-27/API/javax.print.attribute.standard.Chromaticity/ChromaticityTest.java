/*
 * @test
 * @summary javax.print.attribute.standard.Chromaticity conformance: white-box structure + black-box behavior.
 * @run main ChromaticityTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ChromaticityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Chromaticity");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Chromaticity.MONOCHROME.getCategory() == Chromaticity.class, "getCategory");
        check("chromaticity".equals(Chromaticity.MONOCHROME.getName()), "getName");
        check(Chromaticity.MONOCHROME.getValue() == 0, "MONOCHROME value == 0");
        check("monochrome".equals(Chromaticity.MONOCHROME.toString()), "MONOCHROME toString");
        check(Chromaticity.COLOR.getValue() == 1, "COLOR value == 1");
        check("color".equals(Chromaticity.COLOR.toString()), "COLOR toString");
        check(Chromaticity.MONOCHROME.clone() == Chromaticity.MONOCHROME, "clone returns singleton");
        check(Chromaticity.MONOCHROME.hashCode() == 0, "hashCode == value");
        check(Chromaticity.MONOCHROME.equals(Chromaticity.MONOCHROME), "equals self");
        check(!Chromaticity.MONOCHROME.equals(Chromaticity.COLOR), "distinct constants unequal");
        check(!Chromaticity.MONOCHROME.equals(null) && !Chromaticity.MONOCHROME.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
