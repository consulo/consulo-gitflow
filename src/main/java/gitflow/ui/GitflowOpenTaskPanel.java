package gitflow.ui;

import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.task.LocalTask;
import consulo.task.Task;
import consulo.task.TaskManager;
import consulo.task.ui.TaskDialogPanel;
import consulo.versionControlSystem.VcsTaskHandler;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowConfigUtil;
import gitflow.GitflowState;
import gitflow.actions.GitflowAction;
import gitflow.actions.StartBugfixAction;
import gitflow.actions.StartFeatureAction;
import gitflow.actions.StartHotfixAction;
import jakarta.annotation.Nonnull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;

import static consulo.versionControlSystem.VcsTaskHandler.TaskInfo;

public class GitflowOpenTaskPanel extends TaskDialogPanel implements ItemListener {
    private JRadioButton noActionRadioButton;
    private JRadioButton startFeatureRadioButton;
    private JRadioButton startHotfixRadioButton;
    private JTextField featureName;
    private JComboBox featureBaseBranch;
    private JTextField hotfixName;
    private JPanel myPanel;
    private JComboBox hotfixBaseBranch;
    private JRadioButton startBugfixRadioButton;
    private JTextField bugfixName;
    private JComboBox bugfixBaseBranch;

    private final Project myProject;
    private GitRepository myRepo;
    private final GitflowBranchUtil gitflowBranchUtil;
    private final TaskManager myTaskManager;
    private VcsTaskHandler myVcsTaskHandler;
    private final LocalTask myPreviousTask;
    private final Task currentTask;

    private final GitflowState gitflowState;

    public GitflowOpenTaskPanel(Project project, Task task, GitRepository repo) {
        myProject = project;
        currentTask = task;
        myRepo = repo;

        myTaskManager = TaskManager.getManager(project);
        myPreviousTask = myTaskManager.getActiveTask();
        VcsTaskHandler[] vcsTaskHandlers = VcsTaskHandler.getAllHandlers(project);
        if (vcsTaskHandlers.length > 0) {
            //todo handle case of multiple vcs handlers
            myVcsTaskHandler = vcsTaskHandlers[0];
        }

        gitflowState = ServiceManager.getService(GitflowState.class);
        gitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);

        String defaultFeatureBranch = gitflowConfigUtil.developBranch;
        featureBaseBranch.setModel(gitflowBranchUtil.createBranchComboModel(defaultFeatureBranch));

        String defaultHotfixBranch = gitflowConfigUtil.masterBranch;
        hotfixBaseBranch.setModel(gitflowBranchUtil.createBranchComboModel(defaultHotfixBranch));

        String defaultBugfixBranch = gitflowConfigUtil.developBranch;
        bugfixBaseBranch.setModel(gitflowBranchUtil.createBranchComboModel(defaultBugfixBranch));

        myRepo = GitBranchUtil.getCurrentRepository(project);

        String branchName = myVcsTaskHandler != null
            ? myVcsTaskHandler.cleanUpBranchName(myTaskManager.constructDefaultBranchName(task))
            : myTaskManager.suggestBranchName(task);

        featureName.setText(branchName);
        featureName.setEditable(false);
        featureName.setEnabled(false);

        hotfixName.setText(branchName);
        hotfixName.setEditable(false);
        hotfixName.setEnabled(false);

        bugfixName.setText(branchName);
        bugfixName.setEditable(false);
        bugfixName.setEnabled(false);

        featureBaseBranch.setEnabled(false);
        hotfixBaseBranch.setEnabled(false);
        bugfixBaseBranch.setEnabled(false);

        //add listeners
        noActionRadioButton.addItemListener(this);
        startFeatureRadioButton.addItemListener(this);
        startHotfixRadioButton.addItemListener(this);
        startBugfixRadioButton.addItemListener(this);

    }

    @Nonnull
    @Override
    public JComponent getPanel() {
        return myPanel;
    }

    @Override
    public void commit() {
        GitflowBranchUtil.ComboEntry selectedFeatureBaseBranch =
            (GitflowBranchUtil.ComboEntry) featureBaseBranch.getModel().getSelectedItem();
        GitflowBranchUtil.ComboEntry selectedHotfixBaseBranch =
            (GitflowBranchUtil.ComboEntry) hotfixBaseBranch.getModel().getSelectedItem();
        GitflowBranchUtil.ComboEntry selectedBugfixBaseBranch =
            (GitflowBranchUtil.ComboEntry) bugfixBaseBranch.getModel().getSelectedItem();

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);

        if (startFeatureRadioButton.isSelected()) {
            String branchName = gitflowConfigUtil.featurePrefix + featureName.getText();
            attachTaskAndRunAction(new StartFeatureAction(myRepo), selectedFeatureBaseBranch.getBranchName(), branchName);
        }
        else if (startHotfixRadioButton.isSelected()) {
            String branchName = gitflowConfigUtil.hotfixPrefix + hotfixName.getText();
            attachTaskAndRunAction(new StartHotfixAction(myRepo), selectedHotfixBaseBranch.getBranchName(), branchName);
        }
        else if (startBugfixRadioButton.isSelected()) {
            String branchName = gitflowConfigUtil.bugfixPrefix + bugfixName.getText();
            attachTaskAndRunAction(new StartBugfixAction(myRepo), selectedBugfixBaseBranch.getBranchName(), branchName);
        }
    }

    /**
     * @param action         instance of GitflowAction
     * @param baseBranchName Branch name of the branch the new one is based on
     * @param fullBranchName Branch name with feature/hotfix/bugfix prefix for saving to task
     */
    private void attachTaskAndRunAction(GitflowAction action, String baseBranchName, String fullBranchName) {
        String branchName = gitflowBranchUtil.stripFullBranchName(fullBranchName);

        //Create new branch / checkout branch
        action.runAction(
            myProject,
            baseBranchName,
            branchName,
            () -> {
                TaskInfo[] next = {new TaskInfo(fullBranchName, Collections.singleton(myRepo.getPresentableUrl()))};
                LocalTask localTask = myTaskManager.getActiveTask();

                //Add branch to task
                myTaskManager.addBranches(localTask, next, false);

                //maps branch to task
                gitflowState.setTaskBranch(currentTask, fullBranchName);
            }
        );
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        //disable\enable text-fields based on radio selection
        if (e.getStateChange() == ItemEvent.SELECTED && source == noActionRadioButton) {
            featureName.setEditable(false);
            featureName.setEnabled(false);
            featureBaseBranch.setEnabled(false);

            hotfixName.setEditable(false);
            hotfixName.setEnabled(false);
            hotfixBaseBranch.setEnabled(false);

            bugfixName.setEditable(false);
            bugfixName.setEnabled(false);
            bugfixBaseBranch.setEnabled(false);
        }
        else if (e.getStateChange() == ItemEvent.SELECTED && source == startFeatureRadioButton) {
            featureName.setEditable(true);
            featureName.setEnabled(true);
            featureBaseBranch.setEnabled(true);

            hotfixName.setEditable(false);
            hotfixName.setEnabled(false);
            hotfixBaseBranch.setEnabled(false);

            bugfixName.setEditable(false);
            bugfixName.setEnabled(false);
            bugfixBaseBranch.setEnabled(false);
        }
        else if (e.getStateChange() == ItemEvent.SELECTED && source == startHotfixRadioButton) {
            featureName.setEditable(false);
            featureName.setEnabled(false);
            featureBaseBranch.setEnabled(false);

            hotfixName.setEditable(true);
            hotfixName.setEnabled(true);
            hotfixBaseBranch.setEnabled(true);

            bugfixName.setEditable(false);
            bugfixName.setEnabled(false);
            bugfixBaseBranch.setEnabled(false);
        }
        else if (e.getStateChange() == ItemEvent.SELECTED && source == startBugfixRadioButton) {
            featureName.setEditable(false);
            featureName.setEnabled(false);
            featureBaseBranch.setEnabled(false);

            hotfixName.setEditable(false);
            hotfixName.setEnabled(false);
            hotfixBaseBranch.setEnabled(false);

            bugfixName.setEditable(true);
            bugfixName.setEnabled(true);
            bugfixBaseBranch.setEnabled(true);
        }

    }
}
