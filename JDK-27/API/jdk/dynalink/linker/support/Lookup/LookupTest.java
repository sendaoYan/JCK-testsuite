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


    public static class LBean {
        public int field = 3;
        public String name() { return "nm"; }
        public static boolean stat(String s) { return s != null; }
    }

    static void testDeepContract() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        // PUBLIC constant wraps publicLookup
        check(Lookup.PUBLIC != null, "PUBLIC constant");
        check(Lookup.PUBLIC.unreflect(String.class.getMethod("length")).type()
              .equals(MethodType.methodType(int.class, String.class)), "PUBLIC unreflect");
        // instance wrapper exposes the full convenience surface
        Lookup own = new Lookup(lk);
        check(own.findOwnStatic("statTarget", boolean.class, String.class).type()
              .equals(MethodType.methodType(boolean.class, String.class)), "findOwnStatic");
        check(own.findStatic(LookupTest.class, "statTarget", MethodType.methodType(boolean.class, String.class)) != null,
              "findStatic");
        check(own.findVirtual(String.class, "length", MethodType.methodType(int.class)).type()
              .equals(MethodType.methodType(int.class, String.class)), "findVirtual");
        check(own.findGetter(LBean.class, "field", int.class).type()
              .equals(MethodType.methodType(int.class, LBean.class)), "findGetter");
        check(own.unreflectGetter(LBean.class.getField("field")).type()
              .equals(MethodType.methodType(int.class, LBean.class)), "unreflectGetter");
        check(own.unreflectSetter(LBean.class.getField("field")).type()
              .equals(MethodType.methodType(void.class, LBean.class, int.class)), "unreflectSetter");
        check(own.unreflectConstructor(LBean.class.getConstructor()).type()
              .equals(MethodType.methodType(LBean.class)), "unreflectConstructor");
        check(own.findSpecial(LookupTest.class, "toString", MethodType.methodType(String.class)) != null, "findSpecial");
        check(own.findOwnSpecial("toString", String.class) != null, "findOwnSpecial");
        // reflection failures surface as linkage errors, not checked exceptions
        try {
            new Lookup(MethodHandles.publicLookup()).findSpecial(String.class, "length", MethodType.methodType(int.class));
            check(false, "inaccessible special must fail");
        } catch (IllegalAccessError expected) { check(true, "IllegalAccessError for inaccessible special"); }
        // static variants mirror the instance behavior
        check(Lookup.unreflect(lk, String.class.getMethod("length")) != null, "static unreflect");
        check(Lookup.unreflectConstructor(lk, LBean.class.getConstructor()) != null, "static unreflectConstructor");
        check(Lookup.findOwnStatic(lk, "statTarget", boolean.class, String.class) != null, "static findOwnStatic");
    }

    public static boolean statTarget(String s) { return s != null; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
