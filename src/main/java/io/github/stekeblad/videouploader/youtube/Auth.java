package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTubeScopes;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static io.github.stekeblad.videouploader.utils.Constants.AUTH_DIR;

/**
 * Contains the logic of getting permission to access the user's YouTube account.
 */
public class Auth {

    public static final String AUTHMSG_HEADER = "Authentication Required";
    public static final String AUTHMSG_DESC = "To do this you must grant the application permission to access your Youtube channel. " +
            "Do you want to allow \"Stekeblads Youtube Uploader\" to access Your channel?" +
            "\n\nThe permissions is required for the program to upload videos to your channel, " +
            "setting thumbnails, getting a list of video categories, adding videos to playlists and creating new playlists." +
            "\n\nPress yes to open your browser for authentication or no to cancel. " +
            "Authentication does not work in Microsoft Edge, if it is your default browser you need to temporary change to another one.";

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static Credential authUser() throws IOException {
        List<String> scope = new ArrayList<>();
        scope.add(YouTubeScopes.YOUTUBE_UPLOAD);
        scope.add(YouTubeScopes.YOUTUBE);

        Reader clientSecretReader = new InputStreamReader(Auth.class.getClassLoader().getResourceAsStream(".auth/client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);
        FileDataStoreFactory fileFactory = new FileDataStoreFactory(new File(AUTH_DIR));

        GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scope).setDataStoreFactory(fileFactory)
                .build();

        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(7835).build();
        return new AuthorizationCodeInstalledApp(authFlow, localReceiver).authorize("user");
    }
}
