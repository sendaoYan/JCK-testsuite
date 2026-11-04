/*
 * @test
 * @summary javax.naming.NamingException conformance: full contract coverage.
 *          constructors, resolved/remaining name (clone semantics), resolved
 *          obj, explanation, appendRemainingComponent/Name (null no-op),
 *          rootCause/cause chain (setRootCause(this) no-op, initCause, getCause
 *          alias), and toString incl. root cause + remaining name.
 * @run main NamingExceptionTest
 */

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;

public class NamingExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        NamingException e = new NamingException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof Exception, "is Exception");
        check(e.getRemainingName() == null, "remainingName null");
        check(e.getRootCause() == null, "rootCause null");
        Exception cause = new Exception("cause");
        e.setRootCause(cause);
        check(e.getRootCause() == cause, "setRootCause");
        NamingException e0 = new NamingException();
        check(e0.getMessage() == null, "no-arg message null");
    }

    static void testResolvedRemainingNames() throws Exception {
        NamingException e = new NamingException();
        Name resolved = new CompositeName("a/b");
        e.setResolvedName(resolved);
        check(e.getResolvedName().equals(resolved), "setResolvedName");
        check(e.getResolvedName() != resolved, "setResolvedName clones");

        Name remaining = new CompositeName("c/d");
        e.setRemainingName(remaining);
        check(e.getRemainingName().equals(remaining), "setRemainingName");
        check(e.getRemainingName() != remaining, "setRemainingName clones");

        e.setResolvedName(null);
        check(e.getResolvedName() == null, "setResolvedName(null)");
        e.setRemainingName(null);
        check(e.getRemainingName() == null, "setRemainingName(null)");
    }

    static void testResolvedObjAndExplanation() {
        NamingException e = new NamingException();
        Object obj = new Object();
        e.setResolvedObj(obj);
        check(e.getResolvedObj() == obj, "setResolvedObj/getResolvedObj");
        check(e.getExplanation() == null, "explanation null by default");
    }

    static void testAppendRemaining() throws Exception {
        NamingException e = new NamingException();
        e.appendRemainingComponent(null);
        check(e.getRemainingName() == null, "appendRemainingComponent(null) no-op");
        e.appendRemainingComponent("x");
        e.appendRemainingComponent("y");
        check("x/y".equals(e.getRemainingName().toString()), "appendRemainingComponent appends");

        NamingException e2 = new NamingException();
        e2.appendRemainingName(null);
        check(e2.getRemainingName() == null, "appendRemainingName(null) no-op");
        e2.appendRemainingName(new CompositeName("a/b"));
        e2.appendRemainingName(new CompositeName("c"));
        check("a/b/c".equals(e2.getRemainingName().toString()), "appendRemainingName appends");
    }

    static void testRootCauseChain() {
        NamingException e = new NamingException("boom");
        e.setRootCause(e);
        check(e.getRootCause() == null, "setRootCause(this) no-op");
        check(e.getCause() == null, "getCause alias null");

        Exception root = new Exception("root");
        e.setRootCause(root);
        check(e.getRootCause() == root && e.getCause() == root, "rootCause and cause alias");

        NamingException e2 = new NamingException("x");
        e2.initCause(root);
        check(e2.getCause() == root, "initCause sets cause");
        check(e2.getRootCause() == root, "initCause sets rootCause");
    }

    static void testToString() throws Exception {
        NamingException e = new NamingException("boom");
        e.setRootCause(new Exception("root"));
        e.setRemainingName(new CompositeName("a/b"));
        String s = e.toString();
        check(s.contains("boom"), "toString message");
        check(s.contains("Root exception is"), "toString root cause");
        check(s.contains("remaining name 'a/b'"), "toString remaining name");
        check(e.toString(true) != null, "toString(detail)");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testResolvedRemainingNames();
        testResolvedObjAndExplanation();
        testAppendRemaining();
        testRootCauseChain();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
