# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)
- Both playlists and categories should internally be referenced by ID.
  The IDs should also be saved in preset save files etc. instead of the name.
- Convert all save files to a JSON-based format

#### Higher priority things to do


#### Lower priority things to do
- Setting for start folder when:
  - selecting video files
  - selecting thumbnail
- Make the preset pane and upload pane not focusable (without affecting
their child nodes) if possible, it looks really bad when they turn blue.
Maybe try this <https://a-hackers-craic.blogspot.com/2012/11/disabling-focus-in-javafx.html>
or alternatively fixing it with custom styling.
- Rename the namespaces io.github.stekeblad.* to se.stekeblad.*
- user suggestion (issue #18): Show the original file name somewhere in the upload pane.
- Support for scheduled publishing: <https://developers.google.com/youtube/v3/docs/videos/update>
YoutubeVideo.status.publishAt (requires privacyStatus.private or unlisted)

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
- Support for configuring ads and/or end cards and/or tips. Game name?
  Other properties??
  - How to fit this into a preset or videoUpload? It would take to much
    height... Grouping in pop-ups hidden under buttons? Having a
    full-size version and a collapsed version as mentioned already in this
    file?
