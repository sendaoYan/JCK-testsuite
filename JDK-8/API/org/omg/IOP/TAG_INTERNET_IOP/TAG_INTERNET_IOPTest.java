/*
 * @test
 * @summary org.omg.IOP.TAG_INTERNET_IOP conformance: constant holder interface.
 * @run main TAG_INTERNET_IOPTest
 */

public class TAG_INTERNET_IOPTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.TAG_INTERNET_IOP");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == int.class, "value is int");
        // ---- deepened (jdk8u372-b07 probed): ProfileId constant exact contract ----
        check(org.omg.IOP.TAG_INTERNET_IOP.value == 0, "TAG_INTERNET_IOP.value == 0 (ProfileId family)");
        check(c.getDeclaredFields().length == 1, "exactly one declared field (single-constant interface)");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        check(f.getType() == int.class, "value is int (IDL unsigned long -> Java int)");
        check(org.omg.IOP.TAG_INTERNET_IOP.value != org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value,
              "TAG_INTERNET_IOP != TAG_MULTIPLE_COMPONENTS (two reserved profile ids distinct)");
        check(org.omg.IOP.ProfileIdHelper.id().equals("IDL:omg.org/IOP/ProfileId:1.0"),
              "ProfileIdHelper repository id exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
