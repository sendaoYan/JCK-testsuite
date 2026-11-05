/*
 * @test
 * @summary javax.naming.directory.ModificationItem conformance: white-box structure + black-box behavior.
 * @run main ModificationItemTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.directory.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Arrays;

public class ModificationItemTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.directory.ModificationItem");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Arrays.asList(c.getConstructors()).stream().anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == int.class && x.getParameterTypes()[1] == Attribute.class), "ctor(int,Attribute)");
        for (String m : new String[]{"getModificationOp", "getAttribute", "toString"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        Attribute a = new BasicAttribute("cn", "Alice");
        ModificationItem add = new ModificationItem(DirContext.ADD_ATTRIBUTE, a);
        check(add.getModificationOp() == DirContext.ADD_ATTRIBUTE, "ADD op");
        check(add.getAttribute() == a, "attribute identity");

        ModificationItem replace = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, a);
        check(replace.getModificationOp() == DirContext.REPLACE_ATTRIBUTE, "REPLACE op");

        ModificationItem remove = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a);
        check(remove.getModificationOp() == DirContext.REMOVE_ATTRIBUTE, "REMOVE op");

        check(add.getAttribute().getID().equals("cn"), "attr id via getAttribute");
        check("Alice".equals(add.getAttribute().get()), "attr value via getAttribute");

        // toString identifies the operation
        check(add.toString().toLowerCase(Locale.ROOT).contains("add"), "toString ADD");
        check(replace.toString().toLowerCase(Locale.ROOT).contains("replace"), "toString REPLACE");
        check(remove.toString().toLowerCase(Locale.ROOT).contains("remove"), "toString REMOVE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dMiConstants();
        dMiValidOps();
        dMiNegative();
        dMiToString();
        dMiSerialization();
        System.out.println("deep: ModificationItem contracts OK");
    }

    static void dMiConstants() {
        check(DirContext.ADD_ATTRIBUTE == 1, "ADD_ATTRIBUTE == 1");
        check(DirContext.REPLACE_ATTRIBUTE == 2, "REPLACE_ATTRIBUTE == 2");
        check(DirContext.REMOVE_ATTRIBUTE == 3, "REMOVE_ATTRIBUTE == 3");
        check(DirContext.ADD_ATTRIBUTE != DirContext.REPLACE_ATTRIBUTE
                && DirContext.REPLACE_ATTRIBUTE != DirContext.REMOVE_ATTRIBUTE,
                "operation codes are distinct");
    }

    static void dMiValidOps() {
        Attribute a = new BasicAttribute("cn", "v");
        for (int op : new int[] {DirContext.ADD_ATTRIBUTE, DirContext.REPLACE_ATTRIBUTE,
                DirContext.REMOVE_ATTRIBUTE}) {
            ModificationItem mi = new ModificationItem(op, a);
            check(mi.getModificationOp() == op, "op round-trip " + op);
            check(mi.getAttribute() == a, "attribute identity " + op);
        }
        // attribute is kept by reference (live)
        BasicAttribute live = new BasicAttribute("cn", "v1");
        ModificationItem mi = new ModificationItem(DirContext.ADD_ATTRIBUTE, live);
        live.add("v2");
        check(mi.getAttribute().size() == 2, "attribute kept live by reference");
    }

    static void dMiNegative() {
        boolean iae1 = false, iae2 = false;
        try { new ModificationItem(0, new BasicAttribute("cn", "v")); }
        catch (IllegalArgumentException e) {
            iae1 = true;
            check(e.getMessage() != null && e.getMessage().contains("Invalid modification code"),
                    "invalid op message");
        }
        try { new ModificationItem(DirContext.ADD_ATTRIBUTE, null); }
        catch (IllegalArgumentException e) {
            iae2 = true;
            check(e.getMessage() != null
                    && e.getMessage().contains("non-null attribute"),
                    "null attr message");
        }
        check(iae1, "invalid op -> IAE");
        check(iae2, "null attribute -> IAE (not NPE)");
    }

    static void dMiToString() {
        check(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("cn", "v"))
                .toString().startsWith("Add attribute: "), "ADD toString prefix");
        check(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("cn", "v"))
                .toString().startsWith("Replace attribute: "), "REPLACE toString prefix");
        check(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("cn", "v"))
                .toString().startsWith("Remove attribute: "), "REMOVE toString prefix");
    }

    static void dMiSerialization() throws Exception {
        ModificationItem mi = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("cn", "v"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(mi); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ModificationItem back = (ModificationItem) ois.readObject();
            check(back.getModificationOp() == DirContext.REPLACE_ATTRIBUTE, "round-trip op");
            check(back.getAttribute().getID().equals("cn"), "round-trip attribute id");
            check("v".equals(back.getAttribute().get()), "round-trip attribute value");
        }
    }

}
