/*
 * @test
 * @summary jdk.jshell.execution.DirectExecutionControl conformance: direct execution control.
 * @run main DirectExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class DirectExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.DirectExecutionControl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("close")) f=true; check(f, "has close"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("invoke")) f=true; check(f, "has invoke"); }
    }

    static void testBlackBox() throws Exception {

        DirectExecutionControl ec = new DirectExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
