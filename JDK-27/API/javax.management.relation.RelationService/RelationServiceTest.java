/*
 * @test
 * @summary javax.management.relation.RelationService conformance: white-box structure + black-box behavior.
 * @run main RelationServiceTest
 */
import java.util.List;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleInfoNotFoundException;
import javax.management.relation.RoleStatus;

public class RelationServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static RoleInfo ri(String name, int min, int max) { try { return new RoleInfo(name, "java.lang.String", true, true, min, max, "d"); } catch (Exception e) { throw new RuntimeException(e); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationService");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationService must be public");
        check(!c.isInterface(), "RelationService must be a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "RelationService must be concrete");
        check(RelationServiceMBean.class.isAssignableFrom(c), "RelationService must implement RelationServiceMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "RelationService must implement MBeanRegistration");
        check(NotificationListener.class.isAssignableFrom(c), "RelationService must implement NotificationListener");
        c.getConstructor(boolean.class);
        c.getMethod("isActive"); c.getMethod("getPurgeFlag"); c.getMethod("setPurgeFlag", boolean.class);
        c.getMethod("createRelationType", String.class, RoleInfo[].class);
        c.getMethod("getAllRelationTypeNames"); c.getMethod("getAllRelationIds");
        c.getMethod("getRoleInfos", String.class); c.getMethod("getRoleInfo", String.class, String.class);
        c.getMethod("checkRoleReading", String.class, String.class);
        c.getMethod("getNotificationInfo");
    }

    // ---- Part 2: black-box (behavioral, unregistered service) ----
    static void testBlackBox() throws Exception {
        RelationService rs = new RelationService(false);
        check(!rs.getPurgeFlag(), "getPurgeFlag reflects constructor false");
        rs.setPurgeFlag(true);
        check(rs.getPurgeFlag(), "setPurgeFlag(true) then getPurgeFlag");

        RelationService rsTrue = new RelationService(true);
        check(rsTrue.getPurgeFlag(), "getPurgeFlag reflects constructor true");

        // unregistered service must reject isActive with exact message
        try { rs.isActive(); check(false, "isActive on unregistered service must throw"); }
        catch (RelationServiceNotRegisteredException e) {
            check("Relation Service not registered in the MBean Server.".equals(e.getMessage()), "isActive message");
        }

        // initially empty registries
        check(rs.getAllRelationTypeNames().isEmpty(), "getAllRelationTypeNames empty");
        check(rs.getAllRelationIds().isEmpty(), "getAllRelationIds empty");

        // null checks
        try { rs.hasRelation(null); check(false, "hasRelation(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "hasRelation null message"); }
        check(Boolean.FALSE.equals(rs.hasRelation("no-such")), "hasRelation returns false for unknown");
        try { rs.isRelation(null); check(false, "isRelation(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "isRelation null message"); }
        check(rs.isRelation(on("d:type=x")) == null, "isRelation returns null for unknown ObjectName");
        try { rs.isRelationMBean(null); check(false, "isRelationMBean(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "isRelationMBean null message"); }
        try { rs.isRelationMBean("no-such"); check(false, "isRelationMBean(unknown) must throw"); }
        catch (RelationNotFoundException e) { check("No relation associated to relation id no-such".equals(e.getMessage()), "isRelationMBean unknown message"); }

        // checkRoleReading / findRelationsOfType null + unknown-type errors
        try { rs.checkRoleReading(null, "t"); check(false, "checkRoleReading(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "checkRoleReading null message"); }
        try { rs.checkRoleReading("r", "no-type"); check(false, "checkRoleReading(unknown type) must throw"); }
        catch (RelationTypeNotFoundException e) { check("No relation type created in the Relation Service with the name no-type".equals(e.getMessage()), "checkRoleReading unknown type message"); }
        try { rs.findRelationsOfType(null); check(false, "findRelationsOfType(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "findRelationsOfType null message"); }
        try { rs.findRelationsOfType("no-type"); check(false, "findRelationsOfType(unknown) must throw"); }
        catch (RelationTypeNotFoundException e) { check("No relation type created in the Relation Service with the name no-type".equals(e.getMessage()), "findRelationsOfType unknown message"); }

        // createRelationType works even when unregistered
        rs.createRelationType("myType", new RoleInfo[]{ ri("roleR", 1, 3) });
        List<String> typeNames = rs.getAllRelationTypeNames();
        check(typeNames.size() == 1 && "myType".equals(typeNames.get(0)), "createRelationType registers type");

        // getRoleInfos / getRoleInfo on the created type
        List<RoleInfo> infos = rs.getRoleInfos("myType");
        check(infos.size() == 1 && "roleR".equals(infos.get(0).getName()), "getRoleInfos for created type");
        RoleInfo roleR = rs.getRoleInfo("myType", "roleR");
        check("roleR".equals(roleR.getName()), "getRoleInfo existing");
        try { rs.getRoleInfo("myType", "nope"); check(false, "getRoleInfo unknown role must throw"); }
        catch (RoleInfoNotFoundException e) { check("No role info for role nope".equals(e.getMessage()), "getRoleInfo unknown role message"); }

        // checkRoleReading on a readable role returns 0
        check(Integer.valueOf(0).equals(rs.checkRoleReading("roleR", "myType")), "checkRoleReading readable returns 0");
        check(Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME).equals(rs.checkRoleReading("nope", "myType")), "checkRoleReading unknown role returns NO_ROLE_WITH_NAME");

        // checkRoleWriting cardinality violation (no MBeanServer access needed)
        Role emptyRole = new Role("roleR", new java.util.ArrayList<javax.management.ObjectName>());
        check(Integer.valueOf(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE).equals(rs.checkRoleWriting(emptyRole, "myType", Boolean.FALSE)),
              "checkRoleWriting empty role returns LESS_THAN_MIN_ROLE_DEGREE");

        // findRelationsOfType for a known type with no relations -> empty list
        check(rs.findRelationsOfType("myType").isEmpty(), "findRelationsOfType empty for no relations");

        // removeRelationType requires an active (registered) service
        try { rs.removeRelationType("myType"); check(false, "removeRelationType unregistered must throw"); }
        catch (RelationServiceNotRegisteredException e) { check("Relation Service not registered in the MBean Server.".equals(e.getMessage()), "removeRelationType unregistered message"); }

        // getNotificationInfo exposes a single RelationNotification info with 6 types
        MBeanNotificationInfo[] infos2 = rs.getNotificationInfo();
        check(infos2.length == 1, "getNotificationInfo length 1");
        check("javax.management.relation.RelationNotification".equals(infos2[0].getName()), "notification class name");
        check(infos2[0].getNotifTypes().length == 6, "6 notification types");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
