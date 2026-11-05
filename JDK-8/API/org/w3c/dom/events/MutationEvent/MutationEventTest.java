/*
 * @test
 * @summary org.w3c.dom.events.MutationEvent conformance: reflection shape checks.
 * @run main MutationEventTest
 */

import java.lang.reflect.*;

public class MutationEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.events.MutationEvent");
        check(c != null, "org.w3c.dom.events.MutationEvent loads");
        check(c.getName().equals("org.w3c.dom.events.MutationEvent"), "binary name is org.w3c.dom.events.MutationEvent");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.events.MutationEvent is public");
        check(c.isInterface(), "org.w3c.dom.events.MutationEvent is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.events.Event")), "direct superinterfaces");
        check(declares(c, "getRelatedNode"), "declares getRelatedNode");
        check(declares(c, "getPrevValue"), "declares getPrevValue");
        check(declares(c, "getNewValue"), "declares getNewValue");
        check(declares(c, "getAttrName"), "declares getAttrName");
        check(declares(c, "getAttrChange"), "declares getAttrChange");
        check(declares(c, "initMutationEvent"), "declares initMutationEvent");
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
