# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)
- Does the program freeze shortly when adding a lot of videos with apply preset button? 
Maybe do most of the work in a background thread, add a loading indicator and do only some 
preparations and finishing in main thread?
- find a way to prevent user to select null in choiceBoxes

#### Higher priority things to do

#### Lower priority things to do
- Save window sizes/location on exit, use the saved data when starting next time
- When updating playlist, use ID instead of name when looking at old
playlists and deciding if they should be visible or not (playlists can
be renamed, name is different but ID the same.)
- Update YouTube API version
- upload/preset list have a horizontal scroll bar, remove it

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
- Uploader should probably have some protection against concurrent modification
of things.
- drag and drop thumbnails on thumbnail image box 
and drop video files in the list of videos to add list?

#### Java 11 (planned to be released at the end of september)
- Toast notification when all uploads finished
- Progress bar on taskbar icon
- Bundle Java with the program

#### Things to change on wiki when releasing next update
- Pressing enter in playlist/preset name box
- Pressing F1 for wiki
- Translations
- Red progressBar and upload failed dialogs
    - Checking existence of category, thumbnail, video, playlist
        - categories now checked on preset and upload save plus onStartUpload
        - Existence of video file and thumbnail file is checked in uploader
        - If playlist has been deleted from YouTube is NOT CHECKED!
- Wikipage for settings window (add url to F1 click in settings window!)
- Clone preset button
- R-click to
    - reset thumbnail
    - show playlist on YouTube