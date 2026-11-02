/*
 * @test
 * @summary jdk.internal.reflect.ConstantPool conformance: internal package reflection.
 * @modules java.base/jdk.internal.reflect:+open
 * @run main ConstantPoolTest
 */
public class ConstantPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.reflect.ConstantPool", false, ConstantPoolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getClassAt"), "method getClassAt");
        check(hasMethod(c, "getClassAtIfLoaded"), "method getClassAtIfLoaded");
        check(hasMethod(c, "getClassRefIndexAt"), "method getClassRefIndexAt");
        check(hasMethod(c, "getDoubleAt"), "method getDoubleAt");
        check(hasMethod(c, "getFieldAt"), "method getFieldAt");
        check(hasMethod(c, "getFieldAtIfLoaded"), "method getFieldAtIfLoaded");
        check(hasMethod(c, "getFloatAt"), "method getFloatAt");
        check(hasMethod(c, "getIntAt"), "method getIntAt");
        check(hasMethod(c, "getLongAt"), "method getLongAt");
        check(hasMethod(c, "getMemberRefInfoAt"), "method getMemberRefInfoAt");
        check(hasMethod(c, "getMethodAt"), "method getMethodAt");
        check(hasMethod(c, "getMethodAtIfLoaded"), "method getMethodAtIfLoaded");
        check(hasMethod(c, "getNameAndTypeRefIndexAt"), "method getNameAndTypeRefIndexAt");
        check(hasMethod(c, "getNameAndTypeRefInfoAt"), "method getNameAndTypeRefInfoAt");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getStringAt"), "method getStringAt");
        check(hasMethod(c, "getTagAt"), "method getTagAt");
        check(hasMethod(c, "getUTF8At"), "method getUTF8At");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
