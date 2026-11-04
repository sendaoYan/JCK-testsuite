/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.CompletionContext conformance: completion context enum.
 * @run main CompletionContextTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class CompletionContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(CompletionContext.ANNOTATION_ATTRIBUTE != null, "ANNOTATION_ATTRIBUTE");
        check(CompletionContext.NO_PAREN != null, "NO_PAREN");
        check(CompletionContext.TYPES_AS_ANNOTATIONS != null, "TYPES_AS_ANNOTATIONS");
        check(CompletionContext.QUALIFIED != null, "QUALIFIED");
        check(CompletionContext.values().length == 4, "4 constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
