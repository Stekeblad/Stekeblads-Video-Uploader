package io.github.stekeblad.videouploader.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.github.stekeblad.videouploader.utils.Constants.AUTH_DIR;

/**
 * Contains the logic of getting permission to access the user's YouTube account.
 */
public class Auth {

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

    /**
     * @return the YouTube access token
     * @throws IOException if the auth flow throws an IOException
     */
    public static String getToken() throws IOException {
        return authUser().getAccessToken();
    }

    /**
     * @return the name of the authenticated channel, or null if the channel name could not be retrieved
     */
    public static String getChannelName() {
        try {
            Credential creds = Auth.authUser();
            YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, creds).setApplicationName(
                    "Stekeblads Video Uploader").build();
            YouTube.Channels.List myChannel = youtube.channels().list("snippet");
            myChannel.setMine(true);
            ChannelListResponse channelListResponse = myChannel.execute();
            List<Channel> channelList = channelListResponse.getItems();
            return channelList.get(0).getSnippet().getTitle();
        } catch (TokenResponseException tre) {
            // This can occur if the "Token has been expired or revoked." (quoting an error message)
            // Delete the token and on next attempt try authenticate again.
            // Thrown from Channels.List.execute() and not from Auth.authUser()
            try {
                if (Files.deleteIfExists(Paths.get(AUTH_DIR, "StoredCredential")))
                    System.err.println("deleted invalid token");
                else
                    System.err.println("Failed to delete invalid token");
            } catch (Exception ignored) {
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
