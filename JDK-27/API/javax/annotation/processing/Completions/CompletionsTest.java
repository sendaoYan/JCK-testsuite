/*
 * @test
 * @summary javax.annotation.processing.Completions conformance: white-box structure + black-box behavior.
 * @run main CompletionsTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class CompletionsTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Completions");
        check(c != null, "javax.annotation.processing.Completions loads");
        check(c.getName().equals("javax.annotation.processing.Completions"), "binary name is javax.annotation.processing.Completions");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.Completions is public");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(declares(c, "of"), "declares of()");
    }

    static void testBlackBox() throws Exception {
        Completion c1 = Completions.of("value");
        check("value".equals(c1.getValue()), "of(value) value");
        check("".equals(c1.getMessage()), "of(value) empty message");
        Completion c2 = Completions.of("value2", "msg");
        check("value2".equals(c2.getValue()), "of(value,msg) value");
        check("msg".equals(c2.getMessage()), "of(value,msg) message");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
