/*
 * @test
 * @summary java.awt.JobAttributes conformance: copies/pages/dialog/destination/sides.
 * @run main JobAttributesTest
 */


import java.awt.JobAttributes;
public class JobAttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        JobAttributes j = new JobAttributes();
        j.setCopies(3);
        check(j.getCopies() == 3, "copies");
        j.setDestination(JobAttributes.DestinationType.FILE);
        check(j.getDestination() == JobAttributes.DestinationType.FILE, "destination");
        j.setDialog(JobAttributes.DialogType.NONE);
        check(j.getDialog() == JobAttributes.DialogType.NONE, "dialog");
        j.setFromPage(1); j.setToPage(5);
        check(j.getFromPage() == 1 && j.getToPage() == 5, "page range");
        j.setSides(JobAttributes.SidesType.TWO_SIDED_LONG_EDGE);
        check(j.getSides() == JobAttributes.SidesType.TWO_SIDED_LONG_EDGE, "sides");
        j.setDefaultSelection(JobAttributes.DefaultSelectionType.ALL);
        check(j.getDefaultSelection() == JobAttributes.DefaultSelectionType.ALL, "default selection");
        JobAttributes copy = new JobAttributes(j);
        check(copy.getCopies() == 3, "copy ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
