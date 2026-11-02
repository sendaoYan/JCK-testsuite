/*
 * @test
 * @summary sun.java2d.cmm.lcms.LCMSTransform conformance: reflection (no init, native/display-heavy).
 * @run main LCMSTransformTest
 */

import java.util.*;
import java.lang.reflect.*;

public class LCMSTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.cmm.lcms.LCMSTransform", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.cmm.lcms.LCMSTransform".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.cmm.ColorTransform".equals(interfaces(c)), "interfaces");
        check("private synchronized#void#doTransform(sun.java2d.cmm.lcms.LCMSImageLayout,sun.java2d.cmm.lcms.LCMSImageLayout);public#[B#colorConvert([B,[B);public#[S#colorConvert([S,[S);public#int#getNumInComponents();public#int#getNumOutComponents();public#void#colorConvert(java.awt.image.BufferedImage,java.awt.image.BufferedImage);public#void#colorConvert(java.awt.image.Raster,java.awt.image.WritableRaster);public#void#colorConvert(java.awt.image.Raster,java.awt.image.WritableRaster,[F,[F,[F,[F)".equals(methods(c)), "methods");
        check("#[Ljava.awt.color.ICC_Profile;#profiles;#[Lsun.java2d.cmm.lcms.LCMSProfile;#lcmsProfiles;#int#renderType;#int#transformType;#long#ID;private#boolean#isInIntPacked;private#boolean#isOutIntPacked;private#int#inFormatter;private#int#numInComponents;private#int#numOutComponents;private#int#outFormatter;private#java.lang.Object#disposerReferent".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
