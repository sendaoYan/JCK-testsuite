/*
 * @test
 * @summary jdk.dynalink.linker.support.Lookup conformance: white-box structure + black-box behavior.
 * @run main LookupTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class LookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.Lookup");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "findStatic"), "findStatic");
        check(hasMethod(c, "findVirtual"), "findVirtual");
        check(hasMethod(c, "unreflect"), "unreflect");
        check(hasMethod(c, "findGetter"), "findGetter");
        check(hasMethod(c, "findSpecial"), "findSpecial");

    }

    static void testBlackBox() throws Exception {
        check(Lookup.PUBLIC != null, "PUBLIC");
        Lookup lk = new Lookup(MethodHandles.publicLookup());
        check(lk.findStatic(Math.class, "abs", MethodType.methodType(int.class, int.class)) != null, "findStatic");
        check(lk.findVirtual(String.class, "length", MethodType.methodType(int.class)) != null, "findVirtual");
        check(lk.unreflect(String.class.getMethod("length")) != null, "unreflect");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
