/*
 * @test
 * @summary javax.print.attribute.standard.MultipleDocumentHandling conformance: white-box structure + black-box behavior.
 * @run main MultipleDocumentHandlingTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class MultipleDocumentHandlingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.MultipleDocumentHandling");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.getCategory() == MultipleDocumentHandling.class, "getCategory");
        check("multiple-document-handling".equals(MultipleDocumentHandling.SINGLE_DOCUMENT.getName()), "getName");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.getValue() == 0, "SINGLE_DOCUMENT value == 0");
        check("single-document".equals(MultipleDocumentHandling.SINGLE_DOCUMENT.toString()), "SINGLE_DOCUMENT toString");
        check(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.getValue() == 1, "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES value == 1");
        check("separate-documents-uncollated-copies".equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.toString()), "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES toString");
        check(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES.getValue() == 2, "SEPARATE_DOCUMENTS_COLLATED_COPIES value == 2");
        check("separate-documents-collated-copies".equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES.toString()), "SEPARATE_DOCUMENTS_COLLATED_COPIES toString");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET.getValue() == 3, "SINGLE_DOCUMENT_NEW_SHEET value == 3");
        check("single-document-new-sheet".equals(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET.toString()), "SINGLE_DOCUMENT_NEW_SHEET toString");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.clone() == MultipleDocumentHandling.SINGLE_DOCUMENT, "clone returns singleton");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.hashCode() == 0, "hashCode == value");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.equals(MultipleDocumentHandling.SINGLE_DOCUMENT), "equals self");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES), "distinct constants unequal");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals(null) && !MultipleDocumentHandling.SINGLE_DOCUMENT.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
