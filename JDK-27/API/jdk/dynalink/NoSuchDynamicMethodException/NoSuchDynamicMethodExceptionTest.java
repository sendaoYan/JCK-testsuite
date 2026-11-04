/*
 * @test
 * @summary jdk.dynalink.NoSuchDynamicMethodException conformance: white-box structure + black-box behavior.
 * @run main NoSuchDynamicMethodExceptionTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class NoSuchDynamicMethodExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.NoSuchDynamicMethodException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
        check(hasMethod(c, "getMessage"), "getMessage");

    }

    static void testBlackBox() throws Exception {
        NoSuchDynamicMethodException e = new NoSuchDynamicMethodException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof RuntimeException, "runtime exception");
        NoSuchDynamicMethodException e2 = new NoSuchDynamicMethodException("second");
        check(e2.getMessage().equals("second"), "second message");
        check(!e2.getMessage().equals(e.getMessage()), "distinct instances");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
