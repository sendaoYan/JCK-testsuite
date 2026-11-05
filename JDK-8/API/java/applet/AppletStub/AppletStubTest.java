/*
 * @test
 * @summary java.applet.AppletStub conformance: interface face + contract exercised via implementation.
 * @run main AppletStubTest
 */

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.applet.AppletStub;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class AppletStubTest {
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

    /** recording implementation exercising the full contract */
    static class RecStub implements AppletStub {
        boolean active;
        final URL docBase = url("http://doc.example/doc/");
        final URL codeBase = url("http://code.example/code/");
        final Map<String, String> params = new HashMap<String, String>();
        final List<int[]> resizes = new ArrayList<int[]>();
        final AppletContext ctx;
        RecStub(AppletContext ctx) { this.ctx = ctx; params.put("width", "100"); }
        public boolean isActive() { return active; }
        public URL getDocumentBase() { return docBase; }
        public URL getCodeBase() { return codeBase; }
        public String getParameter(String name) { return params.get(name); }
        public AppletContext getAppletContext() { return ctx; }
        public void appletResize(int width, int height) { resizes.add(new int[]{width, height}); }
    }

    static class RecCtx implements AppletContext {
        public AudioClip getAudioClip(URL url) { return null; }
        public java.awt.Image getImage(URL url) { return null; }
        public Applet getApplet(String name) { return null; }
        public Enumeration<Applet> getApplets() { return null; }
        public void showDocument(URL url) {}
        public void showDocument(URL url, String target) {}
        public void showStatus(String status) {}
        public void setStream(String key, java.io.InputStream stream) {}
        public java.io.InputStream getStream(String key) { return null; }
        public Iterator<String> getStreamKeys() { return null; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.applet.AppletStub");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "getDocumentBase"), "method getDocumentBase");
        check(hasMethod(c, "getCodeBase"), "method getCodeBase");
        check(hasMethod(c, "getParameter"), "method getParameter");
        check(hasMethod(c, "getAppletContext"), "method getAppletContext");
        check(hasMethod(c, "appletResize"), "method appletResize");
        check(c.getDeclaredMethods().length == 6, "exactly 6 declared methods (got " + c.getDeclaredMethods().length + ")");
        // exact signatures
        check(hasOwn(c, "isActive"), "boolean isActive()");
        check(c.getDeclaredMethod("isActive").getReturnType() == boolean.class, "isActive returns boolean");
        check(hasOwn(c, "getDocumentBase"), "URL getDocumentBase()");
        check(c.getDeclaredMethod("getDocumentBase").getReturnType() == URL.class, "getDocumentBase returns URL");
        check(hasOwn(c, "getCodeBase"), "URL getCodeBase()");
        check(hasOwn(c, "getParameter", String.class), "String getParameter(String)");
        check(hasOwn(c, "getAppletContext"), "AppletContext getAppletContext()");
        check(hasOwn(c, "appletResize", int.class, int.class), "void appletResize(int, int)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
            check(m.getExceptionTypes().length == 0, "method " + m.getName() + " declares no checked exceptions");
        }
    }

    // ---- Part 2: black-box (contract exercised via a recording implementation) ----
    static void testBlackBox() {
        RecCtx ctx = new RecCtx();
        RecStub s = new RecStub(ctx);
        check(!s.isActive(), "fresh stub reports inactive");
        s.active = true;
        check(s.isActive(), "activated stub reports active");
        check("http://doc.example/doc/".equals(s.getDocumentBase().toString()), "getDocumentBase value");
        check("http://code.example/code/".equals(s.getCodeBase().toString()), "getCodeBase value");
        check("100".equals(s.getParameter("width")), "getParameter present key");
        check(s.getParameter("missing") == null, "getParameter missing key -> null");
        check(s.getAppletContext() == ctx, "getAppletContext returns the wired context");
        s.appletResize(320, 200);
        s.appletResize(640, 480);
        check(s.resizes.size() == 2 && Arrays.equals(s.resizes.get(1), new int[]{640, 480}),
                "appletResize records each call");
        // distinct stub instances hold independent state
        RecStub s2 = new RecStub(ctx);
        check(s2.isActive() != s.isActive(), "stub state is per instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
