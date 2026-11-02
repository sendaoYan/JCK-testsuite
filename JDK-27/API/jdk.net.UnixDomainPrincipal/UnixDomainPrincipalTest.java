/*
 * @test
 * @summary jdk.net.UnixDomainPrincipal conformance: unix domain principal.
 * @run main UnixDomainPrincipalTest
 */

public class UnixDomainPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        java.nio.file.attribute.UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        java.nio.file.attribute.UserPrincipal up = ls.lookupPrincipalByName(System.getProperty("user.name"));
        java.nio.file.attribute.GroupPrincipal gp = ls.lookupPrincipalByGroupName("root");
        jdk.net.UnixDomainPrincipal p = new jdk.net.UnixDomainPrincipal(up, gp);
        check(p.user() == up, "user");
        check(p.group() == gp, "group");
        check(p.toString() != null, "toString");
        check(p.equals(new jdk.net.UnixDomainPrincipal(up, gp)), "equals");
        check(p.hashCode() == new jdk.net.UnixDomainPrincipal(up, gp).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
