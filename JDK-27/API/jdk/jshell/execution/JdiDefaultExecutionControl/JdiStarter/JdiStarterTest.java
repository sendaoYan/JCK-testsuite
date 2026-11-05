/*
 * @test
 * @summary jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter conformance: jdi starter interface.
 * @run main JdiStarterTest
 */

import java.lang.reflect.*;

public class JdiStarterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: start(ExecutionEnv, Map<String,String>, int) -> TargetDescription ----
    static void testDeepShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter");
        int abs = 0;
        String sig = "";
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abs++; sig = m.toGenericString(); }
        check(abs == 1, "single abstract method");
        check(sig.startsWith("public abstract jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter$TargetDescription"),
              "start() returns TargetDescription");
        check(sig.contains("$JdiStarter.start("), "declared on JdiStarter");
        check(sig.contains("jdk.jshell.spi.ExecutionEnv"), "start() takes ExecutionEnv");
        check(sig.contains("java.util.Map"), "start() takes a parameter map");
        check(sig.contains("int"), "start() takes the control port");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.JdiDefaultExecutionControl$JdiStarter");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
