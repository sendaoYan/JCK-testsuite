/*
 * @test
 * @summary javax.script.ScriptException conformance: script exception.
 * @run main ScriptExceptionTest
 */

public class ScriptExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.ScriptException e = new javax.script.ScriptException("boom");
        check("boom".equals(e.getMessage()), "getMessage");
        check(e.getFileName() == null, "default fileName null");
        check(e.getLineNumber() == -1, "default lineNumber");
        check(e.getColumnNumber() == -1, "default columnNumber");
        javax.script.ScriptException e2 = new javax.script.ScriptException("msg", "file.js", 10);
        check("file.js".equals(e2.getFileName()), "fileName");
        check(e2.getLineNumber() == 10, "lineNumber");
        javax.script.ScriptException e3 = new javax.script.ScriptException("m", "f", 1, 2);
        check(e3.getColumnNumber() == 2, "columnNumber");
        javax.script.ScriptException e4 = new javax.script.ScriptException(new RuntimeException("cause"));
        check(e4.getCause() != null, "cause");
        check(e4.getMessage() != null, "message non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
