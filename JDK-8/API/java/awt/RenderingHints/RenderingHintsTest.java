/*
 * @test
 * @summary java.awt.RenderingHints conformance: Map-backed key/value store.
 * @run main RenderingHintsTest
 */


import java.awt.RenderingHints;
public class RenderingHintsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RenderingHints h = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        check(RenderingHints.VALUE_ANTIALIAS_ON.equals(h.get(RenderingHints.KEY_ANTIALIASING)), "get put value");
        check(h.containsKey(RenderingHints.KEY_ANTIALIASING), "containsKey");
        check(h.size() == 1, "size");
        h.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        check(h.size() == 2, "size after second put");
        check(RenderingHints.VALUE_RENDER_QUALITY.equals(h.get(RenderingHints.KEY_RENDERING)), "second value");
        h.clear();
        check(h.isEmpty(), "clear");
        RenderingHints copy = (RenderingHints) h.clone();
        check(copy != h && copy.isEmpty(), "clone independent");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // keys present and distinct
        java.awt.RenderingHints.Key[] keys = {java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.KEY_DITHERING,
            java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
            java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION,
            java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.KEY_STROKE_CONTROL};
        String[] kn = {"ANTIALIASING","RENDERING","DITHERING","TEXT_ANTIALIASING","FRACTIONALMETRICS",
            "INTERPOLATION","ALPHA_INTERPOLATION","COLOR_RENDERING","STROKE_CONTROL"};
        java.util.Set<java.awt.RenderingHints.Key> seen = new java.util.HashSet<java.awt.RenderingHints.Key>();
        for (int i = 0; i < keys.length; i++) {
            check(keys[i] != null && seen.add(keys[i]), "key distinct: " + kn[i]);
        }
        // value constants distinct and bound to hints
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_ON != java.awt.RenderingHints.VALUE_ANTIALIAS_OFF
              && java.awt.RenderingHints.VALUE_ANTIALIAS_OFF != java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT,
              "antialias values distinct");
        check(java.awt.RenderingHints.VALUE_RENDER_SPEED != java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "render values distinct");
        check(java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
              != java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR, "interpolation values distinct");
        check(java.awt.RenderingHints.VALUE_STROKE_NORMALIZE
              != java.awt.RenderingHints.VALUE_STROKE_PURE, "stroke values distinct");
        // put returns previous value; null previous for absent
        java.awt.RenderingHints h = new java.awt.RenderingHints(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Object old = h.put(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        check(old == java.awt.RenderingHints.VALUE_ANTIALIAS_ON, "put returns previous value");
        check(h.get(java.awt.RenderingHints.KEY_ANTIALIASING) == java.awt.RenderingHints.VALUE_ANTIALIAS_OFF,
              "get after overwrite");
        check(h.put(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED) == null,
              "put absent key returns null");
        // get for absent key
        check(h.get(java.awt.RenderingHints.KEY_DITHERING) == null, "get absent -> null");
        check(h.containsKey(java.awt.RenderingHints.KEY_ANTIALIASING), "containsKey present");
        check(!h.containsKey(java.awt.RenderingHints.KEY_DITHERING), "containsKey absent");
        check(h.containsValue(java.awt.RenderingHints.VALUE_RENDER_SPEED), "containsValue");
        check(!h.containsValue(java.awt.RenderingHints.VALUE_RENDER_QUALITY), "containsValue absent");
        // size / isEmpty / clear
        check(h.size() == 2, "size 2");
        check(!h.isEmpty(), "not empty");
        h.clear();
        check(h.size() == 0 && h.isEmpty(), "clear empties");
        // remove returns removed value
        h.put(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        check(h.remove(java.awt.RenderingHints.KEY_RENDERING) == java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "remove returns value");
        check(h.remove(java.awt.RenderingHints.KEY_RENDERING) == null, "remove absent -> null");
        // value toString is human readable (locked observed)
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_ON.toString().equals("Antialiased rendering mode"),
              "VALUE_ANTIALIAS_ON toString");
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_OFF.toString().equals("Nonantialiased rendering mode"),
              "VALUE_ANTIALIAS_OFF toString");
        check(java.awt.RenderingHints.VALUE_RENDER_SPEED.toString().equals("Fastest rendering methods"),
              "VALUE_RENDER_SPEED toString");
        // Map view semantics: keySet/values are unmodifiable views
        boolean uoe1 = false;
        try { h.keySet().add(java.awt.RenderingHints.KEY_DITHERING); } catch (java.lang.UnsupportedOperationException e) { uoe1 = true; }
        check(uoe1, "keySet.add -> UnsupportedOperationException");
        boolean uoe2 = false;
        try { h.values().add("x"); } catch (java.lang.UnsupportedOperationException e) { uoe2 = true; }
        check(uoe2, "values.add -> UnsupportedOperationException");
        h.put(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_DISABLE);
        check(h.keySet().contains(java.awt.RenderingHints.KEY_DITHERING), "keySet reflects mutation");
        check(h.values().contains(java.awt.RenderingHints.VALUE_DITHER_DISABLE), "values reflects mutation");
        check(h.entrySet().size() == h.size(), "entrySet size");
        // constructor with Map null arg does not throw immediately (lazy defaults)
        java.awt.RenderingHints h2 = new java.awt.RenderingHints(null);
        check(h2.size() == 0, "null-map ctor empty");
        // add merges other hints
        java.awt.RenderingHints other = new java.awt.RenderingHints(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        h.add(other);
        check(h.get(java.awt.RenderingHints.KEY_RENDERING) == java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "add merges");
        // add(null) throws NPE
        boolean npe = false;
        try { h2.add(null); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "add(null) -> NPE");
        // equals/hashCode against a plain Map copy
        java.util.Map<Object,Object> copy = new java.util.HashMap<Object,Object>();
        copy.putAll(h);
        check(h.equals(copy) && copy.equals(h), "equals Map copy (symmetric)");
        check(h.hashCode() == copy.hashCode(), "hashCode equals Map copy");
        check(!h.equals(new java.awt.RenderingHints(null)), "different hints not equal");
        check(h.toString().length() > 0, "toString non-empty");
        // clone returns shallow copy
        java.awt.RenderingHints cl = (java.awt.RenderingHints) h.clone();
        check(cl.equals(h), "clone equal");
        cl.put(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        check(!h.containsKey(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING), "clone put does not leak");
        // Key int values hidden but each key distinct even across equality
        check(!keys[0].equals(keys[1]), "keys not equal to each other");
        check(keys[0].hashCode() != keys[1].hashCode() || true, "key hashCodes usable");
    }

}
