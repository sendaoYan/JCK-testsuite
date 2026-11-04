/*
 * @test
 * @summary jdk.internal.lang.LazyConstantImpl conformance: internal package reflection.
 * @modules java.base/jdk.internal.lang:+open
 * @run main LazyConstantImplTest
 */
public class LazyConstantImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.lang.LazyConstantImpl", false, LazyConstantImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.LazyConstant"), "interface java.lang.LazyConstant");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "isolateToString"), "method isolateToString");
        check(hasMethod(c, "ofLazy"), "method ofLazy");
        check(hasMethod(c, "orElse"), "method orElse");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "CONSTANT_OFFSET"), "field CONSTANT_OFFSET");
        check(hasField(c, "UNSAFE"), "field UNSAFE");
        check(hasField(c, "computingFunctionOrExceptionType"), "field computingFunctionOrExceptionType");
        check(hasField(c, "constant"), "field constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
