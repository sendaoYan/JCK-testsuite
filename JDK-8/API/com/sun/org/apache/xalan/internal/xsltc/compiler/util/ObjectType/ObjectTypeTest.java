/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType conformance: reflection.
 * @run main ObjectTypeTest
 */

public class ObjectTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ObjectType", false, ObjectTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ObjectType"), "simple name ObjectType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "equals"), "method equals");
        check(has(c, "getJavaClassName"), "method getJavaClassName");
        check(has(c, "getJavaClass"), "method getJavaClass");
        check(has(c, "toString"), "method toString");
        check(has(c, "identicalTo"), "method identicalTo");
        check(has(c, "toSignature"), "method toSignature");
        check(has(c, "toJCType"), "method toJCType");
        check(has(c, "translateTo"), "method translateTo");
        check(has(c, "translateFrom"), "method translateFrom");
        check(has(c, "LOAD"), "method LOAD");
        check(has(c, "STORE"), "method STORE");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
