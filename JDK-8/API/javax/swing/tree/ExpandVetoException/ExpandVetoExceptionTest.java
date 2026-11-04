/*
 * @test
 * @summary javax.swing.tree.ExpandVetoException conformance: exception (headless-safe).
 * @run main ExpandVetoExceptionTest
 */

import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class ExpandVetoExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        javax.swing.event.TreeExpansionEvent evt = new javax.swing.event.TreeExpansionEvent("src", new TreePath("r"));
        ExpandVetoException e = new ExpandVetoException(evt, "msg");
        check("msg".equals(e.getMessage()), "message");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
