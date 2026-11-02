/*
 * @test
 * @summary jdk.dynalink.SecureLookupSupplier conformance: lookup supplier.
 * @run main SecureLookupSupplierTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class SecureLookupSupplierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        SecureLookupSupplier s = new SecureLookupSupplier(MethodHandles.publicLookup());
        check(s.getLookup() != null, "lookup");
        check(SecureLookupSupplier.GET_LOOKUP_PERMISSION_NAME != null, "permission name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
