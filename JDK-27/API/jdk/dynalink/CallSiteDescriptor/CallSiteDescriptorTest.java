/*
 * @test
 * @summary jdk.dynalink.CallSiteDescriptor conformance: white-box structure + black-box behavior.
 * @run main CallSiteDescriptorTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class CallSiteDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.CallSiteDescriptor");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SecureLookupSupplier.class.isAssignableFrom(c), "extends SecureLookupSupplier");
        check(hasMethod(c, "getOperation"), "getOperation");
        check(hasMethod(c, "getMethodType"), "getMethodType");
        check(hasMethod(c, "changeOperation"), "changeOperation");
        check(hasMethod(c, "changeMethodType"), "changeMethodType");

    }

    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(Object.class, Object.class);
        CallSiteDescriptor csd = new CallSiteDescriptor(lk, StandardOperation.GET, mt);
        check(csd.getOperation() == StandardOperation.GET, "operation");
        check(csd.getMethodType().equals(mt), "method type");
        check(csd.changeOperation(StandardOperation.SET).getOperation() == StandardOperation.SET, "changeOperation");
        check(csd.changeMethodType(mt).getMethodType().equals(mt), "changeMethodType");
        check(csd.hashCode() == csd.hashCode(), "hashCode stable");
        check(csd.equals(csd), "equals self");
        check(csd.toString() != null, "toString");

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

    static void testDeepContract() {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(Object.class, Object.class);
        // ctor rejects all nulls
        for (int k = 0; k < 3; k++) {
            try {
                if (k == 0) new CallSiteDescriptor(null, StandardOperation.GET, mt);
                else if (k == 1) new CallSiteDescriptor(lk, null, mt);
                else new CallSiteDescriptor(lk, StandardOperation.GET, null);
                check(false, "ctor arg " + k + " null must throw");
            } catch (NullPointerException expected) { check(true, "ctor null arg " + k + " NPE"); }
        }
        // equals: lookup class + modes + operation + method type
        CallSiteDescriptor d1 = new CallSiteDescriptor(lk, StandardOperation.GET, mt);
        check(d1.equals(new CallSiteDescriptor(lk, StandardOperation.GET, mt)), "equal components are equal");
        check(d1.equals(new CallSiteDescriptor(MethodHandles.publicLookup(), StandardOperation.GET, mt)) == false,
              "different lookup mode is not equal");
        check(d1.equals(new CallSiteDescriptor(lk, StandardOperation.SET, mt)) == false, "different operation not equal");
        check(d1.equals(new CallSiteDescriptor(lk, StandardOperation.GET, MethodType.methodType(Object.class))) == false,
              "different method type not equal");
        check(d1.hashCode() == new CallSiteDescriptor(lk, StandardOperation.GET, mt).hashCode(), "hashCode matches equals");
        // toString is operation + methodType + '@' + lookup
        check(d1.toString().equals("GET(Object)Object@" + lk), "toString exact: " + d1.toString());
        check(d1.toString().endsWith("@" + lk.toString()), "toString ends with lookup");
        CallSiteDescriptor pub = new CallSiteDescriptor(MethodHandles.publicLookup(), StandardOperation.GET, mt);
        check(pub.toString().equals("GET(Object)Object@java.lang.Object/publicLookup"), "public lookup toString: " + pub.toString());
        // changeOperation / changeMethodType keep the other component
        CallSiteDescriptor ch = d1.changeOperation(StandardOperation.SET);
        check(ch.getOperation() == StandardOperation.SET && ch.getMethodType() == mt, "changeOperation keeps type");
        check(d1.getOperation() == StandardOperation.GET, "changeOperation is immutable on the original");
        CallSiteDescriptor cht = d1.changeMethodType(MethodType.methodType(Object.class));
        check(cht.getMethodType().parameterCount() == 0 && cht.getOperation() == StandardOperation.GET, "changeMethodType keeps op");
        check(d1.getMethodType() == mt, "changeMethodType is immutable on the original");
        // SecureLookupSupplier inheritance is live: getLookup returns the constructor lookup
        check(d1.getLookup() == lk, "getLookup returns the constructor lookup");
        // the operation survives wrapping: named and namespace operations both legal here
        Operation named = StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named("f");
        CallSiteDescriptor dn = new CallSiteDescriptor(lk, named, mt);
        check(dn.getOperation() == named, "wrapped operation kept by identity");
        check(dn.toString().equals("GET:PROPERTY:f(Object)Object@" + lk), "named toString exact: " + dn.toString());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
