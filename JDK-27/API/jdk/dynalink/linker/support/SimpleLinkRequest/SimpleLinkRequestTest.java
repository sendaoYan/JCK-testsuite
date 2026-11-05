/*
 * @test
 * @summary jdk.dynalink.linker.support.SimpleLinkRequest conformance: white-box structure + black-box behavior.
 * @run main SimpleLinkRequestTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class SimpleLinkRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.SimpleLinkRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(LinkRequest.class.isAssignableFrom(c), "implements LinkRequest");
        check(hasMethod(c, "getArguments"), "getArguments");
        check(hasMethod(c, "getReceiver"), "getReceiver");
        check(hasMethod(c, "getCallSiteDescriptor"), "getCallSiteDescriptor");
        check(hasMethod(c, "isCallSiteUnstable"), "isCallSiteUnstable");
        check(hasMethod(c, "replaceArguments"), "replaceArguments");

    }

    static void testBlackBox() throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleLinkRequest r = new SimpleLinkRequest(csd, false, "obj");
        check(r.getReceiver().equals("obj"), "receiver");
        check(r.getArguments().length == 1, "arguments");
        check(r.getCallSiteDescriptor() == csd, "descriptor");
        check(!r.isCallSiteUnstable(), "stable");
        SimpleLinkRequest r2 = new SimpleLinkRequest(csd, true, "a", "b");
        check(r2.isCallSiteUnstable(), "unstable");
        check(r2.getArguments().length == 2, "two args");
        LinkRequest r3 = r.replaceArguments(csd, "x");
        check(r3.getArguments().length == 1, "replaceArguments");

    }


    static void testDeepContract() {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        CallSiteDescriptor d = new CallSiteDescriptor(lk,
            StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("f"),
            MethodType.methodType(Object.class, Object.class));
        SimpleLinkRequest r = new SimpleLinkRequest(d, false, "recv", "a1");
        check(r.getCallSiteDescriptor() == d, "descriptor kept by identity");
        check(!r.isCallSiteUnstable(), "stable request");
        check(r.getReceiver().equals("recv"), "receiver is argument 0");
        check(r.getArguments().length == 2, "arguments length");
        check(r.getArguments()[1].equals("a1"), "extra arguments follow");
        // the unstable flag is simply carried
        SimpleLinkRequest u = new SimpleLinkRequest(d, true, "x");
        check(u.isCallSiteUnstable(), "unstable request");
        check(u.getArguments().length == 1, "single argument");
        // replaceArguments builds a new request over the same descriptor
        LinkRequest rep = r.replaceArguments(d, new Object[]{ "y", "b2" });
        check(rep != r, "new instance");
        check(rep.getCallSiteDescriptor() == d, "descriptor shared");
        check(rep.getReceiver().equals("y"), "replaced receiver");
        check(rep.getArguments()[1].equals("b2"), "replaced arguments");
        check(r.getReceiver().equals("recv"), "original untouched");
        // null receiver is allowed (arguments[0] == null)
        SimpleLinkRequest n = new SimpleLinkRequest(d, false, (Object) null);
        check(n.getReceiver() == null, "null receiver allowed");
        check(n.getArguments().length == 1 && n.getArguments()[0] == null, "null travels as argument 0");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
