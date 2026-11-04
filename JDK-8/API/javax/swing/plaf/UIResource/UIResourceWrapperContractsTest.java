/*
 * @test
 * @summary javax.swing.plaf UIResource wrapper family conformance (Color/Dimension/Insets/Font/Icon/Border/InputMap/ActionMap/ComponentInputMap).
 * @run main UIResourceWrapperContractsTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;

/**
 * The UIResource wrappers mark values installed by a look-and-feel so that
 * UIManager can distinguish LaF-set values from client-set ones. Their
 * contract: wrap the wrapped type's data faithfully (values, equals/hashCode
 * interoperate with the plain AWT/Swing type) and implement UIResource.
 */
public class UIResourceWrapperContractsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkEq(Object a, Object b, String msg) {
        check(a.equals(b) && b.equals(a), msg);
    }

    @SuppressWarnings("unchecked")
    static <T> T roundtrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        T back = (T) in.readObject();
        in.close();
        oos.close();
        return back;
    }

    static void testColorUIResource() throws Exception {
        ColorUIResource c1 = new ColorUIResource(10, 20, 30);
        check(c1 instanceof UIResource, "ColorUIResource is UIResource");
        check(c1.getRed() == 10 && c1.getGreen() == 20 && c1.getBlue() == 30, "rgb ctor");
        check(c1.getAlpha() == 255, "opaque alpha default");
        ColorUIResource c2 = new ColorUIResource(0x0A141E);
        checkEq(c1, c2, "packed-int ctor equals rgb ctor");
        ColorUIResource c3 = new ColorUIResource(10f / 255f, 20f / 255f, 30f / 255f);
        checkEq(c1, c3, "float ctor equals rgb ctor");
        ColorUIResource c4 = new ColorUIResource(new Color(10, 20, 30));
        checkEq(c1, c4, "Color-arg ctor copies values");

        // equals/hashCode interoperate with plain Color
        Color plain = new Color(10, 20, 30);
        checkEq(c1, plain, "ColorUIResource equals plain Color");
        check(c1.hashCode() == plain.hashCode(), "hashCode consistent with plain Color");
        check(!c1.equals(new Color(10, 20, 31)), "different rgb not equal");

        ColorUIResource back = roundtrip(c1);
        checkEq(back, c1, "serialization round-trip keeps color");
        check(back instanceof UIResource, "serialization round-trip keeps UIResource marker");
    }

    static void testDimensionUIResource() throws Exception {
        DimensionUIResource d = new DimensionUIResource(5, 6);
        check(d instanceof UIResource, "DimensionUIResource is UIResource");
        checkEq(d, new Dimension(5, 6), "equals plain Dimension");
        check(d.hashCode() == new Dimension(5, 6).hashCode(), "hashCode consistent");
        check(d.toString() != null, "toString non-null");
        // mutable like Dimension
        d.setSize(7, 8);
        checkEq(d, new Dimension(7, 8), "setSize mutates");
        DimensionUIResource back = roundtrip(d);
        checkEq(back, d, "serialization round-trip");
    }

    static void testInsetsUIResource() throws Exception {
        InsetsUIResource i = new InsetsUIResource(1, 2, 3, 4);
        check(i instanceof UIResource, "InsetsUIResource is UIResource");
        checkEq(i, new Insets(1, 2, 3, 4), "equals plain Insets");
        check(i.hashCode() == new Insets(1, 2, 3, 4).hashCode(), "hashCode consistent");
        i.top = 9;
        checkEq(i, new Insets(9, 2, 3, 4), "fields mutable");
        InsetsUIResource back = roundtrip(i);
        checkEq(back, i, "serialization round-trip");
    }

    static void testFontUIResource() throws Exception {
        FontUIResource f = new FontUIResource("Dialog", Font.BOLD, 12);
        check(f instanceof UIResource, "FontUIResource is UIResource");
        check(f.getName().equals("Dialog") && f.getStyle() == Font.BOLD && f.getSize() == 12,
                "name/style/size ctor round-trip");
        Font plain = new Font("Dialog", Font.BOLD, 12);
        checkEq(f, plain, "equals plain Font with same attrs");
        check(!f.equals(new FontUIResource("Dialog", Font.PLAIN, 12)), "style differs -> not equal");
        check(!f.equals(new FontUIResource("Dialog", Font.BOLD, 13)), "size differs -> not equal");
        FontUIResource copy = new FontUIResource(plain);
        checkEq(f, copy, "Font-arg ctor copies attributes");
        check(copy instanceof UIResource, "Font-arg ctor still a UIResource");
    }

    static void testIconUIResource() {
        Icon inner = javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon();
        IconUIResource icon = new IconUIResource(inner);
        check(icon instanceof UIResource, "IconUIResource is UIResource");
        check(icon.getIconWidth() == inner.getIconWidth(), "width delegates");
        check(icon.getIconHeight() == inner.getIconHeight(), "height delegates");
        // paintIcon delegates the painting
        BufferedImage img = new BufferedImage(inner.getIconWidth(), inner.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(new javax.swing.JCheckBox(), img.getGraphics(), 0, 0);
        int drawn = 0;
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) >>> 24) != 0) drawn++;
        check(drawn > 0, "paintIcon delegates to the wrapped icon");
    }

    static void testBorderUIResource() {
        Border empty = new EmptyBorder(1, 2, 3, 4);
        BorderUIResource wrapped = new BorderUIResource(empty);
        check(wrapped instanceof UIResource, "BorderUIResource is UIResource");
        check(wrapped.getBorderInsets(null).equals(new Insets(1, 2, 3, 4)),
                "insets delegate to the wrapped border");
        check(!wrapped.isBorderOpaque(), "opacity delegates (EmptyBorder transparent)");

        BorderUIResource.EmptyBorderUIResource e = new BorderUIResource.EmptyBorderUIResource(1, 2, 3, 4);
        check(e instanceof UIResource && e.getBorderInsets(null).equals(new Insets(1, 2, 3, 4)),
                "EmptyBorderUIResource insets");

        BorderUIResource.EtchedBorderUIResource et =
                new BorderUIResource.EtchedBorderUIResource();
        check(et instanceof UIResource, "EtchedBorderUIResource is UIResource");
        check(et.getEtchType() == EtchedBorder.LOWERED, "etched default lowered");
        check(et.getBorderInsets(null).equals(new Insets(2, 2, 2, 2)), "etched insets 2");

        BorderUIResource.BevelBorderUIResource be =
                new BorderUIResource.BevelBorderUIResource(BevelBorder.RAISED);
        check(be instanceof UIResource, "BevelBorderUIResource is UIResource");
        check(be.getBevelType() == BevelBorder.RAISED, "bevel type round-trip");
        check(be.getBorderInsets(null).equals(new Insets(2, 2, 2, 2)), "bevel insets 2");

        BorderUIResource.LineBorderUIResource li =
                new BorderUIResource.LineBorderUIResource(Color.BLACK);
        check(li instanceof UIResource, "LineBorderUIResource is UIResource");
        check(li.getBorderInsets(null).equals(new Insets(1, 1, 1, 1)), "line insets 1");
        check(li.getLineColor().equals(Color.BLACK), "line color round-trip");

        BorderUIResource.MatteBorderUIResource ma =
                new BorderUIResource.MatteBorderUIResource(1, 2, 3, 4, Color.RED);
        check(ma instanceof UIResource, "MatteBorderUIResource is UIResource");
        check(ma.getBorderInsets(null).equals(new Insets(1, 2, 3, 4)), "matte insets round-trip");

        CompoundBorderUIResourceProbe co = new CompoundBorderUIResourceProbe(
                new EmptyBorder(1, 1, 1, 1), new EmptyBorder(2, 2, 2, 2));
        check(co instanceof UIResource, "CompoundBorderUIResource is UIResource");
        check(co.getBorderInsets(null).equals(new Insets(3, 3, 3, 3)), "compound insets sum");
        check(co.getOutsideBorder().getBorderInsets(null).equals(new Insets(1, 1, 1, 1)),
                "outside border kept");
        check(co.getInsideBorder().getBorderInsets(null).equals(new Insets(2, 2, 2, 2)),
                "inside border kept");

        BorderUIResource.TitledBorderUIResource ti =
                new BorderUIResource.TitledBorderUIResource(new EmptyBorder(1, 1, 1, 1), "T");
        check(ti instanceof UIResource, "TitledBorderUIResource is UIResource");
        check(ti.getTitle().equals("T"), "title round-trip");
        check(ti.getTitlePosition() == TitledBorder.DEFAULT_POSITION, "default title position");
        check(ti.getBorderInsets(new JPanel()) != null, "titled insets non-null with component");
        try {
            ti.getBorderInsets(null);
            check(false, "titled insets with null component must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "titled insets with null component throws NPE (needs label metrics)");
        }
    }

    /** Exposes the protected CompoundBorder getters via a public subclass. */
    static class CompoundBorderUIResourceProbe extends BorderUIResource.CompoundBorderUIResource {
        CompoundBorderUIResourceProbe(Border outside, Border inside) { super(outside, inside); }
    }

    static void testInputAndActionMaps() {
        InputMapUIResource im = new InputMapUIResource();
        check(im instanceof UIResource, "InputMapUIResource is UIResource");
        KeyStroke ks = KeyStroke.getKeyStroke("ctrl P");
        ActionMap am2 = new ActionMap();
        im.put(ks, "v");
        check("v".equals(im.get(ks)), "InputMap put/get");
        check(im.getParent() == null, "no parent by default");
        InputMap parent = new InputMapUIResource();
        parent.put(ks, "p");
        im.setParent(parent);
        check("v".equals(im.get(ks)), "child binding shadows parent");
        im.remove(ks);
        check("p".equals(im.get(ks)), "removal falls back to parent");
        check(im.keys() != null && im.keys().length == 0, "keys empty after remove");

        ActionMapUIResource am = new ActionMapUIResource();
        check(am instanceof UIResource, "ActionMapUIResource is UIResource");
        javax.swing.Action action = new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { }
        };
        am.put("a", action);
        check(am.get("a") == action, "ActionMap put/get");
        check(am.keys() != null && am.keys().length == 1, "ActionMap keys");
        am.remove("a");
        check(am.get("a") == null, "ActionMap remove");

        JComponent host = new JPanel();
        ComponentInputMapUIResource cim = new ComponentInputMapUIResource(host);
        check(cim instanceof UIResource, "ComponentInputMapUIResource is UIResource");
        cim.put(ks, "cv");
        check("cv".equals(cim.get(ks)), "ComponentInputMap put/get");
        check(cim.getComponent() == host, "ComponentInputMap keeps the component");
        try {
            new ComponentInputMapUIResource(null);
            check(false, "ComponentInputMapUIResource(null) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "ComponentInputMapUIResource(null) throws IAE");
        }
    }

    /** LaF-installed defaults carry the UIResource marker end to end. */
    static void testLaFInstalledDefaultsAreUIResources() throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        Object font = UIManager.get("Label.font");
        check(font instanceof UIResource, "Label.font is a UIResource");
        JPanel p = new JPanel();
        p.updateUI();
        check(p.getBackground() instanceof UIResource, "LaF background is UIResource");
        check(p.getForeground() instanceof UIResource, "LaF foreground is UIResource");
        check(p.getFont() instanceof UIResource, "LaF font is UIResource");
        Border b = p.getBorder();
        check(b == null || b instanceof UIResource, "LaF border is UIResource or null");
    }

    public static void main(String[] args) throws Exception {
        testColorUIResource();
        testDimensionUIResource();
        testInsetsUIResource();
        testFontUIResource();
        testIconUIResource();
        testBorderUIResource();
        testInputAndActionMaps();
        testLaFInstalledDefaultsAreUIResources();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
