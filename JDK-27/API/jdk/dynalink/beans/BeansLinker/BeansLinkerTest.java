/*
 * @test
 * @summary jdk.dynalink.beans.BeansLinker conformance: white-box structure + black-box behavior.
 * @run main BeansLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class BeansLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.BeansLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(GuardingDynamicLinker.class.isAssignableFrom(c), "implements GuardingDynamicLinker");
        check(hasMethod(c, "getLinkerForClass"), "getLinkerForClass");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");
        check(hasMethod(c, "getInstanceMethodNames"), "getInstanceMethodNames");
        check(hasMethod(c, "getStaticMethodNames"), "getStaticMethodNames");

    }

    static void testBlackBox() throws Exception {
        BeansLinker b = new BeansLinker();
        check(b != null, "constructed");
        check(b.getLinkerForClass(String.class) != null, "getLinkerForClass");
        check(BeansLinker.getInstanceMethodNames(String.class).contains("length"), "instance method names");
        check(BeansLinker.getStaticMethodNames(Math.class).contains("abs"), "static method names");
        check(!BeansLinker.isDynamicMethod(b), "isDynamicMethod false");
        check(!BeansLinker.isDynamicConstructor(b), "isDynamicConstructor false");
        check(BeansLinker.getReadableInstancePropertyNames(String.class) != null, "property names");

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

    static void testMemberNameSets() {
        TreeSet<String> readable = new TreeSet<>(BeansLinker.getReadableInstancePropertyNames(POJO.class));
        check(readable.equals(new TreeSet<>(List.of("class", "field", "name"))),
              "readable instance properties [class, field, name]: " + readable);
        TreeSet<String> writable = new TreeSet<>(BeansLinker.getWritableInstancePropertyNames(POJO.class));
        check(writable.equals(new TreeSet<>(List.of("field"))), "writable instance properties [field]: " + writable);
        check(BeansLinker.getInstanceMethodNames(POJO.class).containsAll(List.of("getName", "greet", "add")),
              "instance method names include all declared");
        check(!BeansLinker.getInstanceMethodNames(POJO.class).contains("field"), "fields are not method names");
        check(BeansLinker.getReadableStaticPropertyNames(System.class).contains("out"), "System.out readable static");
        check(BeansLinker.getWritableStaticPropertyNames(System.class).contains("err"), "System.err writable static");
        check(BeansLinker.getStaticMethodNames(System.class).contains("currentTimeMillis"), "System.currentTimeMillis static");
        check(BeansLinker.getReadableStaticPropertyNames(Integer.class).contains("MAX_VALUE"), "Integer.MAX_VALUE readable static");
        // the sets are snapshots: unmodifiable content is not required but the call must be repeatable
        check(BeansLinker.getReadableInstancePropertyNames(POJO.class).equals(new TreeSet<>(readable)), "stable across calls");
    }

    static void testConstructorMethods() {
        Object ctor = BeansLinker.getConstructorMethod(java.awt.Point.class, "int, int");
        check(ctor != null, "getConstructorMethod with the exact signature string");
        check(BeansLinker.isDynamicMethod(ctor), "constructor result is a dynamic method");
        check(BeansLinker.isDynamicConstructor(ctor), "constructor result is a dynamic constructor");
        check(BeansLinker.getConstructorMethod(java.awt.Point.class, "nope") == null, "unknown signature -> null");
        check(BeansLinker.getConstructorMethod(String.class, "new") == null, "String has no public constructor -> null");
        check(BeansLinker.getConstructorMethod(int.class, "") == null, "primitive class -> null");
    }

    static void testLinkerForClass() throws Throwable {
        BeansLinker b1 = new BeansLinker(), b2 = new BeansLinker();
        // the per-class linkers live in a static ClassValue: shared across instances
        check(b1.getLinkerForClass(POJO.class) == b2.getLinkerForClass(POJO.class), "linker cached across instances");
        check(b1.getLinkerForClass(String.class) != b1.getLinkerForClass(POJO.class), "different class, different linker");
        check(b1.getLinkerForClass(POJO.class) instanceof TypeBasedGuardingDynamicLinker, "TypeBased linker returned");
        try { b1.getLinkerForClass(null); check(false, "null class must throw"); }
        catch (NullPointerException expected) { check(true, "null class NPE"); }
        // the linker is live: it links a real GET through BeansLinker.getGuardedInvocation
        LinkerServices svc = beanLinker().getLinkerServices();
        SimpleLinkRequest req = new SimpleLinkRequest(new CallSiteDescriptor(MethodHandles.lookup(),
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class)), false, new POJO());
        check(b1.getGuardedInvocation(req, svc) != null, " BeansLinker links a live request");
    }

    static void testRealOperationLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinker linker = beanLinker();
        POJO p = new POJO();
        // GET property
        check(site(lk, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class)).getTarget() != null, "placeholder-guard");
        SimpleRelinkableCallSite get = site(lk, StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(get);
        check(get.getTarget().invoke((Object) p).equals(7), "GET:PROPERTY:field");
        // SET property
        SimpleRelinkableCallSite set = site(lk, StandardOperation.SET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class, Object.class));
        linker.link(set);
        Object setRet = set.getTarget().invoke(p, Integer.valueOf(41));
        check(p.field == 41, "SET:PROPERTY:field effect: " + p.field);
        // GET:ELEMENT on List, array and Map
        SimpleRelinkableCallSite el = site(lk, StandardOperation.GET.withNamespace(StandardNamespace.ELEMENT),
            MethodType.methodType(Object.class, Object.class, int.class));
        linker.link(el);
        check(el.getTarget().invoke(Arrays.asList("a", "b", "c"), 1).equals("b"), "GET:ELEMENT on List");
        SimpleRelinkableCallSite el2 = site(lk, StandardOperation.GET.withNamespace(StandardNamespace.ELEMENT),
            MethodType.methodType(Object.class, Object.class, Object.class));
        linker.link(el2);
        Map<String, String> m = new HashMap<>(); m.put("k", "v");
        check(el2.getTarget().invoke(m, "k").equals("v"), "GET:ELEMENT on Map");
        // REMOVE:ELEMENT on List
        SimpleRelinkableCallSite rem = site(lk, StandardOperation.REMOVE.withNamespace(StandardNamespace.ELEMENT),
            MethodType.methodType(Object.class, Object.class, int.class));
        linker.link(rem);
        List<String> lst = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Object removed = rem.getTarget().invoke(lst, 1);
        check(removed.equals("b") && lst.equals(List.of("a", "c")), "REMOVE:ELEMENT on List: " + lst);
        // GET:METHOD then CALL: the two-site dynamic-method loop
        SimpleRelinkableCallSite gm = site(lk, StandardOperation.GET.withNamespace(StandardNamespace.METHOD).named("getName"),
            MethodType.methodType(Object.class, Object.class));
        linker.link(gm);
        Object dm = gm.getTarget().invoke((Object) p);
        check(BeansLinker.isDynamicMethod(dm), "GET:METHOD yields a dynamic method");
        check(dm.getClass().getName().equals("jdk.dynalink.beans.SimpleDynamicMethod"), "SimpleDynamicMethod: " + dm.getClass().getName());
        SimpleRelinkableCallSite call = site(lk, new NamedOperation(StandardOperation.CALL, "getName"),
            MethodType.genericMethodType(2));
        linker.link(call);
        check(call.getTarget().invoke(dm, p).equals("name-41"), "CALL on the dynamic method");
        // NEW through StaticClass
        SimpleRelinkableCallSite nw = site(lk, StandardOperation.NEW, MethodType.methodType(Object.class, Object.class));
        linker.link(nw);
        Object made = nw.getTarget().invoke(jdk.dynalink.beans.StaticClass.forClass(POJO.class));
        check(made instanceof POJO && ((POJO) made).field == 7, "NEW on StaticClass");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testMemberNameSets();
        testConstructorMethods();
        testLinkerForClass();
        testRealOperationLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
