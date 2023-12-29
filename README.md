# Test task for JetBrains internship

A library that implements simple Git functionality.

## Usage instructions:

Unlike the real git in this implementation there is no work with files and folders, the analogue of a folder is the `Blob` 
class, and the analogue of a directory, which can contain several blobs, is the `Tree` class

In order to use the library, you need to create an object of the `GitImplementation` class.

In case of a runtime error, a `GitException` will be thrown


The `add` and `commit` commands can then be called on this object:
* `add` - command to add a `Blob` or `Tree` for writing on the next commit, in case the `commit` command is not called, 
the added entities will not appear in the repository.
  * Arguments:
    * `entry: TreeEntry` -  `Blob` or `Tree` object
* `commit` - command that initiates writing of all added objects to the repository
  * Arguments:
    * `author: String` - commit author
    * `message: String` - commit message

### Correct use of `Blob` and `Tree` classes:

To create both classes, the first parameter is a string that is the path to the corresponding file or directory in the actual git. 
The separator is the `"/"` character.

The path must contain only trees. If a `Blob` is encountered on the path where the object is to be created,
a `GitException` will be thrown.

If the path contains names of the trees that have not yet been created and are not stored in the current tree, they will be created
at the time the item is added.

For a `Blob`, its content is passed as the second parameter, while for a `Tree`, the second parameter is a collection of 
stored files and subtrees.

### Some examples:

1. Correct example 1:

```kotlin
val git = GitImplementation()
git.add(Blob("blob1", "Blob 1"))
git.add(Tree("tree3", emptyList()))
git.commit("commit", "author")
```
In this case, a blob will be added to the original tree, as well as a subtree

2. Correct example 2:

```kotlin
val git = GitImplementation()
git.add(Blob("tree1/tree2/blob1", "Blob 1"))
git.commit("commit", "author")
```
In this case, a subtree `tree1` will be added to the original tree, containing a subtree `tree2` that holds the blob.
It is not necessary to create subtrees on your own.

3. Correct example 3:

```kotlin
val git = GitImplementation()
git.add(Tree("tree1", listOf(Tree("tree2", listOf(Blob("blob1", "Blob 1"))))))
git.commit("commit", "author")
```
In this case the final structure will be similar to the case above, but it is created in a different way

4. Incorrect example:

```kotlin
val git = GitImplementation()
git.add(Blob("blob1", "Blob 1"))
git.add(Blob("blob1/blob2", "Blob 2"))
git.commit("commit", "author")
```

In this case `blob1` is not a tree, so `blob2` cannot be created along this path

### Data output:

In order to print data related to a commit, you can use the `printContent()` method.

Also, both `Blob` and `Tree` classes have methods with a similar name, which allow you to output all the information about 
trees and the objects stored in them.

### Tests:

Tests that check some basic scenarios as well as corner cases are in the file `GitImplementationTests`.

Please run test using `./gradlew test` class