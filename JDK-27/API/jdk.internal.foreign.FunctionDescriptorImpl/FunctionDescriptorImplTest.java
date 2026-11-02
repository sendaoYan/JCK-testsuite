/*
 * @test
 * @summary jdk.internal.foreign.FunctionDescriptorImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main FunctionDescriptorImplTest
 */
public class FunctionDescriptorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.FunctionDescriptorImpl", false, FunctionDescriptorImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.FunctionDescriptorImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.foreign.FunctionDescriptor"), "interface java.lang.foreign.FunctionDescriptor");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 18, "declared methods >= 18");
        check(hasM(ms, "appendArgumentLayouts"), "method appendArgumentLayouts");
        check(hasM(ms, "argumentLayouts"), "method argumentLayouts");
        check(hasM(ms, "changeReturnLayout"), "method changeReturnLayout");
        check(hasM(ms, "dropReturnLayout"), "method dropReturnLayout");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "insertArgumentLayouts"), "method insertArgumentLayouts");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "ofVoid"), "method ofVoid");
        check(hasM(ms, "returnLayout"), "method returnLayout");
        check(hasM(ms, "toMethodType"), "method toMethodType");
        check(hasM(ms, "toString"), "method toString");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
