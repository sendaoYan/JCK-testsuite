/*
 * @test
 * @summary javax.annotation.processing.FilerException conformance: white-box structure + black-box behavior.
 * @run main FilerExceptionTest
 */

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.*;

public class FilerExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.FilerException");
        check(c != null, "javax.annotation.processing.FilerException loads");
        check(c.getName().equals("javax.annotation.processing.FilerException"), "binary name is javax.annotation.processing.FilerException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.annotation.processing.FilerException is public");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "superclass is java.io.IOException");
    }

    static void testBlackBox() throws Exception {
        FilerException e = new FilerException("boom");
        check("boom".equals(e.getMessage()), "getMessage");
        check(e instanceof java.io.IOException, "is IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
