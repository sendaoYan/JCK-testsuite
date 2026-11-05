/*
 * @test
 * @summary java.applet.AppletContext conformance: interface face + stream/document contracts.
 * @key headful
 * @run main AppletContextTest
 */

import java.applet.Applet;
import java.applet.AudioClip;
import java.applet.AppletContext;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class AppletContextTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static URL url(String spec) {
        try { return new URL(spec); } catch (MalformedURLException e) { throw new RuntimeException(e); }
    }

    /** in-memory implementation exercising the full contract */
    static class MapContext implements AppletContext {
        final Map<String, InputStream> streams = new LinkedHashMap<String, InputStream>();
        final List<String> status = new ArrayList<String>();
        final List<URL> documents = new ArrayList<URL>();
        final Map<String, Applet> applets = new HashMap<String, Applet>();
        public AudioClip getAudioClip(URL url) { return null; }
        public java.awt.Image getImage(URL url) { return null; }
        public Applet getApplet(String name) { return applets.get(name); }
        public Enumeration<Applet> getApplets() { return Collections.enumeration(applets.values()); }
        public void showDocument(URL url) { documents.add(url); }
        public void showDocument(URL url, String target) { documents.add(url); }
        public void showStatus(String status) { this.status.add(status); }
        public void setStream(String key, InputStream stream) {
            if (stream == null) streams.remove(key); else streams.put(key, stream);
        }
        public InputStream getStream(String key) { return streams.get(key); }
        public Iterator<String> getStreamKeys() { return streams.keySet().iterator(); }
    }

    static class FakeApplet extends Applet {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.applet.AppletContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getAudioClip"), "method getAudioClip");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "getApplet"), "method getApplet");
        check(hasMethod(c, "getApplets"), "method getApplets");
        check(hasMethod(c, "showDocument"), "method showDocument");
        check(hasMethod(c, "showStatus"), "method showStatus");
        check(hasMethod(c, "setStream"), "method setStream");
        check(hasMethod(c, "getStream"), "method getStream");
        check(hasMethod(c, "getStreamKeys"), "method getStreamKeys");
        check(c.getDeclaredMethods().length == 10, "exactly 10 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(c.getDeclaredMethod("setStream", String.class, InputStream.class).getExceptionTypes().length == 1
                && c.getDeclaredMethod("setStream", String.class, InputStream.class).getExceptionTypes()[0] == IOException.class,
                "setStream throws IOException");
        check(hasOwn(c, "getAudioClip", URL.class), "AudioClip getAudioClip(URL)");
        check(hasOwn(c, "getImage", URL.class), "Image getImage(URL)");
        check(hasOwn(c, "getApplet", String.class), "Applet getApplet(String)");
        check(hasOwn(c, "getApplets"), "Enumeration<Applet> getApplets()");
        check(hasOwn(c, "showDocument", URL.class), "void showDocument(URL)");
        check(hasOwn(c, "showDocument", URL.class, String.class), "void showDocument(URL, String)");
        check(hasOwn(c, "showStatus", String.class), "void showStatus(String)");
        check(hasOwn(c, "setStream", String.class, InputStream.class), "void setStream(String, InputStream)");
        check(hasOwn(c, "getStream", String.class), "InputStream getStream(String)");
        check(hasOwn(c, "getStreamKeys"), "Iterator<String> getStreamKeys()");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
    }

    // ---- Part 2: black-box (contract exercised via an in-memory context) ----
    static void testBlackBox() {
        MapContext ctx = new MapContext();
        check(ctx.getAudioClip(url("file:/a.au")) == null, "getAudioClip answered by the context");
        check(ctx.getImage(url("file:/a.png")) == null, "getImage answered by the context");
        // applet lookup: by name, and the enumeration over all applets
        FakeApplet a1 = new FakeApplet(), a2 = new FakeApplet();
        ctx.applets.put("one", a1);
        ctx.applets.put("two", a2);
        check(ctx.getApplet("one") == a1, "getApplet(name) finds the named applet");
        check(ctx.getApplet("nope") == null, "getApplet(unknown) -> null");
        Set<Applet> seen = new HashSet<Applet>();
        Enumeration<Applet> e = ctx.getApplets();
        while (e.hasMoreElements()) seen.add(e.nextElement());
        check(seen.size() == 2 && seen.contains(a1) && seen.contains(a2), "getApplets enumerates all applets");
        // showStatus / showDocument recording
        ctx.showStatus("loading");
        check(ctx.status.equals(Arrays.asList("loading")), "showStatus recorded");
        ctx.showDocument(url("http://x.example/1"));
        ctx.showDocument(url("http://x.example/2"), "_blank");
        check(ctx.documents.size() == 2, "both showDocument overloads are available");
        // stream key/value store semantics
        check(ctx.getStream("k") == null, "getStream(missing) -> null");
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        ctx.setStream("k", in);
        check(ctx.getStream("k") == in, "setStream/getStream round trip");
        List<String> keys = new ArrayList<String>();
        Iterator<String> it = ctx.getStreamKeys();
        while (it.hasNext()) keys.add(it.next());
        check(keys.equals(Arrays.asList("k")), "getStreamKeys reflects the stored keys");
        // replacement and removal-by-null semantics of the store contract
        InputStream other = new ByteArrayInputStream(new byte[0]);
        ctx.setStream("k", other);
        check(ctx.getStream("k") == other, "setStream replaces an existing stream");
        ctx.setStream("k", null);
        check(ctx.getStream("k") == null, "setStream(key, null) clears the entry");
        keys.clear();
        it = ctx.getStreamKeys();
        while (it.hasNext()) keys.add(it.next());
        check(keys.isEmpty(), "cleared entry disappears from getStreamKeys");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
