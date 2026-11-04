/*
 * @test
 * @summary sun.java2d.marlin.RendererStats conformance: reflection (no init, native/display-heavy).
 * @run main RendererStatsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RendererStatsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.RendererStats", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.RendererStats".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.marlin.MarlinConst".equals(interfaces(c)), "interfaces");
        check("#void#dump();public static#void#dumpStats();static#sun.java2d.marlin.RendererStats#getInstance()".equals(methods(c)), "methods");
        check("final#[Lsun.java2d.marlin.stats.Monitor;#monitors;final#[Lsun.java2d.marlin.stats.StatLong;#statistics;final#java.util.concurrent.ConcurrentLinkedQueue#allContexts;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_adds;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_msorts;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_msorts_adds;final#sun.java2d.marlin.stats.Histogram#hist_rdr_crossings_ratio;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_alpha;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_dist;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_ratio;final#sun.java2d.marlin.stats.Histogram#hist_tile_generator_encoding_runLen;final#sun.java2d.marlin.stats.Monitor#mon_debug;final#sun.java2d.marlin.stats.Monitor#mon_npi_currentSegment;final#sun.java2d.marlin.stats.Monitor#mon_pipe_renderTiles;final#sun.java2d.marlin.stats.Monitor#mon_pre_getAATileGenerator;final#sun.java2d.marlin.stats.Monitor#mon_ptg_getAlpha;final#sun.java2d.marlin.stats.Monitor#mon_rdr_addLine;final#sun.java2d.marlin.stats.Monitor#mon_rdr_copyAARow;final#sun.java2d.marlin.stats.Monitor#mon_rdr_endRendering;final#sun.java2d.marlin.stats.Monitor#mon_rdr_endRendering_Y;final#sun.java2d.marlin.stats.StatLong#stat_array_dasher_dasher;final#sun.java2d.marlin.stats.StatLong#stat_array_dasher_firstSegmentsBuffer;final#sun.java2d.marlin.stats.StatLong#stat_array_marlincache_rowAAChunk;final#sun.java2d.marlin.stats.StatLong#stat_array_marlincache_touchedTile;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_alphaline;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_aux_crossings;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_aux_edgePtrs;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_crossings;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgeBucketCounts;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgeBuckets;final#sun.java2d.marlin.stats.StatLong#stat_array_renderer_edgePtrs;final#sun.java2d.marlin.stats.StatLong#stat_array_stroker_polystack_curveTypes;final#sun.java2d.marlin.stats.StatLong#stat_array_stroker_polystack_curves;final#sun.java2d.marlin.stats.StatLong#stat_cache_rowAA;final#sun.java2d.marlin.stats.StatLong#stat_cache_rowAAChunk;final#sun.java2d.marlin.stats.StatLong#stat_cache_tiles;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_adds;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_adds_high;final#sun.java2d.marlin.stats.StatLong#stat_rdr_activeEdges_updates;final#sun.java2d.marlin.stats.StatLong#stat_rdr_addLine;final#sun.java2d.marlin.stats.StatLong#stat_rdr_addLine_skip;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_bsearch;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_msorts;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_sorts;final#sun.java2d.marlin.stats.StatLong#stat_rdr_crossings_updates;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak_dec;final#sun.java2d.marlin.stats.StatLong#stat_rdr_curveBreak_inc;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges_count;final#sun.java2d.marlin.stats.StatLong#stat_rdr_edges_resizes;final#sun.java2d.marlin.stats.StatLong#stat_rdr_poly_stack_curves;final#sun.java2d.marlin.stats.StatLong#stat_rdr_poly_stack_types;final#sun.java2d.marlin.stats.StatLong#stat_rdr_quadBreak;final#sun.java2d.marlin.stats.StatLong#stat_rdr_quadBreak_dec;private static volatile#sun.java2d.marlin.RendererStats#singleton".equals(fields(c)), "fields");
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
            s.append(Modifier.toString(m.getModifiers())).append('#');
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
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
