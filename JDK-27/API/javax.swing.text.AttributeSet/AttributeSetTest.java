/*
 * @test
 * @summary javax.swing.text.AttributeSet conformance: white-box structure + black-box behavior.
 * @run main AttributeSetTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class AttributeSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 9, "nine abstract methods");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet");

        // Marker interface constants exposed as public static final Objects.
        Field nameF = c.getField("NameAttribute");
        Field resolveF = c.getField("ResolveAttribute");
        check(Modifier.isStatic(nameF.getModifiers()) && Modifier.isFinal(nameF.getModifiers()),
                "NameAttribute is static final");
        check(Modifier.isStatic(resolveF.getModifiers()) && Modifier.isFinal(resolveF.getModifiers()),
                "ResolveAttribute is static final");
        Object nameAttr = nameF.get(null);
        Object resolveAttr = resolveF.get(null);
        check(nameAttr != null, "NameAttribute non-null");
        check(resolveAttr != null, "ResolveAttribute non-null");
        check(nameAttr == StyleConstants.NameAttribute, "NameAttribute == StyleConstants.NameAttribute");
        check(resolveAttr == StyleConstants.ResolveAttribute, "ResolveAttribute == StyleConstants.ResolveAttribute");

        // Contract exercised through the immutable empty set, typed as AttributeSet.
        AttributeSet empty = SimpleAttributeSet.EMPTY;
        check(empty.getAttributeCount() == 0, "EMPTY count is 0");
        check(!empty.getAttributeNames().hasMoreElements(), "EMPTY names empty");
        check(empty.getResolveParent() == null, "EMPTY resolve parent null");
        check(empty.getAttribute(StyleConstants.Bold) == null, "EMPTY getAttribute null");
        check(!empty.isDefined(StyleConstants.Bold), "EMPTY isDefined false");
        check(empty.isEqual(empty), "EMPTY isEqual self");
        check(empty.containsAttributes(empty), "EMPTY containsAttributes self");

        // The four nested marker interfaces are empty type signatures.
        String[] markers = {
            "javax.swing.text.AttributeSet$CharacterAttribute",
            "javax.swing.text.AttributeSet$ColorAttribute",
            "javax.swing.text.AttributeSet$FontAttribute",
            "javax.swing.text.AttributeSet$ParagraphAttribute"
        };
        for (String n : markers) {
            Class<?> m = Class.forName(n);
            check(m.isInterface(), n + " is interface");
            check(m.getDeclaredMethods().length == 0, n + " has no methods");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
