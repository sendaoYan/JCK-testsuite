/*
 * @test
 * @summary jdk.dynalink.linker.support.CompositeGuardingDynamicLinker conformance: white-box structure + black-box behavior.
 * @run main CompositeGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class CompositeGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.CompositeGuardingDynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(GuardingDynamicLinker.class.isAssignableFrom(c), "implements GuardingDynamicLinker");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");

    }

    static void testBlackBox() throws Exception {
        CompositeGuardingDynamicLinker c = new CompositeGuardingDynamicLinker(Collections.emptyList());
        check(c != null, "constructed");
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleLinkRequest r = new SimpleLinkRequest(csd, false, "obj");
        LinkerServices svc = new DynamicLinkerFactory().createLinker().getLinkerServices();
        check(c.getGuardedInvocation(r, svc) == null, "empty composite returns null");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
