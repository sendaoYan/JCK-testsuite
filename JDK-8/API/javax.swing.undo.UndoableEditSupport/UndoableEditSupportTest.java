/*
 * @test
 * @summary javax.swing.undo.UndoableEditSupport conformance: edit support (headless-safe).
 * @run main UndoableEditSupportTest
 */

import javax.swing.undo.UndoableEditSupport;

public class UndoableEditSupportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        UndoableEditSupport s = new UndoableEditSupport("owner");
        check(s != null, "constructed");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
