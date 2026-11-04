/*
 * @test
 * @summary javax.print.attribute.standard.PrinterMoreInfoManufacturer conformance: white-box structure + black-box behavior.
 * @run main PrinterMoreInfoManufacturerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrinterMoreInfoManufacturerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrinterMoreInfoManufacturer");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(URISyntax.class.isAssignableFrom(c), "extends URISyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.URI u = java.net.URI.create("ipp://host/printer");
        PrinterMoreInfoManufacturer a = new PrinterMoreInfoManufacturer(u);
        check(u.equals(a.getURI()), "getURI");
        check("printer-more-info-manufacturer".equals(a.getName()), "getName");
        check(a.getCategory() == PrinterMoreInfoManufacturer.class, "getCategory");
        check(u.toString().equals(a.toString()), "toString == uri");
        check(a.equals(new PrinterMoreInfoManufacturer(java.net.URI.create("ipp://host/printer"))), "equals same uri");
        check(!a.equals(new PrinterMoreInfoManufacturer(java.net.URI.create("ipp://other/printer"))), "different uri unequal");
        check(!a.equals(null) && !a.equals("x"), "not equals null/foreign");
        check(a.hashCode() == new PrinterMoreInfoManufacturer(java.net.URI.create("ipp://host/printer")).hashCode(), "hashCode consistent");
        boolean threw = false; try { new PrinterMoreInfoManufacturer(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null uri throws NPE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
