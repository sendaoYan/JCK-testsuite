/*
 * @test
 * @summary org.w3c.dom.TypeInfo conformance: reflection shape checks.
 * @run main TypeInfoTest
 */

import java.lang.reflect.*;

public class TypeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.TypeInfo");
        check(c != null, "org.w3c.dom.TypeInfo loads");
        check(c.getName().equals("org.w3c.dom.TypeInfo"), "binary name is org.w3c.dom.TypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.TypeInfo is public");
        check(c.isInterface(), "org.w3c.dom.TypeInfo is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getTypeName"), "declares getTypeName");
        check(declares(c, "getTypeNamespace"), "declares getTypeNamespace");
        check(declares(c, "isDerivedFrom"), "declares isDerivedFrom");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral TypeInfo contract (DTD-backed + defaults) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        // plain document: TypeInfo object exists but carries no DTD/schema knowledge
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element plain = d.createElement("e");
        org.w3c.dom.TypeInfo empty = plain.getSchemaTypeInfo();
        check(empty != null, "getSchemaTypeInfo non-null even without DTD");
        check(empty.getTypeName() == null, "no DTD: element typeName null");
        check(empty.getTypeNamespace() == null, "no DTD: element typeNamespace null");
        org.w3c.dom.Attr pa = d.createAttribute("a");
        pa.setValue("1");
        check(pa.getSchemaTypeInfo() != null && pa.getSchemaTypeInfo().getTypeName() == null, "no DTD: attr typeName null");
        // DTD-declared ID attribute yields a concrete type
        String xml = "<!DOCTYPE r [<!ATTLIST item id ID #IMPLIED>]><r><item id='x'/></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element item = (org.w3c.dom.Element) pd.getElementsByTagName("item").item(0);
        org.w3c.dom.TypeInfo idInfo = item.getAttributeNode("id").getSchemaTypeInfo();
        check("ID".equals(idInfo.getTypeName()), "DTD ID attr typeName 'ID'");
        check("http://www.w3.org/TR/REC-xml".equals(idInfo.getTypeNamespace()), "DTD type namespace is the REC-xml URI");
        // element type info stays unfilled by the Xerces DTD validator
        check(item.getSchemaTypeInfo().getTypeName() == null, "Xerces DTD does not populate element typeName");
        // isDerivedFrom: false against empty/base types; constants per spec
        check(!idInfo.isDerivedFrom("urn:any", "any", org.w3c.dom.TypeInfo.DERIVATION_EXTENSION), "isDerivedFrom false for unrelated types");
        check(org.w3c.dom.TypeInfo.DERIVATION_RESTRICTION == 1 && org.w3c.dom.TypeInfo.DERIVATION_EXTENSION == 2, "DERIVATION_RESTRICTION=1, DERIVATION_EXTENSION=2");
        check(org.w3c.dom.TypeInfo.DERIVATION_UNION == 4 && org.w3c.dom.TypeInfo.DERIVATION_LIST == 8, "DERIVATION_UNION=4, DERIVATION_LIST=8");
        check(!idInfo.isDerivedFrom(null, null, 0), "isDerivedFrom(null,null,0) false");
        // non-ID attribute also typed by DTD (CDATA) is not reported: Xerces reports only ID/ENTITY/ENUMERATION
        String xml2 = "<!DOCTYPE r [<!ATTLIST item cd CDATA 'x'>]><r><item/></r>";
        org.w3c.dom.Document pd2 = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml2.getBytes("UTF-8")));
        org.w3c.dom.Element item2 = (org.w3c.dom.Element) pd2.getElementsByTagName("item").item(0);
        org.w3c.dom.Attr cd = item2.getAttributeNode("cd");
        check(cd != null && !cd.isId(), "CDATA default attr injected, not an ID");
    }
}
