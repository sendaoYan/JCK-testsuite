/*
 * @test
 * @summary java.lang.reflect.Constructor conformance: parameter types, modifiers, newInstance.
 * @run main ConstructorTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ConstructorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int x;

        public Sample() {
            x = 0;
        }

        public Sample(int v) {
            x = v;
        }

        private Sample(String s) {
            x = s.length();
        }
    }

    static void testConstructor() throws Exception {
        Constructor<Sample> c = Sample.class.getConstructor(int.class);
        check(c.getParameterTypes().length == 1, "getParameterTypes length");
        check(c.getParameterCount() == 1, "getParameterCount");
        check(c.getModifiers() == Modifier.PUBLIC, "public modifier");
        check(c.getName().equals(Sample.class.getName()), "getName");
        check(c.getDeclaringClass() == Sample.class, "getDeclaringClass");
        Sample s = c.newInstance(42);
        check(s.x == 42, "newInstance with args");
        Sample s0 = Sample.class.getConstructor().newInstance();
        check(s0.x == 0, "newInstance no-arg");
        check(Sample.class.getConstructors().length == 2, "getConstructors length");
        check(Sample.class.getDeclaredConstructors().length == 3, "getDeclaredConstructors length");
        check(c.toString() != null, "toString");
        check(c.toGenericString() != null, "toGenericString");
        check(c.getParameterAnnotations().length == 1, "getParameterAnnotations");
    }

    public static void main(String[] args) throws Exception {
        testConstructor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
