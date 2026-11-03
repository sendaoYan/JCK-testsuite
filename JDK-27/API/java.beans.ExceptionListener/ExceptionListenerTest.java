/*
 * @test
 * @summary java.beans.ExceptionListener conformance: white-box structure + black-box behavior.
 * @run main ExceptionListenerTest
 */

import java.beans.ExceptionListener;

public class ExceptionListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ExceptionListener");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final Exception[] got = new Exception[1];
        ExceptionListener l = new ExceptionListener() {
            public void exceptionThrown(Exception e) { got[0] = e; }
        };
        Exception ex = new IllegalArgumentException("x");
        l.exceptionThrown(ex);
        check(ex == got[0], "exceptionThrown dispatches");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
