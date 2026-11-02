/*
 * @test
 * @summary jdk.internal.constant.ConstantUtils conformance: internal package reflection.
 * @modules java.base/jdk.internal.constant:+open
 * @run main ConstantUtilsTest
 */
public class ConstantUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.constant.ConstantUtils", false, ConstantUtilsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "arrayDepth"), "method arrayDepth");
        check(hasMethod(c, "binaryNameToDesc"), "method binaryNameToDesc");
        check(hasMethod(c, "binaryToInternal"), "method binaryToInternal");
        check(hasMethod(c, "classDesc"), "method classDesc");
        check(hasMethod(c, "concat"), "method concat");
        check(hasMethod(c, "dropFirstAndLastChar"), "method dropFirstAndLastChar");
        check(hasMethod(c, "forPrimitiveType"), "method forPrimitiveType");
        check(hasMethod(c, "internalNameToDesc"), "method internalNameToDesc");
        check(hasMethod(c, "internalToBinary"), "method internalToBinary");
        check(hasMethod(c, "methodTypeDesc"), "method methodTypeDesc");
        check(hasMethod(c, "parseReferenceTypeDesc"), "method parseReferenceTypeDesc");
        check(hasMethod(c, "referenceClassDesc"), "method referenceClassDesc");
        check(hasMethod(c, "validateArrayRank"), "method validateArrayRank");
        check(hasMethod(c, "validateBinaryClassName"), "method validateBinaryClassName");
        check(hasMethod(c, "validateBinaryPackageName"), "method validateBinaryPackageName");
        check(hasMethod(c, "validateClassOrInterface"), "method validateClassOrInterface");
        check(hasMethod(c, "validateInternalClassName"), "method validateInternalClassName");
        check(hasMethod(c, "validateInternalPackageName"), "method validateInternalPackageName");
        check(hasMethod(c, "validateMemberName"), "method validateMemberName");
        check(hasMethod(c, "validateModuleName"), "method validateModuleName");
        check(hasField(c, "CD_Object_array"), "field CD_Object_array");
        check(hasField(c, "CD_module_info"), "field CD_module_info");
        check(hasField(c, "EMPTY_CLASSDESC"), "field EMPTY_CLASSDESC");
        check(hasField(c, "EMPTY_CONSTANTDESC"), "field EMPTY_CONSTANTDESC");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "JVM_SIGNATURE_ARRAY"), "field JVM_SIGNATURE_ARRAY");
        check(hasField(c, "JVM_SIGNATURE_BOOLEAN"), "field JVM_SIGNATURE_BOOLEAN");
        check(hasField(c, "JVM_SIGNATURE_BYTE"), "field JVM_SIGNATURE_BYTE");
        check(hasField(c, "JVM_SIGNATURE_CHAR"), "field JVM_SIGNATURE_CHAR");
        check(hasField(c, "JVM_SIGNATURE_CLASS"), "field JVM_SIGNATURE_CLASS");
        check(hasField(c, "JVM_SIGNATURE_DOUBLE"), "field JVM_SIGNATURE_DOUBLE");
        check(hasField(c, "JVM_SIGNATURE_FLOAT"), "field JVM_SIGNATURE_FLOAT");
        check(hasField(c, "JVM_SIGNATURE_INT"), "field JVM_SIGNATURE_INT");
        check(hasField(c, "JVM_SIGNATURE_LONG"), "field JVM_SIGNATURE_LONG");
        check(hasField(c, "JVM_SIGNATURE_SHORT"), "field JVM_SIGNATURE_SHORT");
        check(hasField(c, "JVM_SIGNATURE_VOID"), "field JVM_SIGNATURE_VOID");
        check(hasField(c, "MAX_ARRAY_TYPE_DESC_DIMENSIONS"), "field MAX_ARRAY_TYPE_DESC_DIMENSIONS");
        check(hasField(c, "pointyNames"), "field pointyNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
