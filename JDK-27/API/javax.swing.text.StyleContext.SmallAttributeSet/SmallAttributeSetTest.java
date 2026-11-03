/*
 * @test
 * @summary javax.swing.text.StyleContext.SmallAttributeSet conformance: white-box structure + black-box behavior.
 * @run main SmallAttributeSetTest
 */

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class SmallAttributeSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleContext$SmallAttributeSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet"), "interface 0");
    }

    static void testBlackBox() {
        StyleContext ctx = new StyleContext();

        // Build from a flat key/value array.
        StyleContext.SmallAttributeSet s = ctx.new SmallAttributeSet(new Object[]{
            StyleConstants.Bold, Boolean.TRUE,
            StyleConstants.Italic, Boolean.FALSE
        });
        check(s.getAttributeCount() == 2, "count 2");
        check(Boolean.TRUE.equals(s.getAttribute(StyleConstants.Bold)), "Bold read back");
        check(Boolean.FALSE.equals(s.getAttribute(StyleConstants.Italic)), "Italic read back");
        check(s.isDefined(StyleConstants.Bold), "Bold isDefined");
        check(!s.isDefined(StyleConstants.FontSize), "FontSize not defined");
        check(s.containsAttribute(StyleConstants.Bold, Boolean.TRUE), "containsAttribute true");
        check(!s.containsAttribute(StyleConstants.Bold, Boolean.FALSE), "containsAttribute false");

        List<Object> names = Collections.list((java.util.Enumeration<Object>) s.getAttributeNames());
        check(names.size() == 2, "two names");
        check(names.contains(StyleConstants.Bold) && names.contains(StyleConstants.Italic), "names present");

        // containsAttributes with a subset.
        SimpleAttributeSet sub = new SimpleAttributeSet();
        sub.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        check(s.containsAttributes(sub), "containsAttributes subset");

        // Equality is value-based; the immutable set's clone/copy return itself.
        StyleContext.SmallAttributeSet s2 = ctx.new SmallAttributeSet(new Object[]{
            StyleConstants.Bold, Boolean.TRUE,
            StyleConstants.Italic, Boolean.FALSE
        });
        check(s.equals(s2), "value equality");
        check(s2.equals(s), "value equality symmetric");
        check(s.equals(s), "reflexive equality");
        check(s.isEqual(s), "isEqual self");
        check(s.clone() == s, "clone returns this");
        check(s.copyAttributes() == s, "copyAttributes returns this");
        check(s.getResolveParent() == null, "no resolve parent");

        // Build from an AttributeSet (copies name/value pairs).
        StyleContext.SmallAttributeSet s3 = ctx.new SmallAttributeSet(sub);
        check(s3.getAttributeCount() == 1, "from AttributeSet count 1");
        check(Boolean.TRUE.equals(s3.getAttribute(StyleConstants.Bold)), "from AttributeSet value");

        // Resolve parent is extracted from the source set's ResolveAttribute.
        SimpleAttributeSet par = new SimpleAttributeSet();
        par.addAttribute("pp", "pv");
        SimpleAttributeSet holder = new SimpleAttributeSet();
        holder.setResolveParent(par);
        StyleContext.SmallAttributeSet s4 = ctx.new SmallAttributeSet(holder);
        check(s4.getResolveParent() == par, "resolve parent carried over");
        check("pv".equals(s4.getAttribute("pp")), "value resolved through parent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
