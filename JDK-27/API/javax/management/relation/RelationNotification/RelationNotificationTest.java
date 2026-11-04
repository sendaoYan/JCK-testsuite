/*
 * @test
 * @summary javax.management.relation.RelationNotification conformance: white-box structure + black-box behavior.
 * @run main RelationNotificationTest
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.relation.RelationNotification;

public class RelationNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationNotification");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationNotification must be public");
        check(!c.isInterface(), "RelationNotification must be a class");
        check(c.getSuperclass() == Notification.class, "RelationNotification must extend Notification");
        check("jmx.relation.creation.basic".equals(c.getField("RELATION_BASIC_CREATION").get(null)), "RELATION_BASIC_CREATION value");
        check("jmx.relation.creation.mbean".equals(c.getField("RELATION_MBEAN_CREATION").get(null)), "RELATION_MBEAN_CREATION value");
        check("jmx.relation.update.basic".equals(c.getField("RELATION_BASIC_UPDATE").get(null)), "RELATION_BASIC_UPDATE value");
        check("jmx.relation.update.mbean".equals(c.getField("RELATION_MBEAN_UPDATE").get(null)), "RELATION_MBEAN_UPDATE value");
        check("jmx.relation.removal.basic".equals(c.getField("RELATION_BASIC_REMOVAL").get(null)), "RELATION_BASIC_REMOVAL value");
        check("jmx.relation.removal.mbean".equals(c.getField("RELATION_MBEAN_REMOVAL").get(null)), "RELATION_MBEAN_REMOVAL value");
        c.getMethod("getRelationId"); c.getMethod("getRelationTypeName");
        c.getMethod("getObjectName"); c.getMethod("getMBeansToUnregister");
        c.getMethod("getRoleName"); c.getMethod("getOldRoleValue"); c.getMethod("getNewRoleValue");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName source = on("d:type=source");
        ObjectName relObj = on("d:type=relation");
        ObjectName m1 = on("d:type=m1");
        ObjectName m2 = on("d:type=m2");

        // creation/removal constructor
        RelationNotification create = new RelationNotification(
                RelationNotification.RELATION_BASIC_CREATION, source, 100L, 200L,
                "created", "relId-1", "myType", relObj,
                new ArrayList<>(java.util.Arrays.asList(m1, m2)));
        check("relId-1".equals(create.getRelationId()), "getRelationId");
        check("myType".equals(create.getRelationTypeName()), "getRelationTypeName");
        check(relObj.equals(create.getObjectName()), "getObjectName");
        check(RelationNotification.RELATION_BASIC_CREATION.equals(create.getType()), "getType from Notification");
        check(create.getSequenceNumber() == 100L, "getSequenceNumber");
        check(create.getTimeStamp() == 200L, "getTimeStamp");
        check("created".equals(create.getMessage()), "getMessage");
        check(source.equals(create.getSource()), "getSource");
        check(create.getRoleName() == null, "creation notification has null roleName");
        check(create.getMBeansToUnregister().size() == 2, "getMBeansToUnregister size");

        // getMBeansToUnregister returns a defensive copy
        List<ObjectName> copy = create.getMBeansToUnregister();
        copy.clear();
        check(create.getMBeansToUnregister().size() == 2, "getMBeansToUnregister is defensive");

        // update constructor
        RelationNotification update = new RelationNotification(
                RelationNotification.RELATION_MBEAN_UPDATE, source, 101L, 201L,
                "updated", "relId-1", "myType", relObj, "roleX",
                new ArrayList<>(java.util.Arrays.asList(m1)),
                new ArrayList<>(java.util.Arrays.asList(m2)));
        check("roleX".equals(update.getRoleName()), "update getRoleName");
        check(update.getNewRoleValue().size() == 1 && m1.equals(update.getNewRoleValue().get(0)), "getNewRoleValue");
        check(update.getOldRoleValue().size() == 1 && m2.equals(update.getOldRoleValue().get(0)), "getOldRoleValue");
        check(update.getMBeansToUnregister().isEmpty(), "update notification has empty unregister list");

        // getNewRoleValue/getOldRoleValue are defensive
        List<ObjectName> nv = update.getNewRoleValue();
        nv.clear();
        check(update.getNewRoleValue().size() == 1, "getNewRoleValue is defensive");

        // validation: wrong type, null source, null id, null typeName
        try { new RelationNotification("bad.type", source, 1L, 1L, "m", "id", "t", relObj, null);
            check(false, "wrong creation type must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "wrong type message"); }
        try { new RelationNotification(RelationNotification.RELATION_BASIC_CREATION, null, 1L, 1L, "m", "id", "t", relObj, null);
            check(false, "null source must throw"); }
        catch (IllegalArgumentException e) { check("null source".equals(e.getMessage()), "null source message"); }
        try { new RelationNotification(RelationNotification.RELATION_BASIC_CREATION, source, 1L, 1L, "m", null, "t", relObj, null);
            check(false, "null id must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null id message"); }
        try { new RelationNotification(RelationNotification.RELATION_BASIC_CREATION, source, 1L, 1L, "m", "id", null, relObj, null);
            check(false, "null typeName must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null typeName message"); }
        try { new RelationNotification(RelationNotification.RELATION_BASIC_UPDATE, source, 1L, 1L, "m", "id", "t", relObj, null, new ArrayList<ObjectName>(), new ArrayList<ObjectName>());
            check(false, "null roleName in update must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null roleName message"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
