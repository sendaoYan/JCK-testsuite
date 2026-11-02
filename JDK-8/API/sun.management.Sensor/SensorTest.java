/*
 * @test
 * @summary sun.management.Sensor conformance: abstract class and members.
 * @run main SensorTest
 */

public class SensorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.Sensor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.Sensor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clear"), "declares method clear");
        check(mnames.contains("clearAction"), "declares method clearAction");
        check(mnames.contains("getCount"), "declares method getCount");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("isOn"), "declares method isOn");
        check(mnames.contains("toString"), "declares method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
