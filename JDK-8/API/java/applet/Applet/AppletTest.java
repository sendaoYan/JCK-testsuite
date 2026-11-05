/*
 * @test
 * @summary java.applet.Applet conformance: structure + no-stub/stub-injection behavior loop.
 * @key headful
 * @run main AppletTest
 */

import java.applet.*;
import java.awt.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class AppletTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** recording AppletStub: every method records and returns deterministic values */
    static class RecStub implements AppletStub {
        final java.util.List<String> paramsAsked = new ArrayList<String>();
        final java.util.List<int[]> resizes = new ArrayList<int[]>();
        boolean activeFlag = true;
        final AppletContext ctx;
        RecStub(AppletContext ctx) { this.ctx = ctx; }
        public boolean isActive() { return activeFlag; }
        public URL getDocumentBase() { return url("http", "doc.example", "/doc/"); }
        public URL getCodeBase() { return url("http", "code.example", "/code/"); }
        public String getParameter(String name) { paramsAsked.add(name); return "v:" + name; }
        public AppletContext getAppletContext() { return ctx; }
        public void appletResize(int width, int height) { resizes.add(new int[]{width, height}); }
        static URL url(String proto, String host, String file) {
            try { return new URL(proto, host, file); } catch (MalformedURLException e) { throw new RuntimeException(e); }
        }
    }

    /** recording AppletContext */
    static class RecCtx implements AppletContext {
        final java.util.List<String> status = new ArrayList<String>();
        final java.util.List<URL> docs = new ArrayList<URL>();
        final Map<String, java.io.InputStream> streams = new HashMap<String, java.io.InputStream>();
        AudioClip lastClip; URL lastClipUrl; URL lastImageUrl;
        public AudioClip getAudioClip(URL url) { lastClipUrl = url; return lastClip; }
        public Image getImage(URL url) { lastImageUrl = url; return null; }
        public Applet getApplet(String name) { return null; }
        public Enumeration<Applet> getApplets() { return Collections.emptyEnumeration(); }
        public void showDocument(URL url) { docs.add(url); }
        public void showDocument(URL url, String target) { docs.add(url); }
        public void showStatus(String status) { this.status.add(status); }
        public void setStream(String key, java.io.InputStream stream) { streams.put(key, stream); }
        public java.io.InputStream getStream(String key) { return streams.get(key); }
        public Iterator<String> getStreamKeys() { return streams.keySet().iterator(); }
    }

    /** subclass exposing the protected setStub for the injection loop */
    public static class SubApplet extends Applet {
        public void inject(AppletStub s) { setStub(s); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.applet.Applet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Panel"), "superclass java.awt.Panel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setStub"), "method setStub");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "getDocumentBase"), "method getDocumentBase");
        check(hasMethod(c, "getCodeBase"), "method getCodeBase");
        check(hasMethod(c, "getParameter"), "method getParameter");
        check(hasMethod(c, "getAppletContext"), "method getAppletContext");
        check(hasMethod(c, "resize"), "method resize");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "showStatus"), "method showStatus");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "newAudioClip"), "method newAudioClip");
        check(hasMethod(c, "getAudioClip"), "method getAudioClip");
        check(hasMethod(c, "getAppletInfo"), "method getAppletInfo");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getParameterInfo"), "method getParameterInfo");
        check(hasMethod(c, "play"), "method play");
        check(hasMethod(c, "init"), "method init");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "stop"), "method stop");
        check(hasMethod(c, "destroy"), "method destroy");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        // setStub is public final (the tool-side injection seam) and newAudioClip is static
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("setStub")) {
                check(Modifier.isPublic(m.getModifiers()), "setStub is public");
                check(Modifier.isFinal(m.getModifiers()), "setStub is final");
                check(!Modifier.isStatic(m.getModifiers()), "setStub is an instance method");
            }
            if (m.getName().equals("newAudioClip"))
                check(Modifier.isStatic(m.getModifiers()), "newAudioClip is static");
        }
    }

    // ---- Part 2: black-box ----
    static void testBlackBoxStatic() throws Exception {
        // newAudioClip(null) -> NPE
        try {
            Applet.newAudioClip(null);
            check(false, "expected NPE for newAudioClip(null)");
        } catch (NullPointerException expected) {
            check(true, "newAudioClip(null) -> NullPointerException");
        }
        // a missing file still yields a usable clip whose operations are quiet
        AudioClip clip = Applet.newAudioClip(new URL("file:/nonexistent-applet-test.wav"));
        check(clip != null && clip instanceof AudioClip, "newAudioClip returns an AudioClip for a missing file");
        clip.play();
        clip.loop();
        clip.stop();
        check(true, "play/loop/stop on a missing clip are quiet");
    }

    static void testBlackBoxInstance() throws Exception {
        // headless environment: constructing an Applet throws HeadlessException
        if (GraphicsEnvironment.isHeadless()) {
            try {
                new SubApplet();
                check(false, "expected HeadlessException constructing an Applet headless");
            } catch (HeadlessException expected) {
                check(true, "headless construction -> HeadlessException");
            }
            return;
        }
        // ---- no-stub behavior ----
        SubApplet ap = new SubApplet();
        check(!ap.isActive(), "no-stub isActive() is false");
        try { ap.getParameter("p"); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getParameter -> NPE"); }
        try { ap.getAppletContext(); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getAppletContext -> NPE"); }
        try { ap.showStatus("s"); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub showStatus -> NPE"); }
        try { ap.getDocumentBase(); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getDocumentBase -> NPE"); }
        try { ap.getCodeBase(); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getCodeBase -> NPE"); }
        try { ap.getAudioClip(new URL("file:/x.au")); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getAudioClip -> NPE"); }
        try { ap.getImage(new URL("file:/x.png")); check(false, "expected NPE"); }
        catch (NullPointerException expected) { check(true, "no-stub getImage -> NPE"); }
        try { ap.getLocale(); check(false, "expected IllegalComponentStateException"); }
        catch (IllegalComponentStateException expected) { check(true, "no-stub getLocale -> IllegalComponentStateException"); }
        check(ap.getAppletInfo() == null, "getAppletInfo default null");
        check(ap.getParameterInfo() == null, "getParameterInfo default null");
        check(ap.isValidateRoot(), "applet is a validate root");
        ap.init(); ap.start(); ap.stop(); ap.destroy();
        check(true, "lifecycle methods are no-ops that do not throw");
        ap.resize(5, 6); // forwards only via a stub; without one it stays quiet
        check(true, "resize without a stub does not throw");

        // ---- stub injection loop ----
        RecCtx ctx = new RecCtx();
        RecStub stub = new RecStub(ctx);
        ap.inject(stub);
        // getParameter forwards, records the key and returns the stub's value
        check("v:color".equals(ap.getParameter("color")), "getParameter forwards to the stub");
        check(stub.paramsAsked.equals(Arrays.asList("color")), "stub saw exactly the requested key");
        // bases forward
        check("http://doc.example/doc/".equals(ap.getDocumentBase().toString()), "getDocumentBase forwards");
        check("http://code.example/code/".equals(ap.getCodeBase().toString()), "getCodeBase forwards");
        // isActive forwards and tracks the stub's live flag
        check(ap.isActive(), "isActive forwards (true)");
        stub.activeFlag = false;
        check(!ap.isActive(), "isActive forwards (false)");
        // both resize overloads forward to appletResize
        ap.resize(101, 202);
        check(Arrays.equals(stub.resizes.get(0), new int[]{101, 202}), "resize(int,int) forwards to appletResize");
        ap.resize(new Dimension(33, 44));
        check(stub.resizes.size() == 2 && Arrays.equals(stub.resizes.get(1), new int[]{33, 44}),
                "resize(Dimension) forwards to appletResize");
        // showStatus forwards through the stub's AppletContext
        ap.showStatus("hello");
        check(ctx.status.equals(Arrays.asList("hello")), "showStatus forwards to the context");
        check(ap.getAppletContext() == ctx, "getAppletContext returns the stub's context");
        // showDocument reachable from the injected context
        ap.getAppletContext().showDocument(new URL("http://x.example/y"));
        check(ctx.docs.size() == 1 && "http://x.example/y".equals(ctx.docs.get(0).toString()),
                "context showDocument records the URL");
        // getAudioClip/getImage forward with the requested URL; play is quiet
        AudioClip sent = Applet.newAudioClip(new URL("file:/a.au"));
        ctx.lastClip = sent;
        check(ap.getAudioClip(new URL("file:/a.au")) == sent, "getAudioClip forwards to the context");
        check("file:/a.au".equals(ctx.lastClipUrl.toString()), "context received the audio URL");
        check(ap.getImage(new URL("file:/a.gif")) == null, "getImage forwards to the context");
        check("file:/a.gif".equals(ctx.lastImageUrl.toString()), "context received the image URL");
        ap.play(new URL("file:/a.au"));
        ap.play(new URL("file:/a.au"), "chime");
        check(true, "play(url) / play(url, name) are quiet");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBoxStatic();
        testBlackBoxInstance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
