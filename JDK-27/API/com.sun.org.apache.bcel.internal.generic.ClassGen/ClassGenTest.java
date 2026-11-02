/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ClassGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main ClassGenTest
 */
public class ClassGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ClassGen", false, ClassGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.ClassGen"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
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
        check(md.size() == 47, "method-count");
        check(md.contains("addAnnotationEntry"), "method-addAnnotationEntry");
        check(md.contains("addAttribute"), "method-addAttribute");
        check(md.contains("addEmptyConstructor"), "method-addEmptyConstructor");
        check(md.contains("addField"), "method-addField");
        check(md.contains("addInterface"), "method-addInterface");
        check(md.contains("addMethod"), "method-addMethod");
        check(md.contains("addObserver"), "method-addObserver");
        check(md.contains("clone"), "method-clone");
        check(md.contains("containsField"), "method-containsField");
        check(md.contains("containsMethod"), "method-containsMethod");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getAnnotationEntries"), "method-getAnnotationEntries");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getClassNameIndex"), "method-getClassNameIndex");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("getFields"), "method-getFields");
        check(md.contains("getFileName"), "method-getFileName");
        check(md.contains("getInterfaceNames"), "method-getInterfaceNames");
        check(md.contains("getInterfaces"), "method-getInterfaces");
        check(md.contains("getJavaClass"), "method-getJavaClass");
        check(md.contains("getMajor"), "method-getMajor");
        check(md.contains("getMethodAt"), "method-getMethodAt");
        check(md.contains("getMethods"), "method-getMethods");
        check(md.contains("getMinor"), "method-getMinor");
        check(md.contains("getSuperclassName"), "method-getSuperclassName");
        check(md.contains("getSuperclassNameIndex"), "method-getSuperclassNameIndex");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("removeAttribute"), "method-removeAttribute");
        check(md.contains("removeField"), "method-removeField");
        check(md.contains("removeInterface"), "method-removeInterface");
        check(md.contains("removeMethod"), "method-removeMethod");
        check(md.contains("removeObserver"), "method-removeObserver");
        check(md.contains("replaceField"), "method-replaceField");
        check(md.contains("replaceMethod"), "method-replaceMethod");
        check(md.contains("setClassName"), "method-setClassName");
        check(md.contains("setClassNameIndex"), "method-setClassNameIndex");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        check(md.contains("setMajor"), "method-setMajor");
        check(md.contains("setMethodAt"), "method-setMethodAt");
        check(md.contains("setMethods"), "method-setMethods");
        check(md.contains("setMinor"), "method-setMinor");
        check(md.contains("setSuperclassName"), "method-setSuperclassName");
        check(md.contains("setSuperclassNameIndex"), "method-setSuperclassNameIndex");
        check(md.contains("update"), "method-update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
