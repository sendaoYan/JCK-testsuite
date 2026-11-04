/*
 * @test
 * @summary javax.management.remote.SubjectDelegationPermission conformance: name/implies.
 * @run main SubjectDelegationPermissionTest
 */

import javax.management.remote.*;
public class SubjectDelegationPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        SubjectDelegationPermission p = new SubjectDelegationPermission("com.example.Foo");
        check("com.example.Foo".equals(p.getName()), "name");
        check(p.implies(new SubjectDelegationPermission("com.example.Foo")), "implies same");
        check(!p.implies(new SubjectDelegationPermission("com.example.Bar")), "not implies different");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
