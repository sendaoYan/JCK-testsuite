/*
 * @test
 * @summary jdk.internal.classfile.impl.BoundAttribute conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BoundAttributeTest
 */
public class BoundAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BoundAttribute", false, BoundAttributeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BoundAttribute"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "java.lang.classfile.Attribute"), "interface java.lang.classfile.Attribute");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.Util$Writable"), "interface jdk.internal.classfile.impl.Util$Writable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "attributeMapper"), "method attributeMapper");
        check(hasM(ms, "attributeName"), "method attributeName");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "contents"), "method contents");
        check(hasM(ms, "payloadLen"), "method payloadLen");
        check(hasM(ms, "readAttributes"), "method readAttributes");
        check(hasM(ms, "standardAttribute"), "method standardAttribute");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
