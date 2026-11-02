/*
 * @test
 * @summary javax.security.auth.Subject conformance: subject principals and read-only state.
 * @run main SubjectTest
 */

import javax.security.auth.*;
import javax.security.auth.x500.*;

public class SubjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Subject sub = new Subject();
        check(!sub.isReadOnly(), "initially not read-only");
        X500Principal xp = new X500Principal("CN=Alice");
        sub.getPrincipals().add(xp);
        check(sub.getPrincipals().size() == 1, "principals size");
        check(sub.getPrincipals().contains(xp), "contains principal");
        check(sub.getPrincipals(X500Principal.class).size() == 1, "typed principals");
        check(sub.getPublicCredentials().isEmpty() && sub.getPrivateCredentials().isEmpty(), "empty credentials");
        sub.setReadOnly();
        check(sub.isReadOnly(), "read-only after setReadOnly");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
