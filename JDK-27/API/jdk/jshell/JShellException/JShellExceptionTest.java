/*
 * @test
 * @summary jdk.jshell.JShellException conformance: exception hierarchy and message.
 * @run main/othervm JShellExceptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class JShellExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: the two concrete JShellException subtypes and their channels ----
    static void testExceptionFamily() throws Exception {
        check(EvalException.class.getSuperclass() == JShellException.class, "EvalException extends JShellException");
        check(UnresolvedReferenceException.class.getSuperclass() == JShellException.class, "URE extends JShellException");
        check(JShellException.class.getSuperclass() == Exception.class, "JShellException extends Exception");
        // no public constructors: instances only originate from the engine
        check(JShellException.class.getConstructors().length == 0, "no public ctors");
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            // runtime exception -> EvalException channel
            JShellException ex1 = js.eval("((Object)null).hashCode()").get(0).exception();
            check(ex1 instanceof EvalException && !(ex1 instanceof UnresolvedReferenceException), "NPE -> EvalException");
            // unresolved reference -> UnresolvedReferenceException channel
            js.eval("int jm() { return jmissing(); }");
            JShellException ex2 = js.eval("jm()").get(0).exception();
            check(ex2 instanceof UnresolvedReferenceException && !(ex2 instanceof EvalException), "unresolved -> URE");
            // both carry non-null messages
            check(ex1.getMessage() != null && ex2.getMessage() != null, "messages non-null");
            // getCause default null (engine does not chain for local execution)
            check(ex1.getCause() == null, "no cause by default");
            // serialization contract via Exception (Serializable)
            check(ex1 instanceof java.io.Serializable, "serializable");
            // suppressed/stack machinery inherited
            check(ex1.getSuppressed().length == 0, "no suppressed initially");
        }
    }

    public static void main(String[] args) throws Exception {
        check(JShellException.class.getSuperclass() == Exception.class, "extends Exception");
        try (JShell js = JShell.create()) {
            SnippetEvent e = js.eval("1/0").get(0);
            JShellException ex = e.exception();
            check(ex instanceof JShellException, "instanceof JShellException");
            check(ex.getMessage() != null, "message");
        }
        testExceptionFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
