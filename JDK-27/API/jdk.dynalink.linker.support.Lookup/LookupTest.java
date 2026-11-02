/*
 * @test
 * @summary jdk.dynalink.linker.support.Lookup conformance: lookup unreflect and find.
 * @run main LookupTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class LookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Lookup.PUBLIC != null, "PUBLIC");
        Lookup lk = new Lookup(MethodHandles.publicLookup());
        check(lk.findStatic(Math.class, "abs", MethodType.methodType(int.class, int.class)) != null, "findStatic");
        check(lk.findVirtual(String.class, "length", MethodType.methodType(int.class)) != null, "findVirtual");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
