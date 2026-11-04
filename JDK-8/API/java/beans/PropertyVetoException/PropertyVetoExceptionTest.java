/*
 * @test
 * @summary java.beans.PropertyVetoException conformance: full contract.
 *          extends Exception, message via superclass, getPropertyChangeEvent
 *          identity (incl. null event), and the throw/catch cycle used to
 *          veto a constrained property change.
 *          Deepened: checked-exception typing, serialVersionUID, exact
 *          constructor inventory, toString/getLocalizedMessage, initCause
 *          chaining rules, serialization round-trip (message AND event
 *          field), and a real veto cycle through VetoableChangeSupport.
 * @run main PropertyVetoExceptionTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
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

public class PropertyVetoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testBasics() {
        PropertyChangeEvent evt = new PropertyChangeEvent(new Object(), "p", "old", "new");
        PropertyVetoException e = new PropertyVetoException("rejected", evt);
        check(Exception.class.isAssignableFrom(e.getClass()), "message");
        check("rejected".equals(e.getMessage()), "message");
        check(evt == e.getPropertyChangeEvent(), "propertyChangeEvent identity");

        PropertyVetoException n = new PropertyVetoException(null, null);
        check(n.getMessage() == null, "null message");
        check(n.getPropertyChangeEvent() == null, "null event retained");
    }

    static void testThrowCatch() {
        PropertyChangeEvent evt = new PropertyChangeEvent("src", "p", "a", "b");
        try {
            throw new PropertyVetoException("veto", evt);
        } catch (PropertyVetoException pve) {
            check("veto".equals(pve.getMessage()), "caught message");
            check("p".equals(pve.getPropertyChangeEvent().getPropertyName()), "caught event property");
            passed++;
        }
    }


    static void testStructure() {
        check(Exception.class.isAssignableFrom(PropertyVetoException.class), "extends Exception");
    }

    static void testInventory() throws Exception {
        Class<?> c = PropertyVetoException.class;
        check(!RuntimeException.class.isAssignableFrom(c), "PVE: checked exception (not a RuntimeException)");
        check(!Error.class.isAssignableFrom(c), "PVE: not an Error");
        check(Serializable.class.isAssignableFrom(c), "PVE: Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 129596057694162164L,
                "PVE: serialVersionUID stable across JDK versions");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "PVE: exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == String.class && ps[1] == PropertyChangeEvent.class,
                "PVE: constructor (String, PropertyChangeEvent)");
        check(Modifier.isPublic(c.getModifiers()), "PVE: public class");
        Method getter = null;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && "getPropertyChangeEvent".equals(m.getName())) {
                getter = m;
            }
        }
        check(getter != null, "PVE: declares public getPropertyChangeEvent");
        check(getter.getReturnType() == PropertyChangeEvent.class, "PVE: getPropertyChangeEvent returns PropertyChangeEvent");
        check(getter.getParameterTypes().length == 0, "PVE: getPropertyChangeEvent takes no arguments");
        check(c.isAssignableFrom(new PropertyVetoException("m", null).getClass()), "PVE: exact runtime class");
    }

    static void testMessageContract() {
        PropertyChangeEvent ev = new PropertyChangeEvent("s", "p", "a", "b");
        PropertyVetoException e = new PropertyVetoException("m1", ev);
        check("m1".equals(e.getMessage()), "PVE: message preserved verbatim");
        check("m1".equals(e.getLocalizedMessage()), "PVE: getLocalizedMessage defaults to getMessage");
        check(e.toString().startsWith("java.beans.PropertyVetoException: "), "PVE: toString format");
        check(e.toString().contains("m1"), "PVE: toString contains the message");
        check(e.toString().endsWith(": m1"), "PVE: toString is ClassName: message");
        PropertyVetoException empty = new PropertyVetoException("", ev);
        check("".equals(empty.getMessage()), "PVE: empty message preserved");
        PropertyVetoException nullMsg = new PropertyVetoException(null, ev);
        check(nullMsg.toString() != null, "PVE: toString non-null with null message");
        check(!nullMsg.toString().contains(": "), "PVE: toString has no colon separator with null message");
        check(e.fillInStackTrace() == e, "PVE: fillInStackTrace returns this");
        check(e.getStackTrace() != null, "PVE: getStackTrace non-null");
    }

    static void testCause() {
        PropertyChangeEvent ev = new PropertyChangeEvent("s", "p", "a", "b");
        PropertyVetoException e = new PropertyVetoException("m", ev);
        check(e.getCause() == null, "PVE: cause defaults to null");
        RuntimeException reason = new RuntimeException("reason");
        check(e.initCause(reason) == e, "PVE: initCause returns this");
        check(e.getCause() == reason, "PVE: initCause stores the cause by identity");
        try {
            e.initCause(new RuntimeException("second"));
            check(false, "PVE: second initCause must fail");
        } catch (IllegalStateException ise) {
            passed++;
        }
        PropertyVetoException fresh = new PropertyVetoException("m2", ev);
        fresh.initCause(null);
        check(fresh.getCause() == null, "PVE: initCause(null) leaves cause null");
    }

    static void testSerialization() throws Exception {
        PropertyChangeEvent evt = new PropertyChangeEvent("SRC", "p", "old", "new");
        evt.setPropagationId("PID");
        PropertyVetoException e = new PropertyVetoException("m3", evt);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        PropertyVetoException back = (PropertyVetoException) ois.readObject();
        ois.close();
        check("m3".equals(back.getMessage()), "PVE: serialization preserves the message");
        check(back.getPropertyChangeEvent() != null, "PVE: serialization preserves the event field");
        PropertyChangeEvent be = back.getPropertyChangeEvent();
        check("p".equals(be.getPropertyName()), "PVE: round-trip event property name");
        check("old".equals(be.getOldValue()), "PVE: round-trip event oldValue");
        check("new".equals(be.getNewValue()), "PVE: round-trip event newValue");
        check("PID".equals(be.getPropagationId()), "PVE: round-trip event propagationId");
        check(be.getSource() == null, "PVE: round-trip event source is null (transient in EventObject)");
        check(be != evt, "PVE: round-trip event is a copy, not the same object");
        check(back.getCause() == null, "PVE: round-trip default cause null");
        // cause set before serialization survives the round trip
        PropertyVetoException withCause = new PropertyVetoException("m4", evt);
        RuntimeException r = new RuntimeException("r");
        withCause.initCause(r);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        oos2.writeObject(withCause);
        oos2.close();
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()));
        PropertyVetoException back2 = (PropertyVetoException) ois2.readObject();
        ois2.close();
        check(back2.getCause() != null && "r".equals(back2.getCause().getMessage()),
                "PVE: round-trip preserves the initCause cause");
        check(back2.getPropertyChangeEvent() != null, "PVE: round-trip keeps the event with a cause set");
    }

    static void testRealVetoCycle() throws Exception {
        Object bean = new Object();
        final PropertyVetoException veto =
                new PropertyVetoException("rejected", new PropertyChangeEvent(bean, "salary", "100", "200"));
        VetoableChangeListener vetoer = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException { throw veto; }
        };
        VetoableChangeSupport vcs = new VetoableChangeSupport(bean);
        vcs.addVetoableChangeListener(vetoer);
        PropertyVetoException caught = null;
        try {
            vcs.fireVetoableChange("salary", "100", "200");
        } catch (PropertyVetoException pve) {
            caught = pve;
        }
        check(caught == veto, "PVE: real veto cycle delivers the same exception to the caller");
        check("rejected".equals(caught.getMessage()), "PVE: veto cycle message");
        check("salary".equals(caught.getPropertyChangeEvent().getPropertyName()),
                "PVE: veto cycle event property name");
        check("200".equals(caught.getPropertyChangeEvent().getNewValue()),
                "PVE: veto cycle event newValue");
        // usable as the cause of a wrapping exception
        IllegalStateException wrapper = new IllegalStateException("wrap", caught);
        check(wrapper.getCause() == caught, "PVE: usable as a cause with identity");
        // stack trace can be printed without failing
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        caught.printStackTrace(new PrintStream(sink, true));
        check(sink.toByteArray().length > 0, "PVE: printStackTrace produces output");
        check(caught.getSuppressed().length == 0, "PVE: no suppressed exceptions by default");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasics();
        testThrowCatch();
        testInventory();
        testMessageContract();
        testCause();
        testSerialization();
        testRealVetoCycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
