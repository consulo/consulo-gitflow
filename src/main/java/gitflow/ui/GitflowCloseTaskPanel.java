package gitflow.ui;

import consulo.ide.ServiceManager;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.task.Task;
import consulo.task.TaskManager;
import consulo.task.ui.TaskDialogPanel;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.versionControlSystem.VcsTaskHandler;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowConfigurable;
import gitflow.GitflowState;
import gitflow.actions.FinishBugfixAction;
import gitflow.actions.FinishFeatureAction;
import gitflow.actions.FinishHotfixAction;
import org.jspecify.annotations.Nullable;

public class GitflowCloseTaskPanel extends TaskDialogPanel {
    private final Project myProject;
    private final Task myTask;
    private final GitRepository myRepo;
    private final GitflowBranchUtil gitflowBranchUtil;
    private final GitflowState gitflowState;

    private @Nullable CheckBox finishFeatureCheckbox;
    private @Nullable CheckBox finishHotfixCheckbox;
    private @Nullable TextBox tagMessageTextField;
    private @Nullable CheckBox finishBugfixCheckBox;

    private final Component myComponent;

    @RequiredUIAccess
    GitflowCloseTaskPanel(Project project, Task task, GitRepository repo) {
        myProject = project;
        myTask = task;
        myRepo = repo;
        gitflowState = ServiceManager.getService(GitflowState.class);
        gitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);

        String taskBranchName = gitflowState.getTaskBranch(myTask);
        if (taskBranchName == null) {
            myComponent = VerticalLayout.create();
            return;
        }

        FormBuilder builder = FormBuilder.create();
        if (gitflowBranchUtil.isBranchFeature(taskBranchName)) {
            finishFeatureCheckbox = CheckBox.create(LocalizeValue.localizeTODO("Finish Feature"));
            builder.addBottom(finishFeatureCheckbox);
        }
        else if (gitflowBranchUtil.isBranchHotfix(taskBranchName)) {
            finishHotfixCheckbox = CheckBox.create(LocalizeValue.localizeTODO("Finish Hotfix"));
            tagMessageTextField = TextBox.create(tagMessage(project, task));
            if (GitflowConfigurable.isOptionActive(project, "HOTFIX_dontTag")) {
                tagMessageTextField.setEnabled(false);
                tagMessageTextField.setToolTipText(LocalizeValue.localizeTODO("Hotfix tagging is disabled in Gitflow options"));
            }
            builder.addLabeled(finishHotfixCheckbox, tagMessageTextField);
        }
        else if (gitflowBranchUtil.isBranchBugfix(taskBranchName)) {
            finishBugfixCheckBox = CheckBox.create(LocalizeValue.localizeTODO("Finish Bugfix"));
            builder.addBottom(finishBugfixCheckBox);
        }
        myComponent = builder.build();
    }

    private static String tagMessage(Project project, Task task) {
        if (GitflowConfigurable.isOptionActive(project, "HOTFIX_dontTag")) {
            return "";
        }

        TaskManager taskManager = TaskManager.getManager(project);
        VcsTaskHandler[] vcsTaskHandlers = VcsTaskHandler.getAllHandlers(project);
        //todo handle case of multiple vcs handlers
        String branchName = vcsTaskHandlers.length > 0
            ? vcsTaskHandlers[0].cleanUpBranchName(taskManager.constructDefaultBranchName(task))
            : taskManager.suggestBranchName(task);
        return GitflowConfigurable.getOptionTextString(project, "HOTFIX_customHotfixCommitMessage").replace("%name%", branchName);
    }

    @RequiredUIAccess
    @Override
    public Component getComponent() {
        return myComponent;
    }

    @RequiredUIAccess
    @Override
    public void commit() {
        String taskFullBranchName = gitflowState.getTaskBranch(myTask);

        // test if current task is a gitflow task
        if (taskFullBranchName == null) {
            return;
        }

        String taskBranchName = gitflowBranchUtil.stripFullBranchName(taskFullBranchName);
        if (finishFeatureCheckbox != null && finishFeatureCheckbox.getValueOrError()) {
            new FinishFeatureAction(myRepo).runAction(myProject, taskBranchName);
        }
        else if (finishHotfixCheckbox != null && finishHotfixCheckbox.getValueOrError() && tagMessageTextField != null) {
            new FinishHotfixAction(myRepo).runAction(myProject, taskBranchName, tagMessageTextField.getValueOrError());
        }
        else if (finishBugfixCheckBox != null && finishBugfixCheckBox.getValueOrError()) {
            new FinishBugfixAction(myRepo).runAction(myProject, taskBranchName);
        }
    }
}
