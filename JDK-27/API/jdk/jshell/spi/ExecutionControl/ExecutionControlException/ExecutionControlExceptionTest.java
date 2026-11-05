/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ExecutionControlException conformance: execution control exception abstract.
 * @run main ExecutionControlExceptionTest
 */

import java.lang.reflect.*;

public class ExecutionControlExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: exception family tree ----
    static void testDeepSemantics() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$ExecutionControlException");
        check(Exception.class.isAssignableFrom(c), "checked Exception family");
        // family tree edges: four direct subtypes plus ClassBytecodes (a value type)
        for (String[] edge : new String[][]{
                {"EngineTerminationException", "ExecutionControlException"},
                {"ClassInstallException", "ExecutionControlException"},
                {"RunException", "ExecutionControlException"},
                {"InternalException", "ExecutionControlException"},
                {"NotImplementedException", "InternalException"},
                {"UserException", "RunException"},
                {"ResolutionException", "RunException"},
                {"StoppedException", "RunException"}})
            check(Class.forName("jdk.jshell.spi.ExecutionControl$" + edge[0]).getSuperclass().getSimpleName().equals(edge[1]),
                  edge[0] + " extends " + edge[1]);
        // ten nested member types, all public and static
        Class<?>[] nested = Class.forName("jdk.jshell.spi.ExecutionControl").getDeclaredClasses();
        check(nested.length == 10, "ten nested member types (9 exceptions + ClassBytecodes): " + nested.length);
        for (Class<?> k : nested)
            check(java.lang.reflect.Modifier.isPublic(k.getModifiers()) && java.lang.reflect.Modifier.isStatic(k.getModifiers()),
                  "public static nested: " + k.getSimpleName());
        check(java.io.Serializable.class.isAssignableFrom(Class.forName("jdk.jshell.spi.ExecutionControl$ClassBytecodes")),
              "ClassBytecodes is the serializable member");
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.ExecutionControl$ExecutionControlException");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "extends Exception");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
