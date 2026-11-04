/*
 * @test
 * @summary jdk.internal.constant.MethodTypeDescImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main MethodTypeDescImplTest
 */
public class MethodTypeDescImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.MethodTypeDescImpl", false, MethodTypeDescImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.constant.MethodTypeDesc"), "interface java.lang.constant.MethodTypeDesc");
        check(hasMethod(c, "changeParameterType"), "method changeParameterType");
        check(hasMethod(c, "changeReturnType"), "method changeReturnType");
        check(hasMethod(c, "descriptorString"), "method descriptorString");
        check(hasMethod(c, "dropParameterTypes"), "method dropParameterTypes");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "insertParameterTypes"), "method insertParameterTypes");
        check(hasMethod(c, "ofDescriptor"), "method ofDescriptor");
        check(hasMethod(c, "ofTrusted"), "method ofTrusted");
        check(hasMethod(c, "ofValidated"), "method ofValidated");
        check(hasMethod(c, "parameterArray"), "method parameterArray");
        check(hasMethod(c, "parameterCount"), "method parameterCount");
        check(hasMethod(c, "parameterList"), "method parameterList");
        check(hasMethod(c, "parameterType"), "method parameterType");
        check(hasMethod(c, "resolveConstantDesc"), "method resolveConstantDesc");
        check(hasMethod(c, "returnType"), "method returnType");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "argTypes"), "field argTypes");
        check(hasField(c, "cachedDescriptorString"), "field cachedDescriptorString");
        check(hasField(c, "returnType"), "field returnType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
