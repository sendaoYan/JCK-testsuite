/*
 * @test
 * @summary javax.swing.text.html.ImageView conformance: alt text, url
 *          resolution against the document base, icon fallbacks, span and
 *          alignment contract (headless-safe, model layer only).
 * @run main ImageViewTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

public class ImageViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    static Element findImg(HTMLDocument doc) {
        Deque<Element> q = new ArrayDeque<Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMG) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.ImageView", false, ImageViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAltText"), "method getAltText");
        check(names.contains("getImageURL"), "method getImageURL");
        check(names.contains("getNoImageIcon"), "method getNoImageIcon");
        check(names.contains("getLoadingImageIcon"), "method getLoadingImageIcon");
        check(names.contains("getImage"), "method getImage");
        check(names.contains("setLoadsSynchronously"), "method setLoadsSynchronously");
        check(names.contains("getLoadsSynchronously"), "method getLoadsSynchronously");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument doc = load("<html><body><img src='x.png' alt='ALT' width=20 height=10></body></html>");
        Element img = findImg(doc);
        check(img != null, "img element found");

        ImageView imageV = new ImageView(img);
        check(imageV.getElement() == img, "element identity");
        check("ALT".equals(imageV.getAltText()), "alt text");
        check(imageV.getImageURL() == null, "relative src without base yields null url");
        check(imageV.getNoImageIcon() != null, "no-image icon available");
        check(imageV.getLoadingImageIcon() != null, "loading icon available");
        check(imageV.getLoadsSynchronously() == false, "loads asynchronously by default");
        imageV.setLoadsSynchronously(true);
        check(imageV.getLoadsSynchronously() == true, "setLoadsSynchronously round-trip");

        check(imageV.getAttributes().getAttributeCount() == 0,
              "view attributes empty (html attrs not exposed as css)");
        check(imageV.getAttributes().getAttribute(HTML.Attribute.WIDTH) == null,
              "width attribute not exposed");

        imageV.setSize(100f, 100f);
        // The exact span depends on the LAF's border/metric stack (observed
        // 59x53 on x86 temurin, 61x59 on a riscv64 build whose freetype/font
        // stack differs), so anchor on the contract: the span derives from
        // the no-image icon plus decorations, never from width=20/height=10.
        javax.swing.Icon fallback = imageV.getNoImageIcon();
        float px = imageV.getPreferredSpan(View.X_AXIS);
        float py = imageV.getPreferredSpan(View.Y_AXIS);
        check(px >= fallback.getIconWidth() && px - fallback.getIconWidth() < 32,
              "preferred X derives from the no-image icon, not the width attr (got " + px + ")");
        check(py >= fallback.getIconHeight() && py - fallback.getIconHeight() < 32,
              "preferred Y derives from the no-image icon, not the height attr (got " + py + ")");
        check(imageV.getAlignment(View.X_AXIS) == 0.5f, "alignment X 0.5");
        check(imageV.getAlignment(View.Y_AXIS) == 1.0f, "alignment Y 1.0 (bottom)");
        check("ALT".equals(imageV.getToolTipText(1f, 1f, new java.awt.Rectangle(0, 0, 50, 50))),
              "tooltip falls back to alt text");
        check(imageV.getImage() == null, "image not loaded for a missing file without sync load");

        // image without src: alt kept, no url, icon fallback span
        HTMLDocument dnos = load("<html><body>nosrc<img alt='NO'>x</body></html>");
        Element nos = findImg(dnos);
        check(nos != null, "src-less img element found");
        ImageView ivn = new ImageView(nos);
        check(ivn.getImageURL() == null, "no src -> null url");
        check("NO".equals(ivn.getAltText()), "no src -> alt text kept");
        check(ivn.getImage() == null, "no src -> no image");
        ivn.setSize(50f, 50f);
        javax.swing.Icon fb2 = ivn.getNoImageIcon();
        float nx = ivn.getPreferredSpan(View.X_AXIS);
        float ny = ivn.getPreferredSpan(View.Y_AXIS);
        check(nx >= fb2.getIconWidth() && nx - fb2.getIconWidth() < 32,
              "src-less preferred X derives from the icon (got " + nx + ")");
        check(ny >= fb2.getIconHeight() && ny - fb2.getIconHeight() < 32,
              "src-less preferred Y derives from the icon (got " + ny + ")");

        // url resolution against the document base
        HTMLDocument dabs = load("<html><head><base href='http://ex.com/dir/'></head>"
                + "<body><img src='sub/x.png'></body></html>");
        Element baseImg = findImg(dabs);
        check(baseImg != null, "base img found");
        URL u = new ImageView(baseImg).getImageURL();
        check(u != null && "http://ex.com/dir/sub/x.png".equals(u.toString()),
              "src resolved against base: " + u);
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
