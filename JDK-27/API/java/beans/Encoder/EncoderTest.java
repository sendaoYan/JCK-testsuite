/*
 * @test
 * @summary java.beans.Encoder conformance: white-box structure + black-box behavior.
 *          Modifier matrix (writeObject protected non-final, writeStatement /
 *          writeExpression public, get/remove public), get semantics (null,
 *          String identity, unseen value), writeExpression evaluation and
 *          identity-cached bindings, remove + rewrite dispatch counting,
 *          writeStatement execution in the cloned environment (original
 *          untouched), failure routing to the ExceptionListener (statement
 *          wrapper vs raw expression cause), exception listener defaults,
 *          persistence delegate registry rules, subclassing hooks.
 * @run main EncoderTest
 */

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.Statement;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class PD extends PersistenceDelegate {
        protected Expression instantiate(Object oldInstance, Encoder out) { return null; }
        public void writeObject(Object oldInstance, Encoder out) {}
    }

    public static class EBean {
        private int x;
        private String s;
        public EBean() {}
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }

    static class OpenEncoder extends Encoder {
        void write(Object o) { writeObject(o); }
    }

    static class RecEncoder extends Encoder {
        int wo, ws, we;
        Object lastObj;
        Statement lastSt;
        Expression lastEx;
        public void writeObject(Object o) { wo++; lastObj = o; super.writeObject(o); }
        public void writeStatement(Statement s) { ws++; lastSt = s; super.writeStatement(s); }
        public void writeExpression(Expression e) { we++; lastEx = e; super.writeExpression(e); }
    }

    static class CapturingListener implements ExceptionListener {
        final List<Exception> exs = new ArrayList<Exception>();
        public void exceptionThrown(Exception e) { exs.add(e); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Encoder");
        check(!c.isInterface(), "is a class");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "constructor is public");

        Method w = c.getDeclaredMethod("writeObject", Object.class);
        check(Modifier.isProtected(w.getModifiers()), "writeObject is protected (public only on subclasses like XMLEncoder)");
        check(!Modifier.isFinal(w.getModifiers()), "writeObject is not final");
        check(!Modifier.isAbstract(w.getModifiers()), "writeObject is concrete");

        check(Modifier.isPublic(c.getDeclaredMethod("writeStatement", Statement.class).getModifiers()),
                "writeStatement is public");
        check(Modifier.isPublic(c.getDeclaredMethod("writeExpression", Expression.class).getModifiers()),
                "writeExpression is public");
        check(Modifier.isPublic(c.getDeclaredMethod("get", Object.class).getModifiers()), "get is public");
        check(Modifier.isPublic(c.getDeclaredMethod("remove", Object.class).getModifiers()), "remove is public");
        check(Modifier.isPublic(c.getDeclaredMethod(
                "setPersistenceDelegate", Class.class, PersistenceDelegate.class).getModifiers()),
                "setPersistenceDelegate is public");
        check(Modifier.isPublic(c.getDeclaredMethod("getPersistenceDelegate", Class.class).getModifiers()),
                "getPersistenceDelegate is public");
    }

    static void testBlackBox() throws Exception {
        Encoder enc = new Encoder();
        ExceptionListener l = new ExceptionListener() { public void exceptionThrown(Exception e) {} };
        enc.setExceptionListener(l);
        check(l == enc.getExceptionListener(), "exceptionListener set/get");
        check(enc.getPersistenceDelegate(String.class) != null, "default persistenceDelegate");
        PD pd = new PD();
        enc.setPersistenceDelegate(String.class, pd);
        check(pd == enc.getPersistenceDelegate(String.class), "setPersistenceDelegate");
    }

    static void testRecordingSubclass() {
        // String values short-circuit Encoder.get (identity), so no internal
        // re-entrant writeObject/writeExpression calls perturb the counters.
        RecEncoder enc = new RecEncoder();
        String s = new String("rec-str");
        enc.writeObject(s);
        check(enc.wo == 1, "subclass writeObject override sees one call");
        check(enc.lastObj == s, "subclass writeObject receives exact instance");

        Statement stm = new Statement(s, "hashCode", new Object[0]);
        enc.writeStatement(stm);
        check(enc.ws == 1, "subclass writeStatement override sees one call");
        check(enc.lastSt == stm, "subclass writeStatement receives exact statement");

        Expression exp = new Expression(s, s, "concat", new Object[] { "y" });
        enc.writeExpression(exp);
        check(enc.we == 1, "subclass writeExpression override sees one call");
        check(enc.lastEx == exp, "subclass writeExpression receives exact expression");

        int before = enc.wo;
        enc.writeObject(new EBean());
        check(enc.wo > before,
                "bean encoding re-enters writeObject through the delegate machinery");
    }

    static void testInternalDelegationObservable() {
        RecEncoder enc = new RecEncoder();
        EBean b = new EBean();
        b.setX(5);
        int before = enc.we;
        enc.writeObject(b);
        check(enc.we > before,
                "default delegate initialize routes getter expressions back through writeExpression");
    }

    static void testGetSemantics() {
        Encoder enc = new Encoder();
        check(enc.get(null) == null, "get(null) returns null");
        String s = new String("abc");
        check(enc.get(s) == s, "get returns the identical String for unseen strings");
        EBean b = new EBean();
        check(enc.get(b) == null, "get returns null for unseen non-String values");
    }

    static void testExpressionCaching() {
        Encoder enc = new Encoder();
        EBean b = new EBean();
        b.setX(3);
        enc.writeExpression(new Expression(b, b.getClass(), "new", null));
        Object nu = enc.get(b);
        check(nu != null, "writeExpression publishes a binding for the value");
        check(nu != b, "bound value is a distinct instance");
        check(nu instanceof EBean, "bound value has the same type");
        check(enc.get(b) == nu, "repeated get returns the same cached instance");
    }

    static void testRemoveAndRewrite() {
        final int[] instantiateCalls = new int[1];
        PersistenceDelegate counting = new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                instantiateCalls[0]++;
                return new Expression(old, old.getClass(), "new", null);
            }
        };
        OpenEncoder enc = new OpenEncoder();
        enc.setPersistenceDelegate(EBean.class, counting);
        EBean b = new EBean();
        enc.write(b);
        check(instantiateCalls[0] == 1, "first write dispatches to the delegate");
        enc.write(b);
        check(instantiateCalls[0] == 1, "second write of the same object does not re-instantiate");

        Object removed = enc.remove(b);
        check(removed instanceof EBean, "remove returns the bound clone");
        check(removed != b, "removed value is not the original");
        check(enc.get(b) == null, "get returns null after remove");

        enc.write(b);
        check(instantiateCalls[0] == 2, "write after remove dispatches to the delegate again");
    }

    static void testRemoveUnseen() {
        Encoder enc = new Encoder();
        check(enc.remove(new EBean()) == null, "remove of unseen object returns null");
        check(enc.remove(null) == null, "remove(null) returns null");
    }

    static void testStatementClonedExecution() {
        Encoder enc = new Encoder();
        EBean b = new EBean();
        enc.writeStatement(new Statement(b, "setX", new Object[] { Integer.valueOf(11) }));
        check(b.getX() == 0, "writeStatement does not touch the original object");
        Object nu = enc.get(b);
        check(nu instanceof EBean, "writeStatement creates a cloned environment object");
        check(((EBean) nu).getX() == 11, "statement executes immediately on the cloned object");
    }

    static void testStatementFailureRouted() {
        CapturingListener l = new CapturingListener();
        Encoder enc = new Encoder();
        enc.setExceptionListener(l);
        enc.writeStatement(new Statement(new EBean(), "noSuchMethod", new Object[0]));
        check(l.exs.size() == 1, "failing statement reported exactly once");
        Exception got = l.exs.get(0);
        check(got.getClass() == Exception.class, "statement failure wrapped as plain Exception");
        check(got.getMessage() != null && got.getMessage().contains("discarding"),
                "statement failure message mentions discarding");
        check(got.getCause() instanceof NoSuchMethodException,
                "statement failure cause is NoSuchMethodException");
    }

    static void testExpressionFailureRouted() {
        CapturingListener l = new CapturingListener();
        Encoder enc = new Encoder();
        enc.setExceptionListener(l);
        boolean threw = false;
        try {
            enc.writeExpression(new Expression("abc", "concat", new Object[] { "x", "y" }));
        } catch (RuntimeException e) {
            threw = e.getMessage() != null && e.getMessage().contains("failed to evaluate");
        }
        check(threw, "unevaluable expression makes writeExpression throw RuntimeException");
        check(l.exs.size() == 1, "raw evaluation failure delivered to listener");
        check(l.exs.get(0) instanceof NoSuchMethodException,
                "listener receives the raw NoSuchMethodException");
    }

    static void testExceptionListenerDefaults() {
        Encoder enc = new Encoder();
        check(enc.getExceptionListener() != null, "default exception listener non-null");
        enc.setExceptionListener(null);
        check(enc.getExceptionListener() != null, "setExceptionListener(null) falls back to default");
        ExceptionListener l = new ExceptionListener() { public void exceptionThrown(Exception e) {} };
        enc.setExceptionListener(l);
        check(enc.getExceptionListener() == l, "explicit listener returned verbatim");
    }

    static void testPersistenceDelegateRegistry() {
        Encoder enc = new Encoder();
        check(enc.getPersistenceDelegate(String.class) != null, "built-in delegate for String");
        check(enc.getPersistenceDelegate(Integer.class) != null, "built-in delegate for Integer");
        check(enc.getPersistenceDelegate(null) != null, "shared delegate for the null type");
        check(enc.getPersistenceDelegate(EBean.class) instanceof DefaultPersistenceDelegate,
                "unknown types map to DefaultPersistenceDelegate");
        PD pd = new PD();
        enc.setPersistenceDelegate(EBean.class, pd);
        check(enc.getPersistenceDelegate(EBean.class) == pd, "custom delegate registered");
        enc.setPersistenceDelegate(EBean.class, null);
        check(enc.getPersistenceDelegate(EBean.class) instanceof DefaultPersistenceDelegate,
                "setPersistenceDelegate(null) reverts to the default delegate");
    }

    static void testWriteObjectDelegation() {
        final Object[] seenOld = new Object[1];
        final Encoder[] seenOut = new Encoder[1];
        final int[] calls = new int[1];
        PersistenceDelegate rec = new PersistenceDelegate() {
            protected Expression instantiate(Object o, Encoder e) { return null; }
            public void writeObject(Object o, Encoder e) { calls[0]++; seenOld[0] = o; seenOut[0] = e; }
        };
        OpenEncoder enc = new OpenEncoder();
        enc.setPersistenceDelegate(EBean.class, rec);
        EBean b = new EBean();
        enc.write(b);
        check(calls[0] == 1, "writeObject delegates to the registered persistence delegate");
        check(seenOld[0] == b, "delegate receives the exact object");
        check(seenOut[0] == enc, "delegate receives the exact encoder");
    }

    static void testWriteObjectSelfIsNoOp() {
        final int[] calls = new int[1];
        PersistenceDelegate rec = new PersistenceDelegate() {
            protected Expression instantiate(Object o, Encoder e) { return null; }
            public void writeObject(Object o, Encoder e) { calls[0]++; }
        };
        OpenEncoder enc = new OpenEncoder();
        enc.setPersistenceDelegate(OpenEncoder.class, rec);
        enc.write(enc);
        check(calls[0] == 0, "writeObject(this) is a no-op (no delegate dispatch)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRecordingSubclass();
        testInternalDelegationObservable();
        testGetSemantics();
        testExpressionCaching();
        testRemoveAndRewrite();
        testRemoveUnseen();
        testStatementClonedExecution();
        testStatementFailureRouted();
        testExpressionFailureRouted();
        testExceptionListenerDefaults();
        testPersistenceDelegateRegistry();
        testWriteObjectDelegation();
        testWriteObjectSelfIsNoOp();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
