/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionConst conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main InstructionConstTest
 */
public class InstructionConstTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionConst", false, InstructionConstTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.InstructionConst"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 120, "field-count");
        check(fd.contains("NOP"), "field-NOP");
        check(fd.contains("ACONST_NULL"), "field-ACONST_NULL");
        check(fd.contains("ICONST_M1"), "field-ICONST_M1");
        check(fd.contains("ICONST_0"), "field-ICONST_0");
        check(fd.contains("ICONST_1"), "field-ICONST_1");
        check(fd.contains("ICONST_2"), "field-ICONST_2");
        check(fd.contains("ICONST_3"), "field-ICONST_3");
        check(fd.contains("ICONST_4"), "field-ICONST_4");
        check(fd.contains("ICONST_5"), "field-ICONST_5");
        check(fd.contains("LCONST_0"), "field-LCONST_0");
        check(fd.contains("LCONST_1"), "field-LCONST_1");
        check(fd.contains("FCONST_0"), "field-FCONST_0");
        check(fd.contains("FCONST_1"), "field-FCONST_1");
        check(fd.contains("FCONST_2"), "field-FCONST_2");
        check(fd.contains("DCONST_0"), "field-DCONST_0");
        check(fd.contains("DCONST_1"), "field-DCONST_1");
        check(fd.contains("IALOAD"), "field-IALOAD");
        check(fd.contains("LALOAD"), "field-LALOAD");
        check(fd.contains("FALOAD"), "field-FALOAD");
        check(fd.contains("DALOAD"), "field-DALOAD");
        check(fd.contains("AALOAD"), "field-AALOAD");
        check(fd.contains("BALOAD"), "field-BALOAD");
        check(fd.contains("CALOAD"), "field-CALOAD");
        check(fd.contains("SALOAD"), "field-SALOAD");
        check(fd.contains("IASTORE"), "field-IASTORE");
        check(fd.contains("LASTORE"), "field-LASTORE");
        check(fd.contains("FASTORE"), "field-FASTORE");
        check(fd.contains("DASTORE"), "field-DASTORE");
        check(fd.contains("AASTORE"), "field-AASTORE");
        check(fd.contains("BASTORE"), "field-BASTORE");
        check(fd.contains("CASTORE"), "field-CASTORE");
        check(fd.contains("SASTORE"), "field-SASTORE");
        check(fd.contains("POP"), "field-POP");
        check(fd.contains("POP2"), "field-POP2");
        check(fd.contains("DUP"), "field-DUP");
        check(fd.contains("DUP_X1"), "field-DUP_X1");
        check(fd.contains("DUP_X2"), "field-DUP_X2");
        check(fd.contains("DUP2"), "field-DUP2");
        check(fd.contains("DUP2_X1"), "field-DUP2_X1");
        check(fd.contains("DUP2_X2"), "field-DUP2_X2");
        check(fd.contains("SWAP"), "field-SWAP");
        check(fd.contains("IADD"), "field-IADD");
        check(fd.contains("LADD"), "field-LADD");
        check(fd.contains("FADD"), "field-FADD");
        check(fd.contains("DADD"), "field-DADD");
        check(fd.contains("ISUB"), "field-ISUB");
        check(fd.contains("LSUB"), "field-LSUB");
        check(fd.contains("FSUB"), "field-FSUB");
        check(fd.contains("DSUB"), "field-DSUB");
        check(fd.contains("IMUL"), "field-IMUL");
        check(fd.contains("LMUL"), "field-LMUL");
        check(fd.contains("FMUL"), "field-FMUL");
        check(fd.contains("DMUL"), "field-DMUL");
        check(fd.contains("IDIV"), "field-IDIV");
        check(fd.contains("LDIV"), "field-LDIV");
        check(fd.contains("FDIV"), "field-FDIV");
        check(fd.contains("DDIV"), "field-DDIV");
        check(fd.contains("IREM"), "field-IREM");
        check(fd.contains("LREM"), "field-LREM");
        check(fd.contains("FREM"), "field-FREM");
        check(fd.contains("DREM"), "field-DREM");
        check(fd.contains("INEG"), "field-INEG");
        check(fd.contains("LNEG"), "field-LNEG");
        check(fd.contains("FNEG"), "field-FNEG");
        check(fd.contains("DNEG"), "field-DNEG");
        check(fd.contains("ISHL"), "field-ISHL");
        check(fd.contains("LSHL"), "field-LSHL");
        check(fd.contains("ISHR"), "field-ISHR");
        check(fd.contains("LSHR"), "field-LSHR");
        check(fd.contains("IUSHR"), "field-IUSHR");
        check(fd.contains("LUSHR"), "field-LUSHR");
        check(fd.contains("IAND"), "field-IAND");
        check(fd.contains("LAND"), "field-LAND");
        check(fd.contains("IOR"), "field-IOR");
        check(fd.contains("LOR"), "field-LOR");
        check(fd.contains("IXOR"), "field-IXOR");
        check(fd.contains("LXOR"), "field-LXOR");
        check(fd.contains("I2L"), "field-I2L");
        check(fd.contains("I2F"), "field-I2F");
        check(fd.contains("I2D"), "field-I2D");
        check(fd.contains("L2I"), "field-L2I");
        check(fd.contains("L2F"), "field-L2F");
        check(fd.contains("L2D"), "field-L2D");
        check(fd.contains("F2I"), "field-F2I");
        check(fd.contains("F2L"), "field-F2L");
        check(fd.contains("F2D"), "field-F2D");
        check(fd.contains("D2I"), "field-D2I");
        check(fd.contains("D2L"), "field-D2L");
        check(fd.contains("D2F"), "field-D2F");
        check(fd.contains("I2B"), "field-I2B");
        check(fd.contains("I2C"), "field-I2C");
        check(fd.contains("I2S"), "field-I2S");
        check(fd.contains("LCMP"), "field-LCMP");
        check(fd.contains("FCMPL"), "field-FCMPL");
        check(fd.contains("FCMPG"), "field-FCMPG");
        check(fd.contains("DCMPL"), "field-DCMPL");
        check(fd.contains("DCMPG"), "field-DCMPG");
        check(fd.contains("IRETURN"), "field-IRETURN");
        check(fd.contains("LRETURN"), "field-LRETURN");
        check(fd.contains("FRETURN"), "field-FRETURN");
        check(fd.contains("DRETURN"), "field-DRETURN");
        check(fd.contains("ARETURN"), "field-ARETURN");
        check(fd.contains("RETURN"), "field-RETURN");
        check(fd.contains("ARRAYLENGTH"), "field-ARRAYLENGTH");
        check(fd.contains("ATHROW"), "field-ATHROW");
        check(fd.contains("MONITORENTER"), "field-MONITORENTER");
        check(fd.contains("MONITOREXIT"), "field-MONITOREXIT");
        check(fd.contains("THIS"), "field-THIS");
        check(fd.contains("ALOAD_0"), "field-ALOAD_0");
        check(fd.contains("ALOAD_1"), "field-ALOAD_1");
        check(fd.contains("ALOAD_2"), "field-ALOAD_2");
        check(fd.contains("ILOAD_0"), "field-ILOAD_0");
        check(fd.contains("ILOAD_1"), "field-ILOAD_1");
        check(fd.contains("ILOAD_2"), "field-ILOAD_2");
        check(fd.contains("ASTORE_0"), "field-ASTORE_0");
        check(fd.contains("ASTORE_1"), "field-ASTORE_1");
        check(fd.contains("ASTORE_2"), "field-ASTORE_2");
        check(fd.contains("ISTORE_0"), "field-ISTORE_0");
        check(fd.contains("ISTORE_1"), "field-ISTORE_1");
        check(fd.contains("ISTORE_2"), "field-ISTORE_2");
        check(md.size() == 1, "method-count");
        check(md.contains("getInstruction"), "method-getInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
