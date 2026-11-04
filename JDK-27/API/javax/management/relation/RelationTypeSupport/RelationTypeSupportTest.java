/*
 * @test
 * @summary javax.management.relation.RelationTypeSupport conformance: white-box structure + black-box behavior.
 * @run main RelationTypeSupportTest
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.relation.InvalidRelationTypeException;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleInfoNotFoundException;
import javax.management.relation.RelationType;
import javax.management.relation.RelationTypeSupport;

public class RelationTypeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static RoleInfo ri(String name) { try { return new RoleInfo(name, "java.lang.String"); } catch (Exception e) { throw new RuntimeException(e); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationTypeSupport");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationTypeSupport must be public");
        check(!c.isInterface(), "RelationTypeSupport must be a class");
        check(RelationType.class.isAssignableFrom(c), "RelationTypeSupport must implement RelationType");
        check(java.io.Serializable.class.isAssignableFrom(c), "RelationTypeSupport must be Serializable");
        c.getConstructor(String.class, RoleInfo[].class);
        c.getMethod("getRelationTypeName"); c.getMethod("getRoleInfos");
        c.getMethod("getRoleInfo", String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RelationTypeSupport rts = new RelationTypeSupport("myType",
                new RoleInfo[]{ ri("roleB"), ri("roleA") });
        check("myType".equals(rts.getRelationTypeName()), "getRelationTypeName");

        // getRoleInfos returns all role infos (order unspecified -> sort names)
        List<RoleInfo> infos = rts.getRoleInfos();
        check(infos.size() == 2, "getRoleInfos size");
        List<String> names = new ArrayList<>();
        for (RoleInfo r : infos) names.add(r.getName());
        Collections.sort(names);
        check("roleA,roleB".equals(String.join(",", names)), "getRoleInfos contains both roles");

        // getRoleInfo
        RoleInfo a = rts.getRoleInfo("roleA");
        check(a != null && "roleA".equals(a.getName()), "getRoleInfo existing");
        check(a.isReadable() && a.isWritable(), "stored RoleInfo defaults read/write true");

        try { rts.getRoleInfo(null); check(false, "getRoleInfo(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "getRoleInfo null message"); }
        try { rts.getRoleInfo("nope"); check(false, "getRoleInfo(unknown) must throw"); }
        catch (RoleInfoNotFoundException e) { check("No role info for role nope".equals(e.getMessage()), "unknown role message: " + e.getMessage()); }

        // constructor validation
        try { new RelationTypeSupport(null, new RoleInfo[]{ ri("r") }); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null name message"); }
        try { new RelationTypeSupport("t", (RoleInfo[]) null); check(false, "null array must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null array message"); }
        try { new RelationTypeSupport("t", new RoleInfo[0]); check(false, "empty array must throw"); }
        catch (InvalidRelationTypeException e) { check("No role info provided.".equals(e.getMessage()), "empty array message"); }
        try { new RelationTypeSupport("t", new RoleInfo[]{ null }); check(false, "null element must throw"); }
        catch (InvalidRelationTypeException e) { check("Null role info provided.".equals(e.getMessage()), "null element message"); }
        try { new RelationTypeSupport("t", new RoleInfo[]{ ri("r"), ri("r") }); check(false, "duplicate role must throw"); }
        catch (InvalidRelationTypeException e) { check("Two role infos provided for role r".equals(e.getMessage()), "dup message: " + e.getMessage()); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
