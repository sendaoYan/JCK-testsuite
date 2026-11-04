/*
 * @test
 * @summary com.sun.jndi.ldap.BerEncoder conformance: hierarchy and declared members via reflection.
 * @run main BerEncoderTest
 */

public class BerEncoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.BerEncoder", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.BerEncoder"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.ldap.Ber"), "superclass com.sun.jndi.ldap.Ber");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("beginSeq"), "declares beginSeq");
        check(ms.contains("encodeBoolean"), "declares encodeBoolean");
        check(ms.contains("encodeByte"), "declares encodeByte");
        check(ms.contains("encodeInt"), "declares encodeInt");
        check(ms.contains("encodeLength"), "declares encodeLength");
        check(ms.contains("encodeOctetString"), "declares encodeOctetString");
        check(ms.contains("encodeString"), "declares encodeString");
        check(ms.contains("encodeStringArray"), "declares encodeStringArray");
        check(ms.contains("endSeq"), "declares endSeq");
        check(ms.contains("ensureFreeBytes"), "declares ensureFreeBytes");
        check(ms.contains("getBuf"), "declares getBuf");
        check(ms.contains("getDataLen"), "declares getDataLen");
        check(ms.contains("getTrimmedBuf"), "declares getTrimmedBuf");
        check(ms.contains("reset"), "declares reset");
        check(ms.contains("shiftSeqData"), "declares shiftSeqData");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
