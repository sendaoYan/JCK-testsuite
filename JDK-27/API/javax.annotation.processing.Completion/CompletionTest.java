/*
 * @test
 * @summary javax.annotation.processing.Completion conformance: white-box structure + black-box behavior.
 * @run main CompletionTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class CompletionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Completion");
        check(c != null, "javax.annotation.processing.Completion loads");
        check(c.getName().equals("javax.annotation.processing.Completion"), "binary name is javax.annotation.processing.Completion");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.Completion is public");
        check(c.isInterface() && !c.isAnnotation(), "is a plain interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getValue"), "declares getValue()");
        check(declares(c, "getMessage"), "declares getMessage()");
    }

    static void testBlackBox() throws Exception {
        Completion c = new Completion() {
            public String getValue() { return "v"; }
            public String getMessage() { return "m"; }
        };
        check("v".equals(c.getValue()), "getValue");
        check("m".equals(c.getMessage()), "getMessage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
