/*
 * @test
 * @summary java.lang.LazyConstant conformance: white-box structure + black-box behavior.
 * @run main LazyConstantTest
 * @enablePreview
 */

import java.lang.LazyConstant;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public class LazyConstantTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.LazyConstant");
        check(c.isInterface(), "LazyConstant is an interface");
        check(Supplier.class.isAssignableFrom(c), "LazyConstant extends Supplier");
        Method of = c.getMethod("of", Supplier.class);
        check(Modifier.isStatic(of.getModifiers()), "of is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOfAndGet() {
        final int[] calls = {0};
        LazyConstant<String> lc = LazyConstant.of(() -> { calls[0]++; return "v"; });
        check(lc != null, "of non-null");
        check(calls[0] == 0, "supplier not called eagerly");
        check("v".equals(lc.get()), "get evaluates");
        check("v".equals(lc.get()), "get cached");
        check(calls[0] == 1, "supplier called exactly once (lazy + cached)");
        check(lc instanceof Supplier, "is Supplier");
    }

    static void testContract() {
        LazyConstant<String> lc = LazyConstant.of(() -> "v");
        check(lc.equals(lc), "equals self");
        check(lc.hashCode() == lc.hashCode(), "hashCode stable");
        check(lc.toString() != null, "toString non-null");
    }

    
    static void testBlackBox() throws Exception {
        testOfAndGet();
        testContract();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
