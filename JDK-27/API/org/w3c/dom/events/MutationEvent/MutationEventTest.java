/*
 * @test
 * @summary org.w3c.dom.events.MutationEvent conformance: interface extends Event.
 * @run main MutationEventTest
 */

import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class MutationEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(MutationEvent.class.isInterface(), "MutationEvent is an interface");
        check(Event.class.isAssignableFrom(MutationEvent.class), "extends Event");
        check(MutationEvent.MODIFICATION == 1, "MODIFICATION == 1");
        check(MutationEvent.ADDITION == 2, "ADDITION == 2");
        check(MutationEvent.REMOVAL == 3, "REMOVAL == 3");
        check(MutationEvent.class.getMethod("getAttrChange") != null, "getAttrChange()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.events.MutationEvent.MODIFICATION == 1, "MODIFICATION == 1");
        check(org.w3c.dom.events.MutationEvent.ADDITION == 2, "ADDITION == 2");
        check(org.w3c.dom.events.MutationEvent.REMOVAL == 3, "REMOVAL == 3");
        org.w3c.dom.events.MutationEvent a = new org.w3c.dom.events.MutationEvent(){
            public org.w3c.dom.Node getRelatedNode() { return null; }
            public java.lang.String getPrevValue() { return "s"; }
            public java.lang.String getNewValue() { return "s"; }
            public java.lang.String getAttrName() { return "s"; }
            public short getAttrChange() { return (short)2; }
            public void initMutationEvent(java.lang.String p0, boolean p1, boolean p2, org.w3c.dom.Node p3, java.lang.String p4, java.lang.String p5, java.lang.String p6, short p7) { }
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
        check(a != null, "anon impl of MutationEvent");
        check(a.getRelatedNode() == null, "getRelatedNode() returns fixed default");
        check("s".equals(a.getPrevValue()), "getPrevValue() returns fixed default");
        check("s".equals(a.getNewValue()), "getNewValue() returns fixed default");
        check("s".equals(a.getAttrName()), "getAttrName() returns fixed default");
        check(a.getAttrChange() == (short)2, "getAttrChange() returns fixed default");
        a.initMutationEvent("s", true, true, (org.w3c.dom.Node) null, "s", "s", "s", (short)2);
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

    // ---- deepening round 2: behavioral MutationEvent contract (factory + init + real DOM triggers) ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.events.MutationEvent.MODIFICATION == 1, "MODIFICATION = 1");
        check(org.w3c.dom.events.MutationEvent.ADDITION == 2, "ADDITION = 2");
        check(org.w3c.dom.events.MutationEvent.REMOVAL == 3, "REMOVAL = 3");
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        final org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        root.setAttribute("k", "0");
        root.appendChild(d.createTextNode("txt"));
        org.w3c.dom.events.DocumentEvent de = (org.w3c.dom.events.DocumentEvent) d;
        org.w3c.dom.events.MutationEvent me = (org.w3c.dom.events.MutationEvent) de.createEvent("MutationEvent");
        me.initMutationEvent("m", true, false, root, "old", "new", "k", org.w3c.dom.events.MutationEvent.MODIFICATION);
        check("m".equals(me.getType()) && me.getBubbles() && !me.getCancelable(), "initMutationEvent basic flags");
        check(me.getRelatedNode() == root, "initMutationEvent relatedNode");
        check("old".equals(me.getPrevValue()) && "new".equals(me.getNewValue()), "prev/new values round-trip");
        check("k".equals(me.getAttrName()) && me.getAttrChange() == org.w3c.dom.events.MutationEvent.MODIFICATION, "attrName/attrChange round-trip");
        me.initMutationEvent("m2", false, false, null, null, null, null, (short) 0);
        check(me.getRelatedNode() == null && me.getPrevValue() == null && me.getNewValue() == null && me.getAttrName() == null, "re-init with nulls clears fields");
        // real DOM triggers
        final String[] attr = new String[]{null};
        final short[] change = new short[]{-1};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMAttrModified", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) {
                org.w3c.dom.events.MutationEvent m = (org.w3c.dom.events.MutationEvent) e;
                attr[0] = m.getAttrName() + ":" + m.getPrevValue() + "->" + m.getNewValue();
                change[0] = m.getAttrChange();
            }
        }, false);
        root.setAttribute("k", "1");
        check("k:0->1".equals(attr[0]) && change[0] == org.w3c.dom.events.MutationEvent.MODIFICATION, "setAttribute fires DOMAttrModified (MODIFICATION)");
        final String[] inserted = new String[]{null};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMNodeInserted", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { inserted[0] = ((org.w3c.dom.Node) e.getTarget()).getNodeName(); }
        }, false);
        root.appendChild(d.createElement("added"));
        check("added".equals(inserted[0]), "appendChild fires DOMNodeInserted");
        final String[] cdModified = new String[]{null};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMCharacterDataModified", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) {
                org.w3c.dom.events.MutationEvent m = (org.w3c.dom.events.MutationEvent) e;
                cdModified[0] = m.getPrevValue() + "->" + m.getNewValue();
            }
        }, false);
        root.getFirstChild().setNodeValue("TXT");
        check("txt->TXT".equals(cdModified[0]), "text mutation fires DOMCharacterDataModified");
        // attribute removal reports REMOVAL
        final short[] removed = new short[]{-1};
        ((org.w3c.dom.events.EventTarget) root).addEventListener("DOMAttrModified", new org.w3c.dom.events.EventListener() {
            public void handleEvent(org.w3c.dom.events.Event e) { if (((org.w3c.dom.events.MutationEvent) e).getNewValue() == null) removed[0] = ((org.w3c.dom.events.MutationEvent) e).getAttrChange(); }
        }, false);
        root.removeAttribute("k");
        check(removed[0] == org.w3c.dom.events.MutationEvent.REMOVAL, "removeAttribute fires DOMAttrModified (REMOVAL)");
    }
}
