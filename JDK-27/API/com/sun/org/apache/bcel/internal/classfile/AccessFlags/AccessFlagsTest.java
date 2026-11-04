/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.AccessFlags conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main AccessFlagsTest
 */
public class AccessFlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.AccessFlags", false, AccessFlagsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "name");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("access_flags"), "field-access_flags");
        check(md.size() == 20, "method-count");
        check(md.contains("getAccessFlags"), "method-getAccessFlags");
        check(md.contains("getModifiers"), "method-getModifiers");
        check(md.contains("isAbstract"), "method-isAbstract");
        check(md.contains("isAnnotation"), "method-isAnnotation");
        check(md.contains("isEnum"), "method-isEnum");
        check(md.contains("isFinal"), "method-isFinal");
        check(md.contains("isInterface"), "method-isInterface");
        check(md.contains("isNative"), "method-isNative");
        check(md.contains("isPrivate"), "method-isPrivate");
        check(md.contains("isProtected"), "method-isProtected");
        check(md.contains("isPublic"), "method-isPublic");
        check(md.contains("isStatic"), "method-isStatic");
        check(md.contains("isStrictfp"), "method-isStrictfp");
        check(md.contains("isSynchronized"), "method-isSynchronized");
        check(md.contains("isSynthetic"), "method-isSynthetic");
        check(md.contains("isTransient"), "method-isTransient");
        check(md.contains("isVarArgs"), "method-isVarArgs");
        check(md.contains("isVolatile"), "method-isVolatile");
        check(md.contains("setAccessFlags"), "method-setAccessFlags");
        check(md.contains("setModifiers"), "method-setModifiers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
