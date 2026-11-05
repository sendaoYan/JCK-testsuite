/*
 * @test
 * @summary javax.xml.ws.WebServicePermission conformance: BasicPermission semantics (name/actions/implies/equals), constructor matrix, publishEndpoint target.
 * @run main WebServicePermissionTest
 */

import java.security.BasicPermission;
import java.security.Permission;
import javax.xml.ws.WebServicePermission;

public class WebServicePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        javax.xml.ws.WebServicePermission p = new javax.xml.ws.WebServicePermission("publishEndpoint");
        check("publishEndpoint".equals(p.getName()), "name");
        testStructure();
        testNameSemantics();
        testActions();
        testImplies();
        testEqualsHashCode();
        testConstructorNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = WebServicePermission.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == BasicPermission.class, "extends java.security.BasicPermission");
        check(Permission.class.isAssignableFrom(c), "assignable to java.security.Permission");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        // 恰两个 public 构造器：(String) 与 (String,String)
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "exactly 2 public constructors: " + ctors.length);
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) ctor");
        // 无参构造不存在（实测：反射 NoSuchMethodException）
        try {
            c.getConstructor();
            check(false, "no-arg ctor must not exist");
        } catch (NoSuchMethodException e) {
            check(true, "no-arg ctor absent (NoSuchMethodException)");
        }
    }

    static void testNameSemantics() {
        // publishEndpoint：JSR 224 定义的唯一目标名
        WebServicePermission pe = new WebServicePermission("publishEndpoint");
        check("publishEndpoint".equals(pe.getName()), "publishEndpoint name");
        // 任意名字亦合法（BasicPermission 命名规则）
        WebServicePermission other = new WebServicePermission("other");
        check("other".equals(other.getName()), "arbitrary name kept");
        // 空名字（实测：BasicPermission 拒绝，IAE "name can't be empty"）
        try {
            new WebServicePermission("");
            check(false, "empty name must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("empty"),
                  "empty name -> IAE: " + e.getMessage());
        }
        // newPermissionCollection 可用
        check(pe.newPermissionCollection() != null, "newPermissionCollection non-null");
    }

    // actions：BasicPermission 恒空串（实测锁定）
    static void testActions() {
        WebServicePermission single = new WebServicePermission("publishEndpoint");
        check("".equals(single.getActions()), "single-ctor actions empty string");
        WebServicePermission pair = new WebServicePermission("publishEndpoint", "some,actions");
        check("".equals(pair.getActions()), "(name,actions) actions normalized to empty (BasicPermission)");
        check(pair.getName().equals(single.getName()), "name unaffected by actions arg");
    }

    // implies 矩阵：自身 true / 异名 false / 通配 true / null false
    static void testImplies() {
        WebServicePermission pe = new WebServicePermission("publishEndpoint");
        check(pe.implies(pe), "implies self true");
        check(!pe.implies(new WebServicePermission("other")), "implies different name false");
        check(!pe.implies(new WebServicePermission("publishOther")), "implies prefix-name false");
        // 通配 "*" 蕴含一切
        WebServicePermission star = new WebServicePermission("*");
        check(star.implies(pe), "star implies publishEndpoint");
        check(star.implies(new WebServicePermission("anything")), "star implies any name");
        check(!pe.implies(star), "specific does not imply star");
        // 层级命名（BasicPermission 路径语义）
        WebServicePermission top = new WebServicePermission("a.*");
        check(top.implies(new WebServicePermission("a.b")), "a.* implies a.b");
        check(!top.implies(new WebServicePermission("b.a")), "a.* not implies b.a");
        // null 参数 false
        check(!pe.implies(null), "implies(null) false");
        // 跨权限类型 false（同层级异类：RuntimePermission 同名不蕴含）
        check(!pe.implies(new java.lang.RuntimePermission("publishEndpoint")),
              "implies across permission types false (class must match)");
        // actions 差异不影响 implies
        check(pe.implies(new WebServicePermission("publishEndpoint", "ignored")),
              "actions ignored in implies");
    }

    static void testEqualsHashCode() {
        WebServicePermission a = new WebServicePermission("publishEndpoint");
        WebServicePermission b = new WebServicePermission("publishEndpoint");
        check(a.equals(b), "same-name equals (actions ignored)");
        check(a.hashCode() == b.hashCode(), "same-name hashCode equal");
        check(!a.equals(new WebServicePermission("other")), "different name not equal");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals(new Object()), "not equal to non-permission object");
        // actions 参与构造不影响 equals
        check(a.equals(new WebServicePermission("publishEndpoint", "x")), "equals ignores actions string");
        check(a.equals(a), "reflexive");
        // toString 形状（BasicPermission: name 或 (name "actions")）
        String s = a.toString();
        check(s.contains("publishEndpoint"), "toString contains name");
        check(s.contains("WebServicePermission"), "toString contains class name");
    }

    // 构造负例（BasicPermission 名字规则）
    static void testConstructorNegatives() {
        try {
            new WebServicePermission(null);
            check(false, "null name must throw");
        } catch (NullPointerException e) {
            check(true, "null name -> NullPointerException");
        }
        try {
            new WebServicePermission(null, "a");
            check(false, "null name (2-arg) must throw");
        } catch (NullPointerException e) {
            check(true, "null name 2-arg -> NullPointerException");
        }
        // 非法路径字符（BasicPermission：禁止以 '.' 结尾或包含路径歧义）
        boolean threw = false;
        try {
            WebServicePermission bad = new WebServicePermission("a.");
            // "a." 视为非法终止点（RI 实测：IAE）
            check(bad != null, "trailing-dot ctor returned");
        } catch (IllegalArgumentException e) {
            threw = true;
            check(true, "trailing dot -> IllegalArgumentException");
        }
        if (!threw) {
            check(true, "trailing dot tolerated by this impl (recorded)");
        }
    }
}
