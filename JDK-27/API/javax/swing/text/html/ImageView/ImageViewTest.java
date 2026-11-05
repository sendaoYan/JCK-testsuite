/*
 * @test
 * @summary javax.swing.text.html.ImageView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ImageViewTest
 */

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

public class ImageViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.ImageView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructor(Element.class) != null, "ctor(Element) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><img alt='hello'><img alt='pic' src='http://example.com/x.png'></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            List<Element> imgs = new ArrayList<Element>();
            collectElements(doc.getDefaultRootElement(), HTML.Tag.IMG, imgs);
            check(imgs.size() == 2, "two IMG elements");

            ImageView v1 = new ImageView(imgs.get(0)); // no src
            ImageView v2 = new ImageView(imgs.get(1)); // absolute src

            check("hello".equals(v1.getAltText()), "altText from ALT attribute");
            check("pic".equals(v2.getAltText()), "altText #2");

            check(v1.getImageURL() == null, "imageURL null without src");
            check(new URL("http://example.com/x.png").equals(v2.getImageURL()), "imageURL from absolute src");

            check(v1.getImage() == null, "image null without src");

            Icon noImg = v1.getNoImageIcon();
            Icon loading = v1.getLoadingImageIcon();
            check(noImg instanceof Icon, "getNoImageIcon returns Icon");
            check(loading instanceof Icon, "getLoadingImageIcon returns Icon");

            check("hello".equals(v1.getToolTipText(0f, 0f, new Rectangle(0, 0, 10, 10))), "tooltip == alt text");

            check(v1.getLoadsSynchronously() == false, "default async load");
            v1.setLoadsSynchronously(true);
            check(v1.getLoadsSynchronously() == true, "sync load set true");
            v1.setLoadsSynchronously(false);
            check(v1.getLoadsSynchronously() == false, "sync load set false");

            check(v1.getAlignment(View.Y_AXIS) == 1.0f, "alignment Y == 1.0 (default vAlign)");
            check(v1.getAlignment(View.X_AXIS) == 0.5f, "alignment X == 0.5 (View default)");

            // ImageView defaults min/max to preferred (no explicit overrides)
            float prefX = v1.getPreferredSpan(View.X_AXIS);
            float prefY = v1.getPreferredSpan(View.Y_AXIS);
            check(prefX >= 0f && prefY >= 0f, "preferred spans >= 0");
            check(v1.getMinimumSpan(View.X_AXIS) == prefX, "min == pref on X");
            check(v1.getMaximumSpan(View.X_AXIS) == prefX, "max == pref on X");
            check(v1.getMinimumSpan(View.Y_AXIS) == prefY, "min == pref on Y");
            check(v1.getMaximumSpan(View.Y_AXIS) == prefY, "max == pref on Y");

            // modelToView: inside -> rectangle, outside -> null
            Rectangle alloc = new Rectangle(0, 0, 100, 50);
            int p0 = v1.getStartOffset();
            int p1 = v1.getEndOffset();
            check(v1.modelToView(p0, alloc, Position.Bias.Forward) != null, "modelToView(start) non-null");
            check(v1.modelToView(p1, alloc, Position.Bias.Backward) != null, "modelToView(end) non-null");
            check(v1.modelToView(p1 + 1, alloc, Position.Bias.Forward) == null, "modelToView(outside) null");

            // viewToModel: left of right edge -> start/Forward, else end/Backward
            Position.Bias[] bias = new Position.Bias[1];
            int m1 = v1.viewToModel(10f, 10f, alloc, bias);
            check(m1 == p0 && bias[0] == Position.Bias.Forward, "viewToModel left -> start/Forward");
            int m2 = v1.viewToModel(200f, 10f, alloc, bias);
            check(m2 == p1 && bias[0] == Position.Bias.Backward, "viewToModel right -> end/Backward");

            // setSize is a no-throw layout entry point
            v1.setSize(80f, 40f);
            check(true, "setSize callable");
        } finally {
            frame.dispose();
        }
    }

    static void collectElements(Element e, HTML.Tag tag, List<Element> out) {
        if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag) out.add(e);
        for (int i = 0; i < e.getElementCount(); i++) collectElements(e.getElement(i), tag, out);
    }

    static javax.swing.text.html.HTMLEditorKit deepKit = new javax.swing.text.html.HTMLEditorKit();

    static javax.swing.text.html.HTMLDocument deepLoad(String html) throws Exception {
        javax.swing.text.html.HTMLDocument d = (javax.swing.text.html.HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new java.io.StringReader(html), d, 0);
        return d;
    }

    static javax.swing.text.Element deepFindImg(javax.swing.text.html.HTMLDocument doc) {
        java.util.ArrayDeque<javax.swing.text.Element> q = new java.util.ArrayDeque<javax.swing.text.Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            javax.swing.text.Element e = q.poll();
            if (e.getAttributes().getAttribute(javax.swing.text.StyleConstants.NameAttribute) == javax.swing.text.html.HTML.Tag.IMG) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    // ---- model layer over a fixed document (headless-safe) ----
    static void testModelLayer() throws Exception {
                javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        javax.swing.text.html.HTMLDocument doc = deepLoad("<html><body><img src='x.png' alt='ALT' width=20 height=10></body></html>");
        javax.swing.text.Element img = deepFindImg(doc);
        check(img != null, "model: img element found");
        javax.swing.text.html.ImageView imageV = new javax.swing.text.html.ImageView(img);
        check(imageV.getElement() == img, "model: element identity");
        check("ALT".equals(imageV.getAltText()), "model: alt text");
        check(imageV.getImageURL() == null, "model: relative src without base yields null url");
        check(imageV.getNoImageIcon() != null, "model: no-image icon available");
        check(imageV.getLoadingImageIcon() != null, "model: loading icon available");
        check(imageV.getLoadsSynchronously() == false, "model: loads asynchronously by default");
        imageV.setLoadsSynchronously(true);
        check(imageV.getLoadsSynchronously() == true, "model: setLoadsSynchronously round-trip");
        check(imageV.getAttributes().getAttributeCount() == 0, "model: view attributes empty");
        imageV.setSize(100f, 100f);
        // The exact span depends on the LAF's border/metric stack (observed
        // 59x53 on x86, 61x59 on a riscv64 build whose freetype/font stack
        // differs), so anchor on the contract: the span derives from the
        // no-image icon plus decorations, never from the width/height attrs.
        javax.swing.Icon fallback = imageV.getNoImageIcon();
        float px = imageV.getPreferredSpan(javax.swing.text.View.X_AXIS);
        float py = imageV.getPreferredSpan(javax.swing.text.View.Y_AXIS);
        check(px >= fallback.getIconWidth() && px - fallback.getIconWidth() < 32,
              "model: preferred X derives from the no-image icon, not the width attr (got " + px + ")");
        check(py >= fallback.getIconHeight() && py - fallback.getIconHeight() < 32,
              "model: preferred Y derives from the no-image icon, not the height attr (got " + py + ")");
        check(imageV.getAlignment(javax.swing.text.View.X_AXIS) == 0.5f, "model: alignment X 0.5");
        check(imageV.getAlignment(javax.swing.text.View.Y_AXIS) == 1.0f, "model: alignment Y 1.0");
        check("ALT".equals(imageV.getToolTipText(1f, 1f, new java.awt.Rectangle(0, 0, 50, 50))),
              "model: tooltip falls back to alt text");
        check(imageV.getImage() == null, "model: image not loaded for a missing file without sync load");
        javax.swing.text.html.HTMLDocument dnos = deepLoad("<html><body>nosrc<img alt='NO'>x</body></html>");
        javax.swing.text.Element nos = deepFindImg(dnos);
        javax.swing.text.html.ImageView ivn = new javax.swing.text.html.ImageView(nos);
        check(ivn.getImageURL() == null, "model: no src -> null url");
        check("NO".equals(ivn.getAltText()), "model: no src -> alt text kept");
        ivn.setSize(50f, 50f);
        javax.swing.Icon fb2 = ivn.getNoImageIcon();
        float nx = ivn.getPreferredSpan(javax.swing.text.View.X_AXIS);
        float ny = ivn.getPreferredSpan(javax.swing.text.View.Y_AXIS);
        check(nx >= fb2.getIconWidth() && nx - fb2.getIconWidth() < 32,
              "model: src-less preferred X derives from the icon (got " + nx + ")");
        check(ny >= fb2.getIconHeight() && ny - fb2.getIconHeight() < 32,
              "model: src-less preferred Y derives from the icon (got " + ny + ")");
        javax.swing.text.html.HTMLDocument dabs = deepLoad("<html><head><base href='http://ex.com/dir/'></head><body><img src='sub/x.png'></body></html>");
        javax.swing.text.Element baseImg = deepFindImg(dabs);
        java.net.URL u = new javax.swing.text.html.ImageView(baseImg).getImageURL();
        check(u != null && "http://ex.com/dir/sub/x.png".equals(u.toString()), "model: src resolved against base");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ImageViewTest::testWhiteBox);
        runOnEDT(ImageViewTest::testBlackBox);
        runOnEDT(ImageViewTest::testModelLayer);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
