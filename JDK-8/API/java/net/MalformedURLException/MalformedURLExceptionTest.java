/*
 * @test
 * @summary java.net.MalformedURLException conformance: message and hierarchy.
 *          Both constructors, IOException hierarchy and catch ordering,
 *          exact serialVersionUID, serialization round-trip preserving
 *          message/cause/suppressed, initCause one-shot semantics,
 *          toString/getLocalizedMessage form, and the precise messages the
 *          URL class throws for no-protocol / unknown-protocol / bad-port /
 *          nested-jar inputs.
 * @run main MalformedURLExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.MalformedURLException;
import java.net.URL;

public class MalformedURLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> expected, Runnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static void testConstructors() {
        MalformedURLException e = new MalformedURLException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check("boom".equals(e.getLocalizedMessage()), "getLocalizedMessage(String)");
        MalformedURLException e0 = new MalformedURLException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getLocalizedMessage() == null, "no-arg localized null");
        // exactly two constructors exist
        check(MalformedURLException.class.getDeclaredConstructors().length == 2,
                "exactly two constructors");
        try {
            MalformedURLException.class.getConstructor(String.class);
            passed++;
        } catch (NoSuchMethodException ex) {
            check(false, "String constructor must exist");
        }
        try {
            MalformedURLException.class.getConstructor();
            passed++;
        } catch (NoSuchMethodException ex) {
            check(false, "no-arg constructor must exist");
        }
    }

    static void testHierarchy() {
        MalformedURLException e = new MalformedURLException("x");
        check(e instanceof IOException, "is IOException");
        check(e instanceof Exception, "is Exception");
        // catchable as the more general type first
        try { throw e; } catch (IOException ioe) { passed++; }
        // catch ordering: MalformedURLException is catchable through its declared type
        boolean hitSpecific = false;
        try {
            new URL("nosuchproto://ordering/");
        } catch (MalformedURLException me) {
            hitSpecific = true;
            check(me.getMessage() != null, "specific catch receives message");
        }
        check(hitSpecific, "specific catch taken");
        // runtime wrapper round-trip: cause chain
        RuntimeException rt = new RuntimeException("under", e);
        check(rt.getCause() == e, "usable as cause");
        check("under".equals(rt.getMessage()), "wrapper message");
    }

    static void testSerialization() throws Exception {
        // stable serialVersionUID across implementations (probe-verified both JDKs)
        check(ObjectStreamClass.lookup(MalformedURLException.class).getSerialVersionUID()
                == -182787522200415866L, "serialVersionUID stable");
        // message round-trip
        MalformedURLException e = new MalformedURLException("msg-1");
        MalformedURLException rt = roundTrip(e);
        check("msg-1".equals(rt.getMessage()), "message survives round-trip");
        check(rt instanceof IOException, "type survives round-trip");
        // null message round-trip
        MalformedURLException rt0 = roundTrip(new MalformedURLException());
        check(rt0.getMessage() == null, "null message survives round-trip");
        // cause round-trip (Throwable serialization format keeps the cause)
        RuntimeException cause = new RuntimeException("root");
        MalformedURLException withCause = new MalformedURLException("outer");
        withCause.initCause(cause);
        MalformedURLException rtC = roundTrip(withCause);
        check(rtC.getCause() != null, "cause survives round-trip");
        check("root".equals(rtC.getCause().getMessage()), "cause message survives");
        check(rtC.getCause() instanceof RuntimeException, "cause type survives");
        // suppressed exceptions survive
        withCause.addSuppressed(new IllegalStateException("s1"));
        withCause.addSuppressed(new IllegalArgumentException("s2"));
        MalformedURLException rtS = roundTrip(withCause);
        check(rtS.getSuppressed().length == 2, "suppressed count survives");
        check("s1".equals(rtS.getSuppressed()[0].getMessage()), "first suppressed survives");
        check("s2".equals(rtS.getSuppressed()[1].getMessage()), "second suppressed survives");
        // round-tripped object is still throwable and catchable
        try { throw rt; } catch (MalformedURLException m) { passed++; }
    }

    static void testCauseSemantics() {
        MalformedURLException e = new MalformedURLException("m");
        check(e.getCause() == null, "fresh exception has no cause");
        RuntimeException cause = new RuntimeException("c");
        Throwable self = e.initCause(cause);
        check(self == e, "initCause returns this");
        check(e.getCause() == cause, "initCause stores cause");
        // initCause is one-shot
        checkThrows(IllegalStateException.class, () -> e.initCause(cause), "initCause twice");
        checkThrows(IllegalStateException.class, () -> e.initCause(new RuntimeException()), "initCause replace");
        // a cause cannot be the exception itself
        checkThrows(IllegalArgumentException.class, () -> {
            MalformedURLException f = new MalformedURLException("f");
            f.initCause(f);
        }, "self-cause rejected");
        // suppressed mechanics
        check(e.getSuppressed().length == 0, "no suppressed initially");
        e.addSuppressed(new IllegalStateException("x"));
        check(e.getSuppressed().length == 1, "one suppressed after add");
        check(e.getSuppressed()[0] instanceof IllegalStateException, "suppressed type");
        // addSuppressed does not affect the message
        check("m".equals(e.getMessage()), "message unchanged by suppressed");
        // toString format
        check("java.net.MalformedURLException: m".equals(e.toString()), "toString form");
        // fillInStackTrace returns this
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        // stack trace is captured
        check(e.getStackTrace() != null, "stack trace non-null");
    }

    static void testThrownByURL() throws Exception {
        // real messages produced by the URL class (probe-verified on both JDKs)
        try { new URL("nosuchproto://x/"); check(false, "must throw"); }
        catch (MalformedURLException e) { check("unknown protocol: nosuchproto".equals(e.getMessage()), "unknown protocol message"); }
        try { new URL("relative/path"); check(false, "must throw"); }
        catch (MalformedURLException e) { check("no protocol: relative/path".equals(e.getMessage()), "no protocol message"); }
        try { new URL("http", "h", -2, "/p"); check(false, "must throw"); }
        catch (MalformedURLException e) { check("Invalid port number :-2".equals(e.getMessage()), "invalid port message"); }
        try { new URL("jar:jar:file:/a.jar!/b.jar!/c"); check(false, "must throw"); }
        catch (MalformedURLException e) { check("Nested JAR URLs are not supported".equals(e.getMessage()), "nested jar message"); }
        // a null spec also reports MalformedURLException (not NPE) on both JDKs
        try { new URL((String) null); check(false, "must throw"); }
        catch (NullPointerException npe) { check(false, "null spec is MUE not NPE"); }
        catch (MalformedURLException e) { passed++; }
        // the exception is catchable as IOException at the call site
        try { new URL("badproto://x"); }
        catch (IOException ioe) { check(ioe instanceof MalformedURLException, "caught as IOException"); }
        // thrown-exception equality of class
        try { new URL("badproto2://x"); }
        catch (IOException ioe) { check(MalformedURLException.class == ioe.getClass(), "exact class"); }
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testHierarchy();
        testSerialization();
        testCauseSemantics();
        testThrownByURL();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
