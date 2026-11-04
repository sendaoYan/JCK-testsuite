/*
 * @test
 * @summary jdk.internal.classfile.impl.BoundLocalVariable conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BoundLocalVariableTest
 */
public class BoundLocalVariableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BoundLocalVariable", false, BoundLocalVariableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BoundLocalVariable"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractBoundLocalVariable"), "superclass jdk.internal.classfile.impl.AbstractBoundLocalVariable");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "java.lang.classfile.attribute.LocalVariableInfo"), "interface java.lang.classfile.attribute.LocalVariableInfo");
        check(hasI(c.getInterfaces(), "java.lang.classfile.instruction.LocalVariable"), "interface java.lang.classfile.instruction.LocalVariable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 4, "declared methods >= 4");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "type"), "method type");
        check(hasM(ms, "typeSymbol"), "method typeSymbol");
        check(hasM(ms, "writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
