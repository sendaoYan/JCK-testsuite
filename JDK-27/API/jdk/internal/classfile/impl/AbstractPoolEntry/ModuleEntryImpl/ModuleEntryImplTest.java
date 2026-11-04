/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractPoolEntry.ModuleEntryImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ModuleEntryImplTest
 */
public class ModuleEntryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractPoolEntry$ModuleEntryImpl", false, ModuleEntryImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry$ModuleEntryImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry$AbstractNamedEntry"), "superclass jdk.internal.classfile.impl.AbstractPoolEntry$AbstractNamedEntry");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.constantpool.ModuleEntry"), "interface java.lang.classfile.constantpool.ModuleEntry");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 10, "declared methods >= 10");
        check(hasM(ms, "asInternalName"), "method asInternalName");
        check(hasM(ms, "asSymbol"), "method asSymbol");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "matches"), "method matches");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "ref1"), "method ref1");
        check(hasM(ms, "tag"), "method tag");
        check(hasM(ms, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
