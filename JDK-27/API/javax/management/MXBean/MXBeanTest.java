/*
 * @test
 * @summary javax.management.MXBean conformance: white-box structure + annotation behavior.
 * @run main MXBeanTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;

public class MXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MXBean");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == boolean.class, "value() returns boolean");
        check(Boolean.TRUE.equals(value.getDefaultValue()), "value() default true");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        MXBean ann = SampleMX.class.getAnnotation(MXBean.class);
        check(ann != null, "annotation present");
        check(ann.value(), "annotation value true");

        MXBean annFalse = SampleMXFalse.class.getAnnotation(MXBean.class);
        check(annFalse != null && !annFalse.value(), "annotation value false");
    }

    @MXBean
    public interface SampleMX { }
    @MXBean(false)
    public interface SampleMXFalse { }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
