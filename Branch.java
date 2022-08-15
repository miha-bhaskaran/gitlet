package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Branch implements Serializable {

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

    /** HEAD folder. */
    static final File HEAD = Utils.join(GITLET, "head");

    /** BLOBS folder. */
    static final File BLOBS = Utils.join(GITLET, "blobs");
    /** BRANCHES folder. */
    static final File BRANCHES = Utils.join(GITLET, "branches");

    /** isActive var. */
    private boolean _isActive;

    /** name var. */
    private String _name;

    /** commit var. */
    private Commit _commit;

    public Branch(String name, boolean isActive, Commit c) {
        _isActive = isActive;
        _name = name;
        _commit = c;
        String fileName = _name;
        String sha1Commit = Utils.sha1(Utils.serialize(c));
        if (name.equals("master")) {
            sha1Commit = Utils.sha1(Utils.serialize(HEAD));
        }
    }
    public boolean getActive() {
        return _isActive;
    }
    public String getName() {
        return _name;
    }
    public Commit getCommit() {
        return _commit;
    }
    public void falseActive() {
        _isActive = false;
    }
    public void trueActive() {
        _isActive = true;
    }
    public void commitSetter(File a) {
        _commit = Utils.readObject(a, Commit.class);
    }
    public void commitSetter1(Commit a) {
        _commit = a;
    }




    public void branchSave() throws IOException {
        File d = Utils.join(BRANCHES, this._name);
        d.createNewFile();
        Utils.writeObject(d, this);
    }


}
