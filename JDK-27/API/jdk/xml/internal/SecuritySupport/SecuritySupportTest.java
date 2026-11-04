/*
 * @test
 * @summary jdk.xml.internal.SecuritySupport conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main SecuritySupportTest
 */
public class SecuritySupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.SecuritySupport", false, SecuritySupportTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.SecuritySupport"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
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
        check(fd.contains("NEWLINE"), "field-NEWLINE");
        check(md.size() == 19, "method-count");
        check(md.contains("checkAccess"), "method-checkAccess");
        check(md.contains("doesFileExist"), "method-doesFileExist");
        check(md.contains("getClassLoader"), "method-getClassLoader");
        check(md.contains("getClassSource"), "method-getClassSource");
        check(md.contains("getContextClassLoader"), "method-getContextClassLoader");
        check(md.contains("getErrorMessage"), "method-getErrorMessage");
        check(md.contains("getFileInputStream"), "method-getFileInputStream");
        check(md.contains("getInputStream"), "method-getInputStream");
        check(md.contains("getJAXPSystemProperty"), "method-getJAXPSystemProperty");
        check(md.contains("getParentClassLoader"), "method-getParentClassLoader");
        check(md.contains("getResource"), "method-getResource");
        check(md.contains("getResourceAsStream"), "method-getResourceAsStream");
        check(md.contains("getResourceBundle"), "method-getResourceBundle");
        check(md.contains("getSystemClassLoader"), "method-getSystemClassLoader");
        check(md.contains("isDirectory"), "method-isDirectory");
        check(md.contains("isFile"), "method-isFile");
        check(md.contains("isFileExists"), "method-isFileExists");
        check(md.contains("readConfig"), "method-readConfig");
        check(md.contains("sanitizePath"), "method-sanitizePath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
