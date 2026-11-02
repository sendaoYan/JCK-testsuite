/*
 * @test
 * @summary sun.management.GcInfoBuilder conformance: class and members.
 * @run main GcInfoBuilderTest
 */

public class GcInfoBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.GcInfoBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.GcInfoBuilder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("fillGcAttributeInfo"), "declares method fillGcAttributeInfo");
        check(mnames.contains("getGcExtItemCount"), "declares method getGcExtItemCount");
        check(mnames.contains("getGcInfoCompositeType"), "declares method getGcInfoCompositeType");
        check(mnames.contains("getItemNames"), "declares method getItemNames");
        check(mnames.contains("getLastGcInfo"), "declares method getLastGcInfo");
        check(mnames.contains("getLastGcInfo0"), "declares method getLastGcInfo0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
