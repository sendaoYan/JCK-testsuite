/*
 * @test
 * @summary sun.reflect.generics.reflectiveObjects.TypeVariableImpl conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.reflectiveObjects:+open
 * @run main TypeVariableImplTest
 */
public class TypeVariableImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.reflectiveObjects.TypeVariableImpl", false, TypeVariableImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.reflect.TypeVariable"), "interface java.lang.reflect.TypeVariable");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getAnnotatedBounds"), "method getAnnotatedBounds");
        check(hasMethod(c, "getAnnotation"), "method getAnnotation");
        check(hasMethod(c, "getAnnotations"), "method getAnnotations");
        check(hasMethod(c, "getAnnotationsByType"), "method getAnnotationsByType");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getDeclaredAnnotation"), "method getDeclaredAnnotation");
        check(hasMethod(c, "getDeclaredAnnotations"), "method getDeclaredAnnotations");
        check(hasMethod(c, "getDeclaredAnnotationsByType"), "method getDeclaredAnnotationsByType");
        check(hasMethod(c, "getGenericDeclaration"), "method getGenericDeclaration");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "make"), "method make");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "bounds"), "field bounds");
        check(hasField(c, "genericDeclaration"), "field genericDeclaration");
        check(hasField(c, "name"), "field name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
