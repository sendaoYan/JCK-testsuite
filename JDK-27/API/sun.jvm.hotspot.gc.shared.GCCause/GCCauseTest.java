/*
 * @test
 * @summary sun.jvm.hotspot.gc.shared.GCCause conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.gc.shared:+open
 * @run main GCCauseTest
 */
import java.lang.reflect.*;

public class GCCauseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.gc.shared.GCCause", false, GCCauseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == true, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasField(c, "_java_lang_system_gc"), "field _java_lang_system_gc");
        check(hasField(c, "_full_gc_alot"), "field _full_gc_alot");
        check(hasField(c, "_scavenge_alot"), "field _scavenge_alot");
        check(hasField(c, "_allocation_profiler"), "field _allocation_profiler");
        check(hasField(c, "_jvmti_force_gc"), "field _jvmti_force_gc");
        check(hasField(c, "_heap_inspection"), "field _heap_inspection");
        check(hasField(c, "_heap_dump"), "field _heap_dump");
        check(hasField(c, "_wb_young_gc"), "field _wb_young_gc");
        check(hasField(c, "_wb_full_gc"), "field _wb_full_gc");
        check(hasField(c, "_wb_breakpoint"), "field _wb_breakpoint");
        check(hasField(c, "_no_gc"), "field _no_gc");
        check(hasField(c, "_allocation_failure"), "field _allocation_failure");
        check(hasField(c, "_codecache_GC_threshold"), "field _codecache_GC_threshold");
        check(hasField(c, "_codecache_GC_aggressive"), "field _codecache_GC_aggressive");
        check(hasField(c, "_metadata_GC_threshold"), "field _metadata_GC_threshold");
        check(hasField(c, "_metadata_GC_clear_soft_refs"), "field _metadata_GC_clear_soft_refs");
        check(hasField(c, "_g1_inc_collection_pause"), "field _g1_inc_collection_pause");
        check(hasField(c, "_g1_compaction_pause"), "field _g1_compaction_pause");
        check(hasField(c, "_g1_humongous_allocation"), "field _g1_humongous_allocation");
        check(hasField(c, "_g1_periodic_collection"), "field _g1_periodic_collection");
        check(hasField(c, "_dcmd_gc_run"), "field _dcmd_gc_run");
        check(hasField(c, "_shenandoah_stop_vm"), "field _shenandoah_stop_vm");
        check(hasField(c, "_shenandoah_allocation_failure_evac"), "field _shenandoah_allocation_failure_evac");
        check(hasField(c, "_shenandoah_humongous_allocation_failure"), "field _shenandoah_humongous_allocation_failure");
        check(hasField(c, "_shenandoah_concurrent_gc"), "field _shenandoah_concurrent_gc");
        check(hasField(c, "_shenandoah_upgrade_to_full_gc"), "field _shenandoah_upgrade_to_full_gc");
        check(hasField(c, "_z_timer"), "field _z_timer");
        check(hasField(c, "_z_warmup"), "field _z_warmup");
        check(hasField(c, "_z_allocation_rate"), "field _z_allocation_rate");
        check(hasField(c, "_z_allocation_stall"), "field _z_allocation_stall");
        check(hasField(c, "_z_proactive"), "field _z_proactive");
        check(hasField(c, "_z_high_usage"), "field _z_high_usage");
        check(hasField(c, "_last_gc_cause"), "field _last_gc_cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
