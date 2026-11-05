/*
 * @test
 * @summary jdk.dynalink.support.AbstractRelinkableCallSite conformance: white-box structure + black-box behavior.
 * @run main AbstractRelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class AbstractRelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.support.AbstractRelinkableCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(RelinkableCallSite.class.isAssignableFrom(c), "implements RelinkableCallSite");
        check(MutableCallSite.class.isAssignableFrom(c), "extends MutableCallSite");
        check(hasMethod(c, "getDescriptor"), "getDescriptor");
        check(hasMethod(c, "initialize"), "initialize");

    }

    static void testBlackBox() throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        AbstractRelinkableCallSite abs = cs;
        check(abs.getDescriptor().getOperation() == StandardOperation.GET, "getDescriptor");
        abs.initialize(MethodHandles.identity(Object.class));
        check(true, "initialize callable");

    }


    public static class POJO { public int field = 7; }
    static class MinimalSite extends AbstractRelinkableCallSite {
        int relinks, resets, inits;
        MinimalSite(CallSiteDescriptor d) { super(d); }
        public void relink(GuardedInvocation gi, MethodHandle relink) { relinks++; setTarget(gi.compose(relink)); }
        public void resetAndRelink(GuardedInvocation gi, MethodHandle relink) { resets++; setTarget(gi.compose(relink)); }
    }

    static void testDeepContract() throws Throwable {
        // structure: abstract, extends MutableCallSite, implements RelinkableCallSite
        check(Modifier.isAbstract(AbstractRelinkableCallSite.class.getModifiers()), "abstract");
        check(AbstractRelinkableCallSite.class.getSuperclass() == java.lang.invoke.MutableCallSite.class,
              "extends MutableCallSite");
        check(RelinkableCallSite.class.isAssignableFrom(AbstractRelinkableCallSite.class), "implements RelinkableCallSite");
        check(Modifier.isProtected(AbstractRelinkableCallSite.class.getDeclaredConstructors()[0].getModifiers()),
              "protected constructor");
        // lifecycle through a real linker
        MethodHandles.Lookup lk = MethodHandles.lookup();
        CallSiteDescriptor d = new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class));
        MinimalSite cs = new MinimalSite(d);
        check(cs.getDescriptor() == d, "getDescriptor returns the constructor descriptor");
        check(cs.type().equals(d.getMethodType()), "MutableCallSite type is the descriptor type");
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(new BeansLinker());
        f.setFallbackLinkers();
        f.createLinker().link(cs);
        check(cs.relinks == 0, "link() itself only initializes");
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "first invocation triggers relink");
        check(cs.relinks == 1, "one relink pass: " + cs.relinks);
        // initialize installs the bootstrap handle verbatim
        MethodHandle id = MethodHandles.identity(Object.class);
        cs.initialize(id);
        check(cs.getTarget() == id, "initialize sets the target directly");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
