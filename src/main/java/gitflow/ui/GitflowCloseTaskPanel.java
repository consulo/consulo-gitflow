package gitflow.ui;

import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.task.Task;
import consulo.task.TaskManager;
import consulo.task.ui.TaskDialogPanel;
import consulo.versionControlSystem.VcsTaskHandler;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowConfigurable;
import gitflow.GitflowState;
import gitflow.actions.FinishBugfixAction;
import gitflow.actions.FinishFeatureAction;
import gitflow.actions.FinishHotfixAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GitflowCloseTaskPanel extends TaskDialogPanel {
    private JPanel myPanel;
    private JCheckBox finishFeatureCheckbox;
    private JCheckBox finishHotfixCheckbox;
    private JPanel finishHotfixPanel;
    private JTextField tagMessageTextField;
    private JPanel finishFeaturePanel;
    private JPanel finishBugfixPanel;
    private JCheckBox finishBugfixCheckBox;

    private Project myProject;
    private Task myTask;
    private GitRepository myRepo;
    private GitflowBranchUtil gitflowBranchUtil;
    private TaskManager myTaskManager;
    private VcsTaskHandler myVcsTaskHandler;
    private String tagMessage;

    final private GitflowState gitflowState;

    GitflowCloseTaskPanel(Project project, Task task, GitRepository repo){
        myProject = project;
        myTask = task;
        myRepo = repo;
        gitflowState = ServiceManager.getService(GitflowState.class);

        gitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
        myTaskManager = TaskManager.getManager(project);
        VcsTaskHandler[] vcsTaskHAndlers = VcsTaskHandler.getAllHandlers(project);
        if (vcsTaskHAndlers.length > 0){
            //todo handle case of multiple vcs handlers
            myVcsTaskHandler = vcsTaskHAndlers[0];
        }


        String branchName = myVcsTaskHandler != null
                ? myVcsTaskHandler.cleanUpBranchName(myTaskManager.constructDefaultBranchName(task))
                : myTaskManager.suggestBranchName(task);
        
        if (GitflowConfigurable.isOptionActive(project, "HOTFIX_dontTag")) {
            tagMessage="";
            tagMessageTextField.setEnabled(false);
            tagMessageTextField.setToolTipText("Hotfix tagging is disabled in Gitflow options");

        }
        else{
            tagMessage = GitflowConfigurable.getOptionTextString(project, "HOTFIX_customHotfixCommitMessage").replace("%name%", branchName);
            tagMessageTextField.setToolTipText(null);
        }

        tagMessageTextField.setText(tagMessage);

    }

    @NotNull
    @Override
    public JComponent getPanel() {
        String taskBranchName = gitflowState.getTaskBranch(myTask);
        if (taskBranchName != null) {
            myPanel.setVisible(true);
            if (gitflowBranchUtil.isBranchFeature(taskBranchName)) {
                finishFeaturePanel.setVisible(true);
                finishHotfixPanel.setVisible(false);
                finishBugfixPanel.setVisible(false);
            } else if (gitflowBranchUtil.isBranchHotfix(taskBranchName)) {
                finishFeaturePanel.setVisible(false);
                finishHotfixPanel.setVisible(true);
                finishBugfixPanel.setVisible(false);
            } else if (gitflowBranchUtil.isBranchBugfix(taskBranchName)) {
                finishFeaturePanel.setVisible(false);
                finishHotfixPanel.setVisible(false);
                finishBugfixPanel.setVisible(true);
            }
        }
        else{
            myPanel.setVisible(false);
        }

        return myPanel;
    }

    @Override
    public void commit() {
        String taskFullBranchName = gitflowState.getTaskBranch(myTask);

        // test if current task is a gitflow task
        if (taskFullBranchName != null) {
            String taskBranchName = gitflowBranchUtil.stripFullBranchName(taskFullBranchName);

            if (finishFeatureCheckbox.isSelected()) {
                FinishFeatureAction action = new FinishFeatureAction(myRepo);
                action.runAction(myProject, taskBranchName);
            } else if (finishHotfixCheckbox.isSelected()) {
                FinishHotfixAction action = new FinishHotfixAction(myRepo);
                action.runAction(myProject, taskBranchName, tagMessageTextField.getText());
             } else if (finishBugfixCheckBox.isSelected()) {
                FinishBugfixAction action = new FinishBugfixAction(myRepo);
                action.runAction(myProject, taskBranchName);
            }
        }
    }
}
