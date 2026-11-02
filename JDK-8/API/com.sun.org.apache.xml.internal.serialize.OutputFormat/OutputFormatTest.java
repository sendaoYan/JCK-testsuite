/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.OutputFormat conformance: class hierarchy and members.
 * @run main OutputFormatTest
 */
public class OutputFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.OutputFormat", false, OutputFormatTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("setMethod"), "method setMethod");
        check(mn.contains("getVersion"), "method getVersion");
        check(mn.contains("setVersion"), "method setVersion");
        check(mn.contains("getIndent"), "method getIndent");
        check(mn.contains("getIndenting"), "method getIndenting");
        check(mn.contains("setIndent"), "method setIndent");
        check(mn.contains("setIndenting"), "method setIndenting");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("setEncoding"), "method setEncoding");
        check(mn.contains("getEncodingInfo"), "method getEncodingInfo");
        check(mn.contains("setAllowJavaNames"), "method setAllowJavaNames");
        check(mn.contains("getMediaType"), "method getMediaType");
        check(mn.contains("setMediaType"), "method setMediaType");
        check(mn.contains("setDoctype"), "method setDoctype");
        check(mn.contains("getDoctypePublic"), "method getDoctypePublic");
        check(mn.contains("getDoctypeSystem"), "method getDoctypeSystem");
        check(mn.contains("getOmitComments"), "method getOmitComments");
        check(mn.contains("setOmitComments"), "method setOmitComments");
        check(mn.contains("getOmitDocumentType"), "method getOmitDocumentType");
        check(mn.contains("setOmitDocumentType"), "method setOmitDocumentType");
        check(mn.contains("getOmitXMLDeclaration"), "method getOmitXMLDeclaration");
        check(mn.contains("setOmitXMLDeclaration"), "method setOmitXMLDeclaration");
        check(mn.contains("getStandalone"), "method getStandalone");
        check(mn.contains("setStandalone"), "method setStandalone");
        check(mn.contains("getCDataElements"), "method getCDataElements");
        check(mn.contains("isCDataElement"), "method isCDataElement");
        check(mn.contains("setCDataElements"), "method setCDataElements");
        check(mn.contains("getNonEscapingElements"), "method getNonEscapingElements");
        check(mn.contains("isNonEscapingElement"), "method isNonEscapingElement");
        check(mn.contains("setNonEscapingElements"), "method setNonEscapingElements");
        check(mn.contains("getLineSeparator"), "method getLineSeparator");
        check(mn.contains("setLineSeparator"), "method setLineSeparator");
        check(mn.contains("getPreserveSpace"), "method getPreserveSpace");
        check(mn.contains("setPreserveSpace"), "method setPreserveSpace");
        check(mn.contains("getLineWidth"), "method getLineWidth");
        check(mn.contains("setLineWidth"), "method setLineWidth");
        check(mn.contains("getPreserveEmptyAttributes"), "method getPreserveEmptyAttributes");
        check(mn.contains("setPreserveEmptyAttributes"), "method setPreserveEmptyAttributes");
        check(mn.contains("getLastPrintable"), "method getLastPrintable");
        check(mn.contains("whichMethod"), "method whichMethod");
        check(mn.contains("whichDoctypePublic"), "method whichDoctypePublic");
        check(mn.contains("whichDoctypeSystem"), "method whichDoctypeSystem");
        check(mn.contains("whichMediaType"), "method whichMediaType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
