/*
 * @test
 * @summary javax.management.DescriptorKey conformance: white-box structure + annotation behavior.
 * @run main DescriptorKeyTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;

public class DescriptorKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorKey");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == String.class, "value() returns String");
        check(value.getDefaultValue() == null, "value() has no default");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
        check(c.isAnnotationPresent(Target.class), "has @Target");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // The annotation is usable on methods and readable via reflection.
        Method m = Sample.class.getMethod("describe");
        DescriptorKey key = m.getAnnotation(DescriptorKey.class);
        check(key != null, "annotation present on method");
        check("MyKey".equals(key.value()), "annotation value");
    }

    public static class Sample {
        @DescriptorKey("MyKey")
        public String describe() { return ""; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
