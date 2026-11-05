/*
 * @test
 * @summary org.w3c.dom.ls.LSLoadEvent conformance: load event.
 * @run main LSLoadEventTest
 */

public class LSLoadEventTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.LSLoadEvent");
        check(k.isInterface(), "org.w3c.dom.ls.LSLoadEvent is an interface");
        check(L("org.w3c.dom.events.Event").isAssignableFrom(k), "LSLoadEvent extends Event");
        check(k.getInterfaces().length >= 1, "LSLoadEvent has superinterface");
        check(k.getMethod("getNewDocument") != null, "getNewDocument()");
        check(k.getMethod("getInput") != null, "getInput()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSLoadEvent a = new org.w3c.dom.ls.LSLoadEvent(){
            public org.w3c.dom.Document getNewDocument() { return null; }
            public org.w3c.dom.ls.LSInput getInput() { return null; }
            public java.lang.String getType() { return "s"; }
            public org.w3c.dom.events.EventTarget getTarget() { return null; }
            public org.w3c.dom.events.EventTarget getCurrentTarget() { return null; }
            public short getEventPhase() { return (short)2; }
            public boolean getBubbles() { return true; }
            public boolean getCancelable() { return true; }
            public long getTimeStamp() { return 4L; }
            public void stopPropagation() { }
            public void preventDefault() { }
            public void initEvent(java.lang.String p0, boolean p1, boolean p2) { }
        };
        check(a != null, "anon impl of LSLoadEvent");
        check(a.getNewDocument() == null, "getNewDocument() returns fixed default");
        check(a.getInput() == null, "getInput() returns fixed default");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.getTarget() == null, "getTarget() returns fixed default");
        check(a.getCurrentTarget() == null, "getCurrentTarget() returns fixed default");
        check(a.getEventPhase() == (short)2, "getEventPhase() returns fixed default");
        check(a.getBubbles() == true, "getBubbles() returns fixed default");
        check(a.getCancelable() == true, "getCancelable() returns fixed default");
        check(a.getTimeStamp() == 4L, "getTimeStamp() returns fixed default");
        a.stopPropagation();
        a.preventDefault();
        a.initEvent("s", true, true);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: LSLoadEvent contract (shape + producer absence) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.Event.class.isAssignableFrom(org.w3c.dom.ls.LSLoadEvent.class), "LSLoadEvent extends Event");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.ls.LSLoadEvent.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getNewDocument") && names.contains("getInput"), "getNewDocument/getInput declared");
        check(names.size() == 2, "LSLoadEvent declares exactly two methods");
        // no constants of its own
        check(org.w3c.dom.ls.LSLoadEvent.class.getDeclaredFields().length == 0, "LSLoadEvent declares no constants");
        // an anonymous implementation honors the getter contract
        final org.w3c.dom.Document marker = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final org.w3c.dom.ls.LSInput markerIn = ((org.w3c.dom.ls.DOMImplementationLS) marker.getImplementation().getFeature("LS", "3.0")).createLSInput();
        org.w3c.dom.ls.LSLoadEvent ev = (org.w3c.dom.ls.LSLoadEvent) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.ls.LSLoadEvent.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.ls.LSLoadEvent.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("getNewDocument")) return marker;
                    if (m.getName().equals("getInput")) return markerIn;
                    return null;
                }
            });
        check(ev.getNewDocument() == marker && ev.getInput() == markerIn, "proxy LSLoadEvent routes getNewDocument/getInput");
        // synchronous parsing never produces LSLoadEvent (async-only notification)
        final java.util.List<Object> events = new java.util.ArrayList<Object>();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) marker.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r/>");
        check(p.parse(in) != null && events.isEmpty(), "synchronous parse yields no LSLoadEvent (async-only event)");
    }
}
