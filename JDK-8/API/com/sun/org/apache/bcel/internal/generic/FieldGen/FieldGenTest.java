/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldGen conformance: reflection.
 * @run main FieldGenTest
 */

public class FieldGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldGen", false, FieldGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FieldGen"), "simple name FieldGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen"), "superclass com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setInitValue"), "method setInitValue");
        check(has(c, "cancelInitValue"), "method cancelInitValue");
        check(has(c, "getField"), "method getField");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "addObserver"), "method addObserver");
        check(has(c, "removeObserver"), "method removeObserver");
        check(has(c, "update"), "method update");
        check(has(c, "getInitValue"), "method getInitValue");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
