/*
 * @test
 * @summary jdk.jshell.Snippet.Status conformance: snippet status enum.
 * @run main StatusTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Snippet.Status.VALID != null && Snippet.Status.REJECTED != null && Snippet.Status.DROPPED != null, "constants");
        check(Snippet.Status.RECOVERABLE_DEFINED != null && Snippet.Status.RECOVERABLE_NOT_DEFINED != null && Snippet.Status.NONEXISTENT != null && Snippet.Status.OVERWRITTEN != null, "more constants");
        check(Snippet.Status.values().length == 7, "7 constants");
        check(Snippet.Status.valueOf("VALID") == Snippet.Status.VALID, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
