/*
 * @test
 * @summary com.sun.rowset.internal.WebRowSetXmlWriter conformance: hierarchy and declared members via reflection.
 * @run main WebRowSetXmlWriterTest
 */

public class WebRowSetXmlWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.WebRowSetXmlWriter", false, null);
        check(c.getName().equals("com.sun.rowset.internal.WebRowSetXmlWriter"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.spi.XmlWriter"), "implements javax.sql.rowset.spi.XmlWriter");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("beginSection"), "declares beginSection");
        check(ms.contains("beginTag"), "declares beginTag");
        check(ms.contains("emptyTag"), "declares emptyTag");
        check(ms.contains("endHeader"), "declares endHeader");
        check(ms.contains("endSection"), "declares endSection");
        check(ms.contains("endTag"), "declares endTag");
        check(ms.contains("getTag"), "declares getTag");
        check(ms.contains("processSpecialCharacters"), "declares processSpecialCharacters");
        check(ms.contains("propBoolean"), "declares propBoolean");
        check(ms.contains("propInteger"), "declares propInteger");
        check(ms.contains("propString"), "declares propString");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("setTag"), "declares setTag");
        check(ms.contains("startHeader"), "declares startHeader");
        check(ms.contains("writeBigDecimal"), "declares writeBigDecimal");
        check(ms.contains("writeBoolean"), "declares writeBoolean");
        check(ms.contains("writeData"), "declares writeData");
        check(ms.contains("writeDouble"), "declares writeDouble");
        check(ms.contains("writeEmptyString"), "declares writeEmptyString");
        check(ms.contains("writeFloat"), "declares writeFloat");
        check(ms.contains("writeIndent"), "declares writeIndent");
        check(ms.contains("writeInteger"), "declares writeInteger");
        check(ms.contains("writeLong"), "declares writeLong");
        check(ms.contains("writeMetaData"), "declares writeMetaData");
        check(ms.contains("writeNull"), "declares writeNull");
        check(ms.contains("writeProperties"), "declares writeProperties");
        check(ms.contains("writeRowSet"), "declares writeRowSet");
        check(ms.contains("writeShort"), "declares writeShort");
        check(ms.contains("writeString"), "declares writeString");
        check(ms.contains("writeStringData"), "declares writeStringData");
        check(ms.contains("writeValue"), "declares writeValue");
        check(ms.contains("writeXML"), "declares writeXML");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
