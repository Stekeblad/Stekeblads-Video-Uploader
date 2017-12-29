package io.github.stekeblad.youtubeuploader.youtube.constants;

// I am in Sweden so this categories are localized for sweden
// may be fetched for users locale in the future
// Note that Youtube handles categories by numbers and they may differ based on where you are
// For example id 17 is sport, but may for example be music somewhere else in the world
// Some categories are not available everywhere

public enum Categories  {
    FILM_OCH_ANIMATION      (1, "FILM_OCH_ANIMATION"),
    BILAR_OCH_FORDON        (2, "BILAR_OCH_FORDON"),
    MUSIK                   (10, "MUSIK"),
    DJUR_OCH_HUSDJUR        (15, "DJUR_OCH_HUSDJUR"),
    SPORT                   (17, "SPORT"),
    RESOR_OCH_HANDELSER     (19, "RESOR_OCH_HANDELSER"),
    SPEL                    (20, "SPEL"),
    MANNISKOR_OCH_BLOGGAR   (22, "MANNISKOR_OCH_BLOGGAR"),
    KOMEDI                  (23, "KOMEDI"),
    NOJE                    (24, "NOJE"),
    NYHETER_OCH_POLITIK     (25, "NYHETER_OCH_POLITIK"),
    INSTRUKTION_OCH_STIL    (26, "INSTRUKTION_OCH_STIL"),
    UTBILDNING              (27, "UTBILDNING"),
    VETENSKAP_OCH_TEKNIK    (28, "VETENSKAP_OCH_TEKNIK");

    private final int id;
    private final String name;

    Categories(int categoryId, String categoryName) {
        this.id = categoryId;
        this.name = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
