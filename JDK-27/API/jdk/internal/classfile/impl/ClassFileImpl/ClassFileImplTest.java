/*
 * @test
 * @summary jdk.internal.classfile.impl.ClassFileImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ClassFileImplTest
 */
public class ClassFileImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.ClassFileImpl", false, ClassFileImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.ClassFileImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.ClassFile"), "interface java.lang.classfile.ClassFile");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 20, "declared methods >= 20");
        check(hasM(ms, "attributeMapper"), "method attributeMapper");
        check(hasM(ms, "attributesProcessingOption"), "method attributesProcessingOption");
        check(hasM(ms, "build"), "method build");
        check(hasM(ms, "classHierarchyResolver"), "method classHierarchyResolver");
        check(hasM(ms, "dropDeadLabels"), "method dropDeadLabels");
        check(hasM(ms, "dropStackMaps"), "method dropStackMaps");
        check(hasM(ms, "fixShortJumps"), "method fixShortJumps");
        check(hasM(ms, "generateStackMaps"), "method generateStackMaps");
        check(hasM(ms, "parse"), "method parse");
        check(hasM(ms, "passDebugElements"), "method passDebugElements");
        check(hasM(ms, "passLineNumbers"), "method passLineNumbers");
        check(hasM(ms, "patchDeadCode"), "method patchDeadCode");
        check(hasM(ms, "sharedConstantPool"), "method sharedConstantPool");
        check(hasM(ms, "stackMapsWhenRequired"), "method stackMapsWhenRequired");
        check(hasM(ms, "transformClass"), "method transformClass");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 11, "declared fields >= 11");
        check(hasF(fs, "DEFAULT_CONTEXT"), "field DEFAULT_CONTEXT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
