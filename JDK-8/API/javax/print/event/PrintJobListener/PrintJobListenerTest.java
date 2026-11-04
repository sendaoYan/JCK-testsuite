/*
 * @test
 * @summary javax.print.event.PrintJobListener conformance: listener interface.
 * @run main PrintJobListenerTest
 */

import java.lang.reflect.*;


public class PrintJobListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.event.PrintJobListener");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "printDataTransferCompleted"), "declares printDataTransferCompleted");
        check(hasMethod(c, "printJobCompleted"), "declares printJobCompleted");
        check(hasMethod(c, "printJobFailed"), "declares printJobFailed");
        check(hasMethod(c, "printJobCanceled"), "declares printJobCanceled");
        check(hasMethod(c, "printJobNoMoreEvents"), "declares printJobNoMoreEvents");
        check(hasMethod(c, "printJobRequiresAttention"), "declares printJobRequiresAttention");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
