/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.CollisionCheckStack conformance: class hierarchy and members.
 * @run main CollisionCheckStackTest
 */

public class CollisionCheckStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.CollisionCheckStack", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.CollisionCheckStack"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.util.AbstractList".equals(c.getSuperclass().getName()), "extends java.util.AbstractList");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("expandCapacity"), "has method expandCapacity");
        check(mn.contains("findDuplicate"), "has method findDuplicate");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getCycleString"), "has method getCycleString");
        check(mn.contains("getLatestPushResult"), "has method getLatestPushResult");
        check(mn.contains("getUseIdentity"), "has method getUseIdentity");
        check(mn.contains("hash"), "has method hash");
        check(mn.contains("peek"), "has method peek");
        check(mn.contains("pop"), "has method pop");
        check(mn.contains("push"), "has method push");
        check(mn.contains("pushNocheck"), "has method pushNocheck");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setUseIdentity"), "has method setUseIdentity");
        check(mn.contains("size"), "has method size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
