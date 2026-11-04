/*
 * @test
 * @summary jdk.jshell.execution.JdiExecutionControlProvider conformance: provider metadata and params.
 * @run main JdiExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class JdiExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiExecutionControlProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("defaultParameters")) f=true; check(f, "has defaultParameters"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("generate")) f=true; check(f, "has generate"); }
    }

    static void testBlackBox() throws Exception {

        JdiExecutionControlProvider p = new JdiExecutionControlProvider();
        check(p.name() != null && !p.name().isEmpty(), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(JdiExecutionControlProvider.PARAM_REMOTE_AGENT != null, "PARAM_REMOTE_AGENT");
        check(JdiExecutionControlProvider.PARAM_TIMEOUT != null, "PARAM_TIMEOUT");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
