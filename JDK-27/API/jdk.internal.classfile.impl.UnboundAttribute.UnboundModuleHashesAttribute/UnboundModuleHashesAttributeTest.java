/*
 * @test
 * @summary jdk.internal.classfile.impl.UnboundAttribute.UnboundModuleHashesAttribute conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UnboundModuleHashesAttributeTest
 */
public class UnboundModuleHashesAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.UnboundAttribute$UnboundModuleHashesAttribute", false, UnboundModuleHashesAttributeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.UnboundAttribute$UnboundModuleHashesAttribute"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.UnboundAttribute"), "superclass jdk.internal.classfile.impl.UnboundAttribute");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.attribute.ModuleHashesAttribute"), "interface java.lang.classfile.attribute.ModuleHashesAttribute");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 3, "declared methods >= 3");
        check(hasM(ms, "algorithm"), "method algorithm");
        check(hasM(ms, "attributeName"), "method attributeName");
        check(hasM(ms, "hashes"), "method hashes");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
