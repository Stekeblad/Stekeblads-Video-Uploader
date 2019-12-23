package io.github.stekeblad.videouploader.utils.exceptionHistory;

import java.time.Duration;
import java.util.ArrayList;

/**
 * The ExceptionHistory holds a list of {@link io.github.stekeblad.videouploader.utils.exceptionHistory.ExceptionHistoryPost}
 * with exceptions that recently have been seen. The purpose is to prevent multiple error dialogs from displaying the
 * same error message in rapid succession. This can be archived by calling the {@link #isInHistory(String)} method and
 * checking the return value before showing an exception dialog. If true is returned then an exception with the same
 * stacktrace was seen less than 15 seconds ago and you may want to skip showing an error message.
 */
public class ExceptionHistory {
    private ArrayList<ExceptionHistoryPost> exceptionHistoryPosts;

    public ExceptionHistory() {
        exceptionHistoryPosts = new ArrayList<>();
    }

    /**
     * Checks if an exception with the same stacktrace as {@code stacktrace} has been seen within the last 15 seconds
     * and if so returns true, if there is no record of an identical stacktrace then the stacktrace is recorded and
     * false is returned. If unsure how to get the stacktrace as a string, please look at {@code getStacktrace(Throwable)}
     * in the class {@link io.github.stekeblad.videouploader.utils.AlertUtils}
     *
     * @param stacktrace the stacktrace to compare to previously seen stacktraces
     * @return true if an identical stacktrace has been seen in the last 15 seconds, false otherwise.
     */
    public boolean isInHistory(String stacktrace) {
        for (int i = 0; i < exceptionHistoryPosts.size(); /* no increment */) {
            ExceptionHistoryPost post = exceptionHistoryPosts.get(i);
            // Test the age of the history post.
            // If it was more than a minute ago since its first appeared its to old and should be removed.
            // in this case we do not want to increment i because we remove one element and everything after it is shifted
            // one step towards the beginning on the list.
            if (post.getTimeSinceAdded().compareTo(Duration.ofSeconds(15)) > 0)
                exceptionHistoryPosts.remove(i);

                // Check if it exists a stacktrace in the exceptionHistoryPosts with the same stacktrace as the provided one.
                // If there is a match this exception happened recently, return true.
            else if (post.getStacktrace().equals(stacktrace))
                return true;

                // Else progress to the next post
            else
                i++;
        }
        // The stacktrace is new, add it to the history
        exceptionHistoryPosts.add(new ExceptionHistoryPost(stacktrace));
        return false;
    }
}
