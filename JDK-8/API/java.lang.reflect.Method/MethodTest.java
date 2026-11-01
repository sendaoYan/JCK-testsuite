/*
 * @test
 * @summary java.lang.reflect.Method conformance: return type, invoke, parameter reflection.
 * @run main MethodTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int add(int a, int b) {
            return a + b;
        }
    }

    static void testMethod() throws Exception {
        Method m = Sample.class.getMethod("add", int.class, int.class);
        check("add".equals(m.getName()), "getName");
        check(m.getReturnType() == int.class, "getReturnType");
        check(m.getGenericReturnType() == int.class, "getGenericReturnType");
        check(m.getParameterTypes().length == 2, "getParameterTypes");
        check(m.getParameterCount() == 2, "getParameterCount");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(!Modifier.isStatic(m.getModifiers()), "instance method");
        Sample s = new Sample();
        check(m.invoke(s, 2, 3).equals(5), "invoke");
        check(Sample.class.getMethods().length >= 1, "getMethods");
        check(Sample.class.getDeclaredMethods().length >= 1, "getDeclaredMethods");
        check(m.toString() != null, "toString");
        check(m.toGenericString() != null, "toGenericString");
    }

    public static void main(String[] args) throws Exception {
        testMethod();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
