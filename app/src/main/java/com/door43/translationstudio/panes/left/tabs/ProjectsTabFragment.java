package com.door43.translationstudio.panes.left.tabs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.door43.translationstudio.MainActivity;
import com.door43.translationstudio.R;
import com.door43.translationstudio.dialogs.ChooseProjectDialog;
import com.door43.translationstudio.dialogs.ModelItemAdapter;
import com.door43.translationstudio.events.ChoseProjectEvent;
import com.door43.translationstudio.projects.Model;
import com.door43.translationstudio.projects.Project;
import com.door43.translationstudio.projects.PseudoProject;
import com.door43.translationstudio.util.TabsFragmentAdapterNotification;
import com.door43.translationstudio.util.TranslatorBaseFragment;
import com.squareup.otto.Subscribe;

/**
 * Created by joel on 8/29/2014.
 */
public class ProjectsTabFragment extends TranslatorBaseFragment implements TabsFragmentAdapterNotification {
    private ProjectsTabFragment me = this;
    private ModelItemAdapter mModelItemAdapter;

//    public ProjectsTabFragment() {
//        MainContext.getEventBus().register(this);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pane_left_projects, container, false);
        ListView listView = (ListView)view.findViewById(R.id.projects_list_view);

        // create adapter
        if(mModelItemAdapter == null) mModelItemAdapter = new ModelItemAdapter(app(), app().getSharedProjectManager().getListableProjects());

        // connectAsync adapter
        listView.setAdapter(mModelItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TRICKY: the project list may contain meta projects as well as normal projects.
                // save changes to the current frame first
                ((MainActivity)me.getActivity()).save();
                Model m = mModelItemAdapter.getItem(i);
                boolean isProject = m.getClass().equals(Project.class);

                if(isProject) {
                    // this is a normal project
                    handleProjectSelection((Project)m);
                } else {
                    // this is a meta project
                    handleMetaSelection((PseudoProject)m);
                }
            }
        });

        return view;
    }

    @Override
    public void NotifyAdapterDataSetChanged() {
        if(mModelItemAdapter != null && app() != null && app().getSharedProjectManager() != null) {
            mModelItemAdapter.notifyDataSetChanged();
            mModelItemAdapter.changeDataSet(app().getSharedProjectManager().getListableProjects());
        }
    }

    /**
     * This handles the selection of a project.
     * @param p
     */
    private void handleProjectSelection(Project p) {
        // this is a normal project
        if (app().getSharedProjectManager().getSelectedProject() == null || !app().getSharedProjectManager().getSelectedProject().getId().equals(p.getId())) {
            // reload the center pane so we don't accidently overwrite a frame
            ((MainActivity) me.getActivity()).reloadCenterPane();

            app().getSharedProjectManager().setSelectedProject(p.getId());
            // load the project source
            new LoadProjectTask().execute();
        } else {
            // select the project
            app().getSharedProjectManager().setSelectedProject(p.getId());
            // reload the center pane so we don't accidently overwrite a frame
            ((MainActivity) me.getActivity()).reloadCenterPane();
            // open up the chapters tab
            ((MainActivity) me.getActivity()).getLeftPane().selectTab(1);
            // let the adapter redraw itself so the selected project is corectly highlighted
            NotifyAdapterDataSetChanged();
        }
    }

    /**
     * Handles the selection of a meta project
     * @param p
     */
    private void handleMetaSelection(PseudoProject p) {
        ((MainActivity) me.getActivity()).fixTranslationFocus();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        app().closeToastMessage();
        // Create and show the dialog.
        ChooseProjectDialog newFragment = new ChooseProjectDialog();
        Bundle args = new Bundle();
        args.putString("metaId", p.getId());
        newFragment.setArguments(args);
        newFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ProjectsTabFragment.this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        });
        newFragment.show(ft, "dialog");
    }

    private class LoadProjectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            app().getSharedProjectManager().fetchProjectSource(app().getSharedProjectManager().getSelectedProject());
            return null;
        }

        protected void onPostExecute(Void result) {
            // populate the center pane
            ((MainActivity) me.getActivity()).reloadCenterPane();
            // open up the chapters tab
            ((MainActivity)me.getActivity()).getLeftPane().selectTab(1);
            // reload the frames tab so we don't see frames from the previous project
            ((MainActivity)me.getActivity()).getLeftPane().reloadFramesTab();
            // let the adapter redraw itself so the selected project is corectly highlighted
            NotifyAdapterDataSetChanged();
        }
    }

    @Subscribe
    public void onSelectedProjectFromMeta(ChoseProjectEvent event) {
        handleProjectSelection(event.getProject());
        event.getDialog().dismiss();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
