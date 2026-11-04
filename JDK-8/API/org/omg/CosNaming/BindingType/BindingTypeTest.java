/*
 * @test
 * @summary org.omg.CosNaming.BindingType conformance: enumeration values.
 * @run main BindingTypeTest
 */

public class BindingTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingType.nobject != null, "nobject constant non-null");
        check(org.omg.CosNaming.BindingType.nobject.value() == org.omg.CosNaming.BindingType._nobject, "value() matches int constant");
        check(org.omg.CosNaming.BindingType.from_int(org.omg.CosNaming.BindingType._nobject) == org.omg.CosNaming.BindingType.nobject, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
