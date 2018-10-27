# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)
- Moving tag processing to separate classes and loading them as services 
(https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html)

#### Higher priority things to do
- Find a way to produce a .jar file that the end user can easily run
Currently it can be launched from the commandline with the following command:
```
"%JAVA11_HOME%\bin\java.exe" --module-path "%JAVAFX_HOME%" --add-modules=javafx.controls --add-modules=javafx.fxml -jar Stekeblads_Video_Uploader.jar
```
  - %JAVA11_HOME% is a Windows environment variable I created that points
  to my Java 11 installation directory (%JAVA_HOME% points to my Java 8 installation)
  - %JAVAFX_HOME% points to the /lib directory for JavaFX 11
  - The end user should not need to think about the modules!

#### Lower priority things to do
- Save window sizes/location on exit, use the saved data when starting next time
- When updating playlist, use ID instead of name when looking at old
playlists and deciding if they should be visible or not (playlists can
be renamed, name is different but ID the same.)
- upload/preset list have a horizontal scroll bar, remove it (I don't know how)
- Does the program freeze shortly when adding a lot of videos with apply preset button?
Maybe do most of the work in a background thread, add a loading indicator and do only some
preparations and finishing in main thread?
- If error happens during the uploading, check the Google error message to give better error msg to user

#### Thinking about
- Keep even not started uploads on close?
- Change "Abort all and clear" button to not affect started uploads.
users can currently not remove all not started at once and it would
only be two extra click to first abort all before clearing.
- Support for limiting upload speed? (Appears difficult to limit up but
could probably limit how fast the video file to upload can be read... Not
the solutions users may want, but a solution that could have the same effect
as the one they want (Can not upload a file faster than it can be read.))
- Special system for like "Live on Twitch", delete video after a few hours
or maybe even: if the user goes live on Twitch a short video is automatically
uploaded saying "I am live on Twitch!" and automatically delete video when stream ends.
(probably possible, fun to try, but would someone use it?)
- drag and drop thumbnails on thumbnail image box 
and drop video files in the list of videos to add list?
- Setting for start folder when:
  - selecting video files
  - selecting thumbnail
- Collapsed mode for Presets and Uploads (takes up less height)
- Setting for automatically moving successfully uploaded videos to the recycle bin
(java.awt.Desktop -- public boolean moveToTrash -- since 9)
- Move auto numbering into VideoPreset to remember the numbering for each preset
- User defined tags of some sort
- do something with video meta data? https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/GetContainerInfo.java

#### Java 11 (planned to be released at the end of september)
- Toast notification when all uploads finished
- Progress bar on taskbar icon
- Bundle Java with the program - or was this feature maybe removed? JLink??
- Try remove code that fixes errors in Java 8 (see if they are fixed in 11)
  - On Java 8, function key events is not passed on by TextFields
  - toolbar.getChildrenUnmodifiable()
- http://jdk.java.net/11/
- https://adoptopenjdk.net/
- https://gluonhq.com/products/javafx/
- https://search.maven.org/search?q=g:org.openjfx%20AND%20a:javafx
- Notes: jlink, jdeps, StackWalker, optional
