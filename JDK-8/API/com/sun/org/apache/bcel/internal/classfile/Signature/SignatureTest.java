/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Signature conformance: reflection.
 * @run main SignatureTest
 */

public class SignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Signature", false, SignatureTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Signature"), "simple name Signature");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Attribute"), "superclass com.sun.org.apache.bcel.internal.classfile.Attribute");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getSignatureIndex"), "method getSignatureIndex");
        check(has(c, "setSignatureIndex"), "method setSignatureIndex");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "translate"), "method translate");
        check(has(c, "isFormalParameterList"), "method isFormalParameterList");
        check(has(c, "isActualParameterList"), "method isActualParameterList");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
