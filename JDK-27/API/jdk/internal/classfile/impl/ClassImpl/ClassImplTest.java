/*
 * @test
 * @summary jdk.internal.classfile.impl.ClassImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ClassImplTest
 */
public class ClassImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.ClassImpl", false, ClassImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.ClassImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.ClassModel"), "interface java.lang.classfile.ClassModel");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 16, "declared methods >= 16");
        check(hasM(ms, "attributes"), "method attributes");
        check(hasM(ms, "classfileLength"), "method classfileLength");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "fields"), "method fields");
        check(hasM(ms, "flags"), "method flags");
        check(hasM(ms, "forEach"), "method forEach");
        check(hasM(ms, "interfaces"), "method interfaces");
        check(hasM(ms, "isModuleInfo"), "method isModuleInfo");
        check(hasM(ms, "majorVersion"), "method majorVersion");
        check(hasM(ms, "methods"), "method methods");
        check(hasM(ms, "minorVersion"), "method minorVersion");
        check(hasM(ms, "superclass"), "method superclass");
        check(hasM(ms, "thisClass"), "method thisClass");
        check(hasM(ms, "toString"), "method toString");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
