/*
 * @test
 * @summary jdk.jshell.Snippet.Kind conformance: snippet kind enum.
 * @run main KindTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Snippet.Kind.VAR != null && Snippet.Kind.METHOD != null && Snippet.Kind.IMPORT != null, "constants");
        check(Snippet.Kind.TYPE_DECL != null && Snippet.Kind.EXPRESSION != null && Snippet.Kind.STATEMENT != null && Snippet.Kind.ERRONEOUS != null, "more constants");
        check(Snippet.Kind.values().length == 7, "7 constants");
        check(Snippet.Kind.valueOf("VAR") == Snippet.Kind.VAR, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
