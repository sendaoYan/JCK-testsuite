/*
 * @test
 * @summary javax.print.attribute.AttributeSetUtilities conformance: white-box structure + black-box behavior.
 * @run main AttributeSetUtilitiesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class AttributeSetUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.AttributeSetUtilities");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(hasMethod(c, "unmodifiableView") && hasMethod(c, "synchronizedView") && hasMethod(c, "verifyAttributeCategory") && hasMethod(c, "verifyAttributeValue") && hasMethod(c, "verifyCategoryForValue"), "methods");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HashAttributeSet s = new HashAttributeSet(new Copies(2));
        AttributeSet u = AttributeSetUtilities.unmodifiableView(s);
        check(u.size() == 1 && new Copies(2).equals(u.get(Copies.class)), "unmodifiableView reads");
        boolean threw = false; try { u.add(new JobName("n", Locale.ROOT)); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable add throws UnmodifiableSetException");
        threw = false; try { u.remove(Copies.class); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable remove throws");
        threw = false; try { u.clear(); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "unmodifiable clear throws");
        s.add(new JobName("n", Locale.ROOT));
        check(u.size() == 2, "view reflects underlying");
        AttributeSet syn = AttributeSetUtilities.synchronizedView(s);
        check(syn.size() == 2 && new Copies(2).equals(syn.get(Copies.class)), "synchronizedView reads");
        check(AttributeSetUtilities.verifyAttributeCategory(Copies.class, Attribute.class) == Copies.class, "verifyAttributeCategory ok");
        boolean threw2 = false; try { AttributeSetUtilities.verifyAttributeCategory(String.class, Attribute.class); } catch (ClassCastException e) { threw2 = true; }
        check(threw2, "verifyAttributeCategory wrong type ClassCastException");
        boolean threw3 = false; try { AttributeSetUtilities.verifyAttributeCategory(null, Attribute.class); } catch (NullPointerException e) { threw3 = true; }
        check(threw3, "verifyAttributeCategory null NPE");
        check(AttributeSetUtilities.verifyAttributeValue(new Copies(1), Attribute.class) instanceof Copies, "verifyAttributeValue ok");
        boolean threw4 = false; try { AttributeSetUtilities.verifyAttributeValue(new Copies(1), PrintServiceAttribute.class); } catch (ClassCastException e) { threw4 = true; }
        check(threw4, "verifyAttributeValue wrong interface ClassCastException");
        AttributeSetUtilities.verifyCategoryForValue(Copies.class, new Copies(1));
        boolean threw5 = false; try { AttributeSetUtilities.verifyCategoryForValue(Media.class, new Copies(1)); } catch (IllegalArgumentException e) { threw5 = true; }
        check(threw5, "verifyCategoryForValue mismatch IllegalArgumentException");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
