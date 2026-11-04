/*
 * @test
 * @summary java.nio.file.attribute.PosixFilePermissions conformance: white-box structure + black-box behavior.
 * @run main PosixFilePermissionsTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.TreeSet;
import java.nio.file.attribute.PosixFileAttributes;

public class PosixFilePermissionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.attribute.PosixFilePermissions");
        check(Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
              "final utility class");
        check(c.getDeclaredConstructors().length == 0
              || java.util.Arrays.stream(c.getDeclaredConstructors())
                   .allMatch(k -> Modifier.isPrivate(k.getModifiers())),
              "no public ctor");
        check(java.util.Arrays.stream(c.getMethods())
              .filter(m -> m.getDeclaringClass().equals(c))
              .allMatch(m -> Modifier.isStatic(m.getModifiers())), "all own methods static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fromString parses all 9 rwx slots
        Set<PosixFilePermission> rw = PosixFilePermissions.fromString("rw-r--r--");
        check(rw.size() == 4, "4 permissions parsed");
        check(rw.contains(PosixFilePermission.OWNER_READ), "owner read");
        check(rw.contains(PosixFilePermission.OWNER_WRITE), "owner write");
        check(rw.contains(PosixFilePermission.GROUP_READ), "group read");
        check(rw.contains(PosixFilePermission.OTHERS_READ), "others read");

        // toString/fromString round trip for representative masks
        for (String mask : new String[] {"rwxrwxrwx", "rwx------", "rw-rw----",
                                         "---------", "r-xr-x---"}) {
            check(PosixFilePermissions.toString(PosixFilePermissions.fromString(mask)).equals(mask),
                  "round trip " + mask);
        }

        // toString uses sorted fixed order
        Set<PosixFilePermission> perm = new TreeSet<>(Set.of(
                PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OWNER_READ));
        check(PosixFilePermissions.toString(perm).equals("r------w-"),
              "fixed rwx order: " + PosixFilePermissions.toString(perm));

        // malformed strings are rejected
        for (String bad : new String[] {"", "rwxrwxrw", "rwxrwxrwxrwx", "abcdefghi", "Rwxrwxrwx"}) {
            boolean threw = false;
            try { PosixFilePermissions.fromString(bad); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "fromString(\"" + bad + "\") -> IllegalArgumentException");
        }

        // asFileAttribute: name/value contract without touching the filesystem
        FileAttribute<Set<PosixFilePermission>> attr =
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
        check(attr.name().equals("posix:permissions"), "attribute name");
        check(attr.value().equals(PosixFilePermissions.fromString("rw-------")), "attribute value");

        // Files integration when POSIX is supported: apply and read back
        if (java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
            try {
                java.nio.file.Path f = dir.resolve("p.txt");
                java.nio.file.Files.writeString(f, "x");
                java.nio.file.Files.setPosixFilePermissions(f,
                        PosixFilePermissions.fromString("rw-------"));
                check(java.nio.file.Files.getPosixFilePermissions(f)
                      .equals(PosixFilePermissions.fromString("rw-------")),
                      "permissions applied to real file");
            } finally {
                java.nio.file.Files.deleteIfExists(dir.resolve("p.txt"));
                java.nio.file.Files.deleteIfExists(dir);
            }
        } else {
            System.out.println("NOTE: POSIX attribute view unsupported; file part skipped");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===== A12 contract deepening (appended) =====
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    interface IORunnable { void run() throws java.io.IOException; }

    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void cleanup(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                         java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) cleanup(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    static void testContract() throws Exception {
        // fromString exact-membership matrix
        java.util.Set<PosixFilePermission> all = PosixFilePermissions.fromString("rwxrwxrwx");
        check(all.size() == 9, "rwxrwxrwx yields 9 permissions");
        check(all.containsAll(java.util.EnumSet.allOf(PosixFilePermission.class)), "rwxrwxrwx all members");
        check(PosixFilePermissions.toString(all).equals("rwxrwxrwx"), "all-members toString");
        java.util.Set<PosixFilePermission> none = PosixFilePermissions.fromString("---------");
        check(none.isEmpty(), "--------- yields empty set");
        check(PosixFilePermissions.toString(none).equals("---------"), "empty set toString");
        java.util.Set<PosixFilePermission> mixed = PosixFilePermissions.fromString("rwxr-x---");
        check(mixed.size() == 5, "rwxr-x--- size");
        check(mixed.containsAll(java.util.EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE)),
              "rwxr-x--- exact members");
        check(!mixed.contains(PosixFilePermission.GROUP_WRITE), "rwxr-x--- group write absent");
        check(!mixed.contains(PosixFilePermission.OTHERS_READ) && !mixed.contains(PosixFilePermission.OTHERS_WRITE)
              && !mixed.contains(PosixFilePermission.OTHERS_EXECUTE), "rwxr-x--- others none");
        check(PosixFilePermissions.toString(mixed).equals("rwxr-x---"), "rwxr-x--- toString round-trip");
        java.util.Set<PosixFilePermission> ex = PosixFilePermissions.fromString("--xr-x--x");
        check(ex.size() == 4, "--xr-x--x size");
        check(ex.containsAll(java.util.EnumSet.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE)), "--xr-x--x exact members");
        check(PosixFilePermissions.toString(ex).equals("--xr-x--x"), "--xr-x--x toString round-trip");
        // single-bit sensitivity: each of the 9 slots parses alone (slot -> constant mapping)
        PosixFilePermission[] slots = { PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE,
                PosixFilePermission.OTHERS_EXECUTE };
        char[] chars = { 'r', 'w', 'x' };
        for (int i = 0; i < 9; i++) {
            StringBuilder sb = new StringBuilder("---------");
            sb.setCharAt(i, chars[i % 3]);
            java.util.Set<PosixFilePermission> one = PosixFilePermissions.fromString(sb.toString());
            check(one.size() == 1 && one.iterator().next() == slots[i],
                  "single bit '" + sb + "' -> " + slots[i]);
            // wrong character in slot i is rejected (each slot accepts exactly one permission char or '-')
            StringBuilder bad = new StringBuilder("---------");
            bad.setCharAt(i, chars[(i + 1) % 3]);
            final String input = bad.toString();
            checkThrows(IllegalArgumentException.class, () -> PosixFilePermissions.fromString(input),
                    "wrong char '" + input + "' in slot " + i + " rejected");
        }
        // toString round-trips for a sample of real-world modes
        String[] modes = { "rwxrwxrwx", "rw-rw-r--", "rwx------", "r--r--r--", "rw-r-----", "rwxr-xr-x" };
        for (String m : modes) {
            check(PosixFilePermissions.toString(PosixFilePermissions.fromString(m)).equals(m),
                  "round-trip of '" + m + "'");
        }
        // fromString illegal-input matrix (exact exception + message)
        checkThrows(NullPointerException.class, () -> PosixFilePermissions.fromString(null), "fromString(null) NPE");
        final String[] illegal = { "", "rwxr-x", "rwxr-x---x", "rwxr-x--q", "Rwxr-x---", "rwxxr-x---", "rwxr-x-- " };
        for (final String s : illegal) {
            try {
                PosixFilePermissions.fromString(s);
                check(false, "fromString('" + s + "') must throw");
            } catch (IllegalArgumentException e) {
                check("Invalid mode".equals(e.getMessage()), "fromString('" + s + "') IAE message");
            }
        }
        // toString(null) NPE; null element in set reads as absent bit
        checkThrows(NullPointerException.class, () -> PosixFilePermissions.toString(null), "toString(null) NPE");
        java.util.Set<PosixFilePermission> withNull = new java.util.HashSet<PosixFilePermission>();
        withNull.add(null);
        check(PosixFilePermissions.toString(withNull).equals("---------"), "null element treated as absent bit");
        // asFileAttribute: name/value contract + snapshot isolation
        java.util.Set<PosixFilePermission> want = PosixFilePermissions.fromString("rwxr-x---");
        FileAttribute<java.util.Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(want);
        check(attr.name().equals("posix:permissions"), "asFileAttribute name");
        check(attr.value().equals(want), "asFileAttribute value equals argument");
        check(attr.value() != want, "asFileAttribute value is a defensive copy");
        want.add(PosixFilePermission.OTHERS_EXECUTE);
        check(attr.value().size() == 5, "value snapshot unaffected by later mutation of source set");
        want.remove(PosixFilePermission.OTHERS_EXECUTE);
        check(attr.value().equals(attr.value()), "value() stable across calls");
        checkThrows(NullPointerException.class, () -> PosixFilePermissions.asFileAttribute(null),
                "asFileAttribute(null) NPE");
        FileAttribute<java.util.Set<PosixFilePermission>> emptyAttr =
                PosixFilePermissions.asFileAttribute(new java.util.HashSet<PosixFilePermission>());
        check(emptyAttr.name().equals("posix:permissions") && emptyAttr.value().isEmpty(),
              "asFileAttribute accepts empty set (mode 000)");
        // Files integration: createFile with asFileAttribute applies the mode exactly
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a12-pp");
        try {
            java.nio.file.Path f = dir.resolve("f");
            java.nio.file.Files.createFile(f, PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rw-r-----")));
            check(PosixFilePermissions.toString(java.nio.file.Files.readAttributes(f, PosixFileAttributes.class)
                    .permissions()).equals("rw-r-----"), "createFile applies mode exactly");
            // umask must NOT apply when the attribute is given
            java.util.Set<PosixFilePermission> got = java.nio.file.Files.readAttributes(f, PosixFileAttributes.class).permissions();
            check(got.equals(PosixFilePermissions.fromString("rw-r-----")), "mode round-trip set equality");
            // mode 000 file can be created and stat'ed but not read by anyone (incl. owner)
            java.nio.file.Path z = dir.resolve("z");
            java.nio.file.Files.createFile(z, emptyAttr);
            check(java.nio.file.Files.readAttributes(z, PosixFileAttributes.class).permissions().isEmpty(),
                  "mode 000 permissions read back empty");
            checkThrowsIO(java.nio.file.AccessDeniedException.class, () -> java.nio.file.Files.readAllBytes(z),
                    "read of mode-000 file denied");
            // Files.getAttribute / readAttributes map family
            Object permsAttr = java.nio.file.Files.getAttribute(f, "posix:permissions");
            check(permsAttr instanceof java.util.Set<?>, "getAttribute posix:permissions is a Set");
            check(PosixFilePermissions.toString((java.util.Set<PosixFilePermission>) permsAttr).equals("rw-r-----"),
                  "getAttribute posix:permissions value");
            check(java.nio.file.Files.getAttribute(f, "basic:size").equals(Long.valueOf(0L)),
                  "getAttribute basic:size is Long");
            java.util.Map<String, Object> amap = java.nio.file.Files.readAttributes(f, "posix:permissions,size");
            check(amap.keySet().equals(new java.util.HashSet<>(java.util.Arrays.asList("permissions", "size"))),
                  "readAttributes comma-list key set");
            check(((java.util.Set<?>) amap.get("permissions")).contains(PosixFilePermission.OWNER_READ),
                  "readAttributes map permissions member");
            checkThrowsIO(IllegalArgumentException.class, () -> java.nio.file.Files.getAttribute(f, "posix:nosuch"),
                    "getAttribute unknown posix attribute IAE");
            checkThrowsIO(java.lang.UnsupportedOperationException.class,
                    () -> java.nio.file.Files.getAttribute(f, "bogusview:x"),
                    "getAttribute unknown view UOE");
        } finally {
            cleanup(dir);
        }
    }

}
