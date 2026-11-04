/*
 * @test
 * @summary javax.management.MBeanNotificationInfo conformance: full contract
 *          coverage. notification types, null/empty types -> empty (shared
 *          empty instance), defensive getter, order-sensitive equals with
 *          name/description/descriptor participation, hashCode shape,
 *          descriptor ctor round-trip, toString, clone/equals, serialization.
 * @run main MBeanNotificationInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanNotificationInfo;

public class MBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanNotificationInfo n = new MBeanNotificationInfo(new String[]{"a.b", "c.d"}, "note", "desc");
        check(n.getNotifTypes().length == 2 && "a.b".equals(n.getNotifTypes()[0]), "notif types");
        check("note".equals(n.getName()), "name");
    }

    static void testNullAndEmptyTypes() {
        check(new MBeanNotificationInfo(null, "note", "desc").getNotifTypes().length == 0, "null types -> empty");
        check(new MBeanNotificationInfo(new String[0], "note", "desc").getNotifTypes().length == 0, "empty types -> empty");
    }

    static void testDefensiveGetterAndClone() {
        MBeanNotificationInfo n = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        n.getNotifTypes()[0] = null;
        check("a.b".equals(n.getNotifTypes()[0]), "getNotifTypes defensive");
        MBeanNotificationInfo c = (MBeanNotificationInfo) n.clone();
        check(n.equals(c) && n.hashCode() == c.hashCode(), "clone equals/hashCode");
    }

    // ---- deepened: empty types shared instance ------------------------------

    static void testEmptyTypesShared() {
        MBeanNotificationInfo viaNull = new MBeanNotificationInfo(null, "n", "d");
        MBeanNotificationInfo viaEmpty = new MBeanNotificationInfo(new String[0], "n", "d");
        check(viaNull.getNotifTypes() == viaNull.getNotifTypes(),
                "null-types ctor: empty array shared between calls");
        check(viaEmpty.getNotifTypes() == viaEmpty.getNotifTypes(),
                "empty-types ctor: empty array shared between calls");
        // types are NOT validated: arbitrary dot-separated strings accepted (spec note)
        MBeanNotificationInfo wild = new MBeanNotificationInfo(new String[]{"sun.prob.25"}, "n", "d");
        check(wild.getNotifTypes().length == 1, "unvalidated notif type strings accepted");
    }

    // ---- deepened: equals is order-sensitive over types ---------------------

    static void testEqualsOrderAndParticipation() {
        MBeanNotificationInfo base = new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "n", "d");
        check(!base.equals(new MBeanNotificationInfo(new String[]{"a.b", "x.y"}, "n", "d")),
                "equals is order-sensitive over notif types");
        check(!base.equals(new MBeanNotificationInfo(new String[]{"x.y"}, "n", "d")),
                "different type count not equal");
        check(!base.equals(new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "OTHER", "d")),
                "name participates in equals");
        check(!base.equals(new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "n", "OTHER")),
                "description participates in equals");
        check(!base.equals(new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "n", "d",
                new ImmutableDescriptor("k=v"))), "descriptor participates in equals");
        check(base.equals(new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "n", "d")),
                "identical reconstruction equal");
        check(base.hashCode() == new MBeanNotificationInfo(new String[]{"x.y", "a.b"}, "n", "d").hashCode(),
                "equal objects equal hashCode");
        check(!base.equals(null) && !base.equals("n"), "equals null/foreign false");
    }

    // ---- deepened: descriptor ctor + toString + serialization ----------------

    static void testDescriptorToStringSerialization() throws Exception {
        ImmutableDescriptor d = new ImmutableDescriptor("units=ms");
        MBeanNotificationInfo withD = new MBeanNotificationInfo(new String[]{"a.b"}, "n", "d", d);
        check("ms".equals(withD.getDescriptor().getFieldValue("units")), "descriptor ctor round-trip");
        check(new MBeanNotificationInfo(new String[]{"a.b"}, "n", "d", null).getDescriptor()
                .getFieldNames().length == 0, "null descriptor -> empty");
        check(new MBeanNotificationInfo(new String[]{"a.b"}, "n", "d").getDescriptor()
                .getFieldNames().length == 0, "3-arg ctor descriptor empty");
        String s = withD.toString();
        check(s.contains("notifTypes=[a.b]") && s.contains("name=n") && s.contains("description=d"),
                "toString carries name/description/notifTypes");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(withD);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanNotificationInfo read = (MBeanNotificationInfo) ois.readObject();
        check(read.equals(withD), "serialization round-trip equals");
        check("a.b".equals(read.getNotifTypes()[0]), "notif types survive serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testNullAndEmptyTypes();
        testDefensiveGetterAndClone();
        testEmptyTypesShared();
        testEqualsOrderAndParticipation();
        testDescriptorToStringSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
