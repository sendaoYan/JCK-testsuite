/*
 * @test
 * @summary jdk.jshell.SourceCodeAnalysis.ElementSuggestionConvertor conformance: element suggestion convertor interface.
 * @run main ElementSuggestionConvertorTest
 */

import java.lang.reflect.*;

public class ElementSuggestionConvertorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.SourceCodeAnalysis$ElementSuggestionConvertor");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
