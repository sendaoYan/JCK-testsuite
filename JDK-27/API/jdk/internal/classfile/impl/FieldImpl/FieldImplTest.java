/*
 * @test
 * @summary jdk.internal.classfile.impl.FieldImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main FieldImplTest
 */
public class FieldImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.FieldImpl", false, FieldImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.FieldImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "java.lang.classfile.FieldModel"), "interface java.lang.classfile.FieldModel");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.Util$Writable"), "interface jdk.internal.classfile.impl.Util$Writable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 9, "declared methods >= 9");
        check(hasM(ms, "attributes"), "method attributes");
        check(hasM(ms, "fieldName"), "method fieldName");
        check(hasM(ms, "fieldType"), "method fieldType");
        check(hasM(ms, "flags"), "method flags");
        check(hasM(ms, "forEach"), "method forEach");
        check(hasM(ms, "parent"), "method parent");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
