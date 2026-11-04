/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main TransformerFactoryImplTest
 */
public class TransformerFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", false, TransformerFactoryImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.sax.SAXTransformerFactory"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader"), "if-com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader");
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
        check(fd.size() == 11, "field-count");
        check(fd.contains("TRANSLET_NAME"), "field-TRANSLET_NAME");
        check(fd.contains("DESTINATION_DIRECTORY"), "field-DESTINATION_DIRECTORY");
        check(fd.contains("PACKAGE_NAME"), "field-PACKAGE_NAME");
        check(fd.contains("JAR_NAME"), "field-JAR_NAME");
        check(fd.contains("GENERATE_TRANSLET"), "field-GENERATE_TRANSLET");
        check(fd.contains("AUTO_TRANSLET"), "field-AUTO_TRANSLET");
        check(fd.contains("USE_CLASSPATH"), "field-USE_CLASSPATH");
        check(fd.contains("DEBUG"), "field-DEBUG");
        check(fd.contains("ENABLE_INLINING"), "field-ENABLE_INLINING");
        check(fd.contains("INDENT_NUMBER"), "field-INDENT_NUMBER");
        check(fd.contains("DEFAULT_TRANSLET_NAME"), "field-DEFAULT_TRANSLET_NAME");
        check(md.size() == 19, "method-count");
        check(md.contains("createNewDTMManagerInstance"), "method-createNewDTMManagerInstance");
        check(md.contains("getAssociatedStylesheet"), "method-getAssociatedStylesheet");
        check(md.contains("getAttribute"), "method-getAttribute");
        check(md.contains("getErrorListener"), "method-getErrorListener");
        check(md.contains("getExternalExtensionsMap"), "method-getExternalExtensionsMap");
        check(md.contains("getFeature"), "method-getFeature");
        check(md.contains("getJdkXmlFeatures"), "method-getJdkXmlFeatures");
        check(md.contains("getURIResolver"), "method-getURIResolver");
        check(md.contains("loadSource"), "method-loadSource");
        check(md.contains("newTemplates"), "method-newTemplates");
        check(md.contains("newTemplatesHandler"), "method-newTemplatesHandler");
        check(md.contains("newTransformer"), "method-newTransformer");
        check(md.contains("newTransformerHandler"), "method-newTransformerHandler");
        check(md.contains("newXMLFilter"), "method-newXMLFilter");
        check(md.contains("overrideDefaultParser"), "method-overrideDefaultParser");
        check(md.contains("setAttribute"), "method-setAttribute");
        check(md.contains("setErrorListener"), "method-setErrorListener");
        check(md.contains("setFeature"), "method-setFeature");
        check(md.contains("setURIResolver"), "method-setURIResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
