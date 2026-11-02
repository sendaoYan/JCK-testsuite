/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.TypeIndicators conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main TypeIndicatorsTest
 */
import java.lang.reflect.*;

public class TypeIndicatorsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.TypeIndicators", false, TypeIndicatorsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_REL_I386_ABSOLUTE"), "field IMAGE_REL_I386_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_I386_DIR16"), "field IMAGE_REL_I386_DIR16");
        check(hasField(c, "IMAGE_REL_I386_REL16"), "field IMAGE_REL_I386_REL16");
        check(hasField(c, "IMAGE_REL_I386_DIR32"), "field IMAGE_REL_I386_DIR32");
        check(hasField(c, "IMAGE_REL_I386_DIR32NB"), "field IMAGE_REL_I386_DIR32NB");
        check(hasField(c, "IMAGE_REL_I386_SEG12"), "field IMAGE_REL_I386_SEG12");
        check(hasField(c, "IMAGE_REL_I386_SECTION"), "field IMAGE_REL_I386_SECTION");
        check(hasField(c, "IMAGE_REL_I386_SECREL"), "field IMAGE_REL_I386_SECREL");
        check(hasField(c, "IMAGE_REL_I386_REL32"), "field IMAGE_REL_I386_REL32");
        check(hasField(c, "IMAGE_REL_MIPS_ABSOLUTE"), "field IMAGE_REL_MIPS_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_MIPS_REFHALF"), "field IMAGE_REL_MIPS_REFHALF");
        check(hasField(c, "IMAGE_REL_MIPS_REFWORD"), "field IMAGE_REL_MIPS_REFWORD");
        check(hasField(c, "IMAGE_REL_MIPS_JMPADDR"), "field IMAGE_REL_MIPS_JMPADDR");
        check(hasField(c, "IMAGE_REL_MIPS_REFHI"), "field IMAGE_REL_MIPS_REFHI");
        check(hasField(c, "IMAGE_REL_MIPS_REFLO"), "field IMAGE_REL_MIPS_REFLO");
        check(hasField(c, "IMAGE_REL_MIPS_GPREL"), "field IMAGE_REL_MIPS_GPREL");
        check(hasField(c, "IMAGE_REL_MIPS_LITERAL"), "field IMAGE_REL_MIPS_LITERAL");
        check(hasField(c, "IMAGE_REL_MIPS_SECTION"), "field IMAGE_REL_MIPS_SECTION");
        check(hasField(c, "IMAGE_REL_MIPS_SECREL"), "field IMAGE_REL_MIPS_SECREL");
        check(hasField(c, "IMAGE_REL_MIPS_SECRELLO"), "field IMAGE_REL_MIPS_SECRELLO");
        check(hasField(c, "IMAGE_REL_MIPS_SECRELHI"), "field IMAGE_REL_MIPS_SECRELHI");
        check(hasField(c, "IMAGE_REL_MIPS_JMPADDR16"), "field IMAGE_REL_MIPS_JMPADDR16");
        check(hasField(c, "IMAGE_REL_MIPS_REFWORDNB"), "field IMAGE_REL_MIPS_REFWORDNB");
        check(hasField(c, "IMAGE_REL_MIPS_PAIR"), "field IMAGE_REL_MIPS_PAIR");
        check(hasField(c, "IMAGE_REL_ALPHA_ABSOLUTE"), "field IMAGE_REL_ALPHA_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_ALPHA_REFLONG"), "field IMAGE_REL_ALPHA_REFLONG");
        check(hasField(c, "IMAGE_REL_ALPHA_REFQUAD"), "field IMAGE_REL_ALPHA_REFQUAD");
        check(hasField(c, "IMAGE_REL_ALPHA_GPREL32"), "field IMAGE_REL_ALPHA_GPREL32");
        check(hasField(c, "IMAGE_REL_ALPHA_LITERAL"), "field IMAGE_REL_ALPHA_LITERAL");
        check(hasField(c, "IMAGE_REL_ALPHA_LITUSE"), "field IMAGE_REL_ALPHA_LITUSE");
        check(hasField(c, "IMAGE_REL_ALPHA_GPDISP"), "field IMAGE_REL_ALPHA_GPDISP");
        check(hasField(c, "IMAGE_REL_ALPHA_BRADDR"), "field IMAGE_REL_ALPHA_BRADDR");
        check(hasField(c, "IMAGE_REL_ALPHA_HINT"), "field IMAGE_REL_ALPHA_HINT");
        check(hasField(c, "IMAGE_REL_ALPHA_INLINE_REFLONG"), "field IMAGE_REL_ALPHA_INLINE_REFLONG");
        check(hasField(c, "IMAGE_REL_ALPHA_REFHI"), "field IMAGE_REL_ALPHA_REFHI");
        check(hasField(c, "IMAGE_REL_ALPHA_REFLO"), "field IMAGE_REL_ALPHA_REFLO");
        check(hasField(c, "IMAGE_REL_ALPHA_PAIR"), "field IMAGE_REL_ALPHA_PAIR");
        check(hasField(c, "IMAGE_REL_ALPHA_MATCH"), "field IMAGE_REL_ALPHA_MATCH");
        check(hasField(c, "IMAGE_REL_ALPHA_SECTION"), "field IMAGE_REL_ALPHA_SECTION");
        check(hasField(c, "IMAGE_REL_ALPHA_SECREL"), "field IMAGE_REL_ALPHA_SECREL");
        check(hasField(c, "IMAGE_REL_ALPHA_REFLONGNB"), "field IMAGE_REL_ALPHA_REFLONGNB");
        check(hasField(c, "IMAGE_REL_ALPHA_SECRELLO"), "field IMAGE_REL_ALPHA_SECRELLO");
        check(hasField(c, "IMAGE_REL_ALPHA_SECRELHI"), "field IMAGE_REL_ALPHA_SECRELHI");
        check(hasField(c, "IMAGE_REL_ALPHA_REFQ3"), "field IMAGE_REL_ALPHA_REFQ3");
        check(hasField(c, "IMAGE_REL_ALPHA_REFQ2"), "field IMAGE_REL_ALPHA_REFQ2");
        check(hasField(c, "IMAGE_REL_ALPHA_REFQ1"), "field IMAGE_REL_ALPHA_REFQ1");
        check(hasField(c, "IMAGE_REL_ALPHA_GPRELLO"), "field IMAGE_REL_ALPHA_GPRELLO");
        check(hasField(c, "IMAGE_REL_ALPHA_GPRELHI"), "field IMAGE_REL_ALPHA_GPRELHI");
        check(hasField(c, "IMAGE_REL_PPC_ABSOLUTE"), "field IMAGE_REL_PPC_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_PPC_ADDR64"), "field IMAGE_REL_PPC_ADDR64");
        check(hasField(c, "IMAGE_REL_PPC_ADDR32"), "field IMAGE_REL_PPC_ADDR32");
        check(hasField(c, "IMAGE_REL_PPC_ADDR24"), "field IMAGE_REL_PPC_ADDR24");
        check(hasField(c, "IMAGE_REL_PPC_ADDR16"), "field IMAGE_REL_PPC_ADDR16");
        check(hasField(c, "IMAGE_REL_PPC_ADDR14"), "field IMAGE_REL_PPC_ADDR14");
        check(hasField(c, "IMAGE_REL_PPC_REL24"), "field IMAGE_REL_PPC_REL24");
        check(hasField(c, "IMAGE_REL_PPC_REL14"), "field IMAGE_REL_PPC_REL14");
        check(hasField(c, "IMAGE_REL_PPC_ADDR32NB"), "field IMAGE_REL_PPC_ADDR32NB");
        check(hasField(c, "IMAGE_REL_PPC_SECREL"), "field IMAGE_REL_PPC_SECREL");
        check(hasField(c, "IMAGE_REL_PPC_SECTION"), "field IMAGE_REL_PPC_SECTION");
        check(hasField(c, "IMAGE_REL_PPC_SECREL16"), "field IMAGE_REL_PPC_SECREL16");
        check(hasField(c, "IMAGE_REL_PPC_REFHI"), "field IMAGE_REL_PPC_REFHI");
        check(hasField(c, "IMAGE_REL_PPC_REFLO"), "field IMAGE_REL_PPC_REFLO");
        check(hasField(c, "IMAGE_REL_PPC_PAIR"), "field IMAGE_REL_PPC_PAIR");
        check(hasField(c, "IMAGE_REL_PPC_SECRELLO"), "field IMAGE_REL_PPC_SECRELLO");
        check(hasField(c, "IMAGE_REL_PPC_SECRELHI"), "field IMAGE_REL_PPC_SECRELHI");
        check(hasField(c, "IMAGE_REL_PPC_GPREL"), "field IMAGE_REL_PPC_GPREL");
        check(hasField(c, "IMAGE_REL_SH3_ABSOLUTE"), "field IMAGE_REL_SH3_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT16"), "field IMAGE_REL_SH3_DIRECT16");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT32"), "field IMAGE_REL_SH3_DIRECT32");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT8"), "field IMAGE_REL_SH3_DIRECT8");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT8_WORD"), "field IMAGE_REL_SH3_DIRECT8_WORD");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT8_LONG"), "field IMAGE_REL_SH3_DIRECT8_LONG");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT4"), "field IMAGE_REL_SH3_DIRECT4");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT4_WORD"), "field IMAGE_REL_SH3_DIRECT4_WORD");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT4_LONG"), "field IMAGE_REL_SH3_DIRECT4_LONG");
        check(hasField(c, "IMAGE_REL_SH3_PCREL8_WORD"), "field IMAGE_REL_SH3_PCREL8_WORD");
        check(hasField(c, "IMAGE_REL_SH3_PCREL8_LONG"), "field IMAGE_REL_SH3_PCREL8_LONG");
        check(hasField(c, "IMAGE_REL_SH3_PCREL12_WORD"), "field IMAGE_REL_SH3_PCREL12_WORD");
        check(hasField(c, "IMAGE_REL_SH3_STARTOF_SECTION"), "field IMAGE_REL_SH3_STARTOF_SECTION");
        check(hasField(c, "IMAGE_REL_SH3_SIZEOF_SECTION"), "field IMAGE_REL_SH3_SIZEOF_SECTION");
        check(hasField(c, "IMAGE_REL_SH3_SECTION"), "field IMAGE_REL_SH3_SECTION");
        check(hasField(c, "IMAGE_REL_SH3_SECREL"), "field IMAGE_REL_SH3_SECREL");
        check(hasField(c, "IMAGE_REL_SH3_DIRECT32_NB"), "field IMAGE_REL_SH3_DIRECT32_NB");
        check(hasField(c, "IMAGE_REL_ARM_ABSOLUTE"), "field IMAGE_REL_ARM_ABSOLUTE");
        check(hasField(c, "IMAGE_REL_ARM_ADDR32"), "field IMAGE_REL_ARM_ADDR32");
        check(hasField(c, "IMAGE_REL_ARM_ADDR32NB"), "field IMAGE_REL_ARM_ADDR32NB");
        check(hasField(c, "IMAGE_REL_ARM_BRANCH24"), "field IMAGE_REL_ARM_BRANCH24");
        check(hasField(c, "IMAGE_REL_ARM_BRANCH11"), "field IMAGE_REL_ARM_BRANCH11");
        check(hasField(c, "IMAGE_REL_ARM_SECTION"), "field IMAGE_REL_ARM_SECTION");
        check(hasField(c, "IMAGE_REL_ARM_SECREL"), "field IMAGE_REL_ARM_SECREL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
