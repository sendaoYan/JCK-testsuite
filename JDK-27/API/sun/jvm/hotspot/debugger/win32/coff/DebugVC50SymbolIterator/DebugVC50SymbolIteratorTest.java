/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolIterator conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SymbolIteratorTest
 */
import java.lang.reflect.*;

public class DebugVC50SymbolIteratorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolIterator", false, DebugVC50SymbolIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 2, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolTypes");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolEnums"), "interface sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolEnums");
        check(hasMethod(c, "done"), "method done");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getCompilerTargetProcessor"), "method getCompilerTargetProcessor");
        check(hasMethod(c, "getCompilerFlags"), "method getCompilerFlags");
        check(hasMethod(c, "getComplierVersion"), "method getComplierVersion");
        check(hasMethod(c, "getRegisterSymbolType"), "method getRegisterSymbolType");
        check(hasMethod(c, "getRegisterEnum"), "method getRegisterEnum");
        check(hasMethod(c, "getRegisterSymbolName"), "method getRegisterSymbolName");
        check(hasMethod(c, "getConstantType"), "method getConstantType");
        check(hasMethod(c, "getConstantValueAsInt"), "method getConstantValueAsInt");
        check(hasMethod(c, "getConstantValueAsLong"), "method getConstantValueAsLong");
        check(hasMethod(c, "getConstantValueAsFloat"), "method getConstantValueAsFloat");
        check(hasMethod(c, "getConstantValueAsDouble"), "method getConstantValueAsDouble");
        check(hasMethod(c, "getConstantName"), "method getConstantName");
        check(hasMethod(c, "getUDTType"), "method getUDTType");
        check(hasMethod(c, "getUDTName"), "method getUDTName");
        check(hasMethod(c, "getSearchSymbolOffset"), "method getSearchSymbolOffset");
        check(hasMethod(c, "getSearchSegment"), "method getSearchSegment");
        check(hasMethod(c, "getObjectCodeViewSignature"), "method getObjectCodeViewSignature");
        check(hasMethod(c, "getObjectName"), "method getObjectName");
        check(hasMethod(c, "getManyRegType"), "method getManyRegType");
        check(hasMethod(c, "getManyRegCount"), "method getManyRegCount");
        check(hasMethod(c, "getManyRegRegister"), "method getManyRegRegister");
        check(hasMethod(c, "getManyRegName"), "method getManyRegName");
        check(hasMethod(c, "getReturnFlags"), "method getReturnFlags");
        check(hasMethod(c, "getReturnStyle"), "method getReturnStyle");
        check(hasMethod(c, "getReturnRegisterCount"), "method getReturnRegisterCount");
        check(hasMethod(c, "getReturnRegister"), "method getReturnRegister");
        check(hasMethod(c, "advanceToEntryThisSymbol"), "method advanceToEntryThisSymbol");
        check(hasMethod(c, "getBPRelOffset"), "method getBPRelOffset");
        check(hasMethod(c, "getBPRelType"), "method getBPRelType");
        check(hasMethod(c, "getBPRelName"), "method getBPRelName");
        check(hasMethod(c, "getLGDataType"), "method getLGDataType");
        check(hasMethod(c, "getLGDataOffset"), "method getLGDataOffset");
        check(hasMethod(c, "getLGDataSegment"), "method getLGDataSegment");
        check(hasMethod(c, "getLGDataName"), "method getLGDataName");
        check(hasMethod(c, "getLGProcParent"), "method getLGProcParent");
        check(hasMethod(c, "getLGProcParentOffset"), "method getLGProcParentOffset");
        check(hasMethod(c, "getLGProcEnd"), "method getLGProcEnd");
        check(hasMethod(c, "getLGProcEndOffset"), "method getLGProcEndOffset");
        check(hasMethod(c, "getLGProcNext"), "method getLGProcNext");
        check(hasMethod(c, "getLGProcNextOffset"), "method getLGProcNextOffset");
        check(hasMethod(c, "getLGProcLength"), "method getLGProcLength");
        check(hasMethod(c, "getLGProcDebugStart"), "method getLGProcDebugStart");
        check(hasMethod(c, "getLGProcDebugEnd"), "method getLGProcDebugEnd");
        check(hasMethod(c, "getLGProcType"), "method getLGProcType");
        check(hasMethod(c, "getLGProcOffset"), "method getLGProcOffset");
        check(hasMethod(c, "getLGProcSegment"), "method getLGProcSegment");
        check(hasMethod(c, "getLGProcFlags"), "method getLGProcFlags");
        check(hasMethod(c, "getLGProcName"), "method getLGProcName");
        check(hasMethod(c, "getThunkParent"), "method getThunkParent");
        check(hasMethod(c, "getThunkParentOffset"), "method getThunkParentOffset");
        check(hasMethod(c, "getThunkEnd"), "method getThunkEnd");
        check(hasMethod(c, "getThunkEndOffset"), "method getThunkEndOffset");
        check(hasMethod(c, "getThunkNext"), "method getThunkNext");
        check(hasMethod(c, "getThunkNextOffset"), "method getThunkNextOffset");
        check(hasMethod(c, "getThunkOffset"), "method getThunkOffset");
        check(hasMethod(c, "getThunkSegment"), "method getThunkSegment");
        check(hasMethod(c, "getThunkLength"), "method getThunkLength");
        check(hasMethod(c, "getThunkType"), "method getThunkType");
        check(hasMethod(c, "getThunkName"), "method getThunkName");
        check(hasMethod(c, "getThunkAdjustorThisDelta"), "method getThunkAdjustorThisDelta");
        check(hasMethod(c, "getThunkAdjustorTargetName"), "method getThunkAdjustorTargetName");
        check(hasMethod(c, "getThunkVCallDisplacement"), "method getThunkVCallDisplacement");
        check(hasMethod(c, "getThunkPCodeOffset"), "method getThunkPCodeOffset");
        check(hasMethod(c, "getThunkPCodeSegment"), "method getThunkPCodeSegment");
        check(hasMethod(c, "getBlockParent"), "method getBlockParent");
        check(hasMethod(c, "getBlockParentOffset"), "method getBlockParentOffset");
        check(hasMethod(c, "getBlockEnd"), "method getBlockEnd");
        check(hasMethod(c, "getBlockEndOffset"), "method getBlockEndOffset");
        check(hasMethod(c, "getBlockLength"), "method getBlockLength");
        check(hasMethod(c, "getBlockOffset"), "method getBlockOffset");
        check(hasMethod(c, "getBlockSegment"), "method getBlockSegment");
        check(hasMethod(c, "getBlockName"), "method getBlockName");
        check(hasMethod(c, "getLabelOffset"), "method getLabelOffset");
        check(hasMethod(c, "getLabelSegment"), "method getLabelSegment");
        check(hasMethod(c, "getLabelFlags"), "method getLabelFlags");
        check(hasMethod(c, "getLabelName"), "method getLabelName");
        check(hasMethod(c, "getChangeOffset"), "method getChangeOffset");
        check(hasMethod(c, "getChangeSegment"), "method getChangeSegment");
        check(hasMethod(c, "getChangeModel"), "method getChangeModel");
        check(hasMethod(c, "getVTableRoot"), "method getVTableRoot");
        check(hasMethod(c, "getVTablePath"), "method getVTablePath");
        check(hasMethod(c, "getVTableOffset"), "method getVTableOffset");
        check(hasMethod(c, "getVTableSegment"), "method getVTableSegment");
        check(hasMethod(c, "getRegRelOffset"), "method getRegRelOffset");
        check(hasMethod(c, "getRegRelType"), "method getRegRelType");
        check(hasMethod(c, "getRegRelRegister"), "method getRegRelRegister");
        check(hasMethod(c, "getRegRelName"), "method getRegRelName");
        check(hasMethod(c, "getLThreadType"), "method getLThreadType");
        check(hasMethod(c, "getLThreadOffset"), "method getLThreadOffset");
        check(hasMethod(c, "getLThreadSegment"), "method getLThreadSegment");
        check(hasMethod(c, "getLThreadName"), "method getLThreadName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
