/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl conformance: class and members.
 * @run main ParameterizedTypeImplTest
 */

public class ParameterizedTypeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.reflect.ParameterizedType"), "interface java.lang.reflect.ParameterizedType");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getActualTypeArguments"), "declares method getActualTypeArguments");
        check(mnames.contains("getOwnerType"), "declares method getOwnerType");
        check(mnames.contains("getRawType"), "declares method getRawType");
        check(mnames.contains("hashCode"), "declares method hashCode");
        check(mnames.contains("make"), "declares method make");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
