/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.WildcardTypeImpl conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.reflectiveObjects:+open
 * @run main WildcardTypeImplTest
 */
public class WildcardTypeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.WildcardTypeImpl", false, WildcardTypeImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.reflect.WildcardType"), "interface java.lang.reflect.WildcardType");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getLowerBounds"), "method getLowerBounds");
        check(hasMethod(c, "getUpperBounds"), "method getUpperBounds");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "make"), "method make");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "lowerBounds"), "field lowerBounds");
        check(hasField(c, "upperBounds"), "field upperBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
