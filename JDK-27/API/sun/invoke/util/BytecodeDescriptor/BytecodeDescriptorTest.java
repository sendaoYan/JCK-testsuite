/*
 * @test
 * @summary sun.invoke.util.BytecodeDescriptor conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main BytecodeDescriptorTest
 */
public class BytecodeDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.BytecodeDescriptor", false, BytecodeDescriptorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "parseClass"), "method parseClass");
        check(hasMethod(c, "parseMethod"), "method parseMethod");
        check(hasMethod(c, "unparse"), "method unparse");
        check(hasMethod(c, "unparseMethod"), "method unparseMethod");
        check(hasField(c, "CHECK_OFFSET"), "field CHECK_OFFSET");
        check(hasField(c, "NON_IDENTIFIER_MASK"), "field NON_IDENTIFIER_MASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
