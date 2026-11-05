/*
 * @test
 * @summary org.omg.IOP.TAG_ORB_TYPE conformance: constant holder interface.
 * @run main TAG_ORB_TYPETest
 */

public class TAG_ORB_TYPETest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_ORB_TYPE");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ComponentId constant exact contract ----
        check(org.omg.IOP.TAG_ORB_TYPE.value == 0, "TAG_ORB_TYPE.value == 0 (ComponentId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int");
        check(org.omg.IOP.ComponentIdHelper.id().equals("IDL:omg.org/IOP/ComponentId:1.0"),
              "ComponentIdHelper repository id exact");
        check(org.omg.IOP.TAG_ORB_TYPE.value == org.omg.IOP.TAG_INTERNET_IOP.value,
              "ComponentId TAG_ORB_TYPE==0 numerically collides with ProfileId TAG_INTERNET_IOP==0 (separate namespaces)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
