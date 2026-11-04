/*
 * @test
 * @summary sun.reflect.annotation.TypeAnnotation.LocationInfo conformance: class and members.
 * @run main LocationInfoTest
 */

public class LocationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.TypeAnnotation$LocationInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.TypeAnnotation$LocationInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("filter"), "declares method filter");
        check(mnames.contains("isSameLocationInfo"), "declares method isSameLocationInfo");
        check(mnames.contains("parseLocationInfo"), "declares method parseLocationInfo");
        check(mnames.contains("pushArray"), "declares method pushArray");
        check(mnames.contains("pushInner"), "declares method pushInner");
        check(mnames.contains("pushLocation"), "declares method pushLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
