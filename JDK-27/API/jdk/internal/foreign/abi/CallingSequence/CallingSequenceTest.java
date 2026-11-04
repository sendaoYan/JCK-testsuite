/*
 * @test
 * @summary jdk.internal.foreign.abi.CallingSequence conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main CallingSequenceTest
 */
public class CallingSequenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.CallingSequence", false, CallingSequenceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.CallingSequence"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 18, "declared methods >= 18");
        check(hasM(ms, "allocationSize"), "method allocationSize");
        check(hasM(ms, "argumentBindings"), "method argumentBindings");
        check(hasM(ms, "argumentBindingsCount"), "method argumentBindingsCount");
        check(hasM(ms, "asString"), "method asString");
        check(hasM(ms, "calleeMethodType"), "method calleeMethodType");
        check(hasM(ms, "callerMethodType"), "method callerMethodType");
        check(hasM(ms, "capturedStateMask"), "method capturedStateMask");
        check(hasM(ms, "forDowncall"), "method forDowncall");
        check(hasM(ms, "forUpcall"), "method forUpcall");
        check(hasM(ms, "functionDesc"), "method functionDesc");
        check(hasM(ms, "hasReturnBindings"), "method hasReturnBindings");
        check(hasM(ms, "needsReturnBuffer"), "method needsReturnBuffer");
        check(hasM(ms, "needsTransition"), "method needsTransition");
        check(hasM(ms, "numLeadingParams"), "method numLeadingParams");
        check(hasM(ms, "returnBindings"), "method returnBindings");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
