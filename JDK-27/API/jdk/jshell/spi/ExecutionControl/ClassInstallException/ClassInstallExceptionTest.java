/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ClassInstallException conformance: class install exception.
 * @run main ClassInstallExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class ClassInstallExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: per-class install flags and serialization ----
    static void testDeepSemantics() throws Exception {
        boolean[] flags = new boolean[]{true, false};
        ClassInstallException e = new ClassInstallException("msg", flags);
        // installed() shares the array reference
        check(e.installed() == flags, "installed() shares the array");
        // empty table is legal
        check(new ClassInstallException("m", new boolean[0]).installed().length == 0, "empty installed table");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(e); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ClassInstallException back = (ClassInstallException) oi.readObject();
            check(back.getMessage().equals("msg"), "message survives serialization");
            check(back.installed().length == 2 && back.installed()[0] && !back.installed()[1],
                  "installed flags survive serialization");
        }
    }

    public static void main(String[] args) throws Exception {
        ClassInstallException e = new ClassInstallException("msg", new boolean[]{true, false});
        check(e.getMessage().equals("msg"), "message");
        check(e.installed().length == 2 && e.installed()[0] && !e.installed()[1], "installed");
        check(e instanceof ExecutionControlException, "hierarchy");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
