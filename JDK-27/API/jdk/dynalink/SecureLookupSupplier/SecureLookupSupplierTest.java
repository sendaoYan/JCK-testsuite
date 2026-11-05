/*
 * @test
 * @summary jdk.dynalink.SecureLookupSupplier conformance: white-box structure + black-box behavior.
 * @run main SecureLookupSupplierTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class SecureLookupSupplierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.SecureLookupSupplier");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "getLookup"), "getLookup");

    }

    static void testBlackBox() throws Exception {
        SecureLookupSupplier s = new SecureLookupSupplier(MethodHandles.publicLookup());
        check(s.getLookup() != null, "lookup");
        check(SecureLookupSupplier.GET_LOOKUP_PERMISSION_NAME != null, "permission name");
        SecureLookupSupplier s2 = new SecureLookupSupplier(MethodHandles.lookup());
        check(s2.getLookup() != null, "second lookup");

    }


    static class Exposing extends SecureLookupSupplier {
        Exposing(MethodHandles.Lookup l) { super(l); }
        MethodHandles.Lookup peek() { return getLookupPrivileged(); }
    }

    static void testDeepContract() {
        check(SecureLookupSupplier.GET_LOOKUP_PERMISSION_NAME.equals("dynalink.getLookup"), "permission name constant");
        try { new SecureLookupSupplier(null); check(false, "null lookup must throw"); }
        catch (NullPointerException expected) { check(true, "null lookup NPE"); }
        MethodHandles.Lookup lk = MethodHandles.lookup();
        SecureLookupSupplier s = new SecureLookupSupplier(lk);
        check(s.getLookup() == lk, "getLookup returns the secured lookup");
        // getLookup needs no permission on a stock runtime (no SecurityManager installed)
        check(s.getLookup() == lk, "getLookup callable without permission");
        // the protected privileged view is usable from a subclass
        Exposing e = new Exposing(lk);
        check(e.peek() == lk, "getLookupPrivileged from subclass");
        check(e.getLookup() == lk, "both views agree");
        // CallSiteDescriptor is the canonical subclass
        CallSiteDescriptor d = new CallSiteDescriptor(lk, StandardOperation.GET, MethodType.methodType(Object.class));
        check(d instanceof SecureLookupSupplier, "CallSiteDescriptor extends SecureLookupSupplier");
        check(d.getLookup() == lk, "descriptor lookup through the supplier API");
        check(!Modifier.isFinal(SecureLookupSupplier.class.getModifiers()), "class is not final (subclassable)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
