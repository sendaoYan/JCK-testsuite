/*
 * @test
 * @summary org.omg.IOP.CodeSets conformance: constant holder interface.
 * @run main CodeSetsTest
 */

public class CodeSetsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.CodeSets");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ServiceId constant exact contract ----
        check(org.omg.IOP.CodeSets.value == 1, "CodeSets.value == 1 (ServiceId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.CodeSets.value - org.omg.IOP.TransactionService.value == 1,
              "service ids TransactionService=0, CodeSets=1 contiguous");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
