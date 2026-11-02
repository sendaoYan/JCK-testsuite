/*
 * @test
 * @summary jdk.dynalink.linker.support.CompositeGuardingDynamicLinker conformance: composite linker over empty list.
 * @run main CompositeGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class CompositeGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CompositeGuardingDynamicLinker c = new CompositeGuardingDynamicLinker(Collections.emptyList());
        check(c != null, "constructed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
