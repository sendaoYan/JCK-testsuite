/*
 * @test
 * @summary com.sun.jndi.ldap.BerDecoder conformance: hierarchy and declared members via reflection.
 * @run main BerDecoderTest
 */

public class BerDecoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.BerDecoder", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.BerDecoder"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.ldap.Ber"), "superclass com.sun.jndi.ldap.Ber");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("bytesLeft"), "declares bytesLeft");
        check(ms.contains("getParsePosition"), "declares getParsePosition");
        check(ms.contains("parseBoolean"), "declares parseBoolean");
        check(ms.contains("parseByte"), "declares parseByte");
        check(ms.contains("parseEnumeration"), "declares parseEnumeration");
        check(ms.contains("parseInt"), "declares parseInt");
        check(ms.contains("parseIntWithTag"), "declares parseIntWithTag");
        check(ms.contains("parseLength"), "declares parseLength");
        check(ms.contains("parseOctetString"), "declares parseOctetString");
        check(ms.contains("parseSeq"), "declares parseSeq");
        check(ms.contains("parseString"), "declares parseString");
        check(ms.contains("parseStringWithTag"), "declares parseStringWithTag");
        check(ms.contains("peekByte"), "declares peekByte");
        check(ms.contains("reset"), "declares reset");
        check(ms.contains("seek"), "declares seek");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
