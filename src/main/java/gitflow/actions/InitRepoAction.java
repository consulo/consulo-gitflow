package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.ex.action.AnActionEvent;
import consulo.util.dataholder.Key;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowInitOptions;
import gitflow.ui.GitflowInitOptionsDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class InitRepoAction extends GitflowAction {

    InitRepoAction() {
        this( "Init Repo");
    }

    InitRepoAction(String actionName) {
        this(null, "Init Repo");
    }

    InitRepoAction(GitRepository repo) {
        this(repo,"Init Repo");
    }

    InitRepoAction(GitRepository repo, String actionName) {
        super(repo, actionName);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        GitflowBranchUtil branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
        if (branchUtil != null) {
            // Only show when gitflow isn't setup
            if (branchUtil.hasGitflow()) {
                e.getPresentation().setEnabledAndVisible(false);
            } else {
                e.getPresentation().setEnabledAndVisible(true);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        GitflowInitOptionsDialog optionsDialog = new GitflowInitOptionsDialog(myProject, branchUtil.getLocalBranchNames());
        optionsDialog.show();

        if(optionsDialog.isOK()) {
            final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);
            final GitflowLineHandler localLineHandler = getLineHandler();
            final GitflowInitOptions initOptions = optionsDialog.getOptions();

            new Task.Backgroundable(myProject, getTitle(),false){
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    GitCommandResult result =
                            executeCommand(initOptions, errorLineHandler,
                                    localLineHandler);

                    if (result.success()) {
                        String successMessage = getSuccessMessage();
                        NotifyUtil.notifySuccess((consulo.project.Project) myProject, "", successMessage);
                    } else {
                        NotifyUtil.notifyError((consulo.project.Project) myProject, "Error", "Please have a look at the Version Control console for more details");
                    }

                    //update the widget
                    myProject.getMessageBus().syncPublisher(GitRepositoryChangeListener.class).repositoryChanged(myRepo);
                    myRepo.update();
                }
            }.queue();
        }

    }

    protected String getSuccessMessage() {
        return "Initialized gitflow in repo " + myRepo.getRoot().getPresentableName();
    }

    protected GitCommandResult executeCommand(GitflowInitOptions initOptions,
            GitflowErrorsListener errorLineHandler,
            GitflowLineHandler localLineHandler) {
        return myGitflow.initRepo(myRepo, initOptions, errorLineHandler, localLineHandler);
    }

    protected String getTitle() {
        return "Initializing Repo";
    }

    protected GitflowLineHandler getLineHandler() {
        return new LineHandler();
    }


    private class LineHandler extends GitflowLineHandler {
        @Override
        public void onLineAvailable(String line, Key outputType) {
            if (line.contains("Already initialized for gitflow")){
                myErrors.add("Repo already initialized for gitflow");
            }

        }
    }

}