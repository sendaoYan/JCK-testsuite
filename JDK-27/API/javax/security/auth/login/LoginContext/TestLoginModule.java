import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Map;

public class TestLoginModule implements LoginModule {
    static TestLoginModule lastCreated;
    boolean initialized, loggedIn, committed, aborted, loggedOut;
    Subject subject;

    public TestLoginModule() { lastCreated = this; }

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        initialized = true;
    }
    public boolean login() { loggedIn = true; return true; }
    public boolean commit() {
        committed = true;
        subject.getPrincipals().add(new TestPrincipal("p"));
        subject.getPublicCredentials().add("credential");
        return true;
    }
    public boolean abort() { aborted = true; return true; }
    public boolean logout() {
        loggedOut = true;
        subject.getPrincipals().remove(new TestPrincipal("p"));
        subject.getPublicCredentials().remove("credential");
        return true;
    }

    private static class TestPrincipal implements Principal {
        final String name;
        TestPrincipal(String name) { this.name = name; }
        public String getName() { return name; }
        public boolean equals(Object o) {
            return o instanceof TestPrincipal && name.equals(((TestPrincipal) o).name);
        }
        public int hashCode() { return name.hashCode(); }
        public String toString() { return name; }
    }
}
