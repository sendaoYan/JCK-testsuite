/*
 * @test
 * @summary javax.print.attribute.standard.PrinterStateReasons conformance: printer state reasons map.
 * @run main PrinterStateReasonsTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class PrinterStateReasonsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrinterStateReasons v = new PrinterStateReasons();
        check(v.isEmpty(), "initially empty");
        check(v.put(PrinterStateReason.PAUSED, Severity.WARNING) == null, "put");
        check(Severity.WARNING.equals(v.get(PrinterStateReason.PAUSED)), "get");
        check(v.printerStateReasonSet(Severity.WARNING).contains(PrinterStateReason.PAUSED), "printerStateReasonSet");
        check(v.printerStateReasonSet(Severity.ERROR).isEmpty(), "empty error set");
        check(v.getCategory() == PrinterStateReasons.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
