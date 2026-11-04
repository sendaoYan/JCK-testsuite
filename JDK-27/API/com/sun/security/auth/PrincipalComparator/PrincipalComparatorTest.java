/*
 * @test
 * @summary com.sun.security.auth.PrincipalComparator conformance: white-box structure + black-box behavior.
 * @run main PrincipalComparatorTest
 */

import java.lang.reflect.*;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;
import com.sun.security.auth.UnixPrincipal;
import com.sun.security.auth.PrincipalComparator;

public class PrincipalComparatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.PrincipalComparator");
        check(c.isInterface(), "interface");
        check(methodNames(c).contains("implies"), "implies method");
    }

    static void testBlackBox() {
        // A "group" comparator: implies a Subject that carries a matching principal.
        PrincipalComparator groupComparator = subject -> {
            if (subject == null) return false;
            for (Principal pr : subject.getPrincipals()) {
                if ("jck-group-member".equals(pr.getName())) return true;
            }
            return false;
        };

        check(groupComparator instanceof PrincipalComparator, "lambda instanceof PrincipalComparator");

        Subject empty = new Subject();
        check(!groupComparator.implies(empty), "empty subject not implied");

        Subject member = new Subject();
        member.getPrincipals().add(new UnixPrincipal("jck-group-member"));
        check(groupComparator.implies(member), "subject with matching principal implied");

        Subject other = new Subject();
        other.getPrincipals().add(new UnixPrincipal("someone-else"));
        check(!groupComparator.implies(other), "subject with other principal not implied");

        check(!groupComparator.implies(null), "null subject handled by implementation (returns false)");

        // Two independent implementations are equal only by identity; still usable as distinct comparators.
        PrincipalComparator alwaysTrue = subject -> true;
        check(alwaysTrue.implies(empty), "always-true comparator");
        check(alwaysTrue.implies(member), "always-true comparator on non-empty subject");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
