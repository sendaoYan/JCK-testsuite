/*
 * @test
 * @summary javax.naming.InitialContext conformance: full contract coverage.
 *          lazy construction (no-arg / empty env), NoInitialContextException
 *          from lookup/getEnvironment/addToEnvironment/getNameInNamespace when
 *          no InitialContextFactory is available, eager factory instantiation
 *          failure via the env factory property, and idempotent close().
 * @run main InitialContextTest
 */

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NoInitialContextException;

public class InitialContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() throws Exception {
        InitialContext ctx = new InitialContext();
        check(ctx != null, "constructed");
        ctx.close();
        InitialContext ctx2 = new InitialContext(new Hashtable<Object, Object>());
        ctx2.close();
        check(true, "close with env");
    }

    static void testLazyNoFactory() throws Exception {
        final InitialContext ctx = new InitialContext();
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.lookup("x"); } },
                "lookup without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.getEnvironment(); } },
                "getEnvironment without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.addToEnvironment("k", "v"); } },
                "addToEnvironment without factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { ctx.getNameInNamespace(); } },
                "getNameInNamespace without factory");
        ctx.close();
    }

    static void testCloseIdempotent() throws Exception {
        InitialContext ctx = new InitialContext();
        ctx.close();
        ctx.close();
        check(true, "close idempotent");
    }

    static void testEagerFactoryFailure() {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "no.such.Factory");
        checkThrows(NoInitialContextException.class,
                new ThrowingRunnable() { public void run() throws Exception { new InitialContext(env); } },
                "uninstantiable factory -> NoInitialContextException at construction");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testLazyNoFactory();
        testCloseIdempotent();
        testEagerFactoryFailure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
