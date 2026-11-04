/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeIterator conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50TypeIteratorTest
 */
import java.lang.reflect.*;

public class DebugVC50TypeIteratorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeIterator", false, DebugVC50TypeIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "done"), "method done");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "getMemberName"), "method getMemberName");
        check(hasMethod(c, "getClassName"), "method getClassName");
        check(hasMethod(c, "getMethodName"), "method getMethodName");
        check(hasMethod(c, "getEnumName"), "method getEnumName");
        check(hasMethod(c, "getNumTypes"), "method getNumTypes");
        check(hasMethod(c, "getTypeIndex"), "method getTypeIndex");
        check(hasMethod(c, "typeStringDone"), "method typeStringDone");
        check(hasMethod(c, "typeStringNext"), "method typeStringNext");
        check(hasMethod(c, "typeStringLeaf"), "method typeStringLeaf");
        check(hasMethod(c, "typeStringOffset"), "method typeStringOffset");
        check(hasMethod(c, "getModifierIndex"), "method getModifierIndex");
        check(hasMethod(c, "getModifierAttribute"), "method getModifierAttribute");
        check(hasMethod(c, "getPointerType"), "method getPointerType");
        check(hasMethod(c, "getPointerAttributes"), "method getPointerAttributes");
        check(hasMethod(c, "getPointerBasedOnTypeIndex"), "method getPointerBasedOnTypeIndex");
        check(hasMethod(c, "getPointerBasedOnTypeName"), "method getPointerBasedOnTypeName");
        check(hasMethod(c, "getPointerToMemberClass"), "method getPointerToMemberClass");
        check(hasMethod(c, "getPointerToMemberFormat"), "method getPointerToMemberFormat");
        check(hasMethod(c, "getArrayElementType"), "method getArrayElementType");
        check(hasMethod(c, "getArrayIndexType"), "method getArrayIndexType");
        check(hasMethod(c, "getArrayLength"), "method getArrayLength");
        check(hasMethod(c, "getArrayName"), "method getArrayName");
        check(hasMethod(c, "getClassCount"), "method getClassCount");
        check(hasMethod(c, "getClassProperty"), "method getClassProperty");
        check(hasMethod(c, "getClassFieldList"), "method getClassFieldList");
        check(hasMethod(c, "getClassFieldListIterator"), "method getClassFieldListIterator");
        check(hasMethod(c, "getClassDerivationList"), "method getClassDerivationList");
        check(hasMethod(c, "getClassVShape"), "method getClassVShape");
        check(hasMethod(c, "getClassSize"), "method getClassSize");
        check(hasMethod(c, "getUnionCount"), "method getUnionCount");
        check(hasMethod(c, "getUnionProperty"), "method getUnionProperty");
        check(hasMethod(c, "getUnionFieldList"), "method getUnionFieldList");
        check(hasMethod(c, "getUnionFieldListIterator"), "method getUnionFieldListIterator");
        check(hasMethod(c, "getUnionSize"), "method getUnionSize");
        check(hasMethod(c, "getUnionName"), "method getUnionName");
        check(hasMethod(c, "getEnumCount"), "method getEnumCount");
        check(hasMethod(c, "getEnumProperty"), "method getEnumProperty");
        check(hasMethod(c, "getEnumType"), "method getEnumType");
        check(hasMethod(c, "getEnumFieldList"), "method getEnumFieldList");
        check(hasMethod(c, "getEnumFieldListIterator"), "method getEnumFieldListIterator");
        check(hasMethod(c, "getProcedureReturnType"), "method getProcedureReturnType");
        check(hasMethod(c, "getProcedureCallingConvention"), "method getProcedureCallingConvention");
        check(hasMethod(c, "getProcedureNumberOfParameters"), "method getProcedureNumberOfParameters");
        check(hasMethod(c, "getProcedureArgumentList"), "method getProcedureArgumentList");
        check(hasMethod(c, "getProcedureArgumentListIterator"), "method getProcedureArgumentListIterator");
        check(hasMethod(c, "getMFunctionReturnType"), "method getMFunctionReturnType");
        check(hasMethod(c, "getMFunctionContainingClass"), "method getMFunctionContainingClass");
        check(hasMethod(c, "getMFunctionThis"), "method getMFunctionThis");
        check(hasMethod(c, "getMFunctionCallingConvention"), "method getMFunctionCallingConvention");
        check(hasMethod(c, "getMFunctionNumberOfParameters"), "method getMFunctionNumberOfParameters");
        check(hasMethod(c, "getMFunctionArgumentList"), "method getMFunctionArgumentList");
        check(hasMethod(c, "getMFunctionArgumentListIterator"), "method getMFunctionArgumentListIterator");
        check(hasMethod(c, "getMFunctionThisAdjust"), "method getMFunctionThisAdjust");
        check(hasMethod(c, "getVTShapeCount"), "method getVTShapeCount");
        check(hasMethod(c, "getVTShapeDescriptor"), "method getVTShapeDescriptor");
        check(hasMethod(c, "getBasicArrayType"), "method getBasicArrayType");
        check(hasMethod(c, "getLabelAddressMode"), "method getLabelAddressMode");
        check(hasMethod(c, "getDimArrayType"), "method getDimArrayType");
        check(hasMethod(c, "getDimArrayDimInfo"), "method getDimArrayDimInfo");
        check(hasMethod(c, "getDimArrayName"), "method getDimArrayName");
        check(hasMethod(c, "getVFTPathCount"), "method getVFTPathCount");
        check(hasMethod(c, "getVFTPathBase"), "method getVFTPathBase");
        check(hasMethod(c, "getSkipIndex"), "method getSkipIndex");
        check(hasMethod(c, "getArgListCount"), "method getArgListCount");
        check(hasMethod(c, "getArgListType"), "method getArgListType");
        check(hasMethod(c, "getDefaultArgType"), "method getDefaultArgType");
        check(hasMethod(c, "getDefaultArgExpression"), "method getDefaultArgExpression");
        check(hasMethod(c, "getDerivedCount"), "method getDerivedCount");
        check(hasMethod(c, "getDerivedType"), "method getDerivedType");
        check(hasMethod(c, "getBitfieldFieldType"), "method getBitfieldFieldType");
        check(hasMethod(c, "getBitfieldLength"), "method getBitfieldLength");
        check(hasMethod(c, "getBitfieldPosition"), "method getBitfieldPosition");
        check(hasMethod(c, "getMListAttribute"), "method getMListAttribute");
        check(hasMethod(c, "getMListLength"), "method getMListLength");
        check(hasMethod(c, "getMListType"), "method getMListType");
        check(hasMethod(c, "isMListIntroducingVirtual"), "method isMListIntroducingVirtual");
        check(hasMethod(c, "getMListVtabOffset"), "method getMListVtabOffset");
        check(hasMethod(c, "getRefSym"), "method getRefSym");
        check(hasMethod(c, "getBClassAttribute"), "method getBClassAttribute");
        check(hasMethod(c, "getBClassType"), "method getBClassType");
        check(hasMethod(c, "getBClassOffset"), "method getBClassOffset");
        check(hasMethod(c, "getVBClassAttribute"), "method getVBClassAttribute");
        check(hasMethod(c, "getVBClassBaseClassType"), "method getVBClassBaseClassType");
        check(hasMethod(c, "getVBClassVirtualBaseClassType"), "method getVBClassVirtualBaseClassType");
        check(hasMethod(c, "getVBClassVBPOff"), "method getVBClassVBPOff");
        check(hasMethod(c, "getVBClassVBOff"), "method getVBClassVBOff");
        check(hasMethod(c, "getIVBClassAttribute"), "method getIVBClassAttribute");
        check(hasMethod(c, "getIVBClassBType"), "method getIVBClassBType");
        check(hasMethod(c, "getIVBClassVBPType"), "method getIVBClassVBPType");
        check(hasMethod(c, "getIVBClassVBPOff"), "method getIVBClassVBPOff");
        check(hasMethod(c, "getIVBClassVBOff"), "method getIVBClassVBOff");
        check(hasMethod(c, "getEnumerateAttribute"), "method getEnumerateAttribute");
        check(hasMethod(c, "getEnumerateValue"), "method getEnumerateValue");
        check(hasMethod(c, "getEnumerateName"), "method getEnumerateName");
        check(hasMethod(c, "getFriendFcnType"), "method getFriendFcnType");
        check(hasMethod(c, "getFriendFcnName"), "method getFriendFcnName");
        check(hasMethod(c, "getIndexValue"), "method getIndexValue");
        check(hasMethod(c, "getIndexIterator"), "method getIndexIterator");
        check(hasMethod(c, "getMemberAttribute"), "method getMemberAttribute");
        check(hasMethod(c, "getMemberType"), "method getMemberType");
        check(hasMethod(c, "getMemberOffset"), "method getMemberOffset");
        check(hasMethod(c, "getStaticAttribute"), "method getStaticAttribute");
        check(hasMethod(c, "getStaticType"), "method getStaticType");
        check(hasMethod(c, "getStaticName"), "method getStaticName");
        check(hasMethod(c, "getMethodCount"), "method getMethodCount");
        check(hasMethod(c, "getMethodList"), "method getMethodList");
        check(hasMethod(c, "getNestedType"), "method getNestedType");
        check(hasMethod(c, "getNestedName"), "method getNestedName");
        check(hasMethod(c, "getVFuncTabType"), "method getVFuncTabType");
        check(hasMethod(c, "getFriendClsType"), "method getFriendClsType");
        check(hasMethod(c, "getOneMethodAttribute"), "method getOneMethodAttribute");
        check(hasMethod(c, "getOneMethodType"), "method getOneMethodType");
        check(hasMethod(c, "isOneMethodIntroducingVirtual"), "method isOneMethodIntroducingVirtual");
        check(hasMethod(c, "getOneMethodVBaseOff"), "method getOneMethodVBaseOff");
        check(hasMethod(c, "getOneMethodName"), "method getOneMethodName");
        check(hasMethod(c, "getVFuncOffType"), "method getVFuncOffType");
        check(hasMethod(c, "getVFuncOffOffset"), "method getVFuncOffOffset");
        check(hasMethod(c, "getNestedExAttribute"), "method getNestedExAttribute");
        check(hasMethod(c, "getNestedExType"), "method getNestedExType");
        check(hasMethod(c, "getNestedExName"), "method getNestedExName");
        check(hasMethod(c, "getMemberModifyAttribute"), "method getMemberModifyAttribute");
        check(hasMethod(c, "getMemberModifyType"), "method getMemberModifyType");
        check(hasMethod(c, "getMemberModifyName"), "method getMemberModifyName");
        check(hasMethod(c, "getNumericTypeAt"), "method getNumericTypeAt");
        check(hasMethod(c, "getNumericLengthAt"), "method getNumericLengthAt");
        check(hasMethod(c, "getNumericIntAt"), "method getNumericIntAt");
        check(hasMethod(c, "getNumericLongAt"), "method getNumericLongAt");
        check(hasMethod(c, "getNumericFloatAt"), "method getNumericFloatAt");
        check(hasMethod(c, "getNumericDoubleAt"), "method getNumericDoubleAt");
        check(hasMethod(c, "getNumericDataAt"), "method getNumericDataAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
