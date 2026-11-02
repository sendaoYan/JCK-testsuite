/*
 * @test
 * @summary jdk.internal.classfile.impl.BufWriterImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BufWriterImplTest
 */
public class BufWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BufWriterImpl", false, BufWriterImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BufWriterImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.BufWriter"), "interface java.lang.classfile.BufWriter");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 44, "declared methods >= 44");
        check(hasM(ms, "bytecodeView"), "method bytecodeView");
        check(hasM(ms, "canWriteDirect"), "method canWriteDirect");
        check(hasM(ms, "constantPool"), "method constantPool");
        check(hasM(ms, "context"), "method context");
        check(hasM(ms, "copyTo"), "method copyTo");
        check(hasM(ms, "cpIndex"), "method cpIndex");
        check(hasM(ms, "cpIndexOrZero"), "method cpIndexOrZero");
        check(hasM(ms, "getMajorVersion"), "method getMajorVersion");
        check(hasM(ms, "labelContext"), "method labelContext");
        check(hasM(ms, "labelsMatch"), "method labelsMatch");
        check(hasM(ms, "patchInt"), "method patchInt");
        check(hasM(ms, "patchU2"), "method patchU2");
        check(hasM(ms, "reserveSpace"), "method reserveSpace");
        check(hasM(ms, "setLabelContext"), "method setLabelContext");
        check(hasM(ms, "size"), "method size");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
