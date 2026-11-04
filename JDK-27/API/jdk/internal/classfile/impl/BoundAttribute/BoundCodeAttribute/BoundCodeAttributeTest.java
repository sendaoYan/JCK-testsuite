/*
 * @test
 * @summary jdk.internal.classfile.impl.BoundAttribute.BoundCodeAttribute conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main BoundCodeAttributeTest
 */
public class BoundCodeAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.BoundAttribute$BoundCodeAttribute", false, BoundCodeAttributeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.BoundAttribute$BoundCodeAttribute"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.classfile.impl.BoundAttribute"), "superclass jdk.internal.classfile.impl.BoundAttribute");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.attribute.CodeAttribute"), "interface java.lang.classfile.attribute.CodeAttribute");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 4, "declared methods >= 4");
        check(hasM(ms, "codeArray"), "method codeArray");
        check(hasM(ms, "codeLength"), "method codeLength");
        check(hasM(ms, "maxLocals"), "method maxLocals");
        check(hasM(ms, "maxStack"), "method maxStack");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 7, "declared fields >= 7");
        check(hasF(fs, "attributePos"), "field attributePos");
        check(hasF(fs, "codeEnd"), "field codeEnd");
        check(hasF(fs, "codeLength"), "field codeLength");
        check(hasF(fs, "codeStart"), "field codeStart");
        check(hasF(fs, "enclosingMethod"), "field enclosingMethod");
        check(hasF(fs, "exceptionHandlerCnt"), "field exceptionHandlerCnt");
        check(hasF(fs, "exceptionHandlerPos"), "field exceptionHandlerPos");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
