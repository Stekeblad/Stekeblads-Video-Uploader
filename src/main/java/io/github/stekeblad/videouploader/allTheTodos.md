# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)

#### Higher priority things to do
- Settings window for picking another language than the detected one or
default.

#### Lower priority things to do
- handling of categories/playlists/image does no longer exist

        // Note for self when implementing
        try {
            CategoryUtils.valueOf(uploadQueueVideos.get(selected).getCategory());
        } catch (IllegalArgumentException e) {
            AlertUtils.simpleClose("Category not valid", "The selected category does no longer exist")
        }

- F1 takes you to the wiki page for the current window (https://dzone.com/articles/handling-keyboard-sortcuts)
- Save window sizes on exist, use the saved sizes when starting next time

#### Thinking about
- Keep even unstarted uploads on close?
- Change "Abort all and clear" button to not affect started uploads.
users can currently not remove all not started at once and it would
only be two extra click to first abort all before clearing.
- Support for multiple Youtube accounts/channels?
- As translations will make some strings longer or short than others,
should the buttons in the upload pane swap place with the progressbar to
make better use of the space to the left or should it be a empty space
to the right of the thumbnail? How would it look like with the buttons to
the left and the progressbar under an image?
- Support for limiting upload speed? (Appears difficult to limit up but
could probably limit how fast the video file to upload can be read... Not
the solutions users may want, but a solution that could have the same effect
as the one they want (Can not upload a file faster than it can be read.))