/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Utility conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main UtilityTest
 */
public class UtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Utility", false, UtilityTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.Utility"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 28, "method-count");
        check(md.contains("accessToString"), "method-accessToString");
        check(md.contains("classOrInterface"), "method-classOrInterface");
        check(md.contains("clearBit"), "method-clearBit");
        check(md.contains("codeToString"), "method-codeToString");
        check(md.contains("compactClassName"), "method-compactClassName");
        check(md.contains("convertString"), "method-convertString");
        check(md.contains("decode"), "method-decode");
        check(md.contains("encode"), "method-encode");
        check(md.contains("fillup"), "method-fillup");
        check(md.contains("format"), "method-format");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("isJavaIdentifierPart"), "method-isJavaIdentifierPart");
        check(md.contains("isSet"), "method-isSet");
        check(md.contains("methodSignatureArgumentTypes"), "method-methodSignatureArgumentTypes");
        check(md.contains("methodSignatureReturnType"), "method-methodSignatureReturnType");
        check(md.contains("methodSignatureToString"), "method-methodSignatureToString");
        check(md.contains("methodTypeToSignature"), "method-methodTypeToSignature");
        check(md.contains("packageToPath"), "method-packageToPath");
        check(md.contains("pathToPackage"), "method-pathToPackage");
        check(md.contains("printArray"), "method-printArray");
        check(md.contains("replace"), "method-replace");
        check(md.contains("searchOpcode"), "method-searchOpcode");
        check(md.contains("setBit"), "method-setBit");
        check(md.contains("signatureToString"), "method-signatureToString");
        check(md.contains("toHexString"), "method-toHexString");
        check(md.contains("typeOfMethodSignature"), "method-typeOfMethodSignature");
        check(md.contains("typeOfSignature"), "method-typeOfSignature");
        check(md.contains("typeSignatureToString"), "method-typeSignatureToString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
