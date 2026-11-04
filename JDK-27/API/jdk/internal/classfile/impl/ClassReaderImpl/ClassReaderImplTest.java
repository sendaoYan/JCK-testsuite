/*
 * @test
 * @summary jdk.internal.classfile.impl.ClassReaderImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ClassReaderImplTest
 */
public class ClassReaderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.ClassReaderImpl", false, ClassReaderImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.ClassReaderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.ClassReader"), "interface java.lang.classfile.ClassReader");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 39, "declared methods >= 39");
        check(hasM(ms, "bootstrapMethodCount"), "method bootstrapMethodCount");
        check(hasM(ms, "bootstrapMethodEntry"), "method bootstrapMethodEntry");
        check(hasM(ms, "classfileLength"), "method classfileLength");
        check(hasM(ms, "compare"), "method compare");
        check(hasM(ms, "context"), "method context");
        check(hasM(ms, "copyBytesTo"), "method copyBytesTo");
        check(hasM(ms, "customAttributes"), "method customAttributes");
        check(hasM(ms, "entryByIndex"), "method entryByIndex");
        check(hasM(ms, "flags"), "method flags");
        check(hasM(ms, "readBytes"), "method readBytes");
        check(hasM(ms, "readDouble"), "method readDouble");
        check(hasM(ms, "readEntry"), "method readEntry");
        check(hasM(ms, "readEntryOrNull"), "method readEntryOrNull");
        check(hasM(ms, "readFloat"), "method readFloat");
        check(hasM(ms, "readInt"), "method readInt");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 17, "declared fields >= 17");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
