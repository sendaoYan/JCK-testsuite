/*
 * @test
 * @summary jdk.internal.classfile.impl.AbstractPseudoInstruction.ExceptionCatchImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main ExceptionCatchImplTest
 */
public class ExceptionCatchImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.AbstractPseudoInstruction$ExceptionCatchImpl", false, ExceptionCatchImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.AbstractPseudoInstruction$ExceptionCatchImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.AbstractPseudoInstruction"), "superclass jdk.internal.classfile.impl.AbstractPseudoInstruction");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.instruction.ExceptionCatch"), "interface java.lang.classfile.instruction.ExceptionCatch");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 7, "declared methods >= 7");
        check(hasM(ms, "catchType"), "method catchType");
        check(hasM(ms, "handler"), "method handler");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "tryEnd"), "method tryEnd");
        check(hasM(ms, "tryStart"), "method tryStart");
        check(hasM(ms, "writeTo"), "method writeTo");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        check(hasF(fs, "catchTypeEntry"), "field catchTypeEntry");
        check(hasF(fs, "handler"), "field handler");
        check(hasF(fs, "tryEnd"), "field tryEnd");
        check(hasF(fs, "tryStart"), "field tryStart");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
