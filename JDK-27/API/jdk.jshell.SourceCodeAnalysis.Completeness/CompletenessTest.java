/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Completeness conformance: completeness enum.
 * @run main CompletenessTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class CompletenessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Completeness.COMPLETE.isComplete(), "COMPLETE complete");
        check(Completeness.DEFINITELY_INCOMPLETE != null && !Completeness.DEFINITELY_INCOMPLETE.isComplete(), "DEFINITELY_INCOMPLETE");
        check(Completeness.valueOf("COMPLETE") == Completeness.COMPLETE, "valueOf");
        check(Completeness.values().length == 6, "6 constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
