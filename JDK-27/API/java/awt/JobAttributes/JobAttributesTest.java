/*
 * @test
 * @summary java.awt.JobAttributes conformance: white-box structure + black-box behavior.
 * @run main JobAttributesTest
 */

import java.lang.reflect.*;

public class JobAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.JobAttributes", false, JobAttributesTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.JobAttributes");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(hasInterface(c, "java.lang.Cloneable"), "implements Cloneable");
        check(hasMethod(c, "getCopies"), "declares getCopies");
        check(hasMethod(c, "setPageRanges"), "declares setPageRanges");
        check(hasMethod(c, "getSides"), "declares getSides");
        check(hasMethod(c, "getDefaultSelection"), "declares getDefaultSelection");
    }

    static void testBlackBox() throws Exception {
        java.awt.JobAttributes j = new java.awt.JobAttributes();
        check(j.getCopies() == 1, "default copies 1");
        check(j.getDefaultSelection() == java.awt.JobAttributes.DefaultSelectionType.ALL, "default selection ALL");
        check(j.getDestination() == java.awt.JobAttributes.DestinationType.PRINTER, "default destination PRINTER");
        check(j.getDialog() == java.awt.JobAttributes.DialogType.NATIVE, "default dialog NATIVE");
        check(j.getMultipleDocumentHandling() == java.awt.JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES, "default multiple handling");
        check(j.getSides() == java.awt.JobAttributes.SidesType.ONE_SIDED, "default sides ONE_SIDED");
        check(j.getFromPage() == 1 && j.getToPage() == 1, "default page range 1-1");
        check(j.getFileName() == null, "default fileName null");
        check(j.getPrinter() == null, "default printer null");
        j.setCopies(5);
        check(j.getCopies() == 5, "setCopies");
        j.setCopiesToDefault();
        check(j.getCopies() == 1, "setCopiesToDefault");
        j.setFileName("out.ps");
        check("out.ps".equals(j.getFileName()), "setFileName");
        j.setPrinter("printer1");
        check("printer1".equals(j.getPrinter()), "setPrinter");
        int[][] ranges = {{1, 3}, {5, 6}};
        j.setPageRanges(ranges);
        check(j.getPageRanges() != null && j.getPageRanges().length == 2, "setPageRanges length");
        check(j.getPageRanges()[0][0] == 1 && j.getPageRanges()[1][1] == 6, "page range values");
        j.setFromPage(2);
        j.setToPage(9);
        check(j.getFromPage() == 2 && j.getToPage() == 9, "setFromPage/ToPage");
        j.setSides(java.awt.JobAttributes.SidesType.TWO_SIDED_LONG_EDGE);
        check(j.getSides() == java.awt.JobAttributes.SidesType.TWO_SIDED_LONG_EDGE, "setSides");
        j.setSidesToDefault();
        check(j.getSides() == java.awt.JobAttributes.SidesType.ONE_SIDED, "setSidesToDefault");
        j.setMultipleDocumentHandling(java.awt.JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES);
        check(j.getMultipleDocumentHandling() == java.awt.JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES, "setMultipleDocumentHandling");
        j.setMultipleDocumentHandlingToDefault();
        check(j.getMultipleDocumentHandling() == java.awt.JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES, "setMultipleDocumentHandlingToDefault");
        java.awt.JobAttributes copy = new java.awt.JobAttributes(j);
        check(copy.equals(j), "copy ctor equals");
        check(copy != j, "copy ctor distinct");
        java.awt.JobAttributes cl = (java.awt.JobAttributes) j.clone();
        check(cl.equals(j), "clone equals");
        j.set(j);
        check(j.equals(j), "set(self)");
        check(j.hashCode() == j.hashCode(), "hashCode stable");
        check(j.toString() != null, "toString non-null");
        check(j.equals(new Object()) == false, "not equal to Object");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
