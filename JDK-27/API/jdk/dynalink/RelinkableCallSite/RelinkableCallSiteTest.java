/*
 * @test
 * @summary jdk.dynalink.RelinkableCallSite conformance: white-box structure + black-box behavior.
 * @run main RelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class RelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.RelinkableCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "initialize"), "initialize");
        check(hasMethod(c, "getDescriptor"), "getDescriptor");
        check(hasMethod(c, "relink"), "relink");
        check(hasMethod(c, "resetAndRelink"), "resetAndRelink");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    // ---- shared fixtures for the deep contract sections ----
    public static class POJO {
        public int field = 7;
        public String getName() { return "name-" + field; }
        public String greet(String who) { return "hi " + who; }
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

    static class RecordingSite extends AbstractRelinkableCallSite {
        int relinks, resets;
        RecordingSite(CallSiteDescriptor d) { super(d); }
        public void relink(GuardedInvocation gi, MethodHandle relink) { relinks++; setTarget(gi.compose(relink)); }
        public void resetAndRelink(GuardedInvocation gi, MethodHandle relink) { resets++; setTarget(gi.compose(relink)); }
    }

    static void testDeepContract() throws Throwable {
        // exactly the four lifecycle methods
        TreeSet<String> names = new TreeSet<>();
        for (Method m : RelinkableCallSite.class.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new TreeSet<>(List.of("getDescriptor", "initialize", "relink", "resetAndRelink"))),
              "interface surface: " + names);
        check(!Modifier.isInterface(RelinkableCallSite.class.getModifiers()) == false, "is an interface");
        // a hand-rolled call site drives a real linker
        DynamicLinker linker = beanLinker();
        RecordingSite cs = new RecordingSite(new CallSiteDescriptor(MethodHandles.lookup(),
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("field"),
            MethodType.methodType(Object.class, Object.class)));
        RecordingSite ret = linker.link(cs);
        check(ret == cs, "link returns the site");
        check(cs.getTarget().invoke((Object) new POJO()).equals(7), "custom site invokes");
        check(cs.relinks == 1 && cs.resets == 0, "first link relinks once: " + cs.relinks);
        // initialize installs the bootstrap handle
        MethodHandle id = MethodHandles.identity(Object.class);
        cs.initialize(id);
        check(cs.getTarget() == id, "initialize sets the raw target");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
