/*
 * @test
 * @summary org.omg.CORBA.TypeCode conformance: abstract class shape.
 * @run main TypeCodeTest
 */

public class TypeCodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.TypeCode");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.IDLEntity");

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.TypeCode lng = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
        org.omg.CORBA.TypeCode str = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string);

        // ---- primitive TypeCode ----
        check(lng.kind() == org.omg.CORBA.TCKind.tk_long, "primitive tk_long kind");
        check(lng.equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)), "primitive equal same kind");
        check(!lng.equal(str), "primitive equal different kind");
        check(lng.equivalent(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)), "primitive equivalent same kind");
        check(!lng.equivalent(str), "primitive equivalent different kind");
        check(lng.get_compact_typecode().kind() == org.omg.CORBA.TCKind.tk_long, "primitive compact kind");

        // primitive tc has no id/name/member_count -> BadKind
        try {
            lng.id();
            throw new AssertionError("primitive id() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "primitive id() -> BadKind");
        }
        try {
            lng.name();
            throw new AssertionError("primitive name() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "primitive name() -> BadKind");
        }
        try {
            lng.member_count();
            throw new AssertionError("primitive member_count() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "primitive member_count() -> BadKind");
        }
        try {
            lng.content_type();
            throw new AssertionError("primitive content_type() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "primitive content_type() -> BadKind");
        }

        // ---- struct TypeCode ----
        org.omg.CORBA.StructMember[] sms = {
            new org.omg.CORBA.StructMember("m1", lng, null),
            new org.omg.CORBA.StructMember("m2", str, null) };
        org.omg.CORBA.TypeCode stc = orb.create_struct_tc("IDL:S:1.0", "S", sms);
        check(stc.kind() == org.omg.CORBA.TCKind.tk_struct, "struct kind");
        check("IDL:S:1.0".equals(stc.id()), "struct id readback");
        check("S".equals(stc.name()), "struct name readback");
        check(stc.member_count() == 2, "struct member_count");
        check("m1".equals(stc.member_name(0)), "struct member_name(0)");
        check("m2".equals(stc.member_name(1)), "struct member_name(1)");
        check(stc.member_type(0).kind() == org.omg.CORBA.TCKind.tk_long, "struct member_type(0) kind");
        check(stc.member_type(1).equal(str), "struct member_type(1) equal");

        // ---- enum TypeCode ----
        org.omg.CORBA.TypeCode etc = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B", "C"});
        check(etc.kind() == org.omg.CORBA.TCKind.tk_enum, "enum kind");
        check("IDL:E:1.0".equals(etc.id()), "enum id");
        check("E".equals(etc.name()), "enum name");
        check(etc.member_count() == 3, "enum member_count");
        check("A".equals(etc.member_name(0)), "enum member_name(0)");
        check("C".equals(etc.member_name(2)), "enum member_name(2)");

        // ---- union TypeCode ----
        org.omg.CORBA.Any l1 = orb.create_any(); l1.insert_long(1);
        org.omg.CORBA.Any dflt = orb.create_any();
        org.omg.CORBA.UnionMember[] ums = {
            new org.omg.CORBA.UnionMember("a", l1, str, null),
            new org.omg.CORBA.UnionMember("d", dflt, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean), null) };
        org.omg.CORBA.TypeCode utc = orb.create_union_tc("IDL:U:1.0", "U", lng, ums);
        check(utc.kind() == org.omg.CORBA.TCKind.tk_union, "union kind");
        check(utc.member_count() == 2, "union member_count");
        check("a".equals(utc.member_name(0)), "union member_name(0)");
        check(utc.member_label(0).extract_long() == 1, "union member_label(0) value readback");
        check(utc.member_label(1).type().kind() == org.omg.CORBA.TCKind.tk_null, "union default member label is tk_null any");
        check(utc.discriminator_type().kind() == org.omg.CORBA.TCKind.tk_long, "union discriminator_type");
        check(utc.default_index() == -1, "union default_index -1 (jdk8u372 measured)");
        check(utc.member_type(0).equal(str), "union member_type(0)");

        // ---- alias / sequence / array / string / wstring ----
        org.omg.CORBA.TypeCode atc = orb.create_alias_tc("IDL:A:1.0", "A", lng);
        check(atc.kind() == org.omg.CORBA.TCKind.tk_alias, "alias kind");
        check("IDL:A:1.0".equals(atc.id()), "alias id");
        check(atc.content_type().kind() == org.omg.CORBA.TCKind.tk_long, "alias content_type");

        org.omg.CORBA.TypeCode seqtc = orb.create_sequence_tc(0, lng);
        check(seqtc.kind() == org.omg.CORBA.TCKind.tk_sequence, "sequence kind");
        check(seqtc.length() == 0, "unbounded sequence length 0");
        check(seqtc.content_type().kind() == org.omg.CORBA.TCKind.tk_long, "sequence content_type");
        org.omg.CORBA.TypeCode bseq = orb.create_sequence_tc(3, str);
        check(bseq.length() == 3, "bounded sequence length readback");

        org.omg.CORBA.TypeCode artc = orb.create_array_tc(2, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_octet));
        check(artc.kind() == org.omg.CORBA.TCKind.tk_array, "array kind");
        check(artc.length() == 2, "array length readback");
        check(artc.content_type().kind() == org.omg.CORBA.TCKind.tk_octet, "array content_type");

        check(str.kind() == org.omg.CORBA.TCKind.tk_string, "primitive string kind");
        org.omg.CORBA.TypeCode stc5 = orb.create_string_tc(5);
        check(stc5.length() == 5, "create_string_tc length");
        check(stc5.kind() == org.omg.CORBA.TCKind.tk_string, "create_string_tc kind");
        org.omg.CORBA.TypeCode wstc = orb.create_wstring_tc(7);
        check(wstc.kind() == org.omg.CORBA.TCKind.tk_wstring, "create_wstring_tc kind");
        check(wstc.length() == 7, "create_wstring_tc length");

        // ---- exception / interface / native / abstract interface / value family ----
        org.omg.CORBA.TypeCode extc = orb.create_exception_tc("IDL:Ex:1.0", "Ex", sms);
        check(extc.kind() == org.omg.CORBA.TCKind.tk_except, "exception_tc kind");
        check(extc.member_count() == 2, "exception_tc member_count");
        org.omg.CORBA.TypeCode itc = orb.create_interface_tc("IDL:I:1.0", "I");
        check(itc.kind() == org.omg.CORBA.TCKind.tk_objref, "interface_tc kind tk_objref");
        check("IDL:I:1.0".equals(itc.id()), "interface_tc id");
        org.omg.CORBA.TypeCode ntc = orb.create_native_tc("IDL:N:1.0", "N");
        check(ntc.kind() == org.omg.CORBA.TCKind.tk_native, "native_tc kind");
        org.omg.CORBA.TypeCode aitc = orb.create_abstract_interface_tc("IDL:AI:1.0", "AI");
        check(aitc.kind() == org.omg.CORBA.TCKind.tk_abstract_interface, "abstract_interface_tc kind");
        org.omg.CORBA.TypeCode vbtc = orb.create_value_box_tc("IDL:VB:1.0", "VB", lng);
        check(vbtc.kind() == org.omg.CORBA.TCKind.tk_value_box, "value_box_tc kind");
        check(vbtc.content_type().kind() == org.omg.CORBA.TCKind.tk_long, "value_box content_type");
        org.omg.CORBA.TypeCode ftc = orb.create_fixed_tc((short) 5, (short) 2);
        check(ftc.kind() == org.omg.CORBA.TCKind.tk_fixed, "fixed_tc kind");
        check(ftc.fixed_digits() == 5, "fixed digits");
        check(ftc.fixed_scale() == 2, "fixed scale");

        // value TypeCode: type_modifier + member_visibility
        org.omg.CORBA.ValueMember[] vms = {
            new org.omg.CORBA.ValueMember("pub", "IDL:S:1.0", "", "1.0", lng, null, org.omg.CORBA.PUBLIC_MEMBER.value),
            new org.omg.CORBA.ValueMember("priv", "IDL:S:1.0", "", "1.0", str, null, org.omg.CORBA.PRIVATE_MEMBER.value) };
        org.omg.CORBA.TypeCode vtc = orb.create_value_tc("IDL:V:1.0", "V", org.omg.CORBA.VM_CUSTOM.value, null, vms);
        check(vtc.kind() == org.omg.CORBA.TCKind.tk_value, "value_tc kind");
        check(vtc.type_modifier() == org.omg.CORBA.VM_CUSTOM.value, "value_tc type_modifier readback");
        check(vtc.member_visibility(0) == org.omg.CORBA.PUBLIC_MEMBER.value, "member_visibility(0) PUBLIC");
        check(vtc.member_visibility(1) == org.omg.CORBA.PRIVATE_MEMBER.value, "member_visibility(1) PRIVATE");
        check(vtc.member_count() == 2, "value_tc member_count");
        check("pub".equals(vtc.member_name(0)), "value_tc member_name(0)");

        // ---- equal across independently constructed instances ----
        org.omg.CORBA.TypeCode e1 = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B"});
        org.omg.CORBA.TypeCode e2 = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A", "B"});
        org.omg.CORBA.TypeCode e3 = orb.create_enum_tc("IDL:F:1.0", "F", new String[]{"A", "B"});
        check(e1.equal(e2), "equal: same-shape enum instances true");
        check(!e1.equal(e3), "equal: different id false");
        check(!stc.equal(etc), "equal: struct vs enum false");
        check(stc.equal(orb.create_struct_tc("IDL:S:1.0", "S", sms)), "equal: same-shape struct true");

        // ---- out-of-bounds -> TypeCodePackage.Bounds ----
        try {
            stc.member_name(2);
            throw new AssertionError("member_name(2) should throw Bounds");
        } catch (org.omg.CORBA.TypeCodePackage.Bounds expected) {
            check(true, "struct member_name OOB -> Bounds");
        }
        try {
            stc.member_type(2);
            throw new AssertionError("member_type(2) should throw Bounds");
        } catch (org.omg.CORBA.TypeCodePackage.Bounds expected) {
            check(true, "struct member_type OOB -> Bounds");
        }
        try {
            etc.member_name(3);
            throw new AssertionError("enum member_name(3) should throw Bounds");
        } catch (org.omg.CORBA.TypeCodePackage.Bounds expected) {
            check(true, "enum member_name OOB -> Bounds");
        }
        try {
            utc.member_label(2);
            throw new AssertionError("member_label(2) should throw Bounds");
        } catch (org.omg.CORBA.TypeCodePackage.Bounds expected) {
            check(true, "union member_label OOB -> Bounds");
        }
        try {
            vtc.member_visibility(2);
            throw new AssertionError("member_visibility(2) should throw Bounds");
        } catch (org.omg.CORBA.TypeCodePackage.Bounds expected) {
            check(true, "value member_visibility OOB -> Bounds");
        }
        // kind-inapplicable accessors -> BadKind
        try {
            stc.type_modifier();
            throw new AssertionError("struct type_modifier() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "struct type_modifier -> BadKind");
        }
        try {
            stc.fixed_digits();
            throw new AssertionError("struct fixed_digits() should throw BadKind");
        } catch (org.omg.CORBA.TypeCodePackage.BadKind expected) {
            check(true, "struct fixed_digits -> BadKind");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
