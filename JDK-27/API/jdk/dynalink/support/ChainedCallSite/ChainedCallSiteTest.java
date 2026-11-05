/*
 * @test
 * @summary jdk.dynalink.support.ChainedCallSite conformance: white-box structure + black-box behavior.
 * @run main ChainedCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class ChainedCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.support.ChainedCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(AbstractRelinkableCallSite.class.isAssignableFrom(c), "extends AbstractRelinkableCallSite");
        check(hasMethod(c, "relink"), "relink");
        check(hasMethod(c, "resetAndRelink"), "resetAndRelink");

    }

    static void testBlackBox() throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        ChainedCallSite cs = new ChainedCallSite(csd);
        check(cs instanceof RelinkableCallSite, "relinkable");
        check(cs.type().equals(MethodType.methodType(Object.class, Object.class)), "type");
        check(cs.getDescriptor() == csd, "descriptor");

    }


    static void testChainLoop() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        DynamicLinker linker = f.createLinker();
        // one chained site serving three different receiver kinds on GET:ELEMENT
        ChainedCallSite cs = new ChainedCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.ELEMENT),
            MethodType.methodType(Object.class, Object.class, int.class)));
        linker.link(cs);
        check(cs.getTarget().invoke(Arrays.asList("a", "b", "c"), 1).equals("b"), "List element");
        check(cs.getTarget().invoke(new String[]{"x", "y"}, 1).equals("y"), "array element after relink");
        check(cs.getTarget().invoke(Arrays.asList("a", "b", "c"), 0).equals("a"), "the List branch still serves");
        check(cs.getDescriptor().getMethodType().parameterCount() == 2, "descriptor readable");
        // an unlinkable receiver fails without breaking the chain
        try { cs.getTarget().invoke((Object) Double.valueOf(1), 0); check(false, "unsupported receiver must throw"); }
        catch (NoSuchDynamicMethodException expected) { check(true, "unsupported receiver NSDME"); }
        check(cs.getTarget().invoke(new String[]{"x", "y"}, 0).equals("x"), "chain intact after failure");
    }

    static void testResetAndRelink() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        ChainedCallSite cs = new ChainedCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET, MethodType.methodType(Object.class, Object.class)));
        MethodHandle inv = MethodHandles.identity(Object.class);
        MethodHandle relink = MethodHandles.identity(MethodHandle.class); // placeholder bootstrap handle
        cs.relink(new GuardedInvocation(inv, (MethodHandle) null), relink);
        cs.relink(new GuardedInvocation(inv, (MethodHandle) null), relink);
        // resetAndRelink discards the accumulated chain
        cs.resetAndRelink(new GuardedInvocation(inv, (MethodHandle) null), relink);
        check(cs.getTarget().invoke((Object) "z").equals("z"), "reset target invokes");
        // the maximum chain length is a protected hook
        java.lang.reflect.Method m = ChainedCallSite.class.getDeclaredMethod("getMaxChainLength");
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "getMaxChainLength is protected");
        check(m.getReturnType() == int.class, "returns int");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testChainLoop();
        testResetAndRelink();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
