/*
 * @test
 * @summary jdk.jshell.JShellException conformance: exception hierarchy and message.
 * @run main JShellExceptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class JShellExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(JShellException.class.getSuperclass() == Exception.class, "extends Exception");
        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("1/0").get(0);
            JShellException ex = e.exception();
            check(ex instanceof JShellException, "instanceof JShellException");
            check(ex.getMessage() != null, "message");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
