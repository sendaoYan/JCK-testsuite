/*
 * @test
 * @summary jdk.javadoc.doclet.StandardDoclet conformance: standard doclet.
 * @run main StandardDocletTest
 */

public class StandardDocletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        jdk.javadoc.doclet.Reporter rep = new jdk.javadoc.doclet.Reporter() {
            public void print(javax.tools.Diagnostic.Kind k, String m) {}
            public void print(javax.tools.Diagnostic.Kind k, com.sun.source.util.DocTreePath p, String m) {}
            public void print(javax.tools.Diagnostic.Kind k, javax.lang.model.element.Element e, String m) {}
        };
        jdk.javadoc.doclet.StandardDoclet d = new jdk.javadoc.doclet.StandardDoclet();
        d.init(java.util.Locale.ENGLISH, rep);
        check("Standard".equals(d.getName()), "getName");
        check(d.getLocale() == java.util.Locale.ENGLISH, "getLocale");
        check(d.getReporter() == rep, "getReporter");
        check(d.getSupportedSourceVersion() != null, "getSupportedSourceVersion");
        check(d.getSupportedOptions() != null, "getSupportedOptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
