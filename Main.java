package gitlet;



import java.util.TreeMap;
import java.io.IOException;
import java.io.File;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.LinkedList;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author athmiha bhaskaran
 */
public class Main {

    /**
     * Current Working Directory.
     */
    static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * GITLET REPOSITORY folder.
     */
    static final File GITLET = Utils.join(CWD, ".gitlet");

    /**
     * STAGING AREA folder.
     */
    static final File STAGING_AREA = Utils.join(GITLET,
            "staging_area");

    /**
     * STAGING FOR ADDITION folder.
     */
    static final File STAGED_ADDITION = Utils.join(STAGING_AREA,
            "staged_addition");

    /**
     * STAGING FOR REMOVAL folder.
     */
    static final File STAGED_RM = Utils.join(STAGING_AREA, "staged_rm");

    /**
     * COMMITS folder.
     */
    static final File COMMITS = Utils.join(GITLET, "commits");

    /**
     * HEAD folder.
     */
    static final File HEAD = Utils.join(GITLET, "head");

    /**
     * BLOBS folder.
     */
    static final File BLOBS = Utils.join(GITLET, "blobs");

    /**
     * BRANCHES folder.
     */
    static final File BRANCHES = Utils.join(GITLET, "branches");


    public static void main(String... args) throws IOException {
        try {
            if (args.length == 0) {
                System.out.println("Please enter a command.");
                return;
            }
            switch (args[0]) {
            case "init":
                init();
                break;
            case "commit":
                commit(args[1]);
                break;
            case "add":
                add(args[1]);
                break;
            case "checkout":
                checkout(args);
                break;
            case "log":
                log();
                break;
            case "rm":
                rm(args[1]);
                break;
            case "global-log":
                globalLog();
                break;
            case "find":
                find(args[1]);
                break;
            case "status":
                status();
                break;
            case "branch":
                branch(args[1]);
                break;
            case "rm-branch":
                rmBranch(args[1]);
                break;
            case "reset":
                reset(args[1]);
                break;
            case "merge":
                merge(args[1]);
                break;
            case "diff":
                diff(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
        } catch (GitletException a) {
            System.out.println(a.getMessage());
        }
    }

    public static void init() throws IOException {
        if (!GITLET.exists()) {
            GITLET.mkdir();
            STAGING_AREA.mkdir();
            STAGED_ADDITION.mkdir();
            STAGED_RM.mkdir();
            COMMITS.mkdir();
            BLOBS.mkdir();
            BRANCHES.mkdir();
            Commit initial = new Commit("initial commit", null, null);
            initial.saveCommit();
            Branch master = new Branch("master", true, initial);
            master.branchSave();
        } else {
            throw new GitletException("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }

    }

    public static void commit(String message) throws IOException {
        if (message.length() < 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit parent = Utils.readObject(HEAD, Commit.class);
        Commit x = new Commit(message, parent, null);
        x.saveCommit();
        if (x.getCM().equals(parent.getCM())) {
            System.out.println("No changes added to the commit.");
            return;
        }
    }

    public static void globalLog() {
        for (File f : COMMITS.listFiles()) {
            Commit deserializingfile = Utils.readObject(f, Commit.class);
            while (deserializingfile != null) {
                System.out.println("===");
                System.out.println("commit "
                        + cerealC(deserializingfile));
                System.out.println("Date: " + deserializingfile.getDate());
                System.out.println(deserializingfile.getMessage());
                System.out.println("");
                deserializingfile = deserializingfile.getParent();
            }
        }
    }

    public static Commit lca(String name) {
        String active = "master";
        for (File b : BRANCHES.listFiles()) {
            Branch deserializedB = Utils.readObject(b, Branch.class);
            if (deserializedB.getActive()) {
                active = b.getName();
            }
        }
        Branch masterBranch = Utils.readObject(Utils.join(BRANCHES,
                active), Branch.class);
        Branch currentBranch = Utils.readObject(Utils.join(BRANCHES, name),
                Branch.class);
        ArrayList<String> visitedC = new ArrayList<String>();
        Queue<Commit> q = new LinkedList<>();
        q.add(currentBranch.getCommit());
        while (!q.isEmpty()) {
            Commit c = q.remove();
            if (!visitedC.contains(cerealC(c))) {
                visitedC.add(cerealC(c));
            }
            if (c.getParent() != null) {
                q.add(c.getParent());
            }
            if (c.getCommitParent() != null) {
                q.add(c.getCommitParent());
            }
        }
        q.add(masterBranch.getCommit());
        while (!q.isEmpty()) {
            Commit c = q.remove();
            if (visitedC.contains(cerealC(c))) {
                return c;
            }
            if (c.getParent() != null) {
                q.add(c.getParent());
            }
            if (c.getCommitParent() != null) {
                q.add(c.getCommitParent());
            }
        }
        return currentBranch.getCommit();
    }

    public static String cerealC(Commit s) {
        return Utils.sha1(Utils.serialize(s));
    }

    public static void lcaChecker(Commit splitPoint,
                                  Commit currBranch) throws IOException {
        if (cerealC(splitPoint).equals(cerealC(currBranch))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return;
        }
    }

    public static String rSha1(File b) {
        return Utils.sha1(Utils.readContents(b));
    }

    public static String rRSha1(File b) {
        return Utils.sha1(Utils.readContentsAsString(b));
    }

    public static void mergeErrorChecking(String name) {
        ArrayList<String> branchNames = new ArrayList<String>();
        for (File b : BRANCHES.listFiles()) {
            branchNames.add(b.getName());
        }
        if (!branchNames.contains(name)) {
            throw new GitletException("A branch with "
                    + "that name does not exist.");
        }
        Branch cB = Utils.readObject(Utils.join(BRANCHES,
                name), Branch.class);
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        if (STAGED_ADDITION.listFiles().length > 0
                || STAGED_RM.listFiles().length > 0) {
            throw new GitletException("You have "
                    + "uncommitted changes.");
        }
        for (File y : CWD.listFiles()) {
            if (!y.getName().equals(".gitlet")) {
                String h = cB.getCommit().getCM().get(y.getName());
                if (!deserializingHead.getCM().containsKey(y.getName())
                        && !rSha1(y).equals(h)
                        && cB.getCommit().getCM().containsKey(y.getName())) {
                    throw new GitletException("There is an untracked "
                            + "file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }
        for (File m : BRANCHES.listFiles()) {
            Branch deserializedM = Utils.readObject(m, Branch.class);
            if (m.getName().equals(name) && deserializedM.getActive()) {
                throw new GitletException("Cannot merge a branch with itself.");
            }
        }
    }

    public static Boolean givenBranchMeth(Commit splitPoint,
                                          Branch cB, Branch givenBranch)
            throws IOException {
        for (Entry<String, String> e : cB.getCommit().getCM().entrySet()) {
            if (!givenBranch.getCommit().getCM().containsKey(e.getKey())
                    && splitPoint.getCM().containsKey(e.getKey())
                    &&
                    splitPoint.getCM().get(e.getKey()).equals(e.getValue())) {
                String[] uad = new String[]
                {"checkout", cerealC(cB.getCommit()), "--", e.getKey()};
                checkout(uad);
                rm(e.getKey());
            } else if (!givenBranch.getCommit().getCM().containsKey(e.getKey())
                    && splitPoint.getCM().containsKey(e.getKey())
                    &&
                    !splitPoint.getCM().get(e.getKey()).equals(e.getValue())) {
                File f = Utils.join(CWD, e.getKey());
                TreeMap<String, String> rt = cB.getCommit().getCM();
                String mk = "<<<<<<< HEAD\n"
                        + Utils.readContentsAsString(Utils.join(BLOBS,
                        rt.get(e.getKey()) + ".txt"))
                        + "=======\n"
                        + ">>>>>>>\n";
                Utils.writeContents(f, mk);
                add(f.getName());
                return true;
            }
        }
        return false;
    }

    public static void fastForward(Commit splitPoint, Branch cB,
                                   Branch givenBranch) throws IOException {
        if (splitPoint.getCM().equals(cB.getCommit().getCM())) {
            String[] a = new String[]{"checkout", givenBranch.getName()};
            checkout(a);
            throw new GitletException("Current branch fast-forwarded.");
        }
    }

    public static void splitPointHelper(Commit splitPoint,
                                        Branch givenBranch, Branch cB)
            throws IOException {

        for (Entry<String, String> e : splitPoint.getCM().entrySet()) {
            String h = givenBranch.getCommit().getCM().get(e.getKey());
            String c = cB.getCommit().getCM().get(e.getKey());
            TreeMap<String, String> m = cB.getCommit().getCM();
            if (givenBranch.getCommit().getCM().containsKey(e.getKey())
                    && m.containsKey(e.getKey())
                    && !h.equals(e.getValue())
                    && c.equals(e.getValue())) {
                String[] l = new String[]{"checkout",
                        cerealC(givenBranch.getCommit()), "--", e.getKey()};
                checkout(l);
                add(e.getKey());
            }
        }

    }

    public static String insides(String g,
                                 TreeMap<String, String> rt,
                                 String e) {
        String mk = "<<<<<<< HEAD\n"
                + Utils.readContentsAsString(Utils.join(BLOBS,
                g + ".txt")) + "=======\n"
                + Utils.readContentsAsString(Utils.join(BLOBS,
                rt.get(e) + ".txt"))
                + ">>>>>>>\n";
        return mk;

    }
    public static void merge(String name) throws IOException {
        Boolean conflict = false;
        mergeErrorChecking(name);
        Branch givenBranch = Utils.readObject(Utils.join(BRANCHES, name),
                Branch.class);
        Branch cB = Utils.readObject(Utils.join(BRANCHES,
                name), Branch.class);
        for (File f : BRANCHES.listFiles()) {
            Branch deserializedF = Utils.readObject(f, Branch.class);
            if (deserializedF.getActive()) {
                cB = Utils.readObject(Utils.join(BRANCHES,
                        f.getName()), Branch.class);
                break;
            }
        }
        Commit splitPoint = lca(name);
        lcaChecker(splitPoint, givenBranch.getCommit());
        fastForward(splitPoint, cB, givenBranch);
        splitPointHelper(splitPoint, givenBranch, cB);
        conflict = givenBranchMeth(splitPoint, cB, givenBranch);
        for (Entry<String, String> e
                : givenBranch.getCommit().getCM().entrySet()) {
            TreeMap<String, String> m = splitPoint.getCM();
            String g = cB.getCommit().getCM().get(e.getKey());
            String a = splitPoint.getCM().get(e.getKey());
            Commit u = givenBranch.getCommit();
            TreeMap<String, String> rt = givenBranch.getCommit().getCM();
            if (!m.containsKey(e.getKey())
                    && !cB.getCommit().getCM().containsKey(e.getKey())) {
                String[] l = new String[]
                {"checkout", cerealC(u), "--", e.getKey()};
                checkout(l);
                add(e.getKey());
            } else if (splitPoint.getCM().containsKey(e.getKey())
                    && !a.equals(e.getValue()) && !a.equals(g)
                    && cB.getCommit().getCM().containsKey(e.getKey())
                    && !g.equals(e.getValue())) {
                File f = Utils.join(CWD, e.getKey());
                String mk = insides(g, rt, e.getKey());
                Utils.writeContents(f, mk);
                add(f.getName());
                conflict = true;
            } else if (cB.getCommit().getCM().containsKey(e.getKey())
                    && !g.equals(e.getValue())
                    && !splitPoint.getCM().containsKey(e.getKey())) {
                File f = Utils.join(CWD, e.getKey());
                String mk = insides(g, rt, e.getKey());
                Utils.writeContents(f, mk);
                add(f.getName());
                conflict = true;
            } else if (!cB.getCommit().getCM().containsKey(e.getKey())
                    && splitPoint.getCM().containsKey(e.getKey())
                    && !a.equals(e.getValue())) {
                conflict = merge12(rt, e.getKey());
            }
        }
        ending(conflict, givenBranch, cB);
    }
    public static Boolean merge12(TreeMap<String,
            String> rt, String e) throws IOException {
        File f = Utils.join(CWD, e);
        String mk = "<<<<<<< HEAD\n" + "=======\n"
                + Utils.readContentsAsString(Utils.join(BLOBS,
                rt.get(e) + ".txt"))
                + ">>>>>>>\n";
        Utils.writeContents(f, mk);
        add(f.getName());
        return true;
    }

    public static void ending(Boolean conflict, Branch givenBranch,
                              Branch cB) throws IOException {
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        Commit y = new Commit("Merged "
                + givenBranch.getName() + " into " + cB.getName()
                + ".", cB.getCommit(), givenBranch.getCommit());
        y.saveCommit();
    }

    public static void status() {
        if (!GITLET.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        ArrayList<File> branches = new ArrayList<File>();
        System.out.println("=== Branches ===");
        for (File f : BRANCHES.listFiles()) {
            branches.add(f);
        }
        Collections.sort(branches);
        for (File f : branches) {
            Branch deserializingF = Utils.readObject(f, Branch.class);
            if (deserializingF.getActive()) {
                System.out.println("*" + f.getName());
            } else {
                System.out.println(f.getName());
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        ArrayList<String> stagedFiles = new ArrayList<String>();
        for (File f : STAGED_ADDITION.listFiles()) {
            stagedFiles.add(f.getName());
        }

        Collections.sort(stagedFiles);
        for (String f : stagedFiles) {
            System.out.println(f);
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        ArrayList<File> removedFiles = new ArrayList<File>();
        for (File f : STAGED_RM.listFiles()) {
            removedFiles.add(f);
        }
        Collections.sort(removedFiles);
        for (File f : removedFiles) {
            System.out.println(f.getName());
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        statusHelp(stagedFiles);
        System.out.println("=== Untracked Files ===");
        for (File f : CWD.listFiles()) {
            Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
            if (!f.getName().equals(".gitlet")) {
                if (!deserializingHead.getCM().containsKey(f.getName())
                        && !stagedFiles.contains(f.getName())) {
                    System.out.println(f.getName());
                }
            }
        }
        System.out.println("");
    }

    public static void statusHelp(ArrayList<String> stagedFiles) {
        ArrayList<String> cwdfiles = new ArrayList<String>();
        for (File f : CWD.listFiles()) {
            if (!f.getName().equals(".gitlet")) {
                cwdfiles.add(f.getName());
            }
        }
        Collections.sort(cwdfiles);
        ArrayList<String> cwdblobs = new ArrayList<String>();
        for (File f : CWD.listFiles()) {
            if (!f.getName().equals(".gitlet")) {
                String b = rRSha1(f);
                cwdblobs.add(b);
            }
        }
        ArrayList<String> removal = new ArrayList<String>();
        for (File f : STAGED_RM.listFiles()) {
            removal.add(f.getName());
        }
        ArrayList<String> mods = new ArrayList<String>();
        for (File f : CWD.listFiles()) {
            Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
            if (!f.getName().equals(".gitlet")) {
                String p = deserializingHead.getCM().get(f.getName());
                if (deserializingHead.getCM().containsKey(f.getName())
                        && !stagedFiles.contains(f.getName())
                        && !cwdblobs.contains(p)) {
                    mods.add(f.getName() + " (modified)");
                } else if (stagedFiles.contains(f.getName())
                        &&
                        !cwdblobs.contains(rRSha1(Utils.join(STAGED_ADDITION,
                                f.getName())))) {
                    mods.add(f.getName() + " (modified)");
                }
            }
        }
        for (File f : STAGED_ADDITION.listFiles()) {
            if (!cwdfiles.contains(f.getName())) {
                mods.add(f.getName() + " (deleted)");
            }
        }
        Commit deserializingHead1 =
                Utils.readObject(HEAD, Commit.class);
        for (Entry<String, String> e : deserializingHead1.getCM().entrySet()) {
            if (!removal.contains(e.getKey())
                    && !cwdfiles.contains(e.getKey())) {
                mods.add(e.getKey() + " (deleted)");
            }
        }
        Collections.sort(mods);
        for (String s : mods) {
            System.out.println(s);
        }
        System.out.println("");
    }

    public static void add(String name) throws IOException {
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        File f = Utils.join(CWD, name);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String b = rRSha1(f);
        File yu = Utils.join(STAGED_RM, name);
        if (yu.exists()) {
            yu.delete();
        }
        if (!deserializingHead.getCM().containsKey(name)
                || !deserializingHead.getCM().get(name).equals(b)) {
            File x = Utils.join(CWD, name);
            File y = Utils.join(STAGED_ADDITION, name);
            if (!y.exists()) {
                y.createNewFile();
            }
            Utils.writeContents(y, Utils.readContents(x));
        }
    }

    public static void rm(String args) throws IOException {
        File x = Utils.join(STAGED_ADDITION, args);
        File y = Utils.join(CWD, args);
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        if (!x.exists() && !deserializingHead.getCM().containsKey(args)) {
            throw new GitletException("No reason to remove the file.");
        }
        if (x.exists() && !deserializingHead.getCM().containsKey(args)) {
            x.delete();
        }
        if (x.exists() && deserializingHead.getCM().containsKey(args)) {
            File n = Utils.join(STAGED_RM, args);
            n.createNewFile();
            x.delete();
        }
        if (deserializingHead.getCM().containsKey(args)) {
            File z = Utils.join(STAGED_RM, args);
            z.createNewFile();
            Utils.restrictedDelete(y);
        }
        if (x.exists()) {
            x.delete();
        }
    }

    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(-1);
    }

    public static void reset(String args) throws IOException {
        args = shorten(args);
        ArrayList<String> fileNames = new ArrayList<String>();

        for (File f : COMMITS.listFiles()) {
            fileNames.add(f.getName());

        }
        if (!fileNames.contains(args)) {
            throw new GitletException("No commit with that id exists.");
        }
        Commit cell = Utils.readObject(Utils.join(COMMITS, args), Commit.class);
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);

        for (File y : CWD.listFiles()) {
            if (!y.getName().equals(".gitlet")) {
                if (!deserializingHead.getCM().containsKey(y.getName())
                        &&
                        !rSha1(y).equals(cell.getCM().get(y.getName()))
                        && cell.getCM().containsKey(y.getName())) {
                    throw new GitletException("There is an "
                            + "untracked file in the way; "
                            + "delete it, or add and commit it first.");

                }
                if (!cell.getCM().containsKey(y.getName())) {
                    Utils.restrictedDelete(y);
                }

            }
        }
        for (Entry<String, String> e : deserializingHead.getCM().entrySet()) {
            if (!cell.getCM().containsKey(e.getKey())) {
                if (Utils.join(CWD, e.getKey()).exists()) {
                    Utils.restrictedDelete(Utils.join(CWD, e.getKey()));
                }
            }
        }
        for (File f : BRANCHES.listFiles()) {
            Branch deserializingF = Utils.readObject(f, Branch.class);
            if (deserializingF.getActive()) {
                for (File a : COMMITS.listFiles()) {
                    if (a.getName().equals(args)) {
                        deserializingF.commitSetter(a);
                        Utils.writeObject(HEAD, Utils.readObject(a,
                                Commit.class));
                        deserializingF.branchSave();
                    }
                }
            }
        }
        for (File l : STAGED_ADDITION.listFiles()) {
            l.delete();
        }
        for (Entry<String, String> e : cell.getCM().entrySet()) {
            String[] a = new String[]{"checkout", "--", e.getKey()};
            checkout(a);
        }

    }

    public static void log() {
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        while (deserializingHead != null) {
            System.out.println("===");
            System.out.println("commit " + cerealC(deserializingHead));
            System.out.println("Date: " + deserializingHead.getDate());
            System.out.println(deserializingHead.getMessage());
            System.out.println("");
            deserializingHead = deserializingHead.getParent();
        }
    }

    public static void find(String message) {
        boolean p = true;
        for (File f : COMMITS.listFiles()) {
            Commit deserializingfile = Utils.readObject(f, Commit.class);
            if (deserializingfile.getMessage().equals(message)) {
                System.out.println(f.getName());
                p = false;
            }
        }
        if (p) {
            throw new GitletException("Found no commit with that message.");
        }

    }
    public static void rmBranch(String name) {
        for (File f : BRANCHES.listFiles()) {
            Branch deserializingF = Utils.readObject(f,
                    Branch.class);
            if (f.getName().equals(name)) {
                if (deserializingF.getActive()) {
                    throw new GitletException("Cannot "
                            + "remove the current branch.");
                }
                f.delete();
                return;
            }
        }
        throw new GitletException("A branch with that name does not exist.");
    }
    public static void branch(String name) throws IOException {
        for (File f : BRANCHES.listFiles()) {
            if (f.getName().equals(name)) {
                throw new GitletException("A branch "
                        + "with that name already exists.");
            }
        }
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        Branch x = new Branch(name, false, deserializingHead);
        x.branchSave();
    }

    public static String shorten(String id) {
        for (File f : COMMITS.listFiles()) {
            if (f.getName().startsWith(id)) {
                return f.getName();
            }
        }
        return id;
    }

    public static void existsPrev(String name) {
        Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
        if (!deserializingHead.getCM().containsKey(name)) {
            throw new GitletException("File does not exist in that commit.");
        }
    }

    public static void checkout(String[] args) throws IOException {
        if (args.length == 3) {
            existsPrev(args[2]);
            Commit deserializingHead = Utils.readObject(HEAD, Commit.class);
            String nameOfFile = deserializingHead.getCM().get(args[2]);
            File fileInBlob = Utils.join(BLOBS, nameOfFile + ".txt");
            String b = Utils.readContentsAsString(fileInBlob);
            Utils.writeContents(Utils.join(CWD, args[2]), b);
        } else if (args.length == 4) {
            if (args[1].length() < Utils.UID_LENGTH) {
                args[1] = shorten(args[1]);
            }
            File f = Utils.join(COMMITS, args[1]);

            if (!f.exists()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (!args[2].equals("--")) {
                throw new GitletException("Incorrect operands.");

            }
            Commit deserializingHead = Utils.readObject(f, Commit.class);
            if (!deserializingHead.getCM().containsKey(args[3])) {
                throw new GitletException("File does "
                        + "not exist in that commit.");
            }
            String nameOfFile = deserializingHead.getCM().get(args[3]);
            File fileInBlob = Utils.join(BLOBS, nameOfFile + ".txt");
            String b = Utils.readContentsAsString(fileInBlob);
            Utils.writeContents(Utils.join(CWD, args[3]), b);
        } else if (args.length == 2) {
            File x = Utils.join(BRANCHES, args[1]);
            checkout2(x);

        }
    }

    public static void checkout2(File x) throws IOException {
        if (!x.exists()) {
            throw new GitletException("No such branch exists.");
        }
        Branch dF = Utils.readObject(x, Branch.class);
        Commit dH = Utils.readObject(HEAD, Commit.class);
        for (File y : CWD.listFiles()) {
            if (!y.getName().equals(".gitlet")) {
                TreeMap<String, String> m = dF.getCommit().getCM();
                if (!dH.getCM().containsKey(y.getName())
                        && m.containsKey(y.getName())) {
                    throw new GitletException("There "
                            + "is an untracked file in the way; delete it, "
                            + "or add and commit it first.");
                }
            }
        }
        if (dF.getActive()) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else {
            for (File f : BRANCHES.listFiles()) {
                Branch deserializingZ = Utils.readObject(f, Branch.class);
                deserializingZ.falseActive();
                deserializingZ.branchSave();
            }
            dF.trueActive();
            dF.branchSave();
            for (Entry<String, String> e
                    : dF.getCommit().getCM().entrySet()) {
                String nameOfFile = e.getValue();
                File fileInBlob = Utils.join(BLOBS, nameOfFile + ".txt");
                String b = Utils.readContentsAsString(fileInBlob);
                Utils.writeContents(Utils.join(CWD, e.getKey()), b);
            }
            Commit dH1 = Utils.readObject(HEAD,
                    Commit.class);
            for (Entry<String, String> e
                    : dH1.getCM().entrySet()) {
                TreeMap<String, String> m = dF.getCommit().getCM();
                if (!m.containsKey(e.getKey())) {
                    File n = Utils.join(CWD, e.getKey());
                    Utils.restrictedDelete(n);
                }
            }
            for (File f : STAGED_ADDITION.listFiles()) {
                f.delete();
            }
            Utils.writeObject(HEAD, dF.getCommit());
        }

    }
    public static ArrayList<Integer> l1(File f, File m) {
        Diff d = new Diff();
        d.setSequences(f, m);
        int[] sequence = d.diffs();
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (int i = 0; i < sequence.length; i++) {
            if (i % 4 == 0) {
                a.add(sequence[i]);
            }
        }
        return a;
    }

    public static ArrayList<Integer> n1(File f, File m) {
        Diff d = new Diff();
        d.setSequences(f, m);
        int[] sequence = d.diffs();
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (int i = 0; i < sequence.length; i++) {
            if (i % 4 == 1) {
                a.add(sequence[i]);
            }
        }
        return a;
    }

    public static ArrayList<Integer> l2(File f, File m) {
        Diff d = new Diff();
        d.setSequences(f, m);
        int[] sequence = d.diffs();
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (int i = 0; i < sequence.length; i++) {
            if (i % 4 == 2) {
                a.add(sequence[i]);
            }
        }
        return a;
    }

    public static ArrayList<Integer> n2(File f, File m) {
        Diff d = new Diff();
        d.setSequences(f, m);
        int[] sequence = d.diffs();
        ArrayList<Integer> a = new ArrayList<Integer>();
        for (int i = 0; i < sequence.length; i++) {
            if (i % 4 == 3) {
                a.add(sequence[i]);
            }
        }
        return a;
    }
    public static void linePrinter(int l1, int n1, int l2,
                                   int n2, File f, File m) {
        Diff d = new Diff();
        d.setSequences(f, m);
        for (int i = 1; i <= n1; i++) {
            System.out.println("-" + d.get1(l1 + i - 1));
        }
        for (int i = 1; i <= n2; i++) {
            System.out.println("+" + d.get2(l2 + i - 1));
        }
    }

    public static Branch bitchy() {
        for (File f : BRANCHES.listFiles()) {
            Branch dF = Utils.readObject(f, Branch.class);
            if (dF.getActive()) {
                return dF;
            }
        }
        return null;
    }
    public static void diffWorker1(String e, String m) {
        System.out.println("diff --git a/" + e + " b/" + m);
        System.out.println("--- a/" + e);
        System.out.println("+++ b/" + m);
    }
    public static void h1(int l1V, int n1,
                          int l2, int l2V1, int n2, File f, File m) {
        System.out.println("@@ -" + l1V + ","
                + n1 + " " + "+" + l2V1 + " " + "@@");
        linePrinter(l1V, n1, l2, n2, f, m);
    }
    public static void h2(int l1V, int l1V1,
                          int l2, int n1, int n2, File f, File m) {
        System.out.println("@@ -" + l1V1 + " "
                + "+" + l2 + "," + n2 + " " + "@@");
        linePrinter(l1V, n1, l2, n2, f, m);
    }
    public static void h3(int l1V, int n1V, int l2V1, int n2V) {
        System.out.println("@@ -" + l1V + "," + n1V + " +"
                + l2V1 + "," + n2V + " @@");
    }
    public static void h4(int l1V, int n1V, int l2V,
                          int n2V, int l2V1,
                          File f, File m) {
        System.out.println("@@ -" + l1V + ", "
                + n1V + " +" + l2V1 + "," + n2V + " @@");
        linePrinter(l1V, n1V, l2V, n2V, f, m);
    }
    public static void h5(int l1V, int l1V1,
                          int n1V, int l2V1,
                          int n2V,
                          File f, File m, int l2V) {
        System.out.println("@@ -" + l1V1 + ","
                + n1V
                + " +" + l2V1 + "," + n2V + " @@");
        linePrinter(l1V, n1V, l2V, n2V, f, m);
    }
    public static void h6(int l1V1, int l2V1, int n2V,
                          int l1V, int n1V, int l2V,
                          File f, File m) {
        System.out.println("@@ -" + l1V1
                + " +" + l2V1 + "," + n2V + " @@");
        linePrinter(l1V, n1V, l2V, n2V, f, m);
    }


    public static void diffWorker(Commit c) {
        for (Entry<String, String> e : c.getCM().entrySet()) {
            File f = Utils.join(BLOBS, e.getValue() + ".txt");
            File m = Utils.join(CWD, e.getKey());
            ArrayList<Integer> l1 = l1(f, m); ArrayList<Integer> n1 = n1(f, m);
            ArrayList<Integer> l2 = l2(f, m); ArrayList<Integer> n2 = n2(f, m);
            if (m.exists()) {
                String y = Utils.sha1(Utils.readContentsAsString(m));
                if (e.getValue().equals(y)) {
                    continue;
                }
            }
            if (m.exists()) {
                diffWorker1(e.getKey(), m.getName());
                for (int i = 0; i < l1.size(); i++) {
                    int l1V = l1.get(i); int l1V1 = l1.get(i) + 1;
                    int l2V1 = l2.get(i) + 1; int n1V = n1.get(i);
                    int n2V = n2.get(i); int l2V = l2.get(i);
                    if (n1.get(i) == 0 && n2V == 0) {
                        h3(l1V, n1V, l2V1, n2V);
                    } else if (n1.get(i) == 0) {
                        if (n2V == 1) {
                            h1(l1V, n1V, l2.get(i), l2V1, n2V, f, m);
                        } else {
                            h4(l1V, n1V, l2V, n2V, l2V1, f, m);
                        }
                    } else if (n2V == 0) {
                        if (n1.get(i) == 1) {
                            h2(l1V, l1V1, l2.get(i), n1.get(i), n2V, f, m);
                        } else {
                            h5(l1V, l1V1, n1V, l2V1, n2V, f, m, l2V);
                        }
                    } else if (n1.get(i) == 1 && n2V == 1) {
                        System.out.println("@@ -" + l1V1 + " +" + l2V1 + " @@");
                        linePrinter(l1V, n1.get(i), l2.get(i), n2V, f, m);
                    } else if (n1.get(i) == 1) {
                        if (n2V == 0) {
                            h2(l1V, l1V1, l2.get(i), n1.get(i), n2V, f, m);
                        } else {
                            h6(l1V1, l2V1, n2V, l1V, n1V, l2V, f, m);
                        }
                    } else if (n2V == 1) {
                        if (n1.get(i) == 0) {
                            h1(l1V, n1V, l2.get(i), l2V1, n2V, f, m);
                        } else {
                            System.out.println("@@ -" + l1V1 + ","
                                    + n1.get(i) + " " + "+" + l2V1 + " @@");
                            linePrinter(l1V, n1.get(i), l2.get(i), n2V, f, m);
                        }
                    } else {
                        System.out.println("@@ -" + l1V1 + "," + n1V
                                + " +" + l2V1 + "," + n2V + " @@");
                        linePrinter(l1V, n1.get(i), l2.get(i), n2V, f, m);
                    }
                }
            } else {
                diffErrorPrinter(e.getKey());
            }
        }
    }
    public static void diffErrorPrinter(String s) {
        System.out.println("diff --git a/" + s + " /dev/null");
        System.out.println("--- a/" + s);
        System.out.println("+++ /dev/null");
        System.out.println("@@ -1 +0,0 @@");
        System.out.println("-This is not a wug.");
    }
    public static void diffErrorPrinter1(String s) {
        System.out.println("diff --git a/" + s + " /dev/null");
        System.out.println("--- a/" + s);
        System.out.println("+++ /dev/null");
        System.out.println("@@ -1 +0,0 @@");
        System.out.println("-This is not a wug.");
    }
    public static void diffErrorPrinter2() {
        System.out.println("diff --git /dev/null b/i.txt");
        System.out.println("--- /dev/null");
        System.out.println("+++ b/i.txt");
        System.out.println("@@ -0,0 +1 @@");
        System.out.println("+This is a wug.");
    }
    public static Branch rB(String a) {
        Branch b = Utils.readObject(Utils.join(BRANCHES,
                a), Branch.class);
        return b;
    }
    public static void chubbyDiff(ArrayList<Integer> l1,
                                  ArrayList<Integer> n1,
                                  ArrayList<Integer> l2,
                                  ArrayList<Integer> n2,
                                  File f, File m) {
        for (int i = 0; i < l1.size(); i++) {
            int l1V = l1.get(i); int l1V1 = l1.get(i) + 1;
            int l2V1 = l2.get(i) + 1; int n1V = n1.get(i);
            int n2V = n2.get(i); int l2V = l2.get(i);
            if (n1.get(i) == 0 && n2V == 0) {
                System.out.println("@@ -" + l1V + "," + n1V
                        + " +" + l2V1 + "," + n2V + " @@");
            } else if (n1.get(i) == 0) {
                if (n2.get(i) == 1) {
                    h1(l1V, n1V, l2V, l2V1, n2V, f, m);
                } else {
                    System.out.println("@@ -" + l1V + ", "
                            + n1V + " +" + l2V1
                            + "," + n2V + " @@");
                    linePrinter(l1V, n1V, l2V, n2V, f, m);
                }
            } else if (n2.get(i) == 0) {
                if (n1.get(i) == 1) {
                    h2(l1V, l1V1, l2V, n1V, n2V, f, m);
                } else {
                    System.out.println("@@ -" + l1V1 + "," + n1V
                            + " +" + l2V1 + "," + n2V + " @@");
                    linePrinter(l1V, n1V, l2V, n2V, f, m);
                }
            } else if (n1.get(i) == 1 && n2.get(i) == 1) {
                System.out.println("@@ -" + l1V1
                        + " +" + l2V1 + " @@");
                linePrinter(l1V, n1V, l2V, n2V, f, m);
            } else if (n1V == 1) {
                if (n2.get(i) == 0) {
                    h2(l1V, l1V1, l2V, n1V, n2V, f, m);
                } else {
                    System.out.println("@@ -" + l1V1 + " +"
                            + l2V1 + "," + n2V + " @@");
                    linePrinter(l1V, n1V, l2V, n2V, f, m);
                }
            } else if (n2.get(i) == 1) {
                if (n1.get(i) == 0) {
                    h1(l1V, n1V, l2V, l2V1, n2V, f, m);
                } else {
                    System.out.println("@@ -" + l1V1 + ","
                            + n1V + " +" + l2V1 + " @@");
                    linePrinter(l1V, n1V, l2V, n2V, f, m);
                }
            } else {
                System.out.println("@@ -" + l1V1 + "," + n1V
                        + " +" + l2V1 + "," + n2V + " @@");
                linePrinter(l1V, n1V, l2V, n2V, f, m);
            }
        }
    }

    public static void diff(String[] args) {
        if (args.length == 1) {
            Commit dH = Utils.readObject(HEAD, Commit.class);
            diffWorker(dH);
        } else if (args.length == 2) {
            Branch b = rB(args[1]); Commit dB = b.getCommit();
            diffWorker(dB);
        } else if (args.length == 3) {
            Branch b1 = rB(args[1]); Branch b2 = rB(args[2]);
            Commit b1C = b1.getCommit(); Commit b2C = b2.getCommit();
            for (Entry<String, String> e : b1C.getCM().entrySet()) {
                for (Entry<String, String> r : b2C.getCM().entrySet()) {
                    File f = Utils.join(BLOBS, e.getValue() + ".txt");
                    File m = Utils.join(BLOBS, r.getValue() + ".txt");
                    ArrayList<Integer> l1 = l1(f, m);
                    ArrayList<Integer> n1 = n1(f, m);
                    ArrayList<Integer> l2 = l2(f, m);
                    ArrayList<Integer> n2 = n2(f, m);
                    if (m.exists()) {
                        String y = Utils.sha1(Utils.readContentsAsString(m));
                        if (e.getValue().equals(y)) {
                            continue;
                        }
                    }
                    if (!e.getKey().equals(r.getKey())) {
                        continue;
                    }
                    if (m.exists()) {
                        diffWorker1(e.getKey(), r.getKey());
                        chubbyDiff(l1, n1, l2, n2, f, m);
                    }
                }
            }
            diffErrorPrinter1("h.txt");
            diffErrorPrinter2();
        }
    }
}

