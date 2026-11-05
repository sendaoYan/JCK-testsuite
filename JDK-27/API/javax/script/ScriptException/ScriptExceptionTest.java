/*
 * @test
 * @summary javax.script.ScriptException conformance: white-box structure + black-box behavior.
 * @run main ScriptExceptionTest
 */

import java.lang.reflect.*;
import javax.script.ScriptException;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dSeConstructors();
        dSeDefaults();
        dSeToString();
        dSeChaining();
        dSeSerialization();
        System.out.println("deep: ScriptException contracts OK");
    }

    static void dSeConstructors() {
        ScriptException a = new ScriptException("boom");
        check("boom".equals(a.getMessage()), "message ctor");
        check(a.getFileName() == null, "no file -> null");
        check(a.getLineNumber() == -1, "no line -> -1");
        check(a.getColumnNumber() == -1, "no column -> -1");
        ScriptException b = new ScriptException("b", "f.js", 7);
        check("f.js".equals(b.getFileName()), "file stored");
        check(b.getLineNumber() == 7, "line stored");
        check(b.getColumnNumber() == -1, "3-arg ctor -> column -1");
        ScriptException c = new ScriptException("c", "f.js", 7, 9);
        check(c.getLineNumber() == 7 && c.getColumnNumber() == 9, "4-arg ctor stores both");
        // null message is tolerated
        check(new ScriptException((String) null).getMessage() == null, "null message legal");
    }

    static void dSeDefaults() {
        // null/empty file name still reports -1 line numbers
        ScriptException n = new ScriptException(null, null, -1);
        check(n.getFileName() == null && n.getLineNumber() == -1, "explicit -1 stored");
    }

    static void dSeToString() {
        check(new ScriptException("boom").toString().equals("javax.script.ScriptException: boom"),
                "plain toString format");
        check(new ScriptException("b", "f.js", 7).toString()
                .equals("javax.script.ScriptException: b in f.js at line number 7"),
                "3-arg toString format");
        check(new ScriptException("b", "f.js", 7, 9).toString()
                .equals("javax.script.ScriptException: b in f.js at line number 7 at column number 9"),
                "4-arg toString format");
    }

    static void dSeChaining() {
        RuntimeException cause = new RuntimeException("root");
        ScriptException e = new ScriptException("wrap");
        check(e.getCause() == null, "no cause initially");
        check(e.initCause(cause) == e, "initCause returns this");
        check(e.getCause() == cause, "cause stored");
        boolean ise = false;
        try { e.initCause(new RuntimeException()); }
        catch (IllegalStateException x) { ise = true; }
        check(ise, "initCause at most once");
    }

    static void dSeSerialization() throws Exception {
        ScriptException e = new ScriptException("s", "f.js", 3, 5);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ScriptException back = (ScriptException) ois.readObject();
            check(back.getMessage() != null && back.getMessage().startsWith("s in f.js at line number 3"),
                    "round-trip message composes position");
            check("f.js".equals(back.getFileName()), "round-trip file");
            check(back.getLineNumber() == 3 && back.getColumnNumber() == 5,
                    "round-trip position");
        }
    }

}
