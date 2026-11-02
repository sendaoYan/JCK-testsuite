/*
 * @test
 * @summary jdk.jshell.Snippet.SubKind conformance: snippet subkind enum.
 * @run main SubKindTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SubKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Snippet.SubKind.values().length >= 10, "many subkinds");
        for (Snippet.SubKind k : Snippet.SubKind.values()) check(k.name().length() > 0, "name " + k.name());
        check(Snippet.SubKind.valueOf(Snippet.SubKind.values()[0].name()) != null, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
