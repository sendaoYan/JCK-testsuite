/*
 * @test
 * @summary javax.swing.text.html.FormSubmitEvent conformance: MethodType enum
 *          contract, package-private construction and accessor behavior
 *          (headless-safe; reflective access needs --add-opens on JDK-9+).
 * @run main/othervm --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED FormSubmitEventTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.FormSubmitEvent.MethodType;

public class FormSubmitEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormSubmitEvent", false, FormSubmitEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLFrameHyperlinkEvent"), "superclass");
        // version divergence: FormSubmitEvent became final in JDK-9+
        if (System.getProperty("java.version").startsWith("1.8")) {
            check(Modifier.isFinal(c.getModifiers()) == false, "JDK-8 not final");
        } else {
            check(Modifier.isFinal(c.getModifiers()) == true, "JDK-9+ final");
        }
        check(Modifier.isPublic(c.getModifiers()) == true, "public modifier");
        check(c.getConstructors().length == 0, "no public constructors");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMethod"), "method getMethod");
        check(names.contains("getData"), "method getData");
    }

    // ---- Part 2: MethodType enum contract ----
    static void testMethodType() {
        MethodType[] vals = MethodType.values();
        check(vals.length == 2, "two method types");
        check(vals[0] == MethodType.GET && vals[1] == MethodType.POST, "declaration order GET, POST");
        check(MethodType.GET.ordinal() == 0, "GET ordinal 0");
        check(MethodType.POST.ordinal() == 1, "POST ordinal 1");
        check("GET".equals(MethodType.GET.name()), "GET name");
        check("POST".equals(MethodType.POST.name()), "POST name");
        check("GET".equals(MethodType.GET.toString()), "GET toString");
        check(MethodType.GET.compareTo(MethodType.POST) < 0, "GET < POST");
        check(MethodType.valueOf("GET") == MethodType.GET, "valueOf GET");
        check(MethodType.valueOf("POST") == MethodType.POST, "valueOf POST");
        boolean iae = false;
        try { MethodType.valueOf("no-such"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf(unknown) IllegalArgumentException");
        boolean npe = false;
        try { MethodType.valueOf(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "valueOf(null) NPE");
        check("javax.swing.text.html.FormSubmitEvent$MethodType".equals(MethodType.GET.getDeclaringClass().getName()), "declaring class");
        check(Arrays.toString(MethodType.values()).equals("[GET, POST]"), "values toString");
        MethodType[] again = MethodType.values();
        check(again != vals, "values returns fresh array each call");
    }

    // ---- Part 3: package-private constructor via reflection ----
    static void testReflectiveConstruction() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.FormSubmitEvent");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared constructor");
        Constructor<?> ctor = ctors[0];
        check(ctor.getParameterCount() == 7, "constructor has 7 parameters");
        ctor.setAccessible(true);

        Object source = new Object();
        HyperlinkEvent.EventType type = HyperlinkEvent.EventType.ACTIVATED;
        URL url = new URL("http://example.com/submit");
        Element elem = null;
        FormSubmitEvent ev = (FormSubmitEvent) ctor.newInstance(
                source, type, url, elem, "targetFrame", MethodType.POST, "name=foo&value=bar");

        check(ev.getMethod() == MethodType.POST, "getMethod POST");
        check("name=foo&value=bar".equals(ev.getData()), "getData round-trip");
        check("targetFrame".equals(ev.getTarget()), "getTarget from ctor");
        check(url.equals(ev.getURL()), "getURL from ctor");
        check(ev.getEventType() == type, "event type from ctor");
        check(ev.getSource() == source, "source from ctor");

        FormSubmitEvent ev2 = (FormSubmitEvent) ctor.newInstance(
                source, type, url, elem, "f2", MethodType.GET, "");
        check(ev2.getMethod() == MethodType.GET, "getMethod GET");
        check("".equals(ev2.getData()), "empty data kept");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testMethodType();
        testReflectiveConstruction();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
