/*
 * @test
 * @summary org.w3c.dom.ls.LSProgressEvent conformance: progress event.
 * @run main LSProgressEventTest
 */

public class LSProgressEventTest {
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

        Class<?> k = L("org.w3c.dom.ls.LSProgressEvent");
        check(k.isInterface(), "org.w3c.dom.ls.LSProgressEvent is an interface");
        check(L("org.w3c.dom.events.Event").isAssignableFrom(k), "LSProgressEvent extends Event");
        check(k.getInterfaces().length >= 1, "LSProgressEvent has superinterface");
        check(k.getMethod("getInput") != null, "getInput()");
        check(k.getMethod("getPosition") != null, "getPosition()");
        check(k.getMethod("getTotalSize") != null, "getTotalSize()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSProgressEvent a = new org.w3c.dom.ls.LSProgressEvent(){
            public org.w3c.dom.ls.LSInput getInput() { return null; }
            public int getPosition() { return 3; }
            public int getTotalSize() { return 3; }
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
        check(a != null, "anon impl of LSProgressEvent");
        check(a.getInput() == null, "getInput() returns fixed default");
        check(a.getPosition() == 3, "getPosition() returns fixed default");
        check(a.getTotalSize() == 3, "getTotalSize() returns fixed default");
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

    // ---- deepening round 2: LSProgressEvent contract (shape + producer absence) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.Event.class.isAssignableFrom(org.w3c.dom.ls.LSProgressEvent.class), "LSProgressEvent extends Event");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.ls.LSProgressEvent.class.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getPosition") && names.contains("getTotalSize") && names.contains("getInput"), "getPosition/getTotalSize/getInput declared");
        check(names.size() == 3, "LSProgressEvent declares exactly three methods");
        check(org.w3c.dom.ls.LSProgressEvent.class.getDeclaredFields().length == 0, "LSProgressEvent declares no constants");
        org.w3c.dom.ls.LSProgressEvent ev = (org.w3c.dom.ls.LSProgressEvent) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.ls.LSProgressEvent.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.ls.LSProgressEvent.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("getPosition")) return 7;
                    if (m.getName().equals("getTotalSize")) return 100;
                    return null;
                }
            });
        check(ev.getPosition() == 7 && ev.getTotalSize() == 100, "proxy LSProgressEvent routes the progress counters");
        check(ev.getInput() == null, "proxy LSProgressEvent default null for un-routed getters");
        // synchronous parsing never produces progress events
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        org.w3c.dom.ls.LSInput in = ls.createLSInput(); in.setStringData("<r/>");
        check(p.parse(in) != null, "sync parse completes (progress notifications are async-only)");
    }
}
