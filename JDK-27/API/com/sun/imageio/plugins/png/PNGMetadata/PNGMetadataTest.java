/*
 * @test
 * @summary com.sun.imageio.plugins.png.PNGMetadata conformance: internal class reflection (no init, headless-safe).
 * @modules java.desktop/com.sun.imageio.plugins.png:+open
 * @run main PNGMetadataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PNGMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.png.PNGMetadata", false, PNGMetadataTest.class.getClassLoader());
        check(c != null, "forName");
        check("com.sun.imageio.plugins.png.PNGMetadata".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && "javax.imageio.metadata.IIOMetadata".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Cloneable".equals(ifaces(c)), "interfaces");
        check("clone;getAsTree;getStandardChromaNode;getStandardCompressionNode;getStandardDataNode;getStandardDimensionNode;getStandardDocumentNode;getStandardTextNode;getStandardTransparencyNode;initialize;isReadOnly;mergeTree;reset".equals(methodNames(c)), "public/protected methods");
        check("IHDR_bitDepth;IHDR_colorType;IHDR_compressionMethod;IHDR_filterMethod;IHDR_height;IHDR_interlaceMethod;IHDR_present;IHDR_width;PHYS_UNIT_METER;PHYS_UNIT_UNKNOWN;PLTE_blue;PLTE_green;PLTE_order;PLTE_present;PLTE_red;bKGD_blue;bKGD_colorType;bKGD_gray;bKGD_green;bKGD_index;bKGD_present;bKGD_red;cHRM_blueX;cHRM_blueY;cHRM_greenX;cHRM_greenY;cHRM_present;cHRM_redX;cHRM_redY;cHRM_whitePointX;cHRM_whitePointY;creation_time_day;creation_time_hour;creation_time_minute;creation_time_month;creation_time_offset;creation_time_present;creation_time_second;creation_time_year;gAMA_gamma;gAMA_present;hIST_histogram;hIST_present;iCCP_compressedProfile;iCCP_compressionMethod;iCCP_present;iCCP_profileName;iTXt_compressionFlag;iTXt_compressionMethod;iTXt_keyword;iTXt_languageTag;iTXt_text;iTXt_translatedKeyword;nativeMetadataFormatClassName;nativeMetadataFormatName;pHYs_pixelsPerUnitXAxis;pHYs_pixelsPerUnitYAxis;pHYs_present;pHYs_unitSpecifier;sBIT_alphaBits;sBIT_blueBits;sBIT_colorType;sBIT_grayBits;sBIT_greenBits;sBIT_present;sBIT_redBits;sPLT_alpha;sPLT_blue;sPLT_frequency;sPLT_green;sPLT_paletteName;sPLT_present;sPLT_red;sPLT_sampleDepth;sRGB_present;sRGB_renderingIntent;tEXt_creationTimeKey;tEXt_creation_time_present;tEXt_keyword;tEXt_text;tIME_day;tIME_hour;tIME_minute;tIME_month;tIME_present;tIME_second;tIME_year;tRNS_alpha;tRNS_blue;tRNS_colorType;tRNS_gray;tRNS_green;tRNS_present;tRNS_red;unknownChunkData;unknownChunkType;zTXt_compressionMethod;zTXt_keyword;zTXt_text".equals(fieldNames(c)), "public/protected fields");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String ifaces(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return String.join(";", s);
    }

    static String methodNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return String.join(";", s);
    }

    static String fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            int fm = f.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(f.getName());
        }
        return String.join(";", s);
    }

    static String enumConstants(Class<?> c) {
        Set<String> s = new TreeSet<String>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
        return String.join(";", s);
    }
}
