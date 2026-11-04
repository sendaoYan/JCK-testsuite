/*
 * @test
 * @summary javax.print.event.PrintEvent conformance: event base class.
 * @run main PrintEventTest
 */

import java.lang.reflect.*;
import javax.print.event.*;

public class PrintEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        PrintEvent e = new PrintEvent("src");
        check("src".equals(e.getSource()), "getSource");
        check("PrintEvent on src".equals(e.toString()), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
