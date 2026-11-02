/*
 * @test
 * @summary javax.print.event.PrintJobAttributeEvent conformance: print job attribute event.
 * @run main PrintJobAttributeEventTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintJobAttributeEventTest {
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
        javax.print.attribute.PrintJobAttributeSet attrs = new javax.print.attribute.HashPrintJobAttributeSet();
        PrintJobAttributeEvent e = new PrintJobAttributeEvent(job, attrs);
        check(e.getPrintJob() == job, "getPrintJob");
        check(e.getAttributes() != null, "getAttributes non-null");
        check(e instanceof PrintEvent, "extends PrintEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
