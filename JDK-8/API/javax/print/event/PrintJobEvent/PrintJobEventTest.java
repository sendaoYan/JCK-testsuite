/*
 * @test
 * @summary javax.print.event.PrintJobEvent conformance: print job event.
 * @run main PrintJobEventTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintJobEventTest {
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

    public static void main(String[] a) throws Exception {
        StubJob job = new StubJob();
        PrintJobEvent e = new PrintJobEvent(job, PrintJobEvent.JOB_COMPLETE);
        check(e.getPrintEventType() == PrintJobEvent.JOB_COMPLETE, "getPrintEventType");
        check(e.getPrintJob() == job, "getPrintJob");
        check(PrintJobEvent.JOB_CANCELED != PrintJobEvent.JOB_COMPLETE, "distinct constants");
        check(PrintJobEvent.JOB_FAILED != PrintJobEvent.REQUIRES_ATTENTION, "distinct constants 2");
        check(PrintJobEvent.NO_MORE_EVENTS != PrintJobEvent.DATA_TRANSFER_COMPLETE, "distinct constants 3");
        check(e instanceof PrintEvent, "extends PrintEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
