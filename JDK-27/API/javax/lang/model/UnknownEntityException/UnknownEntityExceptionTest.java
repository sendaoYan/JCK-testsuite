/*
 * @test
 * @summary javax.lang.model.UnknownEntityException conformance: white-box structure + black-box behavior.
 * @run main UnknownEntityExceptionTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class UnknownEntityExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.UnknownEntityException");
        check(c != null, "javax.lang.model.UnknownEntityException loads");
        check(c.getName().equals("javax.lang.model.UnknownEntityException"), "binary name is javax.lang.model.UnknownEntityException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.UnknownEntityException is public");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass is java.lang.RuntimeException");
    }

    static void testBlackBox() throws Exception {
        UnknownEntityException e = new UnknownEntityException("msg") {};
        check("msg".equals(e.getMessage()), "getMessage");
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
