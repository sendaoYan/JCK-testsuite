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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
