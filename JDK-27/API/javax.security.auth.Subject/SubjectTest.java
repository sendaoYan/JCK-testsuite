/*
 * @test
 * @summary javax.security.auth.Subject conformance: white-box structure + black-box behavior.
 * @run main SubjectTest
 */

import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

public class SubjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.Subject");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // empty construction
        Subject sub = new Subject();
        check(!sub.isReadOnly(), "initially not read-only");
        check(sub.getPrincipals().isEmpty(), "principals empty");
        check(sub.getPublicCredentials().isEmpty(), "public credentials empty");
        check(sub.getPrivateCredentials().isEmpty(), "private credentials empty");

        // principal add/contains/size/remove + typed filter
        X500Principal xp = new X500Principal("CN=Alice");
        sub.getPrincipals().add(xp);
        check(sub.getPrincipals().size() == 1, "principals size");
        check(sub.getPrincipals().contains(xp), "contains principal");
        check(sub.getPrincipals(X500Principal.class).size() == 1, "typed principals");
        check(sub.getPrincipals(Principal.class).size() == 1, "typed principals by supertype");
        sub.getPrincipals().remove(xp);
        check(sub.getPrincipals().isEmpty(), "principals empty after remove");
        sub.getPrincipals().add(xp);

        // credentials: public/private with typed filtering
        Object pub = "publicCred";
        Object priv = "privateCred";
        sub.getPublicCredentials().add(pub);
        sub.getPrivateCredentials().add(priv);
        check(sub.getPublicCredentials().size() == 1, "public credentials size");
        check(sub.getPrivateCredentials().size() == 1, "private credentials size");
        check(sub.getPublicCredentials().contains(pub), "contains public credential");
        check(sub.getPrivateCredentials().contains(priv), "contains private credential");
        check(sub.getPublicCredentials(String.class).size() == 1, "typed public credentials");
        check(sub.getPrivateCredentials(String.class).size() == 1, "typed private credentials");
        check(sub.getPublicCredentials(Integer.class).isEmpty(), "typed filter excludes other class");
        sub.getPublicCredentials().remove(pub);
        sub.getPrivateCredentials().remove(priv);
        check(sub.getPublicCredentials().isEmpty() && sub.getPrivateCredentials().isEmpty(), "credentials removed");
        sub.getPublicCredentials().add(pub);

        // null elements are prohibited
        boolean npe1 = false;
        try { sub.getPrincipals().add(null); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "add null principal throws NullPointerException");
        boolean npe2 = false;
        try { sub.getPublicCredentials().contains(null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "contains null public credential throws NullPointerException");
        boolean npe3 = false;
        try { sub.getPrivateCredentials().remove(null); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "remove null private credential throws NullPointerException");

        // read-only transition
        sub.setReadOnly();
        check(sub.isReadOnly(), "read-only after setReadOnly");
        boolean ise1 = false;
        try { sub.getPrincipals().add(new X500Principal("CN=Bob")); } catch (IllegalStateException e) { ise1 = true; }
        check(ise1, "add to read-only principals throws IllegalStateException");
        boolean ise2 = false;
        try { sub.getPublicCredentials().remove(pub); } catch (IllegalStateException e) { ise2 = true; }
        check(ise2, "remove from read-only credentials throws IllegalStateException");

        // read-only constructor
        Subject ro = new Subject(true, new HashSet<Principal>(), new HashSet<Object>(), new HashSet<Object>());
        check(ro.isReadOnly(), "read-only ctor sets read-only");
        boolean ise3 = false;
        try { ro.getPrincipals().add(new X500Principal("CN=C")); } catch (IllegalStateException e) { ise3 = true; }
        check(ise3, "read-only ctor add throws IllegalStateException");

        // typed set is a copy, not backed by the internal set
        Subject t = new Subject();
        t.getPrincipals().add(new X500Principal("CN=Z"));
        Set<X500Principal> typed = t.getPrincipals(X500Principal.class);
        typed.clear();
        check(t.getPrincipals().size() == 1, "typed principal set is a copy");

        // equals / hashCode
        Subject empty1 = new Subject();
        Subject empty2 = new Subject();
        check(empty1.equals(empty2) && empty2.equals(empty1), "empty subjects equal");
        check(empty1.hashCode() == empty2.hashCode(), "empty subjects equal hash");

        Subject a1 = new Subject();
        a1.getPrincipals().add(new X500Principal("CN=A"));
        a1.getPublicCredentials().add("pub");
        Subject a2 = new Subject();
        a2.getPrincipals().add(new X500Principal("CN=A"));
        a2.getPublicCredentials().add("pub");
        check(a1.equals(a2), "same principal+credential subjects equal");
        check(a1.hashCode() == a2.hashCode(), "same subjects equal hash");
        Subject b = new Subject();
        b.getPrincipals().add(new X500Principal("CN=B"));
        check(!a1.equals(b), "different principal subjects not equal");
        check(!empty1.equals("not a subject"), "subject not equal to non-subject");

        // toString includes a principal name (loose assertion, locale-independent)
        check(a1.toString().contains("CN=A"), "toString contains principal name");

        // current subject lifecycle
        check(Subject.current() == null, "current() null before callAs");

        Subject got = Subject.callAs(sub, new Callable<Subject>() {
            public Subject call() { return Subject.current(); }
        });
        check(got == sub, "callAs binds subject inside action");
        check(Subject.current() == null, "current() reset after callAs");

        check(Subject.callAs(sub, () -> Subject.current()) == sub, "callAs lambda returns bound subject");
        check(Subject.callAs(null, () -> Subject.current()) == null, "callAs null binds null subject");

        // callAs wraps checked exceptions in CompletionException
        boolean ce = false;
        try {
            Subject.callAs(sub, (Callable<Void>) () -> { throw new java.io.IOException("x"); });
        } catch (CompletionException e) { ce = e.getCause() instanceof java.io.IOException; }
        check(ce, "callAs wraps checked exception in CompletionException");

        // doAs (PrivilegedAction) propagates subject and result
        Subject doAsGot = Subject.doAs(sub, (PrivilegedAction<Subject>) () -> Subject.current());
        check(doAsGot == sub, "doAs binds subject");
        check(Subject.current() == null, "current() reset after doAs");
        check(Subject.doAs(sub, (PrivilegedAction<String>) () -> "value").equals("value"), "doAs returns action result");

        // doAs (PrivilegedExceptionAction) wraps checked exception in PrivilegedActionException
        boolean pae = false;
        try {
            Subject.doAs(sub, (PrivilegedExceptionAction<Void>) () -> { throw new java.io.IOException("x"); });
        } catch (java.security.PrivilegedActionException e) { pae = e.getCause() instanceof java.io.IOException; }
        check(pae, "doAs wraps checked exception in PrivilegedActionException");

        // doAsPrivileged ignores the AccessControlContext and still binds subject
        AccessControlContext acc = AccessController.getContext();
        Subject dop = Subject.doAsPrivileged(sub, (PrivilegedAction<Subject>) () -> Subject.current(), acc);
        check(dop == sub, "doAsPrivileged binds subject");
        Subject dope = Subject.doAsPrivileged(sub, (PrivilegedExceptionAction<Subject>) () -> Subject.current(), acc);
        check(dope == sub, "doAsPrivileged(PrivilegedExceptionAction) binds subject");

        // null action rejected
        boolean npeA = false;
        try { Subject.doAs(sub, (PrivilegedAction<Void>) null); } catch (NullPointerException e) { npeA = true; }
        check(npeA, "doAs null action throws NullPointerException");
        boolean npeC = false;
        try { Subject.callAs(sub, (Callable<Void>) null); } catch (NullPointerException e) { npeC = true; }
        check(npeC, "callAs null action throws NullPointerException");

        // deprecated getSubject always throws UnsupportedOperationException in JDK-27
        boolean uoe = false;
        try { Subject.getSubject(acc); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "getSubject throws UnsupportedOperationException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
