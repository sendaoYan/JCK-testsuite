/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.Type conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.Type", false, TypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.Type"), "name");
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
        check(fd.size() == 19, "field-count");
        check(fd.contains("VOID"), "field-VOID");
        check(fd.contains("BOOLEAN"), "field-BOOLEAN");
        check(fd.contains("INT"), "field-INT");
        check(fd.contains("SHORT"), "field-SHORT");
        check(fd.contains("BYTE"), "field-BYTE");
        check(fd.contains("LONG"), "field-LONG");
        check(fd.contains("DOUBLE"), "field-DOUBLE");
        check(fd.contains("FLOAT"), "field-FLOAT");
        check(fd.contains("CHAR"), "field-CHAR");
        check(fd.contains("OBJECT"), "field-OBJECT");
        check(fd.contains("CLASS"), "field-CLASS");
        check(fd.contains("STRING"), "field-STRING");
        check(fd.contains("STRINGBUFFER"), "field-STRINGBUFFER");
        check(fd.contains("THROWABLE"), "field-THROWABLE");
        check(fd.contains("NO_ARGS"), "field-NO_ARGS");
        check(fd.contains("NULL"), "field-NULL");
        check(fd.contains("UNKNOWN"), "field-UNKNOWN");
        check(fd.contains("type"), "field-type");
        check(fd.contains("signature"), "field-signature");
        check(md.size() == 12, "method-count");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getArgumentTypes"), "method-getArgumentTypes");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getMethodSignature"), "method-getMethodSignature");
        check(md.contains("getReturnType"), "method-getReturnType");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("getSize"), "method-getSize");
        check(md.contains("getType"), "method-getType");
        check(md.contains("getTypes"), "method-getTypes");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("normalizeForStackOrLocal"), "method-normalizeForStackOrLocal");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
