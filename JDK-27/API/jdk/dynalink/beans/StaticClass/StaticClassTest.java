/*
 * @test
 * @summary jdk.dynalink.beans.StaticClass conformance: white-box structure + black-box behavior.
 * @run main StaticClassTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class StaticClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.StaticClass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "forClass"), "forClass");
        check(hasMethod(c, "getRepresentedClass"), "getRepresentedClass");

    }

    static void testBlackBox() throws Exception {
        StaticClass sc = StaticClass.forClass(String.class);
        check(sc.getRepresentedClass() == String.class, "represented class");
        check(sc.toString().contains("String"), "toString");
        check(sc.equals(StaticClass.forClass(String.class)), "equals");
        check(sc.hashCode() == StaticClass.forClass(String.class).hashCode(), "hashCode");
        check(StaticClass.forClass(Integer.class).getRepresentedClass() == Integer.class, "second class");

    }


    // ---- shared fixtures for the deep contract sections ----
    public static class POJO {
        public int field = 7;
        public String getName() { return "name-" + field; }
        public String greet(String who) { return "hi " + who; }
        public int add(int a, int b) { return a + b; }
    }
    static DynamicLinker beanLinker() {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new jdk.dynalink.beans.BeansLinker());
        f.setFallbackLinkers();
        return f.createLinker();
    }
    static SimpleRelinkableCallSite site(MethodHandles.Lookup lk, Operation op, MethodType mt) {
        return new SimpleRelinkableCallSite(new CallSiteDescriptor(lk, op, mt));
    }

    static void testIdentityAndCache() {
        StaticClass a = StaticClass.forClass(POJO.class);
        StaticClass b = StaticClass.forClass(POJO.class);
        check(a == b, "forClass caches one instance per class");
        check(StaticClass.forClass(String.class) != a, "different class, different instance");
        check(a.getRepresentedClass() == POJO.class, "getRepresentedClass");
        check(a.toString().equals("StaticClass[" + POJO.class.getName() + "]"), "toString: " + a);
        check(a.equals(a), "equals self");
        check(!a.equals(StaticClass.forClass(String.class)), "not equal to another class");
        check(a.hashCode() == b.hashCode(), "same instance implies same hashCode");
        // arrays, primitives and interfaces are all wrappable
        check(StaticClass.forClass(int[].class).getRepresentedClass() == int[].class, "array class");
        check(StaticClass.forClass(Comparable.class).getRepresentedClass() == Comparable.class, "interface class");
    }

    static void testStaticMemberLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinker linker = beanLinker();
        // static property read through a StaticClass receiver
        SimpleRelinkableCallSite gp = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("MAX_VALUE"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(gp);
        check(gp.getTarget().invoke(StaticClass.forClass(Integer.class)).equals(Integer.MAX_VALUE), "static Integer.MAX_VALUE");
        // static method through GET:METHOD + CALL
        SimpleRelinkableCallSite gm = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.METHOD).named("currentTimeMillis"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(gm);
        Object dm = gm.getTarget().invoke(StaticClass.forClass(System.class));
        check(BeansLinker.isDynamicMethod(dm), "static method is a dynamic method");
        SimpleRelinkableCallSite call = site(lk, StandardOperation.CALL, MethodType.genericMethodType(2));
        linker.link(call);
        // static dynamic methods keep a StaticClass parameter after the method itself
        check(((Number) call.getTarget().invoke(dm, StaticClass.forClass(System.class))).longValue() > 0,
              "CALL on the static dynamic method passes the StaticClass");
        check(BeansLinker.isDynamicMethod(dm) && !BeansLinker.isDynamicConstructor(dm),
              "static GET:METHOD yields a plain dynamic method");
        // NEW loop through the class constructor
        SimpleRelinkableCallSite nw = site(lk, StandardOperation.NEW, MethodType.methodType(Object.class, Object.class));
        linker.link(nw);
        Object made = nw.getTarget().invoke(StaticClass.forClass(Object.class));
        check(made instanceof Object && made.getClass() == Object.class, "NEW on StaticClass for Object");
        // a Class receiver (not StaticClass) resolves statics through ClassLinker: GET Class.name
        SimpleRelinkableCallSite cn = site(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("name"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(cn);
        check(cn.getTarget().invoke((Object) String.class).equals("java.lang.String"), "GET:PROPERTY:name on a Class receiver");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testIdentityAndCache();
        testStaticMemberLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
