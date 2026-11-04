/*
 * @test
 * @summary javax.xml.bind.JAXBPermission conformance: JAXB permission name.
 * @run main JAXBPermissionTest
 */


public class JAXBPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.bind.JAXBPermission p = new javax.xml.bind.JAXBPermission("setDatatypeConverter");
        check("setDatatypeConverter".equals(p.getName()), "name");
        check(p instanceof java.security.BasicPermission, "is BasicPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
