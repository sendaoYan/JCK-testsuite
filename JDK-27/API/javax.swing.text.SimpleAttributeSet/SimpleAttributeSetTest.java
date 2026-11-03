/*
 * @test
 * @summary javax.swing.text.SimpleAttributeSet conformance: white-box structure + black-box behavior.
 * @run main SimpleAttributeSetTest
 */

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class SimpleAttributeSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.SimpleAttributeSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.MutableAttributeSet"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.lang.Cloneable"), "interface 2");
    }

    static void testBlackBox() {
        SimpleAttributeSet s = new SimpleAttributeSet();
        check(s.isEmpty(), "initially empty");
        check(s.getAttributeCount() == 0, "initial count 0");
        check(!s.isDefined(StyleConstants.Bold), "isDefined false when unset");
        check(s.getAttribute(StyleConstants.Bold) == null, "getAttribute null when unset");
        check(s.getResolveParent() == null, "initial resolve parent null");
        check(s.isEqual(s), "isEqual self");

        // Add attributes and observe the full read path.
        s.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        s.addAttribute(StyleConstants.Italic, Boolean.FALSE);
        s.addAttribute(StyleConstants.FontSize, 24);
        check(!s.isEmpty(), "non-empty after add");
        check(s.getAttributeCount() == 3, "count 3");
        check(Boolean.TRUE.equals(s.getAttribute(StyleConstants.Bold)), "Bold read back");
        check(s.isDefined(StyleConstants.Bold), "Bold isDefined");
        check(s.containsAttribute(StyleConstants.Bold, Boolean.TRUE), "containsAttribute true");
        check(!s.containsAttribute(StyleConstants.Bold, Boolean.FALSE), "containsAttribute false");

        List<Object> names = Collections.list((java.util.Enumeration<Object>) s.getAttributeNames());
        check(names.size() == 3, "three attribute names");
        check(names.contains(StyleConstants.Bold), "names contains Bold");
        check(names.contains(StyleConstants.Italic), "names contains Italic");
        check(names.contains(StyleConstants.FontSize), "names contains FontSize");

        // Overwrite keeps the count stable.
        s.addAttribute(StyleConstants.FontSize, 18);
        check(s.getAttributeCount() == 3, "count stable after overwrite");
        check(Integer.valueOf(18).equals(s.getAttribute(StyleConstants.FontSize)), "overwritten value");

        // removeAttribute.
        s.removeAttribute(StyleConstants.Italic);
        check(s.getAttributeCount() == 2, "count 2 after remove");
        check(!s.isDefined(StyleConstants.Italic), "Italic removed");

        // Copy constructor.
        SimpleAttributeSet copy = new SimpleAttributeSet(s);
        check(copy.getAttributeCount() == 2, "copy count matches");
        check(copy.isEqual(s), "copy isEqual source");
        check(copy.equals(s), "copy equals source");
        check(s.equals(copy), "source equals copy");

        // containsAttributes: subset check.
        SimpleAttributeSet subset = new SimpleAttributeSet();
        subset.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        check(s.containsAttributes(subset), "containsAttributes subset");

        // clone() and copyAttributes() produce equal-but-distinct sets.
        SimpleAttributeSet cloned = (SimpleAttributeSet) s.clone();
        check(cloned != s, "clone is a distinct instance");
        check(cloned.equals(s), "clone equals original");
        check(cloned.isEqual(s), "clone isEqual original");
        AttributeSet copied = s.copyAttributes();
        check(copied != s, "copyAttributes distinct instance");
        check(copied.isEqual(s), "copyAttributes isEqual original");

        // Resolve parent: fall-through lookup vs. local-only isDefined.
        SimpleAttributeSet parent = new SimpleAttributeSet();
        parent.addAttribute("inherited", "val");
        SimpleAttributeSet child = new SimpleAttributeSet();
        child.setResolveParent(parent);
        check(child.getResolveParent() == parent, "resolve parent set");
        check("val".equals(child.getAttribute("inherited")), "inherited value via parent");
        check(!child.isDefined("inherited"), "isDefined stays local");
        check(child.getAttributeCount() == 1, "only ResolveAttribute stored locally");

        // The shared immutable EMPTY set.
        check(SimpleAttributeSet.EMPTY.getAttributeCount() == 0, "EMPTY count 0");
        check(SimpleAttributeSet.EMPTY.getResolveParent() == null, "EMPTY resolve parent null");
        check(SimpleAttributeSet.EMPTY.isEqual(new SimpleAttributeSet()), "EMPTY isEqual new empty set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
