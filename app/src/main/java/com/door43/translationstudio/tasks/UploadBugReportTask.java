package com.door43.translationstudio.tasks;

import org.unfoldingword.tools.http.Request;
import org.unfoldingword.tools.logger.GithubReporter;
import org.unfoldingword.tools.logger.Logger;

import com.door43.translationstudio.App;
import com.door43.translationstudio.R;
import com.door43.util.EmailReporter;
import com.door43.util.FileUtilities;
import org.unfoldingword.tools.taskmanager.ManagedTask;

import java.io.File;
import java.io.IOException;

/**
 * Uploads a bug report to github
 */
public class UploadBugReportTask extends ManagedTask {
    public static final String TASK_ID = "upload_bug_report_task";
    private final String mNotes;
    private int mResponseCode = -1;


    public UploadBugReportTask(String notes) {
        mNotes = notes;
    }

    /**
     * determine if upload was success
     * @return
     */
    public boolean isSuccess() {
        return (mResponseCode >= 200) && (mResponseCode <= 202);
    }

    /**
     * get response code from server
     * @return
     */
    public int getResponseCode() {
        return mResponseCode;
    }

    @Override
    public void start() {
        mResponseCode = -1;
        File logFile = Logger.getLogFile();

        // TRICKY: make sure the github_oauth2 token has been set
        int helpdeskTokenIdentifier = App.context().getResources().getIdentifier("helpdesk_token", "string", App.context().getPackageName());
        String helpdeskEmail = App.context().getResources().getString(R.string.helpdesk_email);

        if(helpdeskTokenIdentifier != 0) {
            EmailReporter reporter = new EmailReporter(App.context().getResources().getString(helpdeskTokenIdentifier), helpdeskEmail);
//            GithubReporter reporter = new GithubReporter(App.context(), helpdeskEmail, App.context().getResources().getString(helpdeskTokenIdentifier));
            try {
                Request request = reporter.reportBug("Joel", "da1nerd@gmail.com", mNotes, logFile, App.context());
                mResponseCode = request.getResponseCode();
            } catch (Exception e) {
                Logger.e(this.getClass().getName(), "Failed to submit feedback: " + e.getMessage());
                e.printStackTrace();
            }

            if(!isSuccess()) {
                Logger.e(this.getClass().getName(), "Failed to upload bug report.  Code: " + mResponseCode);
            } else { // success
                Logger.i(this.getClass().getName(), "Submitted bug report");
            }

            try {
                FileUtilities.writeStringToFile(logFile, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(helpdeskTokenIdentifier == 0) {
            Logger.w(this.getClass().getName(), "the github oauth2 token is missing");
        }
    }
}
