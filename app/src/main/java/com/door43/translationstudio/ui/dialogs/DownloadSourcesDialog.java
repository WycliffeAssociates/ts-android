package com.door43.translationstudio.ui.dialogs;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.door43.translationstudio.App;
import com.door43.translationstudio.R;
import com.door43.translationstudio.core.Util;
import com.door43.translationstudio.tasks.GetAvailableSourcesTask;

import org.unfoldingword.door43client.Door43Client;
import org.unfoldingword.tools.taskmanager.ManagedTask;
import org.unfoldingword.tools.taskmanager.TaskManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blm on 12/1/16.
 */

public class DownloadSourcesDialog extends DialogFragment implements ManagedTask.OnFinishedListener, ManagedTask.OnProgressListener {
    public static final String TAG = DownloadSourcesDialog.class.getSimpleName();
    private Door43Client mLibrary;
    private ProgressDialog progressDialog = null;
    private DownloadSourcesAdapter mAdapter;
    private List<DownloadSourcesAdapter.FilterStep> mSteps;
    private TextView mNavText;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.dialog_download_sources, container, false);

        mLibrary = App.getLibrary();
        mSteps = new ArrayList<>();
        addStep(DownloadSourcesAdapter.SelectionType.language, R.string.choose_language);
//        addStep(DownloadSourcesAdapter.SelectionType.book_type, R.string.choose_category);

        ManagedTask task = new GetAvailableSourcesTask();
        ((GetAvailableSourcesTask)task).setPrefix(this.getResources().getString(R.string.loading_sources));
        task.addOnProgressListener(this);
        task.addOnFinishedListener(this);
        TaskManager.addTask(task, GetAvailableSourcesTask.TASK_ID);

        mNavText = (TextView) v.findViewById(R.id.nav_text);

        EditText searchView = (EditText) v.findViewById(R.id.search_text);
        searchView.setHint(R.string.choose_source_translations);
        searchView.setEnabled(false);
        ImageButton searchBackButton = (ImageButton) v.findViewById(R.id.search_back_button);
        searchBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSteps.size() > 1) {
                    DownloadSourcesAdapter.FilterStep lastStep = mSteps.get(mSteps.size() - 1);
                    mSteps.remove(lastStep);
                    lastStep = mSteps.get(mSteps.size() - 1);
                    lastStep.filter = null;
                    lastStep.label = lastStep.old_label;
                    setFilter();
                } else {
                    dismiss();
                }
            }
        });

//        searchBackButton.setVisibility(View.GONE);
        ImageView searchIcon = (ImageView) v.findViewById(R.id.search_mag_icon);
        searchIcon.setVisibility(View.GONE);
        // TODO: set up search

        mAdapter = new DownloadSourcesAdapter(getActivity());
        setFilter();

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if((mAdapter != null) && (mSteps != null) && (mSteps.size() > 0)) {
                    DownloadSourcesAdapter.FilterStep currentStep = mSteps.get(mSteps.size() - 1);
                    DownloadSourcesAdapter.ViewItem item = mAdapter.getItem(position);
                    currentStep.old_label = currentStep.label;
                    currentStep.label = item.title.toString();
                    currentStep.filter = item.filter;

                    if(mSteps.size() < 2) { // if we haven't set up last step
                        switch (currentStep.selection) {
                            default:
                            case language:
                                addStep(DownloadSourcesAdapter.SelectionType.book_type, R.string.choose_category);
                                break;

                            case oldTestament:
                            case newTestament:
                            case other:
                                addStep(DownloadSourcesAdapter.SelectionType.language, R.string.choose_language);
                                break;

                            case book_type:
                                int bookTypeSelected = Util.strToInt(currentStep.filter, R.string.other_label);
                                switch (bookTypeSelected) {
                                    case R.string.old_testament_label:
                                        addStep(DownloadSourcesAdapter.SelectionType.oldTestament, R.string.choose_book);
                                        break;
                                    case R.string.new_testament_label:
                                        addStep(DownloadSourcesAdapter.SelectionType.newTestament, R.string.choose_book);
                                        break;
                                    default:
                                    case R.string.other_label:
                                        addStep(DownloadSourcesAdapter.SelectionType.other, R.string.choose_book);
                                        break;
                                }
                                break;
                        }
                    } else if(mSteps.size() < 3) { // set up last step
                        DownloadSourcesAdapter.FilterStep firstStep = mSteps.get(0);
                        switch (firstStep.selection) {
                            case language:
                                addStep(DownloadSourcesAdapter.SelectionType.source_filtered_by_language, R.string.choose_sources);
                                break;
                            default:
                                addStep(DownloadSourcesAdapter.SelectionType.source_filtered_by_book, R.string.choose_sources);
                                break;
                        }
                    } else { // at last step, do toggling
                        mAdapter.toggleSelection(position);
                        return;
                    }
                    setFilter();
                }
            }
        });
        return v;
    }

    private void setFilter() {
        if(mNavText != null) {
            String navText = "";
            for (DownloadSourcesAdapter.FilterStep mStep : mSteps) {
                if(!navText.isEmpty()) {
                    navText += " > ";
                }
                navText += mStep.label;
            }
            mNavText.setText(navText);
        }
        mAdapter.setFilterSteps(mSteps);
    }

    /**
     * add step to sequence
     * @param selection
     * @param prompt
     */
    private void addStep(DownloadSourcesAdapter.SelectionType selection, int prompt) {
        String promptStr = getResources().getString(prompt);
        DownloadSourcesAdapter.FilterStep step = new DownloadSourcesAdapter.FilterStep(selection, promptStr);
        mSteps.add(step);
    }

    @Override
    public void onTaskFinished(final ManagedTask task) {
        TaskManager.clearTask(task);

        Handler hand = new Handler(Looper.getMainLooper());
        hand.post(new Runnable() {
            @Override
            public void run() {
                if(task instanceof GetAvailableSourcesTask) {
                    GetAvailableSourcesTask availableSourcesTask = (GetAvailableSourcesTask) task;
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    mAdapter.setData(availableSourcesTask);
                }
            }
        });
    }

    @Override
    public void onTaskProgress(final ManagedTask task, final double progress, final String message, boolean secondary) {
        Handler hand = new Handler(Looper.getMainLooper());
        hand.post(new Runnable() {
            @Override
            public void run() {
                // init dialog
                if(progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setOnCancelListener(DownloadSourcesDialog.this);
                    progressDialog.setIcon(R.drawable.ic_cloud_download_black_24dp);
                    progressDialog.setTitle(R.string.updating);
                    progressDialog.setMessage("");

                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            TaskManager.cancelTask(task);
                        }
                    });
                }

                // dismiss if finished or cancelled
                if(task.isFinished() || task.isCanceled()) {
                    progressDialog.dismiss();
                    return;
                }

                // progress
                progressDialog.setMax(task.maxProgress());
                progressDialog.setMessage(message);
                if(progress > 0) {
                    progressDialog.setIndeterminate(false);
                    progressDialog.setProgress((int)(progress * progressDialog.getMax()));
                    progressDialog.setProgressNumberFormat("%1d/%2d");
                    progressDialog.setProgressPercentFormat(NumberFormat.getPercentInstance());
                } else {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setProgress(progressDialog.getMax());
                    progressDialog.setProgressNumberFormat(null);
                    progressDialog.setProgressPercentFormat(null);
                }

                // show
                if(task.isFinished()) {
                    progressDialog.dismiss();
                } else if(!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        ManagedTask task = TaskManager.getTask(GetAvailableSourcesTask.TASK_ID);
        if(task != null) TaskManager.cancelTask(task);
    }

    @Override
    public void onDestroy() {
        ManagedTask task = TaskManager.getTask(GetAvailableSourcesTask.TASK_ID);
        if(task != null) {
            task.removeOnProgressListener(this);
            task.removeOnFinishedListener(this);
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        super.onDestroy();
    }

}
