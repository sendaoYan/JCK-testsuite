/*
 * @test
 * @summary sun.font.HBShaper conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main HBShaperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class HBShaperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.HBShaper", false, HBShaperTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.HBShaper".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static final#int#HBFloatToFixed(float);private static transient#java.lang.foreign.FunctionDescriptor#getFunctionDescriptor(java.lang.foreign.MemoryLayout,[Ljava.lang.foreign.MemoryLayout;);private static transient#java.lang.foreign.MemorySegment#getUpcallStub(java.lang.foreign.Arena,java.lang.String,java.lang.foreign.MemoryLayout,[Ljava.lang.foreign.MemoryLayout;);private static#int#getFontTableData(int,java.lang.foreign.MemorySegment);private static#int#get_glyph_contour_point(java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,int,int,java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment);private static#int#get_glyph_h_advance(java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,int,java.lang.foreign.MemorySegment);private static#int#get_glyph_v_advance(java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,int,java.lang.foreign.MemorySegment);private static#int#get_nominal_glyph(java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,int,java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment);private static#int#get_variation_glyph(java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment,int,int,java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment);private static#java.lang.invoke.MethodHandle#getMethodHandle(java.lang.String,java.lang.foreign.FunctionDescriptor);private static#java.lang.invoke.VarHandle#getVarHandle(java.lang.foreign.StructLayout,java.lang.String);private static#void#store_layout_results(int,int,int,float,float,int,int,java.lang.foreign.MemorySegment,java.lang.foreign.MemorySegment);static#java.lang.foreign.MemorySegment#getFace(sun.font.Font2D);static#void#shape(sun.font.Font2D,sun.font.FontStrike,[F,java.lang.foreign.MemorySegment,[C,sun.font.GlyphLayout$GVData,int,int,int,int,java.awt.geom.Point2D$Float,int,int)".equals(methods(c)), "methods");
        check("private static final#float#HBFloatToFixedScale;private static final#java.lang.foreign.Linker#LINKER;private static final#java.lang.foreign.MemorySegment#get_contour_pt_stub;private static final#java.lang.foreign.MemorySegment#get_h_advance_stub;private static final#java.lang.foreign.MemorySegment#get_nominal_glyph_stub;private static final#java.lang.foreign.MemorySegment#get_table_data_fn_stub;private static final#java.lang.foreign.MemorySegment#get_v_advance_stub;private static final#java.lang.foreign.MemorySegment#get_var_glyph_stub;private static final#java.lang.foreign.MemorySegment#hb_jdk_font_funcs_struct;private static final#java.lang.foreign.MemorySegment#store_layout_results_stub;private static final#java.lang.foreign.StructLayout#GlyphInfoLayout;private static final#java.lang.foreign.StructLayout#PositionLayout;private static final#java.lang.foreign.SymbolLookup#SYM_LOOKUP;private static final#java.lang.foreign.UnionLayout#VarIntLayout;private static final#java.lang.invoke.MethodHandle#create_face_handle;private static final#java.lang.invoke.MethodHandle#dispose_face_handle;private static final#java.lang.invoke.MethodHandle#jdk_hb_shape_handle;private static final#java.lang.invoke.MethodHandle#malloc_handle;private static final#java.lang.invoke.MethodHandles$Lookup#MH_LOOKUP;private static final#java.lang.invoke.VarHandle#clusterHandle;private static final#java.lang.invoke.VarHandle#codePointHandle;private static final#java.lang.invoke.VarHandle#x_advanceHandle;private static final#java.lang.invoke.VarHandle#x_offsetHandle;private static final#java.lang.invoke.VarHandle#y_advanceHandle;private static final#java.lang.invoke.VarHandle#y_offsetHandle;private static final#java.util.WeakHashMap#faceMap;static final#java.lang.ScopedValue#scopedVars".equals(fields(c)), "fields");
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
