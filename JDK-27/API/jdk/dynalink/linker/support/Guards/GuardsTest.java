/*
 * @test
 * @summary jdk.dynalink.linker.support.Guards conformance: white-box structure + black-box behavior.
 * @run main GuardsTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.Guards");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "isNull"), "isNull");
        check(hasMethod(c, "isNotNull"), "isNotNull");
        check(hasMethod(c, "getInstanceOfGuard"), "getInstanceOfGuard");
        check(hasMethod(c, "getIdentityGuard"), "getIdentityGuard");
        check(hasMethod(c, "isOfClass"), "isOfClass");
        check(hasMethod(c, "getClassGuard"), "getClassGuard");
        check(hasMethod(c, "isInstance"), "isInstance");

    }

    static void testBlackBox() throws Exception {
        check(Guards.isNull() != null, "isNull");
        check(Guards.isNotNull() != null, "isNotNull");
        check(Guards.getInstanceOfGuard(String.class) != null, "instanceof guard");
        check(Guards.getIdentityGuard("x") != null, "identity guard");
        check(Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class)) != null, "isOfClass");
        check(Guards.getClassGuard(String.class) != null, "class guard");
        check(Guards.isInstance(String.class, MethodType.methodType(boolean.class, Object.class)) != null, "isInstance");

    }


    public static class G { }
    public static class H extends G { }

    static void testDeepContract() throws Throwable {
        // isOfClass: exact class match only
        MethodHandle g = Guards.isOfClass(G.class, MethodType.methodType(boolean.class, Object.class));
        check((boolean) g.invoke((Object) new G()), "exact class matches");
        check(!(boolean) g.invoke((Object) new H()), "subclass does not match isOfClass");
        check(!(boolean) g.invoke((Object) "s"), "unrelated does not match");
        try { Guards.isOfClass(null, MethodType.methodType(boolean.class, Object.class)); check(false, "null class"); }
        catch (NullPointerException expected) { check(true, "null class NPE"); }
        try { Guards.isOfClass(String.class, null); check(false, "null type"); }
        catch (NullPointerException expected) { check(true, "null type NPE"); }
        // isInstance: subclass matches
        MethodHandle inst = Guards.isInstance(G.class, MethodType.methodType(boolean.class, Object.class));
        check((boolean) inst.invoke((Object) new H()), "isInstance accepts subclass");
        // positional variant
        MethodHandle pos = Guards.isInstance(String.class, 1, MethodType.methodType(boolean.class, Object.class, Object.class));
        check((boolean) pos.invoke(Integer.valueOf(5), "a"), "positional guard checks slot 1");
        check(!(boolean) pos.invoke("a", Integer.valueOf(5)), "slot 0 unchecked by the positional guard");
        // isArray
        MethodHandle arr = Guards.isArray(0, MethodType.methodType(boolean.class, Object.class));
        check((boolean) arr.invoke((Object) new int[0]) && (boolean) arr.invoke((Object) new String[0]), "arrays pass");
        check(!(boolean) arr.invoke((Object) 5) && !(boolean) arr.invoke((Object) "a"), "non-arrays fail");
        // null guards
        check((boolean) Guards.isNull().invoke((Object) null), "isNull accepts null");
        check(!(boolean) Guards.isNull().invoke((Object) "x"), "isNull rejects value");
        check((boolean) Guards.isNotNull().invoke((Object) "x"), "isNotNull accepts value");
        check(!(boolean) Guards.isNotNull().invoke((Object) null), "isNotNull rejects null");
        // class/instance/identity guard factories
        check((boolean) Guards.getClassGuard(G.class).invoke((Object) new G()), "getClassGuard");
        check((boolean) Guards.getInstanceOfGuard(G.class).invoke((Object) new H()), "getInstanceOfGuard");
        Object anchor = new Object();
        check((boolean) Guards.getIdentityGuard(anchor).invoke(anchor), "identity guard same object");
        check(!(boolean) Guards.getIdentityGuard(anchor).invoke(new Object()), "identity guard different object");
        // asType adapts guard parameter types
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodHandle typed = lk.findStatic(GuardsTest.class, "isString", MethodType.methodType(boolean.class, String.class));
        MethodHandle adapted = Guards.asType(typed, MethodType.methodType(boolean.class, Object.class));
        check((boolean) adapted.invoke((Object) "a"), "asType adapts parameters");
        // the LinkerServices variant delegates to the services conversion machinery
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        MethodHandle adapted2 = Guards.asType(f.createLinker().getLinkerServices(), typed,
                MethodType.methodType(boolean.class, Object.class));
        check((boolean) adapted2.invoke((Object) "a"), "asType(services, ...) adapts parameters");
    }

    public static boolean isString(String s) { return s != null; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
