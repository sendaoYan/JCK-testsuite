/*
 * @test
 * @summary java.lang.LazyConstant conformance: of()/get() and Supplier contract.
 * @run main LazyConstantTest
 * @enablePreview
 */
import java.lang.LazyConstant;
import java.util.function.Supplier;

public class LazyConstantTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(LazyConstant.class.isInterface(), "is interface");
        LazyConstant<String> lc = LazyConstant.of(() -> "v");
        check(lc != null, "of non-null");
        check("v".equals(lc.get()), "get");
        check(lc instanceof Supplier, "is Supplier");
        LazyConstant<String> lc2 = LazyConstant.of(() -> "v");
        check(lc.equals(lc), "equals self");
        check(lc.hashCode() == lc.hashCode(), "hashCode stable");
        check(lc.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
