package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.ex.action.AnActionEvent;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class PublishBugfixAction extends AbstractPublishAction {

    PublishBugfixAction(){
        super("Publish Bugfix", BranchType.Bugfix);
    }

    PublishBugfixAction(GitRepository repo){
        super(repo, "Publish Bugfix", BranchType.Bugfix);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.actionPerformed(anActionEvent);

        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
        final String bugfixName = gitflowConfigUtil.getBugfixNameFromBranch(branchUtil.getCurrentBranchName());

        new Task.Backgroundable(myProject,"Publishing bugfix "+bugfixName,false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GitCommandResult result = myGitflow.publishBugfix(myRepo, bugfixName,new GitflowErrorsListener((consulo.project.Project) myProject));
                if (result.success()) {
                    String publishedBugfixMessage = String.format("A new remote branch '%s%s' was created", branchUtil.getPrefixBugfix(), bugfixName);
                    NotifyUtil.notifySuccess((consulo.project.Project) myProject, bugfixName, publishedBugfixMessage);
                }
                else {
                    NotifyUtil.notifyError((consulo.project.Project) myProject, "Error", "Please have a look at the Version Control console for more details");
                }
                myRepo.update();
            }
        }.queue();
    }

}