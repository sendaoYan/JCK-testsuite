/*
 * @test
 * @summary javax.tools.DiagnosticCollector conformance: diagnostic collector.
 * @run main DiagnosticCollectorTest
 */

public class DiagnosticCollectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.DiagnosticCollector<Object> dc = new javax.tools.DiagnosticCollector<Object>();
        check(dc.getDiagnostics().isEmpty(), "empty");
        javax.tools.Diagnostic<Object> diag = new javax.tools.Diagnostic<Object>() {
            public javax.tools.Diagnostic.Kind getKind() { return javax.tools.Diagnostic.Kind.ERROR; }
            public Object getSource() { return null; }
            public long getPosition() { return 0; }
            public long getStartPosition() { return 0; }
            public long getEndPosition() { return 0; }
            public long getLineNumber() { return 1; }
            public long getColumnNumber() { return 1; }
            public String getCode() { return "X"; }
            public String getMessage(java.util.Locale l) { return "m"; }
        };
        dc.report(diag);
        check(dc.getDiagnostics().size() == 1, "size after report");
        check(dc.getDiagnostics().get(0).getKind() == javax.tools.Diagnostic.Kind.ERROR, "kind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
