/*
 * @test
 * @summary java.beans.PersistenceDelegate conformance: white-box structure + black-box behavior.
 *          Modifier matrix (writeObject public non-final, instantiate protected
 *          abstract, mutatesTo/initialize protected; constructor public on
 *          JDK 8, protected on JDK 9+), default mutatesTo class-equality rule,
 *          writeObject dispatch and flow (get + mutatesTo + instantiate),
 *          custom factory-method delegate round-trip, instantiate failure
 *          routed to the encoder's ExceptionListener, statement-writing
 *          delegate, initialize chaining for subclass types, null encoder NPE.
 * @run main PersistenceDelegateTest
 */

import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PersistenceDelegateTest {
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

    public static class PBean {
        private int x;
        private String s;
        public PBean() {}
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }
    public static class PSubBean extends PBean {
        public PSubBean() {}
    }
    public static class PFactory {
        private final int v;
        private PFactory(int v) { this.v = v; }
        public static PFactory create(int v) { return new PFactory(v); }
        public int getV() { return v; }
    }

    static class OpenEncoder extends Encoder {
        void write(Object o) { writeObject(o); }
    }

    static class CapturingListener implements ExceptionListener {
        final List<Exception> exs = new ArrayList<Exception>();
        public void exceptionThrown(Exception e) { exs.add(e); }
    }

    static Object decode(String xml) {
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(xml.getBytes()));
        try { return dec.readObject(); } finally { dec.close(); }
    }

    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.PersistenceDelegate");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "writeObject"), "declares writeObject");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testModifierMatrix() throws Exception {
        Class<?> c = PersistenceDelegate.class;
        check(Modifier.isAbstract(c.getModifiers()), "class is abstract");
        java.lang.reflect.Method w = c.getDeclaredMethod("writeObject", Object.class, Encoder.class);
        check(Modifier.isPublic(w.getModifiers()), "writeObject is public");
        check(!Modifier.isAbstract(w.getModifiers()), "writeObject is concrete");
        check(!Modifier.isFinal(w.getModifiers()), "writeObject is not final (subclassable per javadoc)");

        java.lang.reflect.Method im = c.getDeclaredMethod("instantiate", Object.class, Encoder.class);
        check(Modifier.isProtected(im.getModifiers()), "instantiate is protected");
        check(Modifier.isAbstract(im.getModifiers()), "instantiate is abstract");

        java.lang.reflect.Method mm = c.getDeclaredMethod("mutatesTo", Object.class, Object.class);
        check(Modifier.isProtected(mm.getModifiers()), "mutatesTo is protected");
        check(!Modifier.isAbstract(mm.getModifiers()), "mutatesTo is concrete");

        java.lang.reflect.Method ii = c.getDeclaredMethod("initialize",
                Class.class, Object.class, Object.class, Encoder.class);
        check(Modifier.isProtected(ii.getModifiers()), "initialize is protected");
        check(!Modifier.isAbstract(ii.getModifiers()), "initialize is concrete");

        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isPublic(ctor.getModifiers()),
                "implicit constructor is public on JDK 8 (protected on JDK 9+)");
    }

    static void testMutatesToDefaults() {
        class PubPD extends PersistenceDelegate {
            protected Expression instantiate(Object o, Encoder e) { return null; }
            boolean mutatesToPub(Object a, Object b) { return mutatesTo(a, b); }
        }
        PubPD pd = new PubPD();
        check(pd.mutatesToPub("a", "a"), "default mutatesTo true for equal same-class instances");
        check(pd.mutatesToPub("a", "b"), "default mutatesTo true for ANY same-class instances");
        check(!pd.mutatesToPub(null, "a"), "default mutatesTo false for null old instance");
        check(!pd.mutatesToPub("a", null), "default mutatesTo false for null new instance");
        check(!pd.mutatesToPub(null, null), "default mutatesTo false for both null");
        check(!pd.mutatesToPub("a", Integer.valueOf(1)), "default mutatesTo false across classes");
        check(!pd.mutatesToPub(new PBean(), new PSubBean()), "default mutatesTo false for subclass");
    }

    static void testWriteObjectDispatch() {
        final Object[] seenOld = new Object[1];
        final Encoder[] seenOut = new Encoder[1];
        PersistenceDelegate rec = new PersistenceDelegate() {
            protected Expression instantiate(Object o, Encoder e) { return null; }
            public void writeObject(Object o, Encoder e) { seenOld[0] = o; seenOut[0] = e; }
        };
        Encoder enc = new Encoder();
        Object arg = "dispatch-me";
        rec.writeObject(arg, enc);
        check(seenOld[0] == arg, "writeObject receives the exact old instance");
        check(seenOut[0] == enc, "writeObject receives the exact encoder");
    }

    static void testBaseWriteObjectFlow() {
        final int[] instantiateCalls = new int[1];
        final int[] mutatesCalls = new int[1];
        PersistenceDelegate pd = new PersistenceDelegate() {
            protected Expression instantiate(Object o, Encoder e) {
                instantiateCalls[0]++;
                return new Expression(o, o.getClass(), "new", null);
            }
            protected boolean mutatesTo(Object a, Object b) {
                mutatesCalls[0]++;
                return super.mutatesTo(a, b);
            }
        };
        OpenEncoder enc = new OpenEncoder();
        enc.setPersistenceDelegate(PBean.class, pd);
        PBean b = new PBean();
        enc.write(b);
        check(instantiateCalls[0] == 1, "base writeObject flow calls instantiate exactly once");
        check(mutatesCalls[0] >= 1, "base writeObject flow consults mutatesTo");
        Object nu = enc.get(b);
        check(nu instanceof PBean && nu != b, "flow publishes a cloned instance via get");
    }

    static void testFactoryDelegateRoundTrip() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(PFactory.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                return new Expression(old, PFactory.class, "create",
                        new Object[] { Integer.valueOf(((PFactory) old).getV()) });
            }
        });
        enc.writeObject(PFactory.create(9));
        enc.close();
        check(l.exs.isEmpty(), "factory delegate encodes without exceptions");
        String xml = baos.toString("UTF-8");
        check(xml.contains("method=\"create\""), "factory method name emitted");
        check(xml.contains("<int>9</int>"), "factory argument emitted");
        Object back = decode(xml);
        check(back instanceof PFactory, "factory-produced value decodes to same type");
        check(((PFactory) back).getV() == 9, "factory-produced value round-trips");
    }

    static void testInstantiateFailureRouted() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(PBean.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                throw new IllegalStateException("boom-instantiate");
            }
        });
        enc.writeObject(new PBean());
        enc.close();
        check(l.exs.size() == 1, "instantiate failure reported once");
        Exception got = l.exs.get(0);
        check(got.getClass() == Exception.class, "failure surfaced as plain Exception wrapper");
        check(got.getMessage() != null && got.getMessage().contains("discarding"),
                "wrapper message mentions discarding");
        check(got.getCause() instanceof IllegalStateException, "original exception preserved as cause");
        check(!baos.toString("UTF-8").contains("<object"), "failed object omitted from output");
    }

    static void testStatementWritingDelegate() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(PBean.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                return new Expression(old, old.getClass(), "new", null);
            }
            protected void initialize(Class<?> type, Object old, Object nu, Encoder out) {
                out.writeStatement(new Statement(old, "setS",
                        new Object[] { ((PBean) old).getS() }));
                out.writeStatement(new Statement(old, "setX",
                        new Object[] { Integer.valueOf(((PBean) old).getX()) }));
            }
        });
        PBean b = new PBean();
        b.setX(3);
        b.setS("stmt");
        enc.writeObject(b);
        enc.close();
        check(l.exs.isEmpty(), "statement-writing delegate encodes without exceptions");
        String xml = baos.toString("UTF-8");
        check(xml.contains("property=\"s\""), "setter statement emitted for property s");
        check(xml.contains("property=\"x\""), "setter statement emitted for property x");
        PBean back = (PBean) decode(xml);
        check(back.getX() == 3, "mutated state x round-trips");
        check("stmt".equals(back.getS()), "mutated state s round-trips");
    }

    static void testInitializeChainingForSubclass() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(PSubBean.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                return new Expression(old, old.getClass(), "new", null);
            }
        });
        PSubBean sb = new PSubBean();
        sb.setX(4);
        sb.setS("sub");
        enc.writeObject(sb);
        enc.close();
        check(l.exs.isEmpty(), "default initialize chains to superclass delegate silently");
        Object back = decode(baos.toString("UTF-8"));
        check(back instanceof PSubBean, "subclass instance decodes");
        check(((PSubBean) back).getX() == 0, "default initialize writes no property statements");
    }

    static void testNullEncoder() {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new java.beans.DefaultPersistenceDelegate().writeObject("x", null);
                }}, "writeObject with null encoder -> NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testExistingReflection();
        testModifierMatrix();
        testMutatesToDefaults();
        testWriteObjectDispatch();
        testBaseWriteObjectFlow();
        testFactoryDelegateRoundTrip();
        testInstantiateFailureRouted();
        testStatementWritingDelegate();
        testInitializeChainingForSubclass();
        testNullEncoder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
