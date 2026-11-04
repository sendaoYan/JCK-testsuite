/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLEntityStorage conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream:+open
 * @run main XMLEntityStorageTest
 */
public class XMLEntityStorageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLEntityStorage", false, XMLEntityStorageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLEntityStorage"), "name");
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
        check(fd.size() == 8, "field-count");
        check(fd.contains("ERROR_REPORTER"), "field-ERROR_REPORTER");
        check(fd.contains("WARN_ON_DUPLICATE_ENTITYDEF"), "field-WARN_ON_DUPLICATE_ENTITYDEF");
        check(fd.contains("fWarnDuplicateEntityDef"), "field-fWarnDuplicateEntityDef");
        check(fd.contains("fEntities"), "field-fEntities");
        check(fd.contains("fCurrentEntity"), "field-fCurrentEntity");
        check(fd.contains("fErrorReporter"), "field-fErrorReporter");
        check(fd.contains("fPropertyManager"), "field-fPropertyManager");
        check(fd.contains("fInExternalSubset"), "field-fInExternalSubset");
        check(md.size() == 14, "method-count");
        check(md.contains("addExternalEntity"), "method-addExternalEntity");
        check(md.contains("addInternalEntity"), "method-addInternalEntity");
        check(md.contains("addUnparsedEntity"), "method-addUnparsedEntity");
        check(md.contains("endExternalSubset"), "method-endExternalSubset");
        check(md.contains("expandSystemId"), "method-expandSystemId");
        check(md.contains("fixURI"), "method-fixURI");
        check(md.contains("getEntities"), "method-getEntities");
        check(md.contains("getEntity"), "method-getEntity");
        check(md.contains("isDeclaredEntity"), "method-isDeclaredEntity");
        check(md.contains("isEntityDeclInExternalSubset"), "method-isEntityDeclInExternalSubset");
        check(md.contains("isExternalEntity"), "method-isExternalEntity");
        check(md.contains("isUnparsedEntity"), "method-isUnparsedEntity");
        check(md.contains("reset"), "method-reset");
        check(md.contains("startExternalSubset"), "method-startExternalSubset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
