# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)
- Testing with progressBar for uploads to the right and buttons to the left
- Settings window for picking another language than the detected one or
default, and other things.

#### Higher priority things to do
- Better loading of playlist dropDown, clicking multiple times to get it
to open is not ok!

#### Lower priority things to do
- handling of categories/playlists/image does no longer exist

        // Note for self when implementing
        try {
            CategoryUtils.valueOf(uploadQueueVideos.get(selected).getCategory());
        } catch (IllegalArgumentException e) {
            AlertUtils.simpleClose("Category not valid", "The selected category does no longer exist")
        }

- Save window sizes/location on exit, use the saved data when starting next time
- Create a clone button for presets (set the clone to edit automatically)
- "Show Playlist" button in playlist manager window -> opens that playlist
on youtube in the browser
- When updating playlist, use ID instead of name when looking at old
playlists and desiding if they should be visible or not (playlists can
be renamed, name is different but ID the same.)

#### Thinking about
- Keep even unstarted uploads on close?
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

#### Things to change on wiki when releasing next update
- Pressing enter in playlist/preset name box
- Pressing F1 for wiki
- Translations
- red progressBar and upload failed dialogs