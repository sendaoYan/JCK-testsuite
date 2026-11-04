/*
 * @test
 * @summary sun.reflect.generics.factory.CoreReflectionFactory conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.factory:+open
 * @run main CoreReflectionFactoryTest
 */
public class CoreReflectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.factory.CoreReflectionFactory", false, CoreReflectionFactoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.reflect.generics.factory.GenericsFactory"), "interface sun.reflect.generics.factory.GenericsFactory");
        check(hasMethod(c, "findTypeVariable"), "method findTypeVariable");
        check(hasMethod(c, "make"), "method make");
        check(hasMethod(c, "makeArrayType"), "method makeArrayType");
        check(hasMethod(c, "makeBool"), "method makeBool");
        check(hasMethod(c, "makeByte"), "method makeByte");
        check(hasMethod(c, "makeChar"), "method makeChar");
        check(hasMethod(c, "makeDouble"), "method makeDouble");
        check(hasMethod(c, "makeFloat"), "method makeFloat");
        check(hasMethod(c, "makeInt"), "method makeInt");
        check(hasMethod(c, "makeLong"), "method makeLong");
        check(hasMethod(c, "makeNamedType"), "method makeNamedType");
        check(hasMethod(c, "makeParameterizedType"), "method makeParameterizedType");
        check(hasMethod(c, "makeShort"), "method makeShort");
        check(hasMethod(c, "makeTypeVariable"), "method makeTypeVariable");
        check(hasMethod(c, "makeVoid"), "method makeVoid");
        check(hasMethod(c, "makeWildcard"), "method makeWildcard");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "decl"), "field decl");
        check(hasField(c, "scope"), "field scope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
