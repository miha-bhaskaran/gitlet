package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
public class Commit implements Serializable {


    /** message var. */
    private String _message;

    /** date. */
    private String _date;

    /** parent. */
    private Commit _parent;

    /** commitParent. */
    private Commit _commitParent;

    /** Current Working Directory. */

    static final File CWD = new File(System.getProperty("user.dir"));

    /** GITLET REPOSITORY folder. */
    static final File GITLET = Utils.join(CWD, ".gitlet");

    /** STAGING AREA folder. */
    static final File STAGING_AREA = Utils.join(GITLET, "staging_area");

    /** STAGING FOR ADDITION folder. */
    static final File STAGED_ADDITION = Utils.join(STAGING_AREA,
            "staged_addition");

    /** STAGING FOR REMOVAL folder. */
    static final File STAGED_RM = Utils.join(STAGING_AREA, "staged_rm");

    /** COMMITS folder. */
    static final File COMMITS = Utils.join(GITLET, "commits");

    /** BLOBS folder. */
    static final File BLOBS = Utils.join(GITLET, "blobs");

    /** BRANCHES folder. */
    static final File BRANCHES = Utils.join(GITLET, "branches");

    /** HEAD folder. */
    static final File HEAD = Utils.join(GITLET, "head");

    /** commitMap. */
    private TreeMap<String, String> commitMap;

    public Commit(String message, Commit parent, Commit commitParent) {
        commitMap = new TreeMap<String, String>();
        _message = message;
        _parent = parent;
        _commitParent = commitParent;
        if (_parent == null) {
            _date = "Wed Dec 31 16:00:00 1969 -0800";
        } else {

            String pattern = "EEE MMM d HH:mm:ss yyyy Z";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            _date = date;
            for (String s: _parent.commitMap.keySet()) {
                commitMap.put(s, _parent.commitMap.get(s));
            }
        }

        for (File f : STAGED_ADDITION.listFiles()) {
            commitMap.put(f.getName(), Utils.sha1(Utils.readContents(f)));
            f.renameTo(Utils.join(BLOBS,
                    Utils.sha1(Utils.readContents(f)) + ".txt"));
        }
        for (File f : STAGED_RM.listFiles()) {
            if (commitMap.containsKey(f.getName())) {
                commitMap.remove(f.getName());
            }
        }
    }
    public String getMessage() {
        return _message;
    }
    public String getDate() {
        return _date;
    }
    public Commit getParent() {
        return _parent;
    }
    public Commit getCommitParent() {
        return _commitParent;
    }
    public TreeMap<String, String> getCM() {
        return this.commitMap;
    }


    public void saveCommit() throws IOException {
        File d = new File(COMMITS, Utils.sha1(Utils.serialize(this)));
        d.createNewFile();
        Utils.writeObject(d, this);
        Utils.writeObject(HEAD, this);
        for (File m : STAGED_RM.listFiles()) {
            m.delete();
        }
        for (File f : BRANCHES.listFiles()) {
            Branch deserializingF = Utils.readObject(f, Branch.class);
            if (deserializingF.getActive()) {
                deserializingF.commitSetter1(this);
                Utils.writeObject(f, deserializingF);
            }
        }
    }
}

