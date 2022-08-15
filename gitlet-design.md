# Gitlet Design Document
author: Athmiha Bhaskaran

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

###Blob Class
Blob objects are created when gitlet.add is called. Blob objects will now be in the staging area, which is a folder. We chose to use a folder as it is easily accessible. Blobs should basically serialize and deserialize as needed. This will be explicitly explored in the main class in the commit method.

###Commit Class
j





This will take all the blobs in the staging area and it will put it into the tree map structure which represents the commits. 
This class will also have a head pointer that will point to the commit that is currently being worked on. The map structure will use the metadata as keys and the blob contents as values.
####Instance Variables
* Message -  contains the message of a commit
* Timestamp - time at which a commit was created. Assigned by constructor
* Parent - the parent commit of a commit object


## 2. Algorithms


###Main:
static final File CWD = new File(".");
static final File GITLET = Utils.join(CWD,".gitlet");
static final File STAGING_AREA = Utils.join(GITLET,"staging_area");
static final File STAGED_ADDITION = Utils.join(STAGING_AREA,"staged_addition"); ==> folder for staged for addition inside STAGING AREA
static final File STAGED_RM = Utils.join(STAGING_AREA,"staged_rm"); ==> folder for removals inside STAGING AREA
static final File COMMITS = Utils.join(GITLET,"commits"); ==> folder for commits
static final File BLOBS = Utils.join(GITLET,"blobs"); ==> folder for Blobs
static final File HEAD = Utils.join(COMMITS,"head"); ==> folder for head (stores the most current head)

main():
Processing the inputs and call functions accordingly. Here we will have the commands for “commit”, “add”, and “innit”. This will ultimately dictate what function is being called and what function is being run.

init():
Will be reponsible for creating the .gitlet repo if there isn't one already. Inside the .gitlet repo, there will be the
Staging area, the staged for removal, staged for adddition, commit and blobs folders.

Also, there will be a call to the Commit class to create the initial commit object.

commit():
Creates a new commit object and then calls save on that object to save the object.

merge(String name):
Boolean conflict = false;
mergeErrorChecking(name); ==> method to check for errors
Branch givenBranch;
Branch currentBranch;
Bunch of if statements to test all the conditions of merge and proceed accordingly


checkout(args[2]):
Checkout creates a commit object to deserialize the object in head. Then the deserialized object's commit map
is called with the arguments of file name passed in to get the blob of the file's sha code. 
Then a pointer to the blob's sha code in the blobs folder is created. And then another variable will read the contents of the 
blob that corresponds to the sha code. This variable is then passed in to overide the blob of the file in the CWD.

###Commit:

STAGED_ADDITION ==> the folder that is in STAGING_AREA that holds all the files that have been added
BLOBS ==> the folder that holds the blobs

commit(String message, Commit Parent, Commit commitParent):
In the commit class, a hashmap gets created to store all the commits to. Then if the parent is null (this is specefically for the initial commit),
the date will be set accordingly. Then there is a for loop where for each iteration, it will go through the files in the STAGED_ADDITION
folder and put it in the commitMap. The key will be the name of the file and the value will be the sha1 value of the file blob.
The file from the STAGED_ADDITION will also be moved into the BLOBS folder

commitParent is the parent that results from crisscrossing merges.

getCommitParent();
gets commit parent
getParent():
Gets parent


getBlob():
Gets blobs.

getLog();
gets logMessage

getStamp():
gets TimeStamp;

## 3. Persistence

1. In order to look at the most recent states, we would look at the staging folder. The staging folder holds the blob objects that are created after adding but before commiting.
2. The blob folder holds all the blobs that were ever committed and so it represents all of the commits.



## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

