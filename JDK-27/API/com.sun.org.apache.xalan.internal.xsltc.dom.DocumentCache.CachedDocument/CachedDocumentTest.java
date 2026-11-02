/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.DocumentCache.CachedDocument conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main CachedDocumentTest
 */
public class CachedDocumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.DocumentCache$CachedDocument", false, CachedDocumentTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.DocumentCache$CachedDocument"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(md.size() == 12, "method-count");
        check(md.contains("getAccessCount"), "method-getAccessCount");
        check(md.contains("getDocument"), "method-getDocument");
        check(md.contains("getEstimatedSize"), "method-getEstimatedSize");
        check(md.contains("getFirstReferenced"), "method-getFirstReferenced");
        check(md.contains("getLastChecked"), "method-getLastChecked");
        check(md.contains("getLastModified"), "method-getLastModified");
        check(md.contains("getLastReferenced"), "method-getLastReferenced");
        check(md.contains("getLatency"), "method-getLatency");
        check(md.contains("incAccessCount"), "method-incAccessCount");
        check(md.contains("loadDocument"), "method-loadDocument");
        check(md.contains("setLastChecked"), "method-setLastChecked");
        check(md.contains("setLastModified"), "method-setLastModified");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
