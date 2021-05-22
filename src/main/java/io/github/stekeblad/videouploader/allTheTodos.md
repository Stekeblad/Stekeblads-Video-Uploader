# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)

#### Higher priority things to do
- Overload TextField and TextArea to pass through function key events.
On Java 8 you can not open the wiki with F1 if focus is on a node of that type.
- YouTube data layer class for collecting logic related to the API and performing better error handling
  - Catch and show or handle token revoked exceptions that happen after log out
  - When updating playlist, use ID instead of name when looking at old
playlists and deciding if they should be visible or not (playlists can
be renamed, but ID is always the same.)

#### Lower priority things to do
- Setting for start folder when:
  - selecting video files
  - selecting thumbnail
- Make the preset pane and upload pane not focusable (without affecting
their child nodes) if possible, it looks really bad when they turn blue.
Maybe try this <https://a-hackers-craic.blogspot.com/2012/11/disabling-focus-in-javafx.html>
or alternatively fixing it with custom styling.

#### Thinking about
- Keep even not started uploads on close?
- Change "Abort all and clear" button to not affect started uploads.
users can currently not remove all not started at once and it would
only be two extra click to first abort all before clearing.
- Support for limiting upload speed? (Appears difficult to limit up but
could probably limit how fast the video file to upload can be read... Not
the solutions users may want, but a solution that could have the same effect
as the one they want (Can not upload a file faster than it can be read.))
- drag and drop thumbnails on thumbnail image box 
and drop video files in the list of videos to add list?
- Collapsed mode for Presets and Uploads (takes up less height)
- Saving the automatic numbering to the preset, remembering what number 
the next episode will have and inserting it the next time the user selects 
the preset in the select preset dropdown.
