/*
 * @test
 * @summary sun.reflect.generics.repository.GenericDeclRepository conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.repository:+open
 * @run main GenericDeclRepositoryTest
 */
public class GenericDeclRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.repository.GenericDeclRepository", false, GenericDeclRepositoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.repository.AbstractRepository"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getTypeParameters"), "method getTypeParameters");
        check(hasField(c, "EMPTY_TYPE_VARS"), "field EMPTY_TYPE_VARS");
        check(hasField(c, "typeParameters"), "field typeParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
