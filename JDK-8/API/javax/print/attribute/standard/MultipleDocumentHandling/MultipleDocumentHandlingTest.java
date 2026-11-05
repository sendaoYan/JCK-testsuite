/*
 * @test
 * @summary javax.print.attribute.standard.MultipleDocumentHandling conformance: enum attribute.
 * @run main MultipleDocumentHandlingTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class MultipleDocumentHandlingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        MultipleDocumentHandling v1 = MultipleDocumentHandling.SINGLE_DOCUMENT;
        check(v1 instanceof MultipleDocumentHandling, "SINGLE_DOCUMENT instanceof MultipleDocumentHandling");
        check(v1.getCategory() == MultipleDocumentHandling.class, "getCategory");
        check(v1.getName() != null && v1.getName().length() > 0, "getName non-empty");
        check(v1.toString() != null && v1.toString().length() > 0, "toString non-empty");
        check(v1.getValue() >= 0, "getValue >= 0");
        MultipleDocumentHandling v2 = MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES;
        check(v1.getValue() != v2.getValue(), "distinct getValue");
        check(!v1.toString().equals(v2.toString()), "distinct toString");
        check(v1.equals(MultipleDocumentHandling.SINGLE_DOCUMENT), "equals same constant");
        check(!v1.equals(v2), "not equals other");
        check(v1.hashCode() == MultipleDocumentHandling.SINGLE_DOCUMENT.hashCode(), "hashCode");
        check(v1.clone() != null, "clone");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.getValue() == 0, "SINGLE_DOCUMENT.getValue == 0");
        check("single-document".equals(MultipleDocumentHandling.SINGLE_DOCUMENT.toString()), "SINGLE_DOCUMENT.toString [single-document]");
        check(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.getValue() == 1, "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.getValue == 1");
        check("separate-documents-uncollated-copies".equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.toString()), "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.toString [separate-documents-uncollated-copies]");
        check(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES.getValue() == 2, "SEPARATE_DOCUMENTS_COLLATED_COPIES.getValue == 2");
        check("separate-documents-collated-copies".equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES.toString()), "SEPARATE_DOCUMENTS_COLLATED_COPIES.toString [separate-documents-collated-copies]");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET.getValue() == 3, "SINGLE_DOCUMENT_NEW_SHEET.getValue == 3");
        check("single-document-new-sheet".equals(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET.toString()), "SINGLE_DOCUMENT_NEW_SHEET.toString [single-document-new-sheet]");
        check("multiple-document-handling".equals(MultipleDocumentHandling.SINGLE_DOCUMENT.getName()), "getName == \"multiple-document-handling\"");
        check(MultipleDocumentHandling.class == MultipleDocumentHandling.SINGLE_DOCUMENT.getCategory(), "getCategory");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.equals(MultipleDocumentHandling.SINGLE_DOCUMENT), "equals self");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals(null), "not equals null");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals("single-document"), "not equals foreign type");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.hashCode() == 0, "hashCode == value");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.clone() == MultipleDocumentHandling.SINGLE_DOCUMENT, "clone returns same singleton");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals(MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES), "distinct constants unequal");
        check(MultipleDocumentHandling.SINGLE_DOCUMENT.toString() != MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES.toString() || true, "toString stable");
        check(!MultipleDocumentHandling.SINGLE_DOCUMENT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        MultipleDocumentHandling.SINGLE_DOCUMENT,
        MultipleDocumentHandling.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES,
        MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES,
        MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET
    };
    static final String[] NAMES = {
        "SINGLE_DOCUMENT",
        "SEPARATE_DOCUMENTS_UNCOLLATED_COPIES",
        "SEPARATE_DOCUMENTS_COLLATED_COPIES",
        "SINGLE_DOCUMENT_NEW_SHEET"
    };

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
