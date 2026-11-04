/*
 * @test
 * @summary java.beans.EventSetDescriptor conformance: white-box structure + black-box behavior.
 *          All six constructors, derived add/remove/get names (listener simple name + "s"),
 *          event-name compliance check and vetoableChange special case, Method[]/MethodDescriptor[]
 *          constructors (no validation, nulls allowed, defensive copies), unicast semantics
 *          (no auto-detection at construction, Introspector detects the throws clause),
 *          null/bad-name negative matrix, no public setters, not serializable.
 * @run main EventSetDescriptorTest
 */

import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.util.EventObject;

interface FooListener extends java.util.EventListener { void fooHappened(FooEvent e); }
class FooEvent extends EventObject { FooEvent(Object s) { super(s); } }
class Source {
    private java.util.List<FooListener> l = new java.util.ArrayList<FooListener>();
    public void addFooListener(FooListener x) { l.add(x); }
    public void removeFooListener(FooListener x) { l.remove(x); }
}

public class EventSetDescriptorTest {
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

    // helper sources/listeners for the deepening tests
    public interface MultiListener extends java.util.EventListener {
        void firstHappened(EventObject e);
        void secondHappened(EventObject e);
    }
    public static class MultiSrc {
        public void addMultiL(MultiListener l) {}
        public void removeMultiL(MultiListener l) {}
        public MultiListener[] getMultiLs() { return new MultiListener[0]; }
    }
    public static class PclSrc {
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        public PropertyChangeListener[] getPropertyChangeListeners() { return new PropertyChangeListener[0]; }
    }
    public static class PclNoGetSrc {
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }
    public static class PclBadAddSrc {
        public void addPcl(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }
    public static class VetoSrc {
        public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {}
        public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {}
    }
    public interface UniListener extends java.util.EventListener { void uniHappened(EventObject e); }
    public static class UniSrc {
        public void addUniListener(UniListener l) {}
        public void removeUniListener(UniListener l) {}
    }
    public static class UnicastSrc {
        public void addUniListener(UniListener l) throws java.util.TooManyListenersException {}
        public void removeUniListener(UniListener l) {}
    }
    public static class StaticAddSrc {
        public static void addMultiL(MultiListener l) {}
        public void removeMultiL(MultiListener l) {}
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.EventSetDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
        check(c.getConstructors().length == 6, "exactly six public constructors, got "
                + c.getConstructors().length);
        // no public mutators for the reflection-derived state
        String[] forbidden = {"setListenerType", "setAddListenerMethod", "setRemoveListenerMethod",
                "setGetListenerMethod", "setListenerMethods", "setListenerMethodDescriptors"};
        for (String name : forbidden) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) found = true;
            check(!found, "no public " + name);
        }
    }

    static void testBlackBox() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(Source.class, "foo", FooListener.class, "fooHappened");
        check("foo".equals(esd.getName()), "name");
        check(FooListener.class == esd.getListenerType(), "listenerType");
        check(esd.getAddListenerMethod() != null && "addFooListener".equals(esd.getAddListenerMethod().getName()), "addListenerMethod");
        check(esd.getRemoveListenerMethod() != null && "removeFooListener".equals(esd.getRemoveListenerMethod().getName()), "removeListenerMethod");
        check(esd.getListenerMethods() != null && esd.getListenerMethods().length == 1, "listenerMethods");
        check(!esd.isUnicast(), "unicast default false");
        esd.setUnicast(true); check(esd.isUnicast(), "unicast set");
        check(esd.isInDefaultEventSet(), "inDefaultEventSet default true");
        esd.setInDefaultEventSet(false); check(!esd.isInDefaultEventSet(), "inDefaultEventSet set false");
    }

    static void testSixArgStringCtor() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                new String[]{"firstHappened", "secondHappened"}, "addMultiL", "removeMultiL");
        check("custom".equals(esd.getName()), "name is the event set name");
        check(MultiListener.class == esd.getListenerType(), "listenerType identity");
        java.lang.reflect.Method[] lms = esd.getListenerMethods();
        check(lms != null && lms.length == 2, "both listener methods resolved");
        check(lms[0].equals(MultiListener.class.getMethod("firstHappened", EventObject.class))
                && lms[1].equals(MultiListener.class.getMethod("secondHappened", EventObject.class)),
                "listener methods resolved on the listener interface");
        check(esd.getAddListenerMethod().equals(MultiSrc.class.getMethod("addMultiL", MultiListener.class)),
                "add method identity");
        check(esd.getRemoveListenerMethod().equals(MultiSrc.class.getMethod("removeMultiL", MultiListener.class)),
                "remove method identity");
        check(esd.getGetListenerMethod() == null, "get listener method null when not supplied");
        check(esd.getListenerMethodDescriptors() != null
                && esd.getListenerMethodDescriptors().length == 2,
                "listener method descriptors derived");
    }

    static void testSevenArgStringCtor() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                new String[]{"firstHappened"}, "addMultiL", "removeMultiL", "getMultiLs");
        check("getMultiLs".equals(esd.getGetListenerMethod().getName()),
                "7-arg ctor resolves the get listener method");
        check(esd.getGetListenerMethod().equals(MultiSrc.class.getMethod("getMultiLs")),
                "get listener method identity");
        // a get name that resolves to a method with the wrong arity is silently ignored
        EventSetDescriptor wrongArity = new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                new String[]{"firstHappened"}, "addMultiL", "removeMultiL", "removeMultiL");
        check(wrongArity.getGetListenerMethod() == null,
                "get name with wrong arity silently yields null");
    }

    static void testFourArgDerivedNames() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(PclSrc.class, "propertyChange",
                PropertyChangeListener.class, "propertyChange");
        check("addPropertyChangeListener".equals(esd.getAddListenerMethod().getName()),
                "add name derived from listener simple name");
        check("removePropertyChangeListener".equals(esd.getRemoveListenerMethod().getName()),
                "remove name derived from listener simple name");
        check("getPropertyChangeListeners".equals(esd.getGetListenerMethod().getName()),
                "get name derived with trailing s");
        check(esd.getAddListenerMethod().equals(
                PclSrc.class.getMethod("addPropertyChangeListener", PropertyChangeListener.class)),
                "derived add identity");
        EventSetDescriptor noGet = new EventSetDescriptor(PclNoGetSrc.class, "propertyChange",
                PropertyChangeListener.class, "propertyChange");
        check(noGet.getGetListenerMethod() == null,
                "get listener method null when source lacks the getter");
    }

    static void testFourArgNegatives() throws Exception {
        // source without the conventional add method
        checkThrows(java.beans.IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new EventSetDescriptor(PclBadAddSrc.class, "propertyChange",
                            PropertyChangeListener.class, "propertyChange");
                }}, "4-arg without conventional add -> IntrospectionException");
        // event set name not matching the event argument class name
        checkThrows(java.beans.IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new EventSetDescriptor(PclSrc.class, "pc",
                            PropertyChangeListener.class, "propertyChange");
                }}, "4-arg event-name compliance -> IntrospectionException");
        // listener method name not present on the listener type
        checkThrows(java.beans.IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new EventSetDescriptor(PclSrc.class, "propertyChange",
                            PropertyChangeListener.class, "noSuchMethod");
                }}, "4-arg unknown listener method -> IntrospectionException");
        // nested listener types cannot be used with the 4-arg ctor: the derived
        // add name embeds the binary class name (recorded actual behavior)
        checkThrows(java.beans.IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new EventSetDescriptor(MultiSrc.class, "multiL",
                            MultiListener.class, "firstHappened");
                }}, "4-arg nested listener -> IntrospectionException (mangled derived name)");
    }

    static void testVetoableSpecialCase() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(VetoSrc.class, "vetoableChange",
                java.beans.VetoableChangeListener.class, "vetoableChange");
        check("vetoableChange".equals(esd.getName()), "vetoableChange event set name");
        check(java.beans.VetoableChangeListener.class == esd.getListenerType(),
                "vetoable listener type");
        check(esd.getAddListenerMethod() != null,
                "vetoableChange exempt from the event-name compliance check");
    }

    static void testMethodArrayCtors() throws Exception {
        java.lang.reflect.Method lm = MultiListener.class.getMethod("firstHappened", EventObject.class);
        java.lang.reflect.Method add = MultiSrc.class.getMethod("addMultiL", MultiListener.class);
        java.lang.reflect.Method remove = MultiSrc.class.getMethod("removeMultiL", MultiListener.class);
        // 5-arg: null add and remove are permitted
        EventSetDescriptor esd = new EventSetDescriptor("custom", MultiListener.class,
                new java.lang.reflect.Method[]{ lm }, null, null);
        check(esd.getAddListenerMethod() == null && esd.getRemoveListenerMethod() == null,
                "Method[] ctor accepts null add/remove");
        check(esd.getListenerMethods().length == 1
                && esd.getListenerMethods()[0] == lm, "Method[] ctor keeps method identity");
        // a method that is not declared on the listener type is accepted without validation
        java.lang.reflect.Method foreign = PclSrc.class.getMethod("addPropertyChangeListener",
                PropertyChangeListener.class);
        EventSetDescriptor mismatched = new EventSetDescriptor("custom", MultiListener.class,
                new java.lang.reflect.Method[]{ foreign }, null, null);
        check(mismatched.getListenerMethods()[0] == foreign,
                "Method[] ctor performs no listener-type validation (recorded actual)");
        // 6-arg with an explicit get method
        java.lang.reflect.Method get = MultiSrc.class.getMethod("getMultiLs");
        EventSetDescriptor withGet = new EventSetDescriptor("custom", MultiListener.class,
                new java.lang.reflect.Method[]{ lm }, add, remove, get);
        check(get == withGet.getGetListenerMethod(), "6-arg Method ctor get identity");
        check(add == withGet.getAddListenerMethod(), "6-arg Method ctor add identity");
    }

    static void testMethodDescriptorArrayCtor() throws Exception {
        java.lang.reflect.Method lm = MultiListener.class.getMethod("firstHappened", EventObject.class);
        java.lang.reflect.Method add = MultiSrc.class.getMethod("addMultiL", MultiListener.class);
        java.lang.reflect.Method remove = MultiSrc.class.getMethod("removeMultiL", MultiListener.class);
        MethodDescriptor[] mds = new MethodDescriptor[]{ new MethodDescriptor(lm) };
        EventSetDescriptor esd = new EventSetDescriptor("custom", MultiListener.class, mds, add, remove);
        check(esd.getListenerMethodDescriptors().length == 1, "MD[] ctor keeps descriptors");
        check(esd.getListenerMethodDescriptors()[0] == mds[0], "MD[] element identity");
        check(esd.getListenerMethods()[0] == lm, "methods derived from descriptors");
        // defensive copy of the descriptors array
        MethodDescriptor[] got = esd.getListenerMethodDescriptors();
        got[0] = null;
        check(esd.getListenerMethodDescriptors()[0] == mds[0], "descriptors array is defensive copy");
        // empty descriptor array
        EventSetDescriptor empty = new EventSetDescriptor("custom", MultiListener.class,
                new MethodDescriptor[0], null, null);
        check(empty.getListenerMethodDescriptors().length == 0, "empty MD[] keeps length 0");
        check(empty.getListenerMethods().length == 0, "empty MD[] derives no methods");
        // null descriptor array
        EventSetDescriptor nullMds = new EventSetDescriptor("custom", MultiListener.class,
                (MethodDescriptor[]) null, null, null);
        check(nullMds.getListenerMethodDescriptors() == null, "null MD[] -> descriptors null");
        check(nullMds.getListenerMethods() == null, "null MD[] -> methods null");
    }

    static void testNullMethodArray() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor("custom", MultiListener.class,
                (java.lang.reflect.Method[]) null, null, null);
        check(esd.getListenerMethods() == null, "null Method[] -> getListenerMethods null");
    }

    static void testFlagSemantics() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(UniSrc.class, "uni", UniListener.class,
                new String[]{"uniHappened"}, "addUniListener", "removeUniListener");
        check(esd.isInDefaultEventSet(), "inDefaultEventSet defaults true");
        esd.setInDefaultEventSet(false);
        check(!esd.isInDefaultEventSet(), "inDefaultEventSet set false");
        esd.setInDefaultEventSet(true);
        check(esd.isInDefaultEventSet(), "inDefaultEventSet set back true");
        check(!esd.isUnicast(), "unicast defaults false");
        esd.setUnicast(true);
        check(esd.isUnicast(), "unicast set true");
        esd.setUnicast(false);
        check(!esd.isUnicast(), "unicast set back false");
        // the string constructors do NOT inspect the add method's throws clause
        EventSetDescriptor unicastCtor = new EventSetDescriptor(UnicastSrc.class, "uni", UniListener.class,
                new String[]{"uniHappened"}, "addUniListener", "removeUniListener");
        check(!unicastCtor.isUnicast(),
                "throws TooManyListenersException is not auto-detected at construction");
    }

    static void testIntrospectorUnicastDetection() throws Exception {
        java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(UnicastSrc.class);
        EventSetDescriptor found = null;
        for (EventSetDescriptor e : info.getEventSetDescriptors()) {
            if ("uni".equals(e.getName())) found = e;
        }
        check(found != null, "Introspector finds the uni event set");
        check(found.isUnicast(), "Introspector detects unicast from the throws clause");
        check(UniListener.class == found.getListenerType(), "Introspector listener type");
        check("addUniListener".equals(found.getAddListenerMethod().getName()), "Introspector add method");
        check("removeUniListener".equals(found.getRemoveListenerMethod().getName()), "Introspector remove method");
        java.beans.BeanInfo plain = java.beans.Introspector.getBeanInfo(UniSrc.class);
        EventSetDescriptor plainEsd = null;
        for (EventSetDescriptor e : plain.getEventSetDescriptors()) {
            if ("uni".equals(e.getName())) plainEsd = e;
        }
        check(plainEsd != null && !plainEsd.isUnicast(),
                "non-throwing add method introspected as multicast");
    }

    static void testNullArgumentMatrix() throws Exception {
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(null, "custom", MultiListener.class,
                        new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
            }
        }, "null sourceClass -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, null, MultiListener.class,
                        new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
            }
        }, "null eventSetName -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, "custom", null,
                        new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
            }
        }, "null listenerType -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                        new String[]{ null }, "addMultiL", "removeMultiL");
            }
        }, "null listener method name -> NPE");
    }

    static void testBadNameMatrix() throws Exception {
        checkThrows(java.beans.IntrospectionException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                        new String[]{"firstHappened"}, "noAdd", "removeMultiL");
            }
        }, "unknown add name -> IntrospectionException");
        checkThrows(java.beans.IntrospectionException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                        new String[]{"firstHappened"}, "addMultiL", "noRemove");
            }
        }, "unknown remove name -> IntrospectionException");
        checkThrows(java.beans.IntrospectionException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(StaticAddSrc.class, "custom", MultiListener.class,
                        new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
            }
        }, "static add method -> IntrospectionException");
        checkThrows(java.beans.IntrospectionException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                        new String[]{"noSuchListenerMethod"}, "addMultiL", "removeMultiL");
            }
        }, "unknown listener method name -> IntrospectionException");
    }

    static void testInheritedFeatureBehavior() throws Exception {
        EventSetDescriptor esd = new EventSetDescriptor(MultiSrc.class, "custom", MultiListener.class,
                new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
        check(!esd.isExpert() && !esd.isHidden() && !esd.isPreferred(), "flags default false");
        esd.setExpert(true); check(esd.isExpert(), "expert toggle");
        esd.setHidden(true); check(esd.isHidden(), "hidden toggle");
        esd.setPreferred(true); check(esd.isPreferred(), "preferred toggle");
        esd.setDisplayName("Custom events");
        check("Custom events".equals(esd.getDisplayName()), "displayName set/get");
        esd.setValue("k", "v");
        check("v".equals(esd.getValue("k")), "setValue inherited");
        check(esd.toString() != null, "toString non-null");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(EventSetDescriptor.class),
                "EventSetDescriptor is not Serializable");
        final EventSetDescriptor esd = new EventSetDescriptor(MultiSrc.class, "custom",
                MultiListener.class, new String[]{"firstHappened"}, "addMultiL", "removeMultiL");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        try {
            oos.writeObject(esd);
            check(false, "expected NotSerializableException");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSixArgStringCtor();
        testSevenArgStringCtor();
        testFourArgDerivedNames();
        testFourArgNegatives();
        testVetoableSpecialCase();
        testMethodArrayCtors();
        testMethodDescriptorArrayCtor();
        testNullMethodArray();
        testFlagSemantics();
        testIntrospectorUnicastDetection();
        testNullArgumentMatrix();
        testBadNameMatrix();
        testInheritedFeatureBehavior();
        testNotSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
