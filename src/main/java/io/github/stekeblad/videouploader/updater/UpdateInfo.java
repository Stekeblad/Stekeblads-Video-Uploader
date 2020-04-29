package io.github.stekeblad.videouploader.updater;

/**
 * The UpdateInfo class holds the response from the update server about the latest available version
 */
public class UpdateInfo {
    private String heading;
    private String body;
    private String updateUrl;
    private VersionFormat version;
    private String signature;

    public UpdateInfo(String heading, String body, String updateUrl, VersionFormat version, String signature) {
        this.heading = heading;
        this.body = body;
        this.updateUrl = updateUrl;
        this.version = version;
        this.signature = signature;
    }

    public UpdateInfo(String heading, String body, String updateUrl, String version, String signature) {
        this.heading = heading;
        this.body = body;
        this.updateUrl = updateUrl;
        this.version = new VersionFormat(version);
        this.signature = signature;
    }

    public UpdateInfo() {
        this.heading = "";
        this.body = "";
        this.updateUrl = "";
        this.version = new VersionFormat();
        this.signature = "";
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public VersionFormat getVersion() {
        return version;
    }

    public void setVersion(VersionFormat version) {
        this.version = version;
    }

    public void setVersion(String version) {
        this.version = new VersionFormat(version);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
