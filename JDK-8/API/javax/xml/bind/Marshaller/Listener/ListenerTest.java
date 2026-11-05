/*
 * @test
 * @summary javax.xml.bind.Marshaller.Listener conformance: no-op hook defaults,
 *          subclass recording lifecycle, and marshal integration.
 * @run main ListenerTest
 */

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class ListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person {
        @XmlElement
        String name;
        @XmlElement
        int age;
    }

    static final List<String> calls = new ArrayList<String>();

    static class Recording extends Marshaller.Listener {
        public void beforeMarshal(Object source) { calls.add("before:" + tag(source)); }
        public void afterMarshal(Object source) { calls.add("after:" + tag(source)); }
    }

    static String tag(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Marshaller$Listener");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("beforeMarshal"), "has method beforeMarshal");
        check(names.contains("afterMarshal"), "has method afterMarshal");
        // exactly the two hooks declared, both concrete no-ops (empty default bodies)
        check(c.getDeclaredMethods().length == 2, "exactly two declared hooks");
        for (Method m : c.getDeclaredMethods()) {
            check(!Modifier.isAbstract(m.getModifiers()), m.getName() + " is a concrete no-op default");
            check(m.getReturnType() == void.class, m.getName() + " returns void");
            check(m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == Object.class,
                  m.getName() + " takes a single Object");
        }
        // public constructor lets plain subclasses exist
        check(c.getConstructors().length == 1, "one public constructor");
        // --- appended deep coverage ------------------------------------------
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        Marshaller m = JAXBContext.newInstance(Person.class).createMarshaller();
        // default: no listener set, marshal works
        StringWriter sw = new StringWriter();
        m.marshal(p, sw);
        check(sw.toString().contains("<person>"), "marshal without listener works");
        // recording listener sees the before->after lifecycle
        Recording l = new Recording();
        m.setListener(l);
        check(m.getListener() == l, "getListener identity");
        calls.clear();
        m.marshal(p, new StringWriter());
        int before = 0, after = 0;
        for (String s : calls) if (s.startsWith("before")) before++; else after++;
        check(before >= 1, "beforeMarshal fired at least once, got " + before);
        check(after >= 1, "afterMarshal fired at least once, got " + after);
        check(calls.get(0).startsWith("before"), "first event is beforeMarshal");
        check(calls.get(calls.size() - 1).startsWith("after"), "last event is afterMarshal");
        check(calls.get(0).endsWith(":Person"), "beforeMarshal receives the marshalled object");
        check(calls.get(calls.size() - 1).endsWith(":Person"), "afterMarshal receives the marshalled object");
        // object mutations inside beforeMarshal are visible in the output (hook runs pre-serialization)
        class Mutating extends Marshaller.Listener {
            public void beforeMarshal(Object source) { ((Person) source).name = "mutated"; }
        }
        m.setListener(new Mutating());
        StringWriter sw2 = new StringWriter();
        m.marshal(p, sw2);
        check(sw2.toString().contains("<name>mutated</name>"), "beforeMarshal edits reach the output");
        // clearing the listener stops callbacks
        m.setListener(null);
        calls.clear();
        m.marshal(p, new StringWriter());
        check(calls.isEmpty(), "setListener(null) stops callbacks");
        // a bare Listener subclass (no overrides) behaves like no listener at all
        m.setListener(new Marshaller.Listener() {});
        StringWriter sw3 = new StringWriter();
        m.marshal(p, sw3);
        check(sw3.toString().contains("<person>"), "bare Listener subclass is a no-op");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
