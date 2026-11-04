/*
 * @test
 * @summary javax.tools.Diagnostic conformance: diagnostic interface via collector.
 * @run main DiagnosticTest
 */

public class DiagnosticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.DiagnosticCollector<Object> dc = new javax.tools.DiagnosticCollector<Object>();
        javax.tools.Diagnostic<Object> d = new javax.tools.Diagnostic<Object>() {
            public javax.tools.Diagnostic.Kind getKind() { return javax.tools.Diagnostic.Kind.ERROR; }
            public Object getSource() { return "src"; }
            public long getPosition() { return 7; }
            public long getStartPosition() { return 7; }
            public long getEndPosition() { return 8; }
            public long getLineNumber() { return 2; }
            public long getColumnNumber() { return 3; }
            public String getCode() { return "err"; }
            public String getMessage(java.util.Locale l) { return "message"; }
        };
        dc.report(d);
        check(dc.getDiagnostics().size() == 1, "collected");
        javax.tools.Diagnostic<?> got = dc.getDiagnostics().get(0);
        check(got.getKind() == javax.tools.Diagnostic.Kind.ERROR, "getKind");
        check("src".equals(got.getSource()), "getSource");
        check(got.getPosition() == 7, "getPosition");
        check(got.getStartPosition() == 7, "getStartPosition");
        check(got.getEndPosition() == 8, "getEndPosition");
        check(got.getLineNumber() == 2, "getLineNumber");
        check(got.getColumnNumber() == 3, "getColumnNumber");
        check("err".equals(got.getCode()), "getCode");
        check("message".equals(got.getMessage(java.util.Locale.ROOT)), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
