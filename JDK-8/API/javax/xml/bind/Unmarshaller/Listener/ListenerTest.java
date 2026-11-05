/*
 * @test
 * @summary javax.xml.bind.Unmarshaller.Listener conformance: no-op hook defaults,
 *          subclass recording lifecycle, and unmarshal integration.
 * @run main ListenerTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>alice</name><age>30</age></person>";

    static final List<String> calls = new ArrayList<String>();

    static class Recording extends Unmarshaller.Listener {
        public void beforeUnmarshal(Object target, Object parent) {
            calls.add("before:" + tag(target) + "/" + tag(parent));
        }
        public void afterUnmarshal(Object target, Object parent) {
            calls.add("after:" + tag(target) + "/" + tag(parent));
        }
    }

    static String tag(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Unmarshaller$Listener");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("beforeUnmarshal"), "has method beforeUnmarshal");
        check(names.contains("afterUnmarshal"), "has method afterUnmarshal");
        // exactly the two hooks, both concrete no-ops, both (Object,Object)
        check(c.getDeclaredMethods().length == 2, "exactly two declared hooks");
        for (Method m : c.getDeclaredMethods()) {
            check(!Modifier.isAbstract(m.getModifiers()), m.getName() + " is a concrete no-op default");
            check(m.getReturnType() == void.class, m.getName() + " returns void");
            check(m.getParameterTypes().length == 2
                  && m.getParameterTypes()[0] == Object.class
                  && m.getParameterTypes()[1] == Object.class,
                  m.getName() + " takes (target, parent)");
        }
        check(c.getConstructors().length == 1, "one public constructor");
        // --- appended deep coverage ------------------------------------------
        Unmarshaller u = JAXBContext.newInstance(Person.class).createUnmarshaller();
        Person plain = (Person) u.unmarshal(new StringReader(XML));
        check("alice".equals(plain.name) && plain.age == 30, "unmarshal without listener works");
        Recording l = new Recording();
        u.setListener(l);
        check(u.getListener() == l, "getListener identity");
        calls.clear();
        Person p = (Person) u.unmarshal(new StringReader(XML));
        int before = 0, after = 0;
        for (String s : calls) if (s.startsWith("before")) before++; else after++;
        check(before >= 1, "beforeUnmarshal fired at least once, got " + before);
        check(after >= 1, "afterUnmarshal fired at least once, got " + after);
        check(calls.get(0).startsWith("before"), "first event is beforeUnmarshal");
        check(calls.get(calls.size() - 1).startsWith("after"), "last event is afterUnmarshal");
        check(calls.get(0).contains(":Person"), "beforeUnmarshal receives the target object");
        // for a root object the parent parameter is null
        check(calls.get(0).endsWith("/null"), "root object has null parent (measured)");
        // afterUnmarshal edits are visible in the returned object (hook runs post-population)
        class Fixing extends Unmarshaller.Listener {
            public void afterUnmarshal(Object target, Object parent) {
                ((Person) target).name = "fixed";
            }
        }
        u.setListener(new Fixing());
        Person fixed = (Person) u.unmarshal(new StringReader(XML));
        check("fixed".equals(fixed.name), "afterUnmarshal edits reach the result");
        // clearing stops callbacks
        u.setListener(null);
        calls.clear();
        u.unmarshal(new StringReader(XML));
        check(calls.isEmpty(), "setListener(null) stops callbacks");
        // bare subclass is a no-op
        u.setListener(new Unmarshaller.Listener() {});
        Person bare = (Person) u.unmarshal(new StringReader(XML));
        check("alice".equals(bare.name), "bare Listener subclass is a no-op");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
