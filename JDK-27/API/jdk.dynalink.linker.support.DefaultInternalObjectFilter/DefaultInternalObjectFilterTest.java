/*
 * @test
 * @summary jdk.dynalink.linker.support.DefaultInternalObjectFilter conformance: white-box structure + black-box behavior.
 * @run main DefaultInternalObjectFilterTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class DefaultInternalObjectFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.DefaultInternalObjectFilter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MethodHandleTransformer.class.isAssignableFrom(c), "implements MethodHandleTransformer");
        check(hasMethod(c, "transform"), "transform");

    }

    static void testBlackBox() throws Exception {
        MethodHandle id = MethodHandles.identity(Object.class);
        DefaultInternalObjectFilter f = new DefaultInternalObjectFilter(id, id);
        check(f.transform(id) != null, "transform");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
