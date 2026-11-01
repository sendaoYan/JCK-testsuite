/*
 * @test
 * @summary java.nio.charset.CodingErrorAction conformance: IGNORE/REPLACE/REPORT constants.
 * @run main CodingErrorActionTest
 */

import java.nio.charset.CodingErrorAction;

public class CodingErrorActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCodingErrorAction() {
        check("IGNORE".equals(CodingErrorAction.IGNORE.toString()), "IGNORE");
        check("REPLACE".equals(CodingErrorAction.REPLACE.toString()), "REPLACE");
        check("REPORT".equals(CodingErrorAction.REPORT.toString()), "REPORT");
        check(CodingErrorAction.IGNORE != CodingErrorAction.REPLACE, "IGNORE != REPLACE");
        check(CodingErrorAction.REPLACE != CodingErrorAction.REPORT, "REPLACE != REPORT");
    }

    public static void main(String[] args) {
        testCodingErrorAction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
