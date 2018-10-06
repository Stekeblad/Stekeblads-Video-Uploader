module io.github.stekeblad.videouploader {
    requires google.oauth.client;
    requires google.api.client;
    requires google.http.client;
    requires google.api.services.youtube.v3.rev189;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    // Cant require desktop: module [this module] reads package java.awt from both java.desktop and java.datatransfer (compile time)
    // Cant require datatransfer: cant access java.awt.Desktop, but can import java.awt.* (intelliSense)
    // Cant exclude both: Package 'java.awt' is declared in module 'java.desktop', but module [this nodule] does not read it (intelliSense)
    // Cant require both: same error as only require:ing desktop (compile time)
    // If none of the modules is required and import commented out I do not get any import/require suggestions for Desktop...

    requires java.desktop;
    //requires java.datatransfer;
}