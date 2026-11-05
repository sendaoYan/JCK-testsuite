/*
 * @test
 * @summary jdk.dynalink.linker.MethodHandleTransformer conformance: white-box structure + black-box behavior.
 * @run main MethodHandleTransformerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class MethodHandleTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.MethodHandleTransformer");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "transform"), "transform");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    public static class Bean { public String getName() { return "n"; } }

    static void testDeepContract() throws Throwable {
        // single abstract transformMethodHandle
        int n = 0; Method abs = null;
        for (Method m : MethodHandleTransformer.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1 && abs.getName().equals("transform") && abs.getParameterCount() == 1,
              "single abstract transform(MethodHandle)");
        check(abs.getReturnType() == MethodHandle.class, "returns MethodHandle");
        check(Modifier.isInterface(MethodHandleTransformer.class.getModifiers()), "interface");
        // the identity transformer is a legal implementation
        MethodHandleTransformer id = h -> h;
        MethodHandle mh = MethodHandles.identity(Object.class);
        check(id.transform(mh) == mh, "identity transform");
        // a real filter transformer wired through the factory
        final int[] count = {0};
        MethodHandleTransformer filter = h -> { count[0]++; return h; };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        f.setInternalObjectsFilter(filter);
        DynamicLinker linker = f.createLinker();
        MethodHandles.Lookup lk = MethodHandles.lookup();
        // the filter runs when a beans dynamic-method invocation is built (GET:METHOD then CALL)
        SimpleRelinkableCallSite gm = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.METHOD).named("getName"),
            MethodType.methodType(Object.class, Object.class)));
        linker.link(gm);
        Object dm = gm.getTarget().invoke((Object) new Bean());
        SimpleRelinkableCallSite call = new SimpleRelinkableCallSite(new CallSiteDescriptor(lk,
            StandardOperation.CALL, MethodType.genericMethodType(2)));
        linker.link(call);
        check(call.getTarget().invoke(dm, new Bean()).equals("n"), "filtered dynamic-method CALL works");
        check(count[0] >= 1, "transformer ran when the dynamic method invocation was built: " + count[0]);
        // LinkerServices.filterInternalObjects applies the transformer directly
        check(linker.getLinkerServices().filterInternalObjects(MethodHandles.identity(Object.class)).type()
              .equals(MethodType.methodType(Object.class, Object.class)), "pass-through filter preserves the type");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
