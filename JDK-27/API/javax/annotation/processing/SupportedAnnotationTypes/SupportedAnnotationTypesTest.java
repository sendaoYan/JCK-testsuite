/*
 * @test
 * @summary javax.annotation.processing.SupportedAnnotationTypes conformance: white-box structure + black-box behavior.
 * @run main SupportedAnnotationTypesTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class SupportedAnnotationTypesTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.SupportedAnnotationTypes");
        check(c != null, "javax.annotation.processing.SupportedAnnotationTypes loads");
        check(c.getName().equals("javax.annotation.processing.SupportedAnnotationTypes"), "binary name is javax.annotation.processing.SupportedAnnotationTypes");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.SupportedAnnotationTypes is public");
        check(c.isAnnotation(), "is an annotation type");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.lang.annotation.Annotation"), "extends java.lang.annotation.Annotation");
        check(declares(c, "value"), "declares value()");
    }

    static void testBlackBox() throws Exception {
        check(SupportedAnnotationTypes.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "RUNTIME retention");
        check(SupportedAnnotationTypes.class.getMethod("value").getDefaultValue() == null, "value has no default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
