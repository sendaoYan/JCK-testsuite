/*
 * @test
 * @summary jdk.internal.classfile.impl.CodeImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main CodeImplTest
 */
public class CodeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.CodeImpl", false, CodeImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.CodeImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.BoundAttribute$BoundCodeAttribute"), "superclass jdk.internal.classfile.impl.BoundAttribute$BoundCodeAttribute");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "jdk.internal.classfile.impl.LabelContext"), "interface jdk.internal.classfile.impl.LabelContext");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 22, "declared methods >= 22");
        check(hasM(ms, "attributes"), "method attributes");
        check(hasM(ms, "compareCodeBytes"), "method compareCodeBytes");
        check(hasM(ms, "exceptionHandlers"), "method exceptionHandlers");
        check(hasM(ms, "forEach"), "method forEach");
        check(hasM(ms, "getLabel"), "method getLabel");
        check(hasM(ms, "iterateExceptionHandlers"), "method iterateExceptionHandlers");
        check(hasM(ms, "labelToBci"), "method labelToBci");
        check(hasM(ms, "newLabel"), "method newLabel");
        check(hasM(ms, "parent"), "method parent");
        check(hasM(ms, "setLabelTarget"), "method setLabelTarget");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
