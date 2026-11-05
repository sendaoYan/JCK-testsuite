/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.EngineTerminationException conformance: engine termination exception.
 * @run main EngineTerminationExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class EngineTerminationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: terminal failure channel ----
    static void testDeepSemantics() throws Exception {
        EngineTerminationException e = new EngineTerminationException("msg");
        check(e.getCause() == null, "no cause wired by the constructor");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(((EngineTerminationException) oi.readObject()).getMessage().equals("msg"),
                  "message survives serialization");
        }
        // the streaming front-end maps every IOException to this exception
        try {
            byte[] header = new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05};
            jdk.jshell.execution.StreamingExecutionControl sec = new jdk.jshell.execution.StreamingExecutionControl(
                    new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream()),
                    new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(header)));
            sec.addToClasspath("/tmp/jck-e2");
            check(false, "EOF channel must surface as EngineTerminationException");
        } catch (EngineTerminationException expected) {
            check(expected.getMessage().contains("Exception") || expected.getMessage().length() > 0,
                  "EOF mapped to EngineTerminationException with a message");
        }
    }

    public static void main(String[] args) throws Exception {
        EngineTerminationException e = new EngineTerminationException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof ExecutionControlException, "hierarchy");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
