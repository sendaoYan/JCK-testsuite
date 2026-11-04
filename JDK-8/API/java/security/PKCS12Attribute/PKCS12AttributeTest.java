/*
 * @test
 * @summary java.security.PKCS12Attribute conformance: constructors and getters.
 * @run main PKCS12AttributeTest
 */

public class PKCS12AttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void ctMsg(Class<? extends Throwable> expected, String frag, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(frag)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but nothing was thrown");
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.PKCS12Attribute.class;

        // ---- structure: final class, KeyStore.Entry.Attribute, exactly two public ctors ----
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers())
                && java.lang.reflect.Modifier.isPublic(c.getModifiers()), "final public class");
        check(java.security.KeyStore.Entry.Attribute.class.isAssignableFrom(c),
                "implements KeyStore.Entry.Attribute");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "exactly 2 constructors: (String,String) and (byte[]), no no-arg ctor");

        // ---- constructor exception matrix ----
        ct(NullPointerException.class, () -> new java.security.PKCS12Attribute(null, "v"), "ctor(null name) -> NPE");
        ct(NullPointerException.class, () -> new java.security.PKCS12Attribute("1.2.3", (String) null), "ctor(null value) -> NPE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: name",
                () -> new java.security.PKCS12Attribute("not-an-oid", "v"), "ctor(non-OID name) -> IAE");
        ct(IllegalArgumentException.class, () -> new java.security.PKCS12Attribute("1.2.x", "v"),
                "ctor(malformed OID) -> IAE");
        ct(NullPointerException.class, () -> new java.security.PKCS12Attribute((byte[]) null),
                "byte[] ctor(null) -> NPE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: encoded",
                () -> new java.security.PKCS12Attribute(new byte[]{1}), "byte[] ctor(garbage) -> IAE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: encoded",
                () -> new java.security.PKCS12Attribute(new byte[0]), "byte[] ctor(empty) -> IAE");

        // ---- DER encoding contract ----
        java.security.PKCS12Attribute a = new java.security.PKCS12Attribute("1.2.3.4", "value");
        check("300E06032A030431070C0576616C7565".equals(hex(a.getEncoded())),
                "DER: SEQUENCE(0x30){ OID(06) 1.2.3.4, SET(31){ UTF8String(0C) \"value\" } } exactly");
        check(a.getEncoded().length == 16, "encoded length 16");
        byte[] e1 = a.getEncoded();
        check(a.getEncoded() != e1, "getEncoded returns a fresh copy on every call");
        e1[e1.length - 1] = 0x00;
        check(a.getEncoded()[15] == (byte)0x65, "mutating a returned copy does not corrupt the attribute");
        byte[] input = a.getEncoded().clone();
        java.security.PKCS12Attribute b = new java.security.PKCS12Attribute(input);
        input[0] = 0x31;
        check(hex(b.getEncoded()).equals("300E06032A030431070C0576616C7565"),
                "byte[] ctor defensively copies its input");

        // ---- byte[] ctor round-trip ----
        check("1.2.3.4".equals(b.getName()) && "value".equals(b.getValue()), "byte[] ctor round-trips name/value");
        check(b.equals(a) && a.equals(b), "byte[] ctor round-trip equals (symmetric)");
        java.security.PKCS12Attribute uni = new java.security.PKCS12Attribute("1.2.3.4", "välue-π");
        java.security.PKCS12Attribute uni2 = new java.security.PKCS12Attribute(uni.getEncoded());
        check(uni2.getValue().equals("välue-π"), "unicode value survives UTF-8 DER round-trip");

        // ---- equals / hashCode are value-based ----
        java.security.PKCS12Attribute same = new java.security.PKCS12Attribute("1.2.3.4", "value");
        check(a.equals(a) && a.equals(same) && same.equals(a), "equals reflexive and symmetric on same values");
        check(!a.equals(new java.security.PKCS12Attribute("1.2.3.4", "other")), "different value not equals");
        check(!a.equals(new java.security.PKCS12Attribute("1.2.3.5", "value")), "different name not equals");
        check(!a.equals(null) && !a.equals("1.2.3.4=value"), "equals null and non-attribute false");
        check(a.hashCode() == same.hashCode() && a.hashCode() == b.hashCode(),
                "hashCode consistent across equal instances");

        // ---- toString ----
        check("1.2.3.4=value".equals(a.toString()), "toString is exactly \"name=value\"");
    }

    public static void main(String[] args) throws Exception {
        java.security.PKCS12Attribute a = new java.security.PKCS12Attribute("1.2.3.4", "value");
        check("1.2.3.4".equals(a.getName()), "getName");
        check("value".equals(a.getValue()), "getValue");
        check(a.getEncoded() != null, "getEncoded");
        byte[] der = a.getEncoded();
        java.security.PKCS12Attribute b = new java.security.PKCS12Attribute(der);
        check("1.2.3.4".equals(b.getName()), "byte[] ctor name");

        // ---- contract deepening: ctor exception matrix + DER bytes + defensive copies + equals/hashCode ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
