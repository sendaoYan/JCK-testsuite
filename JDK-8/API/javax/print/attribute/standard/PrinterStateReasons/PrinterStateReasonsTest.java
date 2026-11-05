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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: reason->Severity map / put old value / null NPE / unmodifiable view ----
    static void testContractDeep() throws Exception {
        PrinterStateReasons m = new PrinterStateReasons();
        check(m.put(PrinterStateReason.TONER_LOW, Severity.WARNING) == null, "first put returns null");
        check(Severity.WARNING.equals(m.get(PrinterStateReason.TONER_LOW)), "get severity");
        check(Severity.WARNING.equals(m.put(PrinterStateReason.TONER_LOW, Severity.ERROR)), "put returns old value");
        check(Severity.ERROR.equals(m.get(PrinterStateReason.TONER_LOW)), "updated value");
        check(m.containsKey(PrinterStateReason.TONER_LOW) && !m.containsKey(PrinterStateReason.PAUSED), "containsKey");
        try { m.put(PrinterStateReason.PAUSED, null); throw new AssertionError("expected NPE null value"); }
        catch (NullPointerException e) { passed++; }
        try { m.put(null, Severity.WARNING); throw new AssertionError("expected NPE null key"); }
        catch (NullPointerException e) { passed++; }
        try { java.util.Collections.unmodifiableMap(m).put(PrinterStateReason.PAUSED, Severity.ERROR); throw new AssertionError("expected UOE"); }
        catch (UnsupportedOperationException e) { passed++; }
        check(m.remove(PrinterStateReason.TONER_LOW).equals(Severity.ERROR), "remove returns value");
        check(m.isEmpty(), "empty after remove");
        m.put(PrinterStateReason.TONER_EMPTY, Severity.REPORT);
        check("printer-state-reasons".equals(m.getName()), "getName");
        check(PrinterStateReasons.class == m.getCategory(), "getCategory");
        PrinterStateReasons ser = (PrinterStateReasons) serRoundTrip(m);
        check(ser.equals(m) && Severity.REPORT.equals(ser.get(PrinterStateReason.TONER_EMPTY)), "serialization round-trip");
        check(m.toString().contains("toner-empty=report") || m.toString().startsWith("{"), "toString map form");
        check(new PrinterStateReasons(java.util.Collections.singletonMap(PrinterStateReason.PAUSED, Severity.WARNING)).size() == 1, "map ctor");
    }

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
