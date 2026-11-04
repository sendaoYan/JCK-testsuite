/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.WildcardTypeImpl conformance: class and members.
 * @run main WildcardTypeImplTest
 */

public class WildcardTypeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.WildcardTypeImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.reflectiveObjects.WildcardTypeImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator"), "superclass sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.reflect.WildcardType"), "interface java.lang.reflect.WildcardType");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getLowerBoundASTs"), "declares method getLowerBoundASTs");
        check(mnames.contains("getLowerBounds"), "declares method getLowerBounds");
        check(mnames.contains("getUpperBoundASTs"), "declares method getUpperBoundASTs");
        check(mnames.contains("getUpperBounds"), "declares method getUpperBounds");
        check(mnames.contains("hashCode"), "declares method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
