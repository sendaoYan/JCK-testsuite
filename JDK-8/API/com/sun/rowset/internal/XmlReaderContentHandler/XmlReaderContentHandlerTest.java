/*
 * @test
 * @summary com.sun.rowset.internal.XmlReaderContentHandler conformance: hierarchy and declared members via reflection.
 * @run main XmlReaderContentHandlerTest
 */

public class XmlReaderContentHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.XmlReaderContentHandler", false, null);
        check(c.getName().equals("com.sun.rowset.internal.XmlReaderContentHandler"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("applyUpdates"), "declares applyUpdates");
        check(ms.contains("characters"), "declares characters");
        check(ms.contains("endDocument"), "declares endDocument");
        check(ms.contains("endElement"), "declares endElement");
        check(ms.contains("error"), "declares error");
        check(ms.contains("getBigDecimalValue"), "declares getBigDecimalValue");
        check(ms.contains("getBinaryValue"), "declares getBinaryValue");
        check(ms.contains("getBooleanValue"), "declares getBooleanValue");
        check(ms.contains("getByteValue"), "declares getByteValue");
        check(ms.contains("getDateValue"), "declares getDateValue");
        check(ms.contains("getDoubleValue"), "declares getDoubleValue");
        check(ms.contains("getEmptyStringValue"), "declares getEmptyStringValue");
        check(ms.contains("getFloatValue"), "declares getFloatValue");
        check(ms.contains("getIntegerValue"), "declares getIntegerValue");
        check(ms.contains("getLongValue"), "declares getLongValue");
        check(ms.contains("getNullValue"), "declares getNullValue");
        check(ms.contains("getPresentRow"), "declares getPresentRow");
        check(ms.contains("getShortValue"), "declares getShortValue");
        check(ms.contains("getState"), "declares getState");
        check(ms.contains("getStringValue"), "declares getStringValue");
        check(ms.contains("getTag"), "declares getTag");
        check(ms.contains("getTimeValue"), "declares getTimeValue");
        check(ms.contains("getTimestampValue"), "declares getTimestampValue");
        check(ms.contains("initMaps"), "declares initMaps");
        check(ms.contains("insertValue"), "declares insertValue");
        check(ms.contains("notationDecl"), "declares notationDecl");
        check(ms.contains("setDataValue"), "declares setDataValue");
        check(ms.contains("setEmptyStringValue"), "declares setEmptyStringValue");
        check(ms.contains("setMetaDataValue"), "declares setMetaDataValue");
        check(ms.contains("setNullValue"), "declares setNullValue");
        check(ms.contains("setPropertyValue"), "declares setPropertyValue");
        check(ms.contains("setState"), "declares setState");
        check(ms.contains("setTag"), "declares setTag");
        check(ms.contains("startDocument"), "declares startDocument");
        check(ms.contains("startElement"), "declares startElement");
        check(ms.contains("unparsedEntityDecl"), "declares unparsedEntityDecl");
        check(ms.contains("warning"), "declares warning");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
