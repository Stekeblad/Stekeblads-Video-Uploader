# /resources/strings

Maybe its more logical for me to be in the strings directory but that is
not allowed by the translation system

This is the translations directory. The subdirectories are for different
sets of translations, translations that is used in several windows are
placed in the baseStrings directory, translations for the mainWindow is
in the mainWindow directory. In the subdirectories there is one file per
language.

The meta directory is special as it is for listing the
existing translations and holding information about when the translation
was updated and who helped with the translation for that language.
More details of what should be in a meta file is described in
the meta/meta.properties file.

The file in a subdirectory with the same name as the directory and the
extension .properties is the default translation file, all translations
should be based on it as it should be the one that is most up to date.

Translations must have a name like this: First the name of the directory
followed by an underscore, the locale (eg. sv_SE) and ending in
.properties.
