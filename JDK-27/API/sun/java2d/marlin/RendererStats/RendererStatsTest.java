/*
 * @test
 * @summary sun.java2d.marlin.RendererStats conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.marlin:+open
 * @run main RendererStatsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RendererStatsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.RendererStats", false, RendererStatsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.RendererStats".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.marlin.MarlinConst".equals(interfaces(c)), "interfaces");
        check("#void#dump();public static#void#dumpStats();static#sun.java2d.marlin.RendererStats#createInstance(java.lang.Object,java.lang.String)".equals(methods(c)), "methods");
        check("#[Lsun.java2d.marlin.ArrayCacheConst$CacheStats;#cacheStats;#long#totalOffHeap;#long#totalOffHeapInitial;#long#totalOffHeapMax;final#[Lsun.java2d.marlin.stats.Monitor;#monitors;final#[Lsun.java2d.marlin.stats.StatLong;#statistics;final#java.lang.String#name;final#sun.java2d.marlin.stats.Histogram#hist_cpd_polystack_curves;final#sun.java2d.marlin.stats.Histogram#hist_pcf_idxstack_indices;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_adds;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_msorts;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_msorts_adds;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_ratio;final#sun.java2d.marlin.stats.Histogram#hist_rdr_edges_count;final#sun.java2d.marlin.stats.Histogram#hist_str_polystack_curves;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_alpha;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_dist;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_ratio;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_runLen;final#sun.java2d.marlin.stats.Monitor#mon_debug;final#sun.java2d.marlin.stats.Monitor#mon_pipe_renderTiles;final#sun.java2d.marlin.stats.Monitor#mon_pre_getAATileGenerator;final#sun.java2d.marlin.stats.Monitor#mon_ptg_getAlpha;final#sun.java2d.marlin.stats.Monitor#mon_rdr_addLine;final#sun.java2d.marlin.stats.Monitor#mon_rdr_copyAARow;final#sun.java2d.marlin.stats.Monitor#mon_rdr_endRendering;final#sun.java2d.marlin.stats.Monitor#mon_rdr_endRendering_Y;final#sun.java2d.marlin.stats.StatLong#stat_array_cpd_polystack_curves;final#sun.java2d.marlin.stats.StatLong#stat_array_cpd_polystack_types;final#sun.java2d.marlin.stats.StatLong#stat_array_dasher_dasher;final#sun.java2d.marlin.stats.StatLong#stat_array_dasher_firstSegmentsBuffer;final#sun.java2d.marlin.stats.StatLong#stat_array_marlincache_rowAAChunk;final#sun.java2d.marlin.stats.StatLong#stat_array_marlincache_touchedTile;final#sun.java2d.marlin.stats.StatLong#stat_array_pcf_idxstack_indices;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_alphaline;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_aux_crossings;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_aux_edgePtrs;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_crossings;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgeBucketCounts;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgeBuckets;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgePtrs;final#sun.java2d.marlin.stats.StatLong#stat_array_str_polystack_curves;final#sun.java2d.marlin.stats.StatLong#stat_array_str_polystack_types;final#sun.java2d.marlin.stats.StatLong#stat_cache_rowAA;final#sun.java2d.marlin.stats.StatLong#stat_cache_rowAAChunk;final#sun.java2d.marlin.stats.StatLong#stat_cache_tiles;final#sun.java2d.marlin.stats.StatLong#stat_cpd_polystack_curves;final#sun.java2d.marlin.stats.StatLong#stat_cpd_polystack_types;final#sun.java2d.marlin.stats.StatLong#stat_pcf_idxstack_indices;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_adds;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_adds_high;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_updates;final#sun.java2d.marlin.stats.StatLong#stat_rdr_addLine;final#sun.java2d.marlin.stats.StatLong#stat_rdr_addLine_skip;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_bsearch;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_dpqs;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_msorts;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_sorts;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_updates;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak_dec;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak_inc;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges_count;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges_resizes;final#sun.java2d.marlin.stats.StatLong#stat_rdr_quadBreak;final#sun.java2d.marlin.stats.StatLong#stat_rdr_quadBreak_dec;final#sun.java2d.marlin.stats.StatLong#stat_str_polystack_curves;final#sun.java2d.marlin.stats.StatLong#stat_str_polystack_types".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
