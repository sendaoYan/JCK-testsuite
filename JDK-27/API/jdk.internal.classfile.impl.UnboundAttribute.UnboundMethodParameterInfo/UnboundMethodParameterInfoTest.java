/*
 * @test
 * @summary jdk.internal.classfile.impl.UnboundAttribute.UnboundMethodParameterInfo conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main UnboundMethodParameterInfoTest
 */
public class UnboundMethodParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.UnboundAttribute$UnboundMethodParameterInfo", false, UnboundMethodParameterInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.UnboundAttribute$UnboundMethodParameterInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass java.lang.Record");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.classfile.attribute.MethodParameterInfo"), "interface java.lang.classfile.attribute.MethodParameterInfo");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 5, "declared methods >= 5");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "flagsMask"), "method flagsMask");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "name"), "method name");
        check(hasM(ms, "toString"), "method toString");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
