package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import git4idea.branch.GitBranchUtil;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.GitflowConfigurable;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class FinishHotfixAction extends AbstractBranchAction {

    public FinishHotfixAction() {
        super("Finish Hotfix", BranchType.Hotfix);
    }
    public FinishHotfixAction(GitRepository repo) {
        super(repo, "Finish Hotfix", BranchType.Hotfix);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        String currentBranchName = GitBranchUtil.getBranchNameOrRev(myRepo);


        if (currentBranchName.isEmpty() == false){

            GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
            //TODO HOTFIX NAME
            final String hotfixName = gitflowConfigUtil.getHotfixNameFromBranch(currentBranchName);

            final String tagMessage;
            String tagMessageTemplate = GitflowConfigurable.getOptionTextString(myProject, "HOTFIX_customHotfixCommitMessage").replace("%name%", hotfixName);

            if (GitflowConfigurable.isOptionActive(myProject, "HOTFIX_dontTag")) {
                tagMessage = "";
            }
            else {
                tagMessage = Messages.showInputDialog(myProject, "Enter the tag message:", "Finish Hotfix", Messages.getQuestionIcon(), tagMessageTemplate, null);
            }

            this.runAction(e.getData(Project.KEY), hotfixName, tagMessage);

        }

    }

    public void runAction(final Project project, final String hotfixName, final String tagMessage){
        super.runAction(project, null, hotfixName, null);

        final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

        if (tagMessage!=null){
            new Task.Backgroundable(myProject,"Finishing hotfix "+hotfixName,false){
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    GitCommandResult result=  myGitflow.finishHotfix(myRepo, hotfixName, tagMessage, errorLineHandler);

                    if (result.success()) {
                        String finishedHotfixMessage = String.format("The hotfix branch '%s%s' was merged into '%s' and '%s'", branchUtil.getPrefixHotfix(), hotfixName, branchUtil.getBranchnameDevelop(), branchUtil.getBranchnameMaster());
                        NotifyUtil.notifySuccess((Project) myProject, hotfixName, finishedHotfixMessage);
                    }
                    else {
                        NotifyUtil.notifyError((Project) myProject, "Error", "Please have a look at the Version Control console for more details");
                    }

                    myRepo.update();

                }
            }.queue();
        }

    }

}