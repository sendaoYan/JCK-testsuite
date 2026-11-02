/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Utility conformance: reflection.
 * @run main UtilityTest
 */

public class UtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Utility", false, UtilityTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Utility"), "simple name Utility");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accessToString"), "method accessToString");
        check(has(c, "classOrInterface"), "method classOrInterface");
        check(has(c, "codeToString"), "method codeToString");
        check(has(c, "compactClassName"), "method compactClassName");
        check(has(c, "setBit"), "method setBit");
        check(has(c, "clearBit"), "method clearBit");
        check(has(c, "isSet"), "method isSet");
        check(has(c, "methodTypeToSignature"), "method methodTypeToSignature");
        check(has(c, "methodSignatureArgumentTypes"), "method methodSignatureArgumentTypes");
        check(has(c, "methodSignatureReturnType"), "method methodSignatureReturnType");
        check(has(c, "methodSignatureToString"), "method methodSignatureToString");
        check(has(c, "replace"), "method replace");
        check(has(c, "signatureToString"), "method signatureToString");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "typeOfMethodSignature"), "method typeOfMethodSignature");
        check(has(c, "typeOfSignature"), "method typeOfSignature");
        check(has(c, "searchOpcode"), "method searchOpcode");
        check(has(c, "toHexString"), "method toHexString");
        check(has(c, "format"), "method format");
        check(has(c, "fillup"), "method fillup");
        check(has(c, "printArray"), "method printArray");
        check(has(c, "isJavaIdentifierPart"), "method isJavaIdentifierPart");
        check(has(c, "encode"), "method encode");
        check(has(c, "decode"), "method decode");
        check(has(c, "convertString"), "method convertString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
