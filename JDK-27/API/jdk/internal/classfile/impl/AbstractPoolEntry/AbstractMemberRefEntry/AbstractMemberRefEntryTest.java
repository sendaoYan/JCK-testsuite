/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractPoolEntry.AbstractMemberRefEntry conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main AbstractMemberRefEntryTest
 */
public class AbstractMemberRefEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractPoolEntry$AbstractMemberRefEntry", false, AbstractMemberRefEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry$AbstractMemberRefEntry"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractPoolEntry$AbstractRefsEntry"), "superclass jdk.internal.classfile.impl.AbstractPoolEntry$AbstractRefsEntry");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.constantpool.MemberRefEntry"), "interface java.lang.classfile.constantpool.MemberRefEntry");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 8, "declared methods >= 8");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "nameAndType"), "method nameAndType");
        check(hasM(ms, "owner"), "method owner");
        check(hasM(ms, "ref1"), "method ref1");
        check(hasM(ms, "ref2"), "method ref2");
        check(hasM(ms, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
