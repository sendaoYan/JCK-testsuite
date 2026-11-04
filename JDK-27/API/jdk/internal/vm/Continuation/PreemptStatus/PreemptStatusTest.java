/*
 * @test
 * @summary jdk.internal.vm.Continuation.PreemptStatus conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm:+open
 * @run main PreemptStatusTest
 */
public class PreemptStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.Continuation$PreemptStatus", false, PreemptStatusTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "pinned"), "method pinned");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "PERM_FAIL_NOT_MOUNTED"), "field PERM_FAIL_NOT_MOUNTED");
        check(hasField(c, "PERM_FAIL_UNSUPPORTED"), "field PERM_FAIL_UNSUPPORTED");
        check(hasField(c, "PERM_FAIL_YIELDING"), "field PERM_FAIL_YIELDING");
        check(hasField(c, "SUCCESS"), "field SUCCESS");
        check(hasField(c, "TRANSIENT_FAIL_PINNED_CRITICAL_SECTION"), "field TRANSIENT_FAIL_PINNED_CRITICAL_SECTION");
        check(hasField(c, "TRANSIENT_FAIL_PINNED_NATIVE"), "field TRANSIENT_FAIL_PINNED_NATIVE");
        check(hasField(c, "pinned"), "field pinned");
        check(hasEnumConstant(c, "PERM_FAIL_NOT_MOUNTED"), "enum PERM_FAIL_NOT_MOUNTED");
        check(hasEnumConstant(c, "PERM_FAIL_UNSUPPORTED"), "enum PERM_FAIL_UNSUPPORTED");
        check(hasEnumConstant(c, "PERM_FAIL_YIELDING"), "enum PERM_FAIL_YIELDING");
        check(hasEnumConstant(c, "SUCCESS"), "enum SUCCESS");
        check(hasEnumConstant(c, "TRANSIENT_FAIL_PINNED_CRITICAL_SECTION"), "enum TRANSIENT_FAIL_PINNED_CRITICAL_SECTION");
        check(hasEnumConstant(c, "TRANSIENT_FAIL_PINNED_NATIVE"), "enum TRANSIENT_FAIL_PINNED_NATIVE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
