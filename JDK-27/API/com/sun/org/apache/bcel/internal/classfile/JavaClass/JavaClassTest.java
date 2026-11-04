/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.JavaClass conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main JavaClassTest
 */
public class JavaClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.JavaClass", false, JavaClassTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.JavaClass"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
        check(ifs.contains("com.sun.org.apache.bcel.internal.classfile.Node"), "if-com.sun.org.apache.bcel.internal.classfile.Node");
        check(ifs.contains("java.lang.Comparable"), "if-java.lang.Comparable");
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
        check(fd.size() == 5, "field-count");
        check(fd.contains("EXTENSION"), "field-EXTENSION");
        check(fd.contains("EMPTY_ARRAY"), "field-EMPTY_ARRAY");
        check(fd.contains("HEAP"), "field-HEAP");
        check(fd.contains("FILE"), "field-FILE");
        check(fd.contains("ZIP"), "field-ZIP");
        check(md.size() == 58, "method-count");
        check(md.contains("accept"), "method-accept");
        check(md.contains("compareTo"), "method-compareTo");
        check(md.contains("copy"), "method-copy");
        check(md.contains("dump"), "method-dump");
        check(md.contains("equals"), "method-equals");
        check(md.contains("findField"), "method-findField");
        check(md.contains("getAllInterfaces"), "method-getAllInterfaces");
        check(md.contains("getAnnotationEntries"), "method-getAnnotationEntries");
        check(md.contains("getAttribute"), "method-getAttribute");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getBytes"), "method-getBytes");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getClassNameIndex"), "method-getClassNameIndex");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getFields"), "method-getFields");
        check(md.contains("getFileName"), "method-getFileName");
        check(md.contains("getInterfaceIndices"), "method-getInterfaceIndices");
        check(md.contains("getInterfaceNames"), "method-getInterfaceNames");
        check(md.contains("getInterfaces"), "method-getInterfaces");
        check(md.contains("getMajor"), "method-getMajor");
        check(md.contains("getMethod"), "method-getMethod");
        check(md.contains("getMethods"), "method-getMethods");
        check(md.contains("getMinor"), "method-getMinor");
        check(md.contains("getPackageName"), "method-getPackageName");
        check(md.contains("getRepository"), "method-getRepository");
        check(md.contains("getSource"), "method-getSource");
        check(md.contains("getSourceFileName"), "method-getSourceFileName");
        check(md.contains("getSourceFilePath"), "method-getSourceFilePath");
        check(md.contains("getSuperClass"), "method-getSuperClass");
        check(md.contains("getSuperClasses"), "method-getSuperClasses");
        check(md.contains("getSuperclassName"), "method-getSuperclassName");
        check(md.contains("getSuperclassNameIndex"), "method-getSuperclassNameIndex");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("implementationOf"), "method-implementationOf");
        check(md.contains("instanceOf"), "method-instanceOf");
        check(md.contains("isAnonymous"), "method-isAnonymous");
        check(md.contains("isClass"), "method-isClass");
        check(md.contains("isNested"), "method-isNested");
        check(md.contains("isRecord"), "method-isRecord");
        check(md.contains("isSuper"), "method-isSuper");
        check(md.contains("setAttributes"), "method-setAttributes");
        check(md.contains("setClassName"), "method-setClassName");
        check(md.contains("setClassNameIndex"), "method-setClassNameIndex");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        check(md.contains("setFields"), "method-setFields");
        check(md.contains("setFileName"), "method-setFileName");
        check(md.contains("setInterfaceNames"), "method-setInterfaceNames");
        check(md.contains("setInterfaces"), "method-setInterfaces");
        check(md.contains("setMajor"), "method-setMajor");
        check(md.contains("setMethods"), "method-setMethods");
        check(md.contains("setMinor"), "method-setMinor");
        check(md.contains("setRepository"), "method-setRepository");
        check(md.contains("setSourceFileName"), "method-setSourceFileName");
        check(md.contains("setSuperclassName"), "method-setSuperclassName");
        check(md.contains("setSuperclassNameIndex"), "method-setSuperclassNameIndex");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
