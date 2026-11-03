/*
 * @test
 * @summary javax.management.ConstructorParameters conformance: white-box structure + annotation behavior.
 * @run main ConstructorParametersTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;

public class ConstructorParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ConstructorParameters");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == String[].class, "value() returns String[]");
        String[] def = (String[]) value.getDefaultValue();
        check(def == null, "value() has no default");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        Constructor<?> ctor = Sample.class.getConstructor(String.class, int.class);
        ConstructorParameters cp = ctor.getAnnotation(ConstructorParameters.class);
        check(cp != null, "annotation present on constructor");
        check(cp.value().length == 2, "two parameter names");
        check("name".equals(cp.value()[0]) && "count".equals(cp.value()[1]), "parameter names");
    }

    public static class Sample {
        @ConstructorParameters({"name", "count"})
        public Sample(String name, int count) { }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
