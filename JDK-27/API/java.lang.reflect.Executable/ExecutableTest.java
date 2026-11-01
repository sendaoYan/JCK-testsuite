/*
 * @test
 * @summary java.lang.reflect.Executable conformance: parameters/exceptions/modifiers via a
 *          Method.
 * @run main ExecutableTest
 */

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;

public class ExecutableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public static void m(int a, String b) throws Exception {
        }

        public static void varargs(String... s) {
        }
    }

    static void testExecutable() throws Exception {
        Executable e = Sample.class.getMethod("m", int.class, String.class);
        check(e.getParameterCount() == 2, "getParameterCount");
        check(e.getParameters().length == 2, "getParameters");
        check(e.getParameterTypes().length == 2, "getParameterTypes");
        check("m".equals(e.getName()), "getName");
        check(e.getExceptionTypes().length == 1, "getExceptionTypes");
        check(e.getGenericExceptionTypes().length == 1, "getGenericExceptionTypes");
        check(e.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC), "getModifiers");
        check(e.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(!e.isVarArgs(), "not varargs");
        check(!e.isSynthetic(), "not synthetic");

        Executable v = Sample.class.getMethod("varargs", String[].class);
        check(v.isVarArgs(), "isVarArgs");
    }

    public static void main(String[] args) throws Exception {
        testExecutable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
