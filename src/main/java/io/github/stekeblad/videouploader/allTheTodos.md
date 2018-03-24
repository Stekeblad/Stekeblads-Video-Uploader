# What I am working on and planning on maybe doing in the future

#### Right now
- Bugs and small things (there is always bugs and small things...)
- Creating localization support and moving all strings visible for the
users there
- Finish translations for english and swedish, review translations
and check for strings not yet added in translations
- Check if Uploader and the Video* Classes can have translation
- replace Auth.AUTHMSG_HEADER and Auth.AUTHMSG_DESC with translations
in baseStrings
- Rethink how to do with meta information about translations (do not
like the comments in the translation files as the file itself becomes a
mess when using IntelliJ's Resource Bundle translation tool)

#### Higher priority things to do

#### Lower priority things to do
- handling of categories/playlists/image does no longer exist

        // Note for self when implementing
        try {
            CategoryUtils.valueOf(uploadQueueVideos.get(selected).getCategory());
        } catch (IllegalArgumentException e) {
            AlertUtils.simpleClose("Category not valid", "The selected category does no longer exist")
        }

#### Thinking about
- Keep even unstarted uploads on close?
- Change "Abort all and clear" button to not affect started uploads.
users can currently not remove all not started at once and it would
only be two extra click to first abort all before clearing.
- Support for multiple Youtube accounts/channels?