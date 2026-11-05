/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.InternalException conformance: internal exception.
 * @run main InternalExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class InternalExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: internal-failure channel surface ----
    static void testDeepSemantics() throws Exception {
        InternalException e = new InternalException("msg");
        check(e.getCause() == null, "no cause wired by the constructor");
        // NotImplementedException is the only shipped subtype
        Class<?> ni = Class.forName("jdk.jshell.spi.ExecutionControl$NotImplementedException");
        check(ni.getSuperclass().equals(InternalException.class), "NotImplementedException extends InternalException");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(((InternalException) oi.readObject()).getMessage().equals("msg"), "message survives serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        InternalException e = new InternalException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof ExecutionControlException, "hierarchy");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
