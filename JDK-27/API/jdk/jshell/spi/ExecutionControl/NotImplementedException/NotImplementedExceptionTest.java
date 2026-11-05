/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.NotImplementedException conformance: not implemented exception.
 * @run main NotImplementedExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class NotImplementedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: produced by the direct engine for three operations ----
    static void testDeepSemantics() throws Exception {
        NotImplementedException e = new NotImplementedException("msg");
        check(e instanceof InternalException, "extends InternalException");
        // the direct engine's exact messages
        jdk.jshell.execution.DirectExecutionControl dec = new jdk.jshell.execution.DirectExecutionControl();
        try { dec.redefine(new jdk.jshell.spi.ExecutionControl.ClassBytecodes[0]); }
        catch (NotImplementedException x) { check(x.getMessage().equals("redefine not supported"), "redefine message"); }
        try { dec.stop(); }
        catch (NotImplementedException x) { check(x.getMessage().equals("stop: Not supported."), "stop message"); }
        try { dec.extensionCommand("anything", null); }
        catch (NotImplementedException x) { check(x.getMessage().equals("Unknown command: anything"), "extension message"); }
        // serialization keeps the message
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(((NotImplementedException) oi.readObject()).getMessage().equals("msg"), "message survives serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        NotImplementedException e = new NotImplementedException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof InternalException, "extends InternalException");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
