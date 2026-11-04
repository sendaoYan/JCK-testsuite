/*
 * @test
 * @summary javax.print.attribute.standard.PrinterResolution conformance: white-box structure + black-box behavior.
 * @run main PrinterResolutionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterResolutionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterResolution");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(ResolutionSyntax.class.isAssignableFrom(c), "extends ResolutionSyntax");
        check(DocAttribute.class.isAssignableFrom(c) && PrintRequestAttribute.class.isAssignableFrom(c) && PrintJobAttribute.class.isAssignableFrom(c), "implements interfaces");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PrinterResolution r = new PrinterResolution(300, 600, ResolutionSyntax.DPI);
        check(r.getCrossFeedResolution(ResolutionSyntax.DPI) == 300 && r.getFeedResolution(ResolutionSyntax.DPI) == 600, "getCrossFeed/getFeed");
        int[] v = r.getResolution(ResolutionSyntax.DPI);
        check(v.length == 2 && v[0] == 300 && v[1] == 600, "getResolution");
        check("300x600 dpi".equals(r.toString(ResolutionSyntax.DPI, "dpi")), "toString(DPI)");
        check("30000x60000 dphi".equals(r.toString()), "toString default");
        check("printer-resolution".equals(r.getName()) && r.getCategory() == PrinterResolution.class, "getCategory/getName");
        check(r.lessThanOrEquals(new PrinterResolution(300, 600, ResolutionSyntax.DPI)), "lessThanOrEquals equal");
        check(r.lessThanOrEquals(new PrinterResolution(600, 600, ResolutionSyntax.DPI)), "lessThanOrEquals larger");
        check(!r.lessThanOrEquals(new PrinterResolution(300, 300, ResolutionSyntax.DPI)), "not lessThanOrEquals smaller feed");
        check(r.equals(new PrinterResolution(300, 600, ResolutionSyntax.DPI)), "equals");
        check(!r.equals(new PrinterResolution(300, 300, ResolutionSyntax.DPI)), "different unequal");
        check(!r.equals(null) && !r.equals("x"), "not equals null/foreign");
        boolean threw = false; try { new PrinterResolution(0, 600, ResolutionSyntax.DPI); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero crossfeed IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
