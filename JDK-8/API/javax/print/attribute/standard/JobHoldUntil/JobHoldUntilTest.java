/*
 * @test
 * @summary javax.print.attribute.standard.JobHoldUntil conformance: date-time attribute.
 * @run main JobHoldUntilTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class JobHoldUntilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        JobHoldUntil v = new JobHoldUntil(new java.util.Date(0L));
        check(new java.util.Date(0L).equals(v.getValue()), "getValue");
        check(v.getCategory() == JobHoldUntil.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        check(v.equals(new JobHoldUntil(new java.util.Date(0L))), "equals");
        check(v.hashCode() == new JobHoldUntil(new java.util.Date(0L)).hashCode(), "hashCode");
        check(v.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: millis preservation / defensive copy / null NPE / serialization ----
    static void testContractDeep() throws Exception {
        java.util.GregorianCalendar cal = new java.util.GregorianCalendar(2026, java.util.Calendar.NOVEMBER, 5, 12, 34, 56);
        cal.set(java.util.Calendar.MILLISECOND, 789);
        long epoch = cal.getTimeInMillis();
        JobHoldUntil v = new JobHoldUntil(cal.getTime());
        check(v.getValue().getTime() == epoch, "millis preserved exactly");
        java.util.Date got = v.getValue();
        check(got != cal.getTime(), "getValue returns new Date");
        got.setTime(0L);
        check(v.getValue().getTime() == epoch, "getValue defensive copy independent");
        check("job-hold-until".equals(v.getName()), "getName");
        check(JobHoldUntil.class == v.getCategory(), "getCategory");
        expectNPE("JobHoldUntil(null)", new Runnable() { public void run() { new JobHoldUntil(null); } }, "value is null");
        check(v.equals(new JobHoldUntil(new java.util.Date(epoch))), "equals same instant");
        check(!v.equals(new JobHoldUntil(new java.util.Date(epoch + 1))), "different instant unequal");
        check(!v.equals(null) && !v.equals(new java.util.Date(epoch)), "null/foreign unequal");
        check(v.hashCode() == new JobHoldUntil(new java.util.Date(epoch)).hashCode(), "hashCode consistent");
        JobHoldUntil rt = (JobHoldUntil) serRoundTrip(v);
        check(rt.equals(v) && rt.getValue().getTime() == epoch, "serialization round-trip");
        check(v.toString() != null && v.toString().length() > 0, "toString non-null");
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
