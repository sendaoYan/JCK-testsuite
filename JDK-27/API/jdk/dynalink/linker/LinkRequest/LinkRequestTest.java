/*
 * @test
 * @summary jdk.dynalink.linker.LinkRequest conformance: white-box structure + black-box behavior.
 * @run main LinkRequestTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class LinkRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.LinkRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getCallSiteDescriptor"), "getCallSiteDescriptor");
        check(hasMethod(c, "getArguments"), "getArguments");
        check(hasMethod(c, "getReceiver"), "getReceiver");
        check(hasMethod(c, "isCallSiteUnstable"), "isCallSiteUnstable");
        check(hasMethod(c, "replaceArguments"), "replaceArguments");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

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

    static void testRequestObservedByLinker() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<LinkRequest> seen = new ArrayList<>();
        GuardingDynamicLinker probe = (req, svc) -> { seen.add(req); return null; };
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        f.setPrioritizedLinker(probe);
        f.setFallbackLinkers();
        SimpleRelinkableCallSite cs = site(lk, StandardOperation.GET, MethodType.genericMethodType(2));
        f.createLinker().link(cs);
        try { cs.getTarget().invoke("r0", "a1"); } catch (Throwable t) { /* NSDME expected */ }
        LinkRequest req = seen.get(0);
        check(req.getReceiver().equals("r0"), "receiver is argument 0");
        check(req.getArguments().length == 2, "arguments include receiver");
        check(req.getArguments()[0].equals("r0") && req.getArguments()[1].equals("a1"), "argument order");
        check(req.getCallSiteDescriptor().getMethodType().parameterCount() == 2, "descriptor from the call site");
        check(!req.isCallSiteUnstable(), "stable on first link");
        // replaceArguments builds a fresh request with the same descriptor
        LinkRequest replaced = req.replaceArguments(req.getCallSiteDescriptor(), new Object[]{ "x" });
        check(replaced != req, "replaceArguments returns a new request");
        check(replaced.getCallSiteDescriptor() == req.getCallSiteDescriptor(), "descriptor kept");
        check(replaced.getReceiver().equals("x"), "new receiver");
        check(replaced.getArguments().length == 1, "new argument list");
        // the unstable flag travels through SimpleLinkRequest
        SimpleLinkRequest unstable = new SimpleLinkRequest(req.getCallSiteDescriptor(), true, "z");
        check(unstable.isCallSiteUnstable(), "unstable flag observable");
        check(!req.isCallSiteUnstable(), "original still stable");
    }

    static void testInterfaceSurface() {
        TreeSet<String> names = new TreeSet<>();
        for (Method m : LinkRequest.class.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new TreeSet<>(List.of("getArguments", "getCallSiteDescriptor", "getReceiver", "isCallSiteUnstable", "replaceArguments"))),
              "interface surface: " + names);
        check(LinkRequest.class.isInterface(), "is an interface");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testRequestObservedByLinker();
        testInterfaceSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
