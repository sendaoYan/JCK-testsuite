/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup conformance: find* and unreflect.
 * @run main LookupTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LookupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLookup() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(lookup.lookupClass() == LookupTest.class, "lookupClass");
        check(lookup.lookupModes() != 0, "lookupModes");

        MethodHandle virtual = lookup.findVirtual(
                String.class, "length", MethodType.methodType(int.class));
        check((int) virtual.invoke("abc") == 3, "findVirtual");

        MethodHandle stat = lookup.findStatic(
                Integer.class, "valueOf", MethodType.methodType(Integer.class, int.class));
        check((Integer) stat.invoke(5) == 5, "findStatic");

        MethodHandle ctor = lookup.findConstructor(
                java.util.ArrayList.class, MethodType.methodType(void.class));
        java.util.ArrayList<?> list = (java.util.ArrayList<?>) ctor.invoke();
        check(list.isEmpty(), "findConstructor");

        MethodHandle getter = lookup.findGetter(
                java.awt.Point.class, "x", int.class);
        check((int) getter.invoke(new java.awt.Point(9, 0)) == 9, "findGetter");

        MethodHandle unreflected = lookup.unreflect(
                String.class.getMethod("length"));
        check((int) unreflected.invoke("xy") == 2, "unreflect");

        check(lookup.in(Integer.class) != null, "in");
    }

    public static void main(String[] args) throws Throwable {
        testLookup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
