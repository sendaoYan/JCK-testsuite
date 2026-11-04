/*
 * @test
 * @summary java.beans.ExceptionListener conformance: white-box structure + black-box behavior.
 *          Functional interface shape (single abstract method exceptionThrown(Exception)
 *          returning void, lambda-usable), capture-list listener semantics,
 *          integration with Encoder (failing writeStatement wrapped as Exception
 *          with cause), with XMLEncoder (DefaultPersistenceDelegate unknown
 *          property name surfaces IllegalStateException) and with XMLDecoder
 *          (truncated XML yields SAXParseException, listener identity from
 *          constructors, default listener non-null), listener-throws
 *          propagation, exception subclass pass-through.
 * @run main ExceptionListenerTest
 */

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXParseException;

public class ExceptionListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class LBean {
        private int a;
        public LBean() {}
        public int getA() { return a; }
        public void setA(int v) { a = v; }
    }

    static class Capture implements ExceptionListener {
        final List<Exception> exs = new ArrayList<Exception>();
        public void exceptionThrown(Exception e) { exs.add(e); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ExceptionListener");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final Exception[] got = new Exception[1];
        ExceptionListener l = new ExceptionListener() {
            public void exceptionThrown(Exception e) { got[0] = e; }
        };
        Exception ex = new IllegalArgumentException("x");
        l.exceptionThrown(ex);
        check(ex == got[0], "exceptionThrown dispatches");
    }


    // Preserved from the original JDK-8 test file (same check calls).
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.ExceptionListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "exceptionThrown"), "declares exceptionThrown");
    }

    static void testInterfaceStructure() throws Exception {
        Class<?> c = ExceptionListener.class;
        check(c.isInterface(), "ExceptionListener is an interface");
        int abstractCount = 0;
        for (Method m : c.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 1, "exactly one abstract method (functional interface)");
        Method m = c.getMethod("exceptionThrown", Exception.class);
        check("exceptionThrown".equals(m.getName()), "method name is exceptionThrown");
        check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == Exception.class,
                "single parameter of type java.lang.Exception");
        check(m.getReturnType() == void.class, "returns void");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class),
                "not annotated @FunctionalInterface (still lambda-usable)");
        ExceptionListener lambda = new ExceptionListener() {
            public void exceptionThrown(Exception e) {}
        };
        check(lambda != null, "implementable with a single anonymous/lambda body");
    }

    static void testCaptureSemantics() {
        Capture cap = new Capture();
        IllegalStateException first = new IllegalStateException("one");
        IllegalArgumentException second = new IllegalArgumentException("two");
        cap.exceptionThrown(first);
        cap.exceptionThrown(second);
        check(cap.exs.size() == 2, "listener accumulates every exception");
        check(cap.exs.get(0) == first && cap.exs.get(1) == second,
                "listener preserves arrival order and identity");
        class Marker extends Exception {}
        Marker marker = new Marker();
        cap.exceptionThrown(marker);
        check(cap.exs.get(2).getClass() == Marker.class,
                "arbitrary Exception subclasses pass through unwrapped");
    }

    static void testEncoderIntegration() {
        Capture cap = new Capture();
        Encoder enc = new Encoder();
        enc.setExceptionListener(cap);
        enc.writeStatement(new Statement(new LBean(), "noSuchMethod", new Object[0]));
        check(cap.exs.size() == 1, "Encoder reports a failing statement exactly once");
        Exception got = cap.exs.get(0);
        check(got.getClass() == Exception.class, "Encoder wraps statement failure as plain Exception");
        check(got.getCause() instanceof NoSuchMethodException,
                "wrapper cause carries the original NoSuchMethodException");
        check(got.getMessage() != null && got.getMessage().contains("discarding"),
                "wrapper message mentions discarding");

        enc.writeStatement(new Statement(new LBean(), "setA", new Object[] { Integer.valueOf(4) }));
        check(cap.exs.size() == 1, "valid statements after a failure raise no exception");
    }

    static void testXMLEncoderIntegration() throws Exception {
        Capture cap = new Capture();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(cap);
        enc.setPersistenceDelegate(LBean.class,
                new DefaultPersistenceDelegate(new String[] { "nope" }));
        enc.writeObject(new LBean());
        enc.close();
        check(cap.exs.size() >= 1, "XMLEncoder reports delegate failures to the listener");
        Exception first = cap.exs.get(0);
        check(first instanceof IllegalStateException,
                "unknown property name surfaces as IllegalStateException");
        check(first.getMessage() != null && first.getMessage().contains("nope"),
                "failure message names the missing property");
    }

    static void testXMLDecoderIntegration() {
        Capture cap = new Capture();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><string>unclosed".getBytes()), null, cap);
        try {
            d.readObject();
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected: no complete object in the archive
        }
        check(cap.exs.size() >= 1, "XMLDecoder reports parse errors to the listener");
        check(cap.exs.get(0) instanceof SAXParseException,
                "truncated XML surfaces as SAXParseException");
        d.close();
    }

    static void testXMLDecoderListenerIdentity() throws Exception {
        Capture cap = new Capture();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject("x");
        enc.close();
        byte[] xml = baos.toByteArray();

        XMLDecoder d3 = new XMLDecoder(new ByteArrayInputStream(xml), null, cap);
        check(d3.getExceptionListener() == cap, "three-arg constructor wires the listener");
        d3.close();

        XMLDecoder d4 = new XMLDecoder(new ByteArrayInputStream(xml), null, cap,
                ExceptionListenerTest.class.getClassLoader());
        check(d4.getExceptionListener() == cap, "four-arg constructor wires the listener");
        d4.close();

        XMLDecoder dn = new XMLDecoder(new ByteArrayInputStream(xml), null, null);
        check(dn.getExceptionListener() != null, "null listener in constructor falls back to default");
        dn.close();
    }

    static void testDefaultListenersNonNull() throws Exception {
        check(new Encoder().getExceptionListener() != null, "Encoder default listener non-null");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder xe = new XMLEncoder(baos);
        check(xe.getExceptionListener() != null, "XMLEncoder default listener non-null");
        xe.close();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        XMLEncoder xe2 = new XMLEncoder(baos2);
        xe2.writeObject("x");
        xe2.close();
        XMLDecoder xd = new XMLDecoder(new ByteArrayInputStream(baos2.toByteArray()));
        check(xd.getExceptionListener() != null, "XMLDecoder default listener non-null");
        xd.close();
    }

    static void testThrowingListenerPropagates() {
        final RuntimeException marker = new RuntimeException("listener-threw");
        ExceptionListener bomb = new ExceptionListener() {
            public void exceptionThrown(Exception e) { throw marker; }
        };
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><string>unclosed".getBytes()), null, bomb);
        boolean propagated = false;
        try {
            d.readObject();
        } catch (Throwable t) {
            propagated = (t == marker);
        }
        check(propagated, "exception thrown by the listener propagates out of readObject unchanged");
        // close() re-runs the pending parse, which throws from the listener again.
        boolean closeThrew = false;
        try { d.close(); } catch (RuntimeException e) { closeThrew = true; }
        check(closeThrew, "listener exception also propagates out of the re-parsed close()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExistingReflection();
        testInterfaceStructure();
        testCaptureSemantics();
        testEncoderIntegration();
        testXMLEncoderIntegration();
        testXMLDecoderIntegration();
        testXMLDecoderListenerIdentity();
        testDefaultListenersNonNull();
        testThrowingListenerPropagates();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
