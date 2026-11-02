/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.Highlight conformance: highlight record.
 * @run main HighlightTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class HighlightTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Highlight h = new Highlight(0, 3, EnumSet.noneOf(Attribute.class));
        check(h.start() == 0, "start");
        check(h.end() == 3, "end");
        check(h.attributes() != null && h.attributes().isEmpty(), "attributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
