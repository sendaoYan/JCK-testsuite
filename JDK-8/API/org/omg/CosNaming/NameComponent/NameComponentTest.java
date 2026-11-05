/*
 * @test
 * @summary org.omg.CosNaming.NameComponent conformance: value type fields and constructors.
 * @run main NameComponentTest
 */

public class NameComponentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NameComponent s0 = new org.omg.CosNaming.NameComponent();
        check(s0.id == null, "no-arg id default");
        check(s0.kind == null, "no-arg kind default");
        org.omg.CosNaming.NameComponent s1 = new org.omg.CosNaming.NameComponent("s", "s");
        check(s1.id == "s", "ctor id stored");
        check(s1.kind == "s", "ctor kind stored");
        // ---- deepened (batch C, jdk8u372 probed): identity semantics + field contract ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NameComponent.class.getModifiers()),
              "NameComponent is final (IDL struct mapping)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NameComponent.class),
              "NameComponent implements IDLEntity");
        check(java.io.Serializable.class.isAssignableFrom(org.omg.CosNaming.NameComponent.class),
              "NameComponent is Serializable via IDLEntity");
        org.omg.CosNaming.NameComponent self = new org.omg.CosNaming.NameComponent("a", "b");
        check(self.equals(self), "equals is identity: same instance true");
        check(!self.equals(new org.omg.CosNaming.NameComponent("a", "b")),
              "equals is identity: distinct instance with identical id/kind is NOT equal (no value-equals override in JDK mapping)");
        check(!self.equals(null), "equals(null) false");
        check(!self.equals("a"), "equals(non-NameComponent) false");
        check(self.hashCode() == System.identityHashCode(self),
              "hashCode not overridden: equals identity hash code");
        check(self.toString().startsWith("org.omg.CosNaming.NameComponent@"),
              "toString not overridden: default Object toString pattern ClassName@hex");
        org.omg.CosNaming.NameComponent nullId = new org.omg.CosNaming.NameComponent(null, "k");
        check(nullId.id == null && nullId.kind.equals("k"), "null id accepted, kind stored verbatim");
        org.omg.CosNaming.NameComponent nullKind = new org.omg.CosNaming.NameComponent("i", null);
        check(nullKind.id.equals("i") && nullKind.kind == null, "null kind accepted, id stored verbatim");
        org.omg.CosNaming.NameComponent mut = new org.omg.CosNaming.NameComponent();
        mut.id = "mutI";
        mut.kind = "mutK";
        check(mut.id.equals("mutI") && mut.kind.equals("mutK"), "public fields id/kind are mutable and read back");
        mut.id = null;
        mut.kind = null;
        check(mut.id == null && mut.kind == null, "fields resettable to null");
        // constructor stores identical String references (no defensive copy)
        String sharedId = "shared";
        org.omg.CosNaming.NameComponent refKeep = new org.omg.CosNaming.NameComponent(sharedId, sharedId);
        check(refKeep.id == sharedId && refKeep.kind == sharedId, "ctor stores the given String references (no copy)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
