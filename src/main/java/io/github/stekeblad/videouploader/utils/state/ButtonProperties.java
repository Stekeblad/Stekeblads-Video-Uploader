package io.github.stekeblad.videouploader.utils.state;

import java.util.function.Consumer;

public class ButtonProperties {
    private String id;
    private String text;
    private Consumer<String> onClick;

    public ButtonProperties(String id, String text, Consumer<String> onClick) {
        this.id = id;
        this.text = text;
        this.onClick = onClick;
    }

    String getId() {
        return id;
    }

    String getText() {
        return text;
    }

    Consumer<String> getOnClick() {
        return onClick;
    }
}
