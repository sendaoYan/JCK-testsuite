/*
 * @test
 * @summary javax.print.event.PrintJobAdapter conformance: listener adapter.
 * @run main PrintJobAdapterTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintJobAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static class StubJob implements javax.print.DocPrintJob {
        public javax.print.PrintService getPrintService() { return null; }
        public javax.print.attribute.PrintJobAttributeSet getAttributes() { return null; }
        public void addPrintJobListener(javax.print.event.PrintJobListener l) { }
        public void removePrintJobListener(javax.print.event.PrintJobListener l) { }
        public void addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l, javax.print.attribute.PrintJobAttributeSet s) { }
        public void removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener l) { }
        public void print(javax.print.Doc d, javax.print.attribute.PrintRequestAttributeSet s) throws javax.print.PrintException { }
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.event.PrintJobAdapter");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(hasIf(c, "javax.print.event.PrintJobListener"), "implements PrintJobListener");
        PrintJobAdapter adapter = new PrintJobAdapter() { };
        StubJob job = new StubJob();
        PrintJobEvent e = new PrintJobEvent(job, PrintJobEvent.JOB_COMPLETE);
        adapter.printDataTransferCompleted(e);
        adapter.printJobCompleted(e);
        adapter.printJobFailed(e);
        adapter.printJobCanceled(e);
        adapter.printJobNoMoreEvents(e);
        adapter.printJobRequiresAttention(e);
        check(true, "no-op adapter methods");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
