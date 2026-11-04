/*
 * @test
 * @summary java.beans.IntrospectionException conformance: exception hierarchy
 *          and construction.
 *          Deepened: exact constructor inventory (single (String) constructor
 *          on both JDK-8 and JDK-27), message/null-message contract,
 *          toString, initCause chaining, serialVersionUID + serialization
 *          round-trip, and REAL triggers from Introspector.getBeanInfo with
 *          a stop class that is not a superclass.
 * @run main IntrospectionExceptionTest
 */

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class IntrospectionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testLegacy() {
        // preserved original assertions
        java.beans.IntrospectionException x = new java.beans.IntrospectionException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        try {
            check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
    }

    static void testInventory() throws Exception {
        Class<?> c = IntrospectionException.class;
        check(Exception.class.isAssignableFrom(c), "IE: extends Exception");
        check(!RuntimeException.class.isAssignableFrom(c), "IE: checked exception (not a RuntimeException)");
        check(Serializable.class.isAssignableFrom(c), "IE: Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -3728150539969542619L,
                "IE: serialVersionUID stable across JDK versions");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "IE: exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 1 && ps[0] == String.class, "IE: constructor (String)");
        try {
            c.getConstructor(String.class, Throwable.class);
            check(false, "IE: no (String,Throwable) constructor exists");
        } catch (NoSuchMethodException nsme) {
            passed++;
        }
        int publicDeclared = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) publicDeclared++;
        }
        check(publicDeclared == 0, "IE: no public declared methods of its own");
        check(Modifier.isPublic(c.getModifiers()), "IE: public class");
    }

    static void testMessageContract() {
        IntrospectionException e = new IntrospectionException("boom");
        check("boom".equals(e.getMessage()), "IE: message preserved verbatim");
        check("boom".equals(e.getLocalizedMessage()), "IE: getLocalizedMessage defaults to getMessage");
        IntrospectionException nullMsg = new IntrospectionException(null);
        check(nullMsg.getMessage() == null, "IE: null message retained as null");
        IntrospectionException empty = new IntrospectionException("");
        check("".equals(empty.getMessage()), "IE: empty message preserved");
        check(e.toString().equals("java.beans.IntrospectionException: boom"), "IE: toString is ClassName: message");
        check(nullMsg.toString() != null, "IE: toString non-null with null message");
        check(e.fillInStackTrace() == e, "IE: fillInStackTrace returns this");
        check(e.getStackTrace() != null, "IE: getStackTrace non-null");
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(sink, true));
        check(sink.toString().contains("java.beans.IntrospectionException"), "IE: printStackTrace names the class");
    }

    static void testCause() {
        IntrospectionException e = new IntrospectionException("m");
        check(e.getCause() == null, "IE: cause defaults to null");
        RuntimeException reason = new RuntimeException("reason");
        check(e.initCause(reason) == e, "IE: initCause returns this");
        check(e.getCause() == reason, "IE: initCause stores the cause by identity");
        try {
            e.initCause(new RuntimeException("second"));
            check(false, "IE: second initCause must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
        IntrospectionException fresh = new IntrospectionException("m2");
        fresh.initCause(null);
        check(fresh.getCause() == null, "IE: initCause(null) leaves cause null");
    }

    static void testSerialization() throws Exception {
        IntrospectionException e = new IntrospectionException("round");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        IntrospectionException back = (IntrospectionException) ois.readObject();
        ois.close();
        check("round".equals(back.getMessage()), "IE: round-trip preserves the message");
        check(back.getClass() == IntrospectionException.class, "IE: round-trip keeps the exact class");
        check(back.getCause() == null, "IE: round-trip default cause null");
        IntrospectionException withCause = new IntrospectionException("rc");
        withCause.initCause(new RuntimeException("why"));
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(withCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        IntrospectionException back2 = (IntrospectionException) ois2.readObject();
        ois2.close();
        check(back2.getCause() != null && "why".equals(back2.getCause().getMessage()),
                "IE: round-trip preserves the initCause cause");
    }

    static void testRealTriggers() throws Exception {
        // stop class is not a superclass of the bean class -> IntrospectionException
        try {
            Introspector.getBeanInfo(String.class, Integer.class);
            check(false, "IE: stop class that is not a superclass must throw");
        } catch (IntrospectionException ie) {
            check(ie.getClass() == IntrospectionException.class, "IE: exact exception class from Introspector");
            check(ie.getMessage() != null, "IE: trigger produces a non-null message");
            check(ie.getMessage().contains("superclass"), "IE: message explains the stop-class problem");
        }
        // stop class is a SUBclass of the bean class (not a superclass) -> also throws
        try {
            Introspector.getBeanInfo(Number.class, Integer.class);
            check(false, "IE: stop class that is a subclass must throw");
        } catch (IntrospectionException ie) {
            passed++;
        }
        // stop class equal to the bean class itself -> also throws
        try {
            Introspector.getBeanInfo(String.class, String.class);
            check(false, "IE: stop class equal to the bean class must throw");
        } catch (IntrospectionException ie) {
            passed++;
        }
        // a legitimate stop class (true superclass) succeeds
        BeanInfo bi = Introspector.getBeanInfo(String.class, Object.class);
        check(bi != null, "IE: legitimate stop class does not throw");
        // documented non-triggers: primitive, array, and interface beans introspect fine
        check(Introspector.getBeanInfo(int.class) != null, "IE: primitive bean class is not a trigger");
        check(Introspector.getBeanInfo(String[].class) != null, "IE: array bean class is not a trigger");
        check(Introspector.getBeanInfo(Runnable.class) != null, "IE: interface bean class is not a trigger");
        // the exception is catchable as Exception (checked typing) and rethrowable with identity
        try {
            try {
                Introspector.getBeanInfo(String.class, Integer.class);
            } catch (IntrospectionException inner) {
                throw inner;
            }
            check(false, "IE: rethrow path unreachable");
        } catch (Exception caught) {
            check(caught.getClass() == IntrospectionException.class, "IE: catch (Exception) receives exact class");
        }
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testInventory();
        testMessageContract();
        testCause();
        testSerialization();
        testRealTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
