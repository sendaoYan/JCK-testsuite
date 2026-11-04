/*
 * @test
 * @summary javax.annotation.processing.SupportedSourceVersion conformance: white-box structure + black-box behavior.
 * @run main SupportedSourceVersionTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class SupportedSourceVersionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.SupportedSourceVersion");
        check(c != null, "javax.annotation.processing.SupportedSourceVersion loads");
        check(c.getName().equals("javax.annotation.processing.SupportedSourceVersion"), "binary name is javax.annotation.processing.SupportedSourceVersion");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.SupportedSourceVersion is public");
        check(c.isAnnotation(), "is an annotation type");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.lang.annotation.Annotation"), "extends java.lang.annotation.Annotation");
        check(declares(c, "value"), "declares value()");
    }

    static void testBlackBox() throws Exception {
        check(SupportedSourceVersion.class.getAnnotation(java.lang.annotation.Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME, "RUNTIME retention");
        check(SupportedSourceVersion.class.getMethod("value").getDefaultValue() == null, "value has no default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
