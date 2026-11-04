/*
 * @test
 * @summary java.awt.JobAttributes conformance: job attribute bundle (headless-safe):
 *          defaults, scalar setters with validation (copies, page ranges), enum-typed
 *          attributes with null rejection, full constructor (including the documented
 *          maxPage-before-minPage order), page-ranges validation matrix, derived
 *          getPageRanges, value equality, clone/copy-constructor independence,
 *          non-serializability.
 * @run main JobAttributesTest
 */

import java.awt.JobAttributes;

public class JobAttributesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static String ranges(int[][] r) {
        StringBuilder sb = new StringBuilder();
        for (int[] pair : r) sb.append(pair[0]).append("-").append(pair[1]).append(";");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        // Defaults.
        JobAttributes j = new JobAttributes();
        check(j.getCopies() == 1, "default copies 1");
        check(j.getDefaultSelection() == JobAttributes.DefaultSelectionType.ALL, "default selection all");
        check(j.getDestination() == JobAttributes.DestinationType.PRINTER, "default destination printer");
        check(j.getDialog() == JobAttributes.DialogType.NATIVE, "default dialog native");
        check(j.getFileName() == null, "default fileName null");
        check(j.getFromPage() == 1 && j.getToPage() == 1, "default from/to 1");
        check(j.getMaxPage() == Integer.MAX_VALUE, "default maxPage Integer.MAX_VALUE");
        check(j.getMinPage() == 1, "default minPage 1");
        check(j.getMultipleDocumentHandling() == JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES,
                "default MDH uncollated");
        check(ranges(j.getPageRanges()).equals("1-1;"), "derived page range from from/to: " + ranges(j.getPageRanges()));
        check(j.getPrinter() == null, "default printer null");
        check(j.getSides() == JobAttributes.SidesType.ONE_SIDED, "default sides one-sided");

        // Scalar setters.
        j.setCopies(3);
        check(j.getCopies() == 3, "copies");
        j.setFromPage(1);
        j.setToPage(5);
        check(j.getFromPage() == 1 && j.getToPage() == 5, "page range");
        j.setMinPage(2);
        check(j.getMinPage() == 2, "minPage");
        j.setMaxPage(50);
        check(j.getMaxPage() == 50, "maxPage");
        j.setFileName("out.ps");
        check("out.ps".equals(j.getFileName()), "fileName");
        j.setPrinter("prn");
        check("prn".equals(j.getPrinter()), "printer");
        final JobAttributes fj = j;
        checkThrows(IllegalArgumentException.class, "setCopies(0)",
                new ThrowingRunnable() { public void run() { fj.setCopies(0); } });
        checkThrows(IllegalArgumentException.class, "setCopies(-1)",
                new ThrowingRunnable() { public void run() { fj.setCopies(-1); } });

        // Enum-typed setters.
        j.setDestination(JobAttributes.DestinationType.FILE);
        check(j.getDestination() == JobAttributes.DestinationType.FILE, "destination");
        j.setDialog(JobAttributes.DialogType.NONE);
        check(j.getDialog() == JobAttributes.DialogType.NONE, "dialog");
        j.setSides(JobAttributes.SidesType.TWO_SIDED_LONG_EDGE);
        check(j.getSides() == JobAttributes.SidesType.TWO_SIDED_LONG_EDGE, "sides");
        j.setDefaultSelection(JobAttributes.DefaultSelectionType.SELECTION);
        check(j.getDefaultSelection() == JobAttributes.DefaultSelectionType.SELECTION, "default selection");
        j.setMultipleDocumentHandling(JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES);
        check(j.getMultipleDocumentHandling() == JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES, "MDH");
        checkThrows(IllegalArgumentException.class, "setDialog(null)",
                new ThrowingRunnable() { public void run() { fj.setDialog(null); } });
        checkThrows(IllegalArgumentException.class, "setSides(null)",
                new ThrowingRunnable() { public void run() { fj.setSides(null); } });
        checkThrows(IllegalArgumentException.class, "setDestination(null)",
                new ThrowingRunnable() { public void run() { fj.setDestination(null); } });
        checkThrows(IllegalArgumentException.class, "setDefaultSelection(null)",
                new ThrowingRunnable() { public void run() { fj.setDefaultSelection(null); } });
        checkThrows(IllegalArgumentException.class, "setMultipleDocumentHandling(null)",
                new ThrowingRunnable() { public void run() { fj.setMultipleDocumentHandling(null); } });

        // Nested constant classes: identity singletons with lowercase names.
        check(!JobAttributes.SidesType.class.isEnum(), "SidesType is a constant class");
        check("one-sided".equals(JobAttributes.SidesType.ONE_SIDED.toString()), "ONE_SIDED name");
        check("two-sided-long-edge".equals(JobAttributes.SidesType.TWO_SIDED_LONG_EDGE.toString()), "TWO_SIDED_LONG_EDGE name");
        check("two-sided-short-edge".equals(JobAttributes.SidesType.TWO_SIDED_SHORT_EDGE.toString()), "TWO_SIDED_SHORT_EDGE name");
        check("common,native,none".equals(JobAttributes.DialogType.COMMON + "," + JobAttributes.DialogType.NATIVE
                + "," + JobAttributes.DialogType.NONE), "DialogType names");
        check("file,printer".equals(JobAttributes.DestinationType.FILE + "," + JobAttributes.DestinationType.PRINTER),
                "DestinationType names");
        check("all,range,selection".equals(JobAttributes.DefaultSelectionType.ALL + ","
                + JobAttributes.DefaultSelectionType.RANGE + "," + JobAttributes.DefaultSelectionType.SELECTION),
                "DefaultSelectionType names");

        // pageRanges validation: pairs must be {lo,hi} with lo>previous hi, lo>=minPage, hi<=maxPage.
        j.setPageRanges(new int[][]{{2, 3}, {5, 7}});
        check(ranges(j.getPageRanges()).equals("2-3;5-7;"), "explicit page ranges: " + ranges(j.getPageRanges()));
        checkThrows(IllegalArgumentException.class, "pageRanges null",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(null); } });
        checkThrows(IllegalArgumentException.class, "pageRanges empty",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[0][]); } });
        checkThrows(IllegalArgumentException.class, "pageRanges one-element pair",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{{1}}); } });
        checkThrows(IllegalArgumentException.class, "pageRanges null pair",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{null}); } });
        checkThrows(IllegalArgumentException.class, "pageRanges below minPage",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{{1, 3}}); } });
        checkThrows(IllegalArgumentException.class, "pageRanges overlapping",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{{2, 4}, {3, 6}}); } });
        checkThrows(IllegalArgumentException.class, "pageRanges above maxPage",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{{2, 4}, {51, 60}}); } });
        checkThrows(IllegalArgumentException.class, "pageRanges inverted pair",
                new ThrowingRunnable() { public void run() { fj.setPageRanges(new int[][]{{7, 3}}); } });
        // Returned ranges are defensive copies.
        int[][] got = j.getPageRanges();
        got[0][0] = 99;
        check(j.getPageRanges()[0][0] == 2, "returned page ranges are copies");

        // Full constructor: parameter order ends with (fileName, maxPage, minPage,
        // MDH, pageRanges, printer, sides).
        JobAttributes f = new JobAttributes(
                2,
                JobAttributes.DefaultSelectionType.SELECTION,
                JobAttributes.DestinationType.PRINTER,
                JobAttributes.DialogType.NONE,
                "out.ps",
                20, 1,
                JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES,
                new int[][]{{1, 5}},
                "prn",
                JobAttributes.SidesType.ONE_SIDED);
        check(f.getCopies() == 2, "full ctor copies");
        check("out.ps".equals(f.getFileName()), "full ctor fileName");
        check(f.getMaxPage() == 20 && f.getMinPage() == 1, "full ctor max/min order");
        check(ranges(f.getPageRanges()).equals("1-5;"), "full ctor pageRanges");
        check("prn".equals(f.getPrinter()), "full ctor printer");
        check(f.getSides() == JobAttributes.SidesType.ONE_SIDED, "full ctor sides");

        // Value equality.
        JobAttributes a = new JobAttributes();
        JobAttributes b = new JobAttributes();
        check(a.equals(b), "equal defaults");
        check(a.hashCode() == b.hashCode(), "hashCode follows equals");
        b.setCopies(9);
        check(!a.equals(b), "difference detected");
        check(!a.equals(null), "equals(null) false");

        // Copy constructor and clone are independent.
        JobAttributes copy = new JobAttributes(a);
        copy.setCopies(77);
        check(a.getCopies() == 1, "copy ctor independent");
        JobAttributes clone = (JobAttributes) a.clone();
        check(clone.equals(a) && clone != a, "clone equal and distinct");
        clone.setCopies(5);
        check(a.getCopies() == 1, "clone independent");

        // Not serializable.
        boolean failed = false;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(a);
            oos.close();
        } catch (java.io.NotSerializableException expected) {
            failed = true;
        }
        check(failed, "JobAttributes is not serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
