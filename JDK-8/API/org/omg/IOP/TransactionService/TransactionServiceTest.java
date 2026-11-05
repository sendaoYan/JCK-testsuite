/*
 * @test
 * @summary org.omg.IOP.TransactionService conformance: constant holder interface.
 * @run main TransactionServiceTest
 */

public class TransactionServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TransactionService");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ServiceId constant exact contract ----
        check(org.omg.IOP.TransactionService.value == 0, "TransactionService.value == 0 (ServiceId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.ServiceIdHelper.id().equals("IDL:omg.org/IOP/ServiceId:1.0"),
              "ServiceIdHelper repository id exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
