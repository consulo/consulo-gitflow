package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import git4idea.branch.GitBranchUtil;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class FinishFeatureAction extends AbstractBranchAction {

    String customFeatureName=null;

    public FinishFeatureAction() {
        super("Finish Feature", BranchType.Feature);
    }

    public FinishFeatureAction(GitRepository repo) {
        super(repo, "Finish Feature", BranchType.Feature);
    }

    FinishFeatureAction(GitRepository repo, String name) {
        super(repo, "Finish Feature", BranchType.Feature);
        customFeatureName=name;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        String currentBranchName = GitBranchUtil.getBranchNameOrRev(myRepo);
        if (currentBranchName.isEmpty()==false){

            final AnActionEvent event=e;
            final String featureName;
            // Check if a feature name was specified, otherwise take name from current branch
            if (customFeatureName!=null){
                featureName = customFeatureName;
            }
            else{
                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);
                featureName = gitflowConfigUtil.getFeatureNameFromBranch(currentBranchName);
            }

            this.runAction(myProject, featureName);
        }

    }

    public void runAction(final Project project, final String featureName){
        super.runAction(project, null, featureName, null);

        final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);
        final FinishFeatureAction that = this;

        //get the base branch for this feature
        GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);

        new Task.Backgroundable(myProject,"Finishing feature "+featureName,false){
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                final String baseBranch = gitflowConfigUtil.getBaseBranch(branchUtil.getPrefixFeature()+featureName);

                GitCommandResult result =  myGitflow.finishFeature(myRepo,featureName,errorLineHandler);

                if (result.success()) {
                    String finishedFeatureMessage = String.format("The feature branch '%s%s' was merged into '%s'", branchUtil.getPrefixFeature(), featureName, baseBranch);
                    NotifyUtil.notifySuccess((Project) myProject, featureName, finishedFeatureMessage);
                }
                else if(errorLineHandler.hasMergeError){
                    // (merge errors are handled in the onSuccess handler)
                }
                else {
                    NotifyUtil.notifyError((Project) myProject, "Error", "Please have a look at the Version Control console for more details");
                }

                myRepo.update();

            }

            @Override
            public void onSuccess() {
                super.onSuccess();

                //merge conflicts if necessary
                if (errorLineHandler.hasMergeError){
                    if (handleMerge(project)) {
                        that.runAction(project, featureName);
                        FinishFeatureAction completeFinishFeatureAction = new FinishFeatureAction(myRepo, featureName);
                    }

                }

            }
        }.queue();;
    }

}