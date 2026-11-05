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


    public static Object unhide(Object o) { return o instanceof String && ((String) o).startsWith("#") ? ((String) o).substring(1) : o; }
    public static Object hide(Object o) { return "H"; }

    static void testDeepContract() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        // null filters are accepted: the filter becomes a no-op
        check(new DefaultInternalObjectFilter(null, null) != null, "null/null accepted");
        MethodHandle paramFilter = lk.findStatic(DefaultInternalObjectFilterTest.class, "unhide",
                MethodType.methodType(Object.class, Object.class));
        MethodHandle retFilter = lk.findStatic(DefaultInternalObjectFilterTest.class, "hide",
                MethodType.methodType(Object.class, Object.class));
        DefaultInternalObjectFilter f = new DefaultInternalObjectFilter(paramFilter, retFilter);
        check(f instanceof MethodHandleTransformer, "implements MethodHandleTransformer");
        // a matching-typed handle is transformed on both parameter and return
        MethodHandle echo = lk.findStatic(DefaultInternalObjectFilterTest.class, "echo",
                MethodType.methodType(Object.class, Object.class));
        MethodHandle t = f.transform(echo);
        check(t.type().equals(MethodType.methodType(Object.class, Object.class)), "transformed type: " + t.type());
        check(t.invoke((Object) "#x").equals("H"), "parameter unhidden then return hidden");
        check(t.invoke((Object) "plain").equals("H"), "plain parameter still filtered on return");
        // the original handle is untouched
        check(echo.invoke((Object) "#x").equals("#x"), "original handle unchanged");
    }

    public static Object echo(Object o) { return o; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
