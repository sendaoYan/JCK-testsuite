/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Attribute conformance: highlight attribute enum.
 * @run main AttributeTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class AttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Attribute.DECLARATION != null, "DECLARATION");
        check(Attribute.KEYWORD != null, "KEYWORD");
        check(Attribute.DEPRECATED != null, "DEPRECATED");
        check(Attribute.values().length == 3, "3 constants");
        check(Attribute.valueOf("KEYWORD") == Attribute.KEYWORD, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
