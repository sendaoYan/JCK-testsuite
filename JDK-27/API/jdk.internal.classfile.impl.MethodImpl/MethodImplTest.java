/*
 * @test
 * @summary jdk.internal.classfile.impl.MethodImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main MethodImplTest
 */
public class MethodImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.MethodImpl", false, MethodImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.MethodImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 3, "direct interfaces >= 3");
        check(hasI(c.getInterfaces(), "java.lang.classfile.MethodModel"), "interface java.lang.classfile.MethodModel");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.MethodInfo"), "interface jdk.internal.classfile.impl.MethodInfo");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.Util$Writable"), "interface jdk.internal.classfile.impl.Util$Writable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "attributes"), "method attributes");
        check(hasM(ms, "code"), "method code");
        check(hasM(ms, "flags"), "method flags");
        check(hasM(ms, "forEach"), "method forEach");
        check(hasM(ms, "methodFlags"), "method methodFlags");
        check(hasM(ms, "methodName"), "method methodName");
        check(hasM(ms, "methodType"), "method methodType");
        check(hasM(ms, "methodTypeSymbol"), "method methodTypeSymbol");
        check(hasM(ms, "parameterSlot"), "method parameterSlot");
        check(hasM(ms, "parent"), "method parent");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
