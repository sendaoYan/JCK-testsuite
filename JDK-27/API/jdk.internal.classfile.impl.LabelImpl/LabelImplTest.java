/*
 * @test
 * @summary jdk.internal.classfile.impl.LabelImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main LabelImplTest
 */
public class LabelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.LabelImpl", false, LabelImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.LabelImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractElement"), "superclass jdk.internal.classfile.impl.AbstractElement");
        check(c.getInterfaces().length >= 2, "direct interfaces >= 2");
        check(hasI(c.getInterfaces(), "java.lang.classfile.Label"), "interface java.lang.classfile.Label");
        check(hasI(c.getInterfaces(), "java.lang.classfile.instruction.LabelTarget"), "interface java.lang.classfile.instruction.LabelTarget");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "getBCI"), "method getBCI");
        check(hasM(ms, "label"), "method label");
        check(hasM(ms, "labelContext"), "method labelContext");
        check(hasM(ms, "setBCI"), "method setBCI");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
