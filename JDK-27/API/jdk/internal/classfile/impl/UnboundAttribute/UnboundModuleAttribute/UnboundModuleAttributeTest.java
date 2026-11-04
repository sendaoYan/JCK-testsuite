/*
 * @test
 * @summary jdk.internal.classfile.impl.UnboundAttribute.UnboundModuleAttribute conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UnboundModuleAttributeTest
 */
public class UnboundModuleAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.UnboundAttribute$UnboundModuleAttribute", false, UnboundModuleAttributeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.UnboundAttribute$UnboundModuleAttribute"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.UnboundAttribute"), "superclass jdk.internal.classfile.impl.UnboundAttribute");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.attribute.ModuleAttribute"), "interface java.lang.classfile.attribute.ModuleAttribute");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 9, "declared methods >= 9");
        check(hasM(ms, "attributeName"), "method attributeName");
        check(hasM(ms, "exports"), "method exports");
        check(hasM(ms, "moduleFlagsMask"), "method moduleFlagsMask");
        check(hasM(ms, "moduleName"), "method moduleName");
        check(hasM(ms, "moduleVersion"), "method moduleVersion");
        check(hasM(ms, "opens"), "method opens");
        check(hasM(ms, "provides"), "method provides");
        check(hasM(ms, "requires"), "method requires");
        check(hasM(ms, "uses"), "method uses");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 9, "declared fields >= 9");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
