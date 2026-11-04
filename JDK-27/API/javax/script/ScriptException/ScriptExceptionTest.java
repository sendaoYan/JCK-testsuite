/*
 * @test
 * @summary javax.script.ScriptException conformance: white-box structure + black-box behavior.
 * @run main ScriptExceptionTest
 */

import java.lang.reflect.*;

public class ScriptExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptException");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Exception.class, "extends Exception");
        check(hasMethod(c, "getMessage"), "declares getMessage");
        check(hasMethod(c, "getFileName"), "declares getFileName");
        check(hasMethod(c, "getLineNumber"), "declares getLineNumber");
        check(hasMethod(c, "getColumnNumber"), "declares getColumnNumber");
    }

    static void testBlackBox() throws Exception {
        javax.script.ScriptException e = new javax.script.ScriptException("boom");
        check("boom".equals(e.getMessage()), "getMessage");
        check(e.getFileName() == null, "default fileName null");
        check(e.getLineNumber() == -1, "default lineNumber -1");
        check(e.getColumnNumber() == -1, "default columnNumber -1");

        javax.script.ScriptException e2 = new javax.script.ScriptException("msg", "file.js", 10);
        check("file.js".equals(e2.getFileName()), "fileName");
        check(e2.getLineNumber() == 10, "lineNumber");
        check(e2.getColumnNumber() == -1, "columnNumber default -1");
        check("msg in file.js at line number 10".equals(e2.getMessage()), "message with file + line");

        javax.script.ScriptException e3 = new javax.script.ScriptException("m", "f", 1, 2);
        check(e3.getColumnNumber() == 2, "columnNumber");
        check("m in f at line number 1 at column number 2".equals(e3.getMessage()),
                "message with file + line + column");

        javax.script.ScriptException e4 = new javax.script.ScriptException(new RuntimeException("cause"));
        check(e4.getCause() != null, "cause non-null");
        check(e4.getCause() instanceof RuntimeException, "cause type");
        check(e4.getMessage() != null && e4.getMessage().contains("cause"), "message includes cause");

        // No filename -> message is just the base message.
        javax.script.ScriptException e5 = new javax.script.ScriptException("plain", null, -1);
        check("plain".equals(e5.getMessage()), "message with null file is plain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
