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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
