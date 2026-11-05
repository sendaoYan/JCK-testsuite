/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.StoppedException conformance: stopped exception.
 * @run main StoppedExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class StoppedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: fixed message and closed constructor surface ----
    static void testDeepSemantics() throws Exception {
        StoppedException e = new StoppedException();
        check(e.getMessage().equals("stopped by stop()"), "fixed message 'stopped by stop()'");
        check(StoppedException.class.getConstructors().length == 1, "single public no-arg constructor");
        // serialization keeps the fixed message
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            check(((StoppedException) oi.readObject()).getMessage().equals("stopped by stop()"),
                  "message survives serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        StoppedException e = new StoppedException();
        check(e instanceof RunException, "extends RunException");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
