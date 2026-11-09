package gitflow.ui;

import consulo.ide.ServiceManager;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.task.LocalTask;
import consulo.task.Task;
import consulo.task.TaskManager;
import consulo.task.ui.TaskDialogPanel;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.RadioButton;
import consulo.ui.RadioGroup;
import consulo.ui.Space;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.util.FormBuilder;
import consulo.versionControlSystem.VcsTaskHandler;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowConfigUtil;
import gitflow.GitflowState;
import gitflow.actions.GitflowAction;
import gitflow.actions.StartBugfixAction;
import gitflow.actions.StartFeatureAction;
import gitflow.actions.StartHotfixAction;

import java.util.Collections;
import java.util.List;

import static consulo.versionControlSystem.VcsTaskHandler.TaskInfo;

public class GitflowOpenTaskPanel extends TaskDialogPanel {
    private enum Mode {
        NONE,
        FEATURE,
        HOTFIX,
        BUGFIX
    }

    private final Project myProject;
    private final GitRepository myRepo;
    private final GitflowBranchUtil gitflowBranchUtil;
    private final TaskManager myTaskManager;
    private final Task currentTask;
    private final GitflowState gitflowState;

    private final RadioGroup<Mode> myMode = RadioGroup.create();

    private final TextBox featureName;
    private final ComboBox<GitflowBranchUtil.ComboEntry> featureBaseBranch;
    private final TextBox hotfixName;
    private final ComboBox<GitflowBranchUtil.ComboEntry> hotfixBaseBranch;
    private final TextBox bugfixName;
    private final ComboBox<GitflowBranchUtil.ComboEntry> bugfixBaseBranch;

    private final Component myComponent;

    @RequiredUIAccess
    public GitflowOpenTaskPanel(Project project, Task task, GitRepository repo) {
        myProject = project;
        currentTask = task;
        myRepo = repo;

        myTaskManager = TaskManager.getManager(project);
        VcsTaskHandler[] vcsTaskHandlers = VcsTaskHandler.getAllHandlers(project);
        //todo handle case of multiple vcs handlers
        VcsTaskHandler vcsTaskHandler = vcsTaskHandlers.length > 0 ? vcsTaskHandlers[0] : null;

        gitflowState = ServiceManager.getService(GitflowState.class);
        gitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);

        String branchName = vcsTaskHandler != null
            ? vcsTaskHandler.cleanUpBranchName(myTaskManager.constructDefaultBranchName(task))
            : myTaskManager.suggestBranchName(task);

        featureName = TextBox.create(branchName);
        featureBaseBranch = createBranchCombo(gitflowConfigUtil.developBranch);
        hotfixName = TextBox.create(branchName);
        hotfixBaseBranch = createBranchCombo(gitflowConfigUtil.masterBranch);
        bugfixName = TextBox.create(branchName);
        bugfixBaseBranch = createBranchCombo(gitflowConfigUtil.developBranch);

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(myMode.newButton(LocalizeValue.localizeTODO("No action"), Mode.NONE), Label.create(LocalizeValue.empty()));
        addRow(builder, Mode.FEATURE, "Start Feature", featureName, featureBaseBranch);
        addRow(builder, Mode.HOTFIX, "Start Hotfix", hotfixName, hotfixBaseBranch);
        addRow(builder, Mode.BUGFIX, "Start Bugfix", bugfixName, bugfixBaseBranch);
        myComponent = builder.build();

        myMode.setValue(Mode.NONE, false);
        myMode.addValueListener(mode -> updateFields());
        updateFields();
    }

    @RequiredUIAccess
    private ComboBox<GitflowBranchUtil.ComboEntry> createBranchCombo(String defaultBranch) {
        List<GitflowBranchUtil.ComboEntry> entries = gitflowBranchUtil.createBranchComboEntries(defaultBranch);
        ComboBox<GitflowBranchUtil.ComboEntry> comboBox = ComboBox.create(entries);
        comboBox.setTextRenderer(entry -> entry == null ? LocalizeValue.empty() : LocalizeValue.of(entry.toString()));
        if (!entries.isEmpty()) {
            comboBox.setValue(entries.get(0));
        }
        return comboBox;
    }

    @RequiredUIAccess
    private void addRow(FormBuilder builder,
                        Mode mode,
                        String text,
                        TextBox name,
                        ComboBox<GitflowBranchUtil.ComboEntry> baseBranch) {
        RadioButton button = myMode.newButton(LocalizeValue.localizeTODO(text), mode);

        HorizontalLayout row = HorizontalLayout.create(Space.SMALL);
        row.add(name);
        row.add(Label.create(LocalizeValue.localizeTODO("base branch")));
        row.add(baseBranch);
        builder.addLabeled(button, row);
    }

    @RequiredUIAccess
    private void updateFields() {
        Mode mode = myMode.getValue();
        featureName.setEnabled(mode == Mode.FEATURE);
        featureBaseBranch.setEnabled(mode == Mode.FEATURE);
        hotfixName.setEnabled(mode == Mode.HOTFIX);
        hotfixBaseBranch.setEnabled(mode == Mode.HOTFIX);
        bugfixName.setEnabled(mode == Mode.BUGFIX);
        bugfixBaseBranch.setEnabled(mode == Mode.BUGFIX);
    }

    @RequiredUIAccess
    @Override
    public Component getComponent() {
        return myComponent;
    }

    @RequiredUIAccess
    @Override
    public void commit() {
        Mode mode = myMode.getValue();
        if (mode == null || mode == Mode.NONE) {
            return;
        }

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
        switch (mode) {
            case FEATURE -> start(new StartFeatureAction(myRepo), featureBaseBranch, gitflowConfigUtil.featurePrefix + featureName.getValue());
            case HOTFIX -> start(new StartHotfixAction(myRepo), hotfixBaseBranch, gitflowConfigUtil.hotfixPrefix + hotfixName.getValue());
            case BUGFIX -> start(new StartBugfixAction(myRepo), bugfixBaseBranch, gitflowConfigUtil.bugfixPrefix + bugfixName.getValue());
            default -> {
            }
        }
    }

    private void start(GitflowAction action, ComboBox<GitflowBranchUtil.ComboEntry> baseBranch, String fullBranchName) {
        GitflowBranchUtil.ComboEntry selected = baseBranch.getValue();
        if (selected == null) {
            return;
        }
        attachTaskAndRunAction(action, selected.getBranchName(), fullBranchName);
    }

    /**
     * @param action         instance of GitflowAction
     * @param baseBranchName Branch name of the branch the new one is based on
     * @param fullBranchName Branch name with feature/hotfix/bugfix prefix for saving to task
     */
    private void attachTaskAndRunAction(GitflowAction action, String baseBranchName, final String fullBranchName) {
        final String branchName = gitflowBranchUtil.stripFullBranchName(fullBranchName);

        //Create new branch / checkout branch
        action.runAction(myProject, baseBranchName, branchName, () -> {
            final TaskInfo[] next = {new TaskInfo(fullBranchName, Collections.singleton(myRepo.getPresentableUrl()))};
            final LocalTask localTask = myTaskManager.getActiveTask();

            //Add branch to task
            myTaskManager.addBranches(localTask, next, false);

            //maps branch to task
            gitflowState.setTaskBranch(currentTask, fullBranchName);
        });
    }
}
