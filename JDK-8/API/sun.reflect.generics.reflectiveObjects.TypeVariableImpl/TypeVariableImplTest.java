/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.TypeVariableImpl conformance: class and members.
 * @run main TypeVariableImplTest
 */

public class TypeVariableImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.TypeVariableImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.generics.reflectiveObjects.TypeVariableImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator"), "superclass sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.reflect.TypeVariable"), "interface java.lang.reflect.TypeVariable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getAnnotatedBounds"), "declares method getAnnotatedBounds");
        check(mnames.contains("getAnnotation"), "declares method getAnnotation");
        check(mnames.contains("getAnnotations"), "declares method getAnnotations");
        check(mnames.contains("getAnnotationsByType"), "declares method getAnnotationsByType");
        check(mnames.contains("getBoundASTs"), "declares method getBoundASTs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
