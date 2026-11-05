/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Completeness conformance: completeness enum.
 * @run main/othervm CompletenessTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class CompletenessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: enum table + per-input matrix via the real parser ----
    static void testMatrix() throws Exception {
        Completeness[] vs = Completeness.values();
        check(vs.length == 6, "6 constants");
        check(vs[0] == Completeness.COMPLETE && vs[1] == Completeness.COMPLETE_WITH_SEMI
              && vs[2] == Completeness.DEFINITELY_INCOMPLETE && vs[3] == Completeness.CONSIDERED_INCOMPLETE
              && vs[4] == Completeness.EMPTY && vs[5] == Completeness.UNKNOWN, "declaration order");
        // isComplete table
        check(Completeness.COMPLETE.isComplete(), "COMPLETE");
        check(Completeness.COMPLETE_WITH_SEMI.isComplete(), "COMPLETE_WITH_SEMI");
        check(!Completeness.DEFINITELY_INCOMPLETE.isComplete(), "DEFINITELY_INCOMPLETE");
        check(!Completeness.CONSIDERED_INCOMPLETE.isComplete(), "CONSIDERED_INCOMPLETE");
        check(!Completeness.EMPTY.isComplete(), "EMPTY not complete");
        check(Completeness.UNKNOWN.isComplete(), "UNKNOWN is complete (treat as complete)");
        try { Completeness.valueOf("BOGUS"); check(false, "valueOf bogus IAE"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf bogus IAE"); }
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            SourceCodeAnalysis sca = js.sourceCodeAnalysis();
            check(sca.analyzeCompletion("2+2").completeness() == Completeness.COMPLETE, "expression");
            check(sca.analyzeCompletion("int x=1;").completeness() == Completeness.COMPLETE, "terminated decl");
            check(sca.analyzeCompletion("int x=1").completeness() == Completeness.COMPLETE_WITH_SEMI, "unterminated decl");
            check(sca.analyzeCompletion("if(x>").completeness() == Completeness.DEFINITELY_INCOMPLETE, "open block");
            check(sca.analyzeCompletion("if (x>1) { a(); }").completeness() == Completeness.COMPLETE, "closed block");
            check(sca.analyzeCompletion("class C").completeness() == Completeness.DEFINITELY_INCOMPLETE, "bare class");
            check(sca.analyzeCompletion("int m() { return 1; }").completeness() == Completeness.COMPLETE, "method");
            check(sca.analyzeCompletion("").completeness() == Completeness.EMPTY, "empty");
            check(sca.analyzeCompletion("   ").completeness() == Completeness.EMPTY, "whitespace only");
            check(sca.analyzeCompletion("// c").completeness() == Completeness.EMPTY, "comment only");
            check(sca.analyzeCompletion("/*").completeness() == Completeness.DEFINITELY_INCOMPLETE, "open comment");
            check(sca.analyzeCompletion("System.out.println();").completeness() == Completeness.COMPLETE, "statement");
            check(sca.analyzeCompletion("throw new RuntimeException(").completeness() == Completeness.DEFINITELY_INCOMPLETE, "open call");
        }
    }

    public static void main(String[] args) throws Exception {
        check(Completeness.COMPLETE.isComplete(), "COMPLETE complete");
        check(Completeness.DEFINITELY_INCOMPLETE != null && !Completeness.DEFINITELY_INCOMPLETE.isComplete(), "DEFINITELY_INCOMPLETE");
        check(Completeness.valueOf("COMPLETE") == Completeness.COMPLETE, "valueOf");
        check(Completeness.values().length == 6, "6 constants");
        testMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
