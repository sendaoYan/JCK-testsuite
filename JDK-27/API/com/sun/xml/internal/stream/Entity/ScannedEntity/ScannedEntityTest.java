/*
 * @test
 * @summary com.sun.xml.internal.stream.Entity.ScannedEntity conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream:+open
 * @run main ScannedEntityTest
 */
public class ScannedEntityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.Entity$ScannedEntity", false, ScannedEntityTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.Entity$ScannedEntity"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.xml.internal.stream.Entity"), "superclass");
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
        check(fd.size() == 24, "field-count");
        check(fd.contains("DEFAULT_BUFFER_SIZE"), "field-DEFAULT_BUFFER_SIZE");
        check(fd.contains("fBufferSize"), "field-fBufferSize");
        check(fd.contains("DEFAULT_XMLDECL_BUFFER_SIZE"), "field-DEFAULT_XMLDECL_BUFFER_SIZE");
        check(fd.contains("DEFAULT_INTERNAL_BUFFER_SIZE"), "field-DEFAULT_INTERNAL_BUFFER_SIZE");
        check(fd.contains("stream"), "field-stream");
        check(fd.contains("reader"), "field-reader");
        check(fd.contains("entityLocation"), "field-entityLocation");
        check(fd.contains("encoding"), "field-encoding");
        check(fd.contains("literal"), "field-literal");
        check(fd.contains("isExternal"), "field-isExternal");
        check(fd.contains("version"), "field-version");
        check(fd.contains("ch"), "field-ch");
        check(fd.contains("position"), "field-position");
        check(fd.contains("count"), "field-count");
        check(fd.contains("lineNumber"), "field-lineNumber");
        check(fd.contains("columnNumber"), "field-columnNumber");
        check(fd.contains("xmlVersion"), "field-xmlVersion");
        check(fd.contains("fTotalCountTillLastLoad"), "field-fTotalCountTillLastLoad");
        check(fd.contains("fLastCount"), "field-fLastCount");
        check(fd.contains("baseCharOffset"), "field-baseCharOffset");
        check(fd.contains("startPosition"), "field-startPosition");
        check(fd.contains("mayReadChunks"), "field-mayReadChunks");
        check(fd.contains("xmlDeclChunkRead"), "field-xmlDeclChunkRead");
        check(fd.contains("isGE"), "field-isGE");
        check(md.size() == 13, "method-count");
        check(md.contains("close"), "method-close");
        check(md.contains("getEncodingName"), "method-getEncodingName");
        check(md.contains("getEntityInputStream"), "method-getEntityInputStream");
        check(md.contains("getEntityReader"), "method-getEntityReader");
        check(md.contains("getEntityVersion"), "method-getEntityVersion");
        check(md.contains("isDeclaredEncoding"), "method-isDeclaredEncoding");
        check(md.contains("isEncodingExternallySpecified"), "method-isEncodingExternallySpecified");
        check(md.contains("isExternal"), "method-isExternal");
        check(md.contains("isUnparsed"), "method-isUnparsed");
        check(md.contains("setDeclaredEncoding"), "method-setDeclaredEncoding");
        check(md.contains("setEncodingExternallySpecified"), "method-setEncodingExternallySpecified");
        check(md.contains("setEntityVersion"), "method-setEntityVersion");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
