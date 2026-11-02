/*
 * @test
 * @summary jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker conformance: composite type-based linker.
 * @run main CompositeTypeBasedGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class CompositeTypeBasedGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeTypeBasedGuardingDynamicLinker c = new CompositeTypeBasedGuardingDynamicLinker(Collections.emptyList());
        check(c != null, "constructed");
        check(!c.canLinkType(String.class), "cannot link");
        check(CompositeTypeBasedGuardingDynamicLinker.optimize(Collections.emptyList()) != null, "optimize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
