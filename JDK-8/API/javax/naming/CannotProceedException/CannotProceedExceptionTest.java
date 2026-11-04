/*
 * @test
 * @summary javax.naming.CannotProceedException conformance: full contract
 *          coverage. message/hierarchy plus environment (reference semantics),
 *          remainingNewName (clone semantics), altName / altNameCtx (reference
 *          semantics).
 * @run main CannotProceedExceptionTest
 */

import java.util.Hashtable;
import javax.naming.CannotProceedException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;

public class CannotProceedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        CannotProceedException e = new CannotProceedException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        check(e.getRemainingNewName() == null, "remainingNewName null");
        check(e.getAltName() == null, "altName null");
        check(e.getAltNameCtx() == null, "altNameCtx null");
        check(e.getEnvironment() == null, "environment null");
        CannotProceedException e0 = new CannotProceedException();
        check(e0.getMessage() == null, "no-arg message null");
    }

    static void testEnvironment() {
        CannotProceedException e = new CannotProceedException();
        Hashtable<Object, Object> h = new Hashtable<Object, Object>();
        h.put("k", "v");
        e.setEnvironment(h);
        check(e.getEnvironment() == h, "setEnvironment stores reference (no clone)");
    }

    static void testNames() throws Exception {
        CannotProceedException e = new CannotProceedException();
        Name n = new CompositeName("a/b");
        e.setRemainingNewName(n);
        check(e.getRemainingNewName().equals(n) && e.getRemainingNewName() != n,
                "setRemainingNewName clones");
        e.setAltName(n);
        check(e.getAltName() == n, "setAltName stores reference");
        e.setRemainingNewName(null);
        check(e.getRemainingNewName() == null, "setRemainingNewName(null)");
        e.setAltName(null);
        check(e.getAltName() == null, "setAltName(null)");
    }

    static void testAltNameCtx() throws Exception {
        CannotProceedException e = new CannotProceedException();
        Context c = new InitialContext();
        e.setAltNameCtx(c);
        check(e.getAltNameCtx() == c, "setAltNameCtx stores reference");
        e.setAltNameCtx(null);
        check(e.getAltNameCtx() == null, "setAltNameCtx(null)");
        c.close();
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEnvironment();
        testNames();
        testAltNameCtx();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
