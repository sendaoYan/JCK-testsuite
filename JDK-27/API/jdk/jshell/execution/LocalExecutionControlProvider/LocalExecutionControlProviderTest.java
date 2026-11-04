/*
 * @test
 * @summary jdk.jshell.execution.LocalExecutionControlProvider conformance: local provider.
 * @run main LocalExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class LocalExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.LocalExecutionControlProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("defaultParameters")) f=true; check(f, "has defaultParameters"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("createExecutionControl")) f=true; check(f, "has createExecutionControl"); }
    }

    static void testBlackBox() throws Exception {

        LocalExecutionControlProvider p = new LocalExecutionControlProvider();
        check(p.name().equals("local"), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(p.createExecutionControl(null, p.defaultParameters()) != null, "createExecutionControl");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
