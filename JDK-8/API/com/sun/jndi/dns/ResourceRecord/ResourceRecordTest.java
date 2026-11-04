/*
 * @test
 * @summary com.sun.jndi.dns.ResourceRecord conformance: hierarchy and declared members via reflection.
 * @run main ResourceRecordTest
 */

public class ResourceRecordTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.ResourceRecord", false, null);
        check(c.getName().equals("com.sun.jndi.dns.ResourceRecord"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("compareSerialNumbers"), "declares compareSerialNumbers");
        check(ms.contains("decode"), "declares decode");
        check(ms.contains("decodeA"), "declares decodeA");
        check(ms.contains("decodeAAAA"), "declares decodeAAAA");
        check(ms.contains("decodeCharString"), "declares decodeCharString");
        check(ms.contains("decodeHinfo"), "declares decodeHinfo");
        check(ms.contains("decodeMx"), "declares decodeMx");
        check(ms.contains("decodeName"), "declares decodeName");
        check(ms.contains("decodeNaptr"), "declares decodeNaptr");
        check(ms.contains("decodeRdata"), "declares decodeRdata");
        check(ms.contains("decodeSoa"), "declares decodeSoa");
        check(ms.contains("decodeSrv"), "declares decodeSrv");
        check(ms.contains("decodeTxt"), "declares decodeTxt");
        check(ms.contains("dprint"), "declares dprint");
        check(ms.contains("getInt"), "declares getInt");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getRdata"), "declares getRdata");
        check(ms.contains("getRrclass"), "declares getRrclass");
        check(ms.contains("getRrclassName"), "declares getRrclassName");
        check(ms.contains("getType"), "declares getType");
        check(ms.contains("getTypeName"), "declares getTypeName");
        check(ms.contains("getUByte"), "declares getUByte");
        check(ms.contains("getUInt"), "declares getUInt");
        check(ms.contains("getUShort"), "declares getUShort");
        check(ms.contains("nameToValue"), "declares nameToValue");
        check(ms.contains("size"), "declares size");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("valueToName"), "declares valueToName");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
