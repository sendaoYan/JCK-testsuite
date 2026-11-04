/*
 * @test
 * @summary java.lang.reflect.Parameter conformance: type/name/declaring executable/varargs.
 * @run main ParameterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public void m(int a, String b) {
        }

        public void varargs(String... s) {
        }
    }

    static void testParameter() throws Exception {
        Method m = Sample.class.getMethod("m", int.class, String.class);
        Parameter[] params = m.getParameters();
        check(params.length == 2, "getParameters length");
        Parameter p0 = params[0];
        check(p0.getType() == int.class, "getType");
        check(p0.getParameterizedType() == int.class, "getParameterizedType");
        check(p0.getName() != null, "getName non-null");
        check(p0.getModifiers() == 0, "getModifiers");
        check(p0.getDeclaringExecutable() == m, "getDeclaringExecutable");
        check(p0.getAnnotations().length == 0, "getAnnotations");
        check(!p0.isSynthetic(), "isSynthetic false");
        check(!p0.isVarArgs(), "isVarArgs false");
        check(p0.toString() != null, "toString");

        Method v = Sample.class.getMethod("varargs", String[].class);
        check(v.getParameters()[0].isVarArgs(), "varargs parameter");
    }

    public static void main(String[] args) throws Exception {
        testParameter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
