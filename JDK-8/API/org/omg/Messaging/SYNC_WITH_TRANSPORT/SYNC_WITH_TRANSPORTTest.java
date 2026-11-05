/*
 * @test
 * @summary org.omg.Messaging.SYNC_WITH_TRANSPORT conformance: constant holder interface.
 * @run main SYNC_WITH_TRANSPORTTest
 */

public class SYNC_WITH_TRANSPORTTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.Messaging.SYNC_WITH_TRANSPORT");
        check(c.isInterface(), "is interface");
        java.lang.reflect.Field f = c.getField("value");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "value is static");
        check(java.lang.reflect.Modifier.isPublic(f.getModifiers()), "value is public");
        check(f.getType() == short.class, "value is short");
        // ---- deepened (jdk8u372-b07 probed): Messaging sync-scope constant exact contract ----
        check(org.omg.Messaging.SYNC_WITH_TRANSPORT.value == (short) 1, "SYNC_WITH_TRANSPORT.value == 1");
        check(c.getDeclaredFields().length == 1, "exactly one declared field");
        check(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "value is final");
        // only SYNC_WITH_TRANSPORT is materialized in jdk8u372 rt.jar; SYNC_NONE(0)/SYNC_WITH_SERVER(2)/
        // SYNC_WITH_TARGET(3) from the Messaging spec are NOT shipped as classes in this JDK
        boolean noneAbsent = true;
        try { Class.forName("org.omg.Messaging.SYNC_NONE"); noneAbsent = false; } catch (ClassNotFoundException cn) { }
        check(noneAbsent, "org.omg.Messaging.SYNC_NONE not materialized in jdk8u372 rt.jar");
        boolean serverAbsent = true;
        try { Class.forName("org.omg.Messaging.SYNC_WITH_SERVER"); serverAbsent = false; } catch (ClassNotFoundException cn) { }
        check(serverAbsent, "SYNC_WITH_SERVER not materialized");
        boolean targetAbsent = true;
        try { Class.forName("org.omg.Messaging.SYNC_WITH_TARGET"); targetAbsent = false; } catch (ClassNotFoundException cn) { }
        check(targetAbsent, "SYNC_WITH_TARGET not materialized");
        check(org.omg.Messaging.SyncScopeHelper.id().equals("IDL:omg.org/Messaging/SyncScope:1.0"),
              "SyncScope typedef repository id exact");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
