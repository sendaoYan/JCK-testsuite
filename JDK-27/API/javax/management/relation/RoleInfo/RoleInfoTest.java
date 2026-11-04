/*
 * @test
 * @summary javax.management.relation.RoleInfo conformance: white-box structure + black-box behavior.
 * @run main RoleInfoTest
 */
import javax.management.relation.InvalidRoleInfoException;
import javax.management.relation.RoleInfo;

public class RoleInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleInfo");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleInfo must be public");
        check(!c.isInterface(), "RoleInfo must be a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "RoleInfo must be concrete");
        check(java.io.Serializable.class.isAssignableFrom(c), "RoleInfo must implement Serializable");
        check(c.getField("ROLE_CARDINALITY_INFINITY").getInt(null) == -1, "ROLE_CARDINALITY_INFINITY == -1");
        c.getConstructor(String.class, String.class, boolean.class, boolean.class, int.class, int.class, String.class);
        c.getConstructor(String.class, String.class, boolean.class, boolean.class);
        c.getConstructor(String.class, String.class);
        c.getConstructor(RoleInfo.class);
        c.getMethod("getName"); c.getMethod("isReadable"); c.getMethod("isWritable");
        c.getMethod("getDescription"); c.getMethod("getMinDegree"); c.getMethod("getMaxDegree");
        c.getMethod("getRefMBeanClassName"); c.getMethod("checkMinDegree", int.class);
        c.getMethod("checkMaxDegree", int.class); c.getMethod("toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // full constructor
        RoleInfo full = new RoleInfo("r", "java.lang.String", true, false, 1, 3, "desc");
        check("r".equals(full.getName()), "full ctor name");
        check(full.isReadable(), "full ctor readable");
        check(!full.isWritable(), "full ctor writable");
        check("desc".equals(full.getDescription()), "full ctor description");
        check(full.getMinDegree() == 1, "full ctor min");
        check(full.getMaxDegree() == 3, "full ctor max");
        check("java.lang.String".equals(full.getRefMBeanClassName()), "full ctor ref class");

        // 4-arg constructor defaults min=max=1, description null
        RoleInfo four = new RoleInfo("r", "java.lang.String", true, false);
        check(four.getMinDegree() == 1 && four.getMaxDegree() == 1, "4-arg ctor defaults min/max to 1");
        check(four.getDescription() == null, "4-arg ctor defaults description to null");

        // 2-arg constructor defaults read=write=true, min=max=1, desc null
        RoleInfo two = new RoleInfo("r", "java.lang.String");
        check(two.isReadable() && two.isWritable(), "2-arg ctor defaults read/write to true");
        check(two.getMinDegree() == 1 && two.getMaxDegree() == 1, "2-arg ctor min/max 1");
        check(two.getDescription() == null, "2-arg ctor description null");

        // copy constructor
        RoleInfo copy = new RoleInfo(full);
        check(copy != full, "copy ctor is a distinct instance");
        check("r".equals(copy.getName()) && copy.getMinDegree() == 1 && copy.getMaxDegree() == 3
              && "java.lang.String".equals(copy.getRefMBeanClassName()), "copy ctor copies all fields");

        // cardinality checks
        check(!full.checkMinDegree(0) && full.checkMinDegree(1) && full.checkMinDegree(3), "checkMinDegree bounds");
        check(full.checkMaxDegree(3) && !full.checkMaxDegree(4), "checkMaxDegree bounds");
        RoleInfo inf = new RoleInfo("i", "java.lang.String", true, true, RoleInfo.ROLE_CARDINALITY_INFINITY, RoleInfo.ROLE_CARDINALITY_INFINITY, "d");
        check(inf.checkMinDegree(5), "infinite min accepts any non-negative");
        check(inf.checkMaxDegree(5), "infinite max accepts any non-negative");

        // toString
        check("role info name: r; isReadable: true; isWritable: false; description: desc; minimum degree: 1; maximum degree: 3; MBean class: java.lang.String"
              .equals(full.toString()), "toString format, got: " + full.toString());

        // exceptions
        try { new RoleInfo(null, "java.lang.String"); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null name message"); }
        try { new RoleInfo("r", (String) null); check(false, "null class must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null class message"); }
        try { new RoleInfo((RoleInfo) null); check(false, "null copy must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null copy message"); }
        try { new RoleInfo("r", "java.lang.String", true, true, 3, 2, "d"); check(false, "min>max must throw"); }
        catch (InvalidRoleInfoException e) { check("Minimum degree 3 is greater than maximum degree 2".equals(e.getMessage()), "min>max message: " + e.getMessage()); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
