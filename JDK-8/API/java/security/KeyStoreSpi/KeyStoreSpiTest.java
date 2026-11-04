/*
 * @test
 * @summary java.security.KeyStoreSpi conformance: abstract class hierarchy and key methods.
 * @run main KeyStoreSpiTest
 */

public class KeyStoreSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStoreSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineAliases"), "declares engineAliases");
        check(ms.contains("engineContainsAlias"), "declares engineContainsAlias");
        check(ms.contains("engineDeleteEntry"), "declares engineDeleteEntry");
        check(ms.contains("engineEntryInstanceOf"), "declares engineEntryInstanceOf");
        check(ms.contains("engineGetCertificate"), "declares engineGetCertificate");
        check(ms.contains("engineGetCertificateAlias"), "declares engineGetCertificateAlias");
        check(ms.contains("engineGetCertificateChain"), "declares engineGetCertificateChain");
        check(ms.contains("engineGetCreationDate"), "declares engineGetCreationDate");
        check(ms.contains("engineGetEntry"), "declares engineGetEntry");
        check(ms.contains("engineGetKey"), "declares engineGetKey");
        check(ms.contains("engineIsCertificateEntry"), "declares engineIsCertificateEntry");
        check(ms.contains("engineIsKeyEntry"), "declares engineIsKeyEntry");
        check(ms.contains("engineLoad"), "declares engineLoad");
        check(ms.contains("engineSetCertificateEntry"), "declares engineSetCertificateEntry");
        check(ms.contains("engineSetEntry"), "declares engineSetEntry");
        check(ms.contains("engineSetKeyEntry"), "declares engineSetKeyEntry");
        check(ms.contains("engineSize"), "declares engineSize");
        check(ms.contains("engineStore"), "declares engineStore");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
