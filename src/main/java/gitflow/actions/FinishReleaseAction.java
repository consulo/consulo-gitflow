package gitflow.actions;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.awt.Messages;
import git4idea.branch.GitBranchUtil;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.GitflowConfigurable;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

public class FinishReleaseAction extends AbstractBranchAction {

    String customReleaseName = null;
    String customtagMessage = null;

    FinishReleaseAction() {
        super("Finish Release", AbstractBranchAction.BranchType.Release);
    }

    FinishReleaseAction(GitRepository repo) {
        super(repo, "Finish Release", BranchType.Release);
    }

    FinishReleaseAction(String name, String tagMessage) {
        super("Finish Release", BranchType.Release);
        customReleaseName = name;
        customtagMessage = tagMessage;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        String currentBranchName = GitBranchUtil.getBranchNameOrRev(myRepo);
        if (!currentBranchName.isEmpty()) {

            final AnActionEvent event = e;

            final String tagMessage;
            final String releaseName;

            GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(myProject, myRepo);

            // Check if a release name was specified, otherwise take name from current branch
            releaseName = customReleaseName != null ? customReleaseName : gitflowConfigUtil.getReleaseNameFromBranch(currentBranchName);

            final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(myProject);

            String tagMessageTemplate = GitflowConfigurable.getOptionTextString(myProject, "RELEASE_customTagCommitMessage").replace("%name%", releaseName);
            String tagMessageDraft;

            boolean cancelAction = false;

            if (GitflowConfigurable.isOptionActive(myProject, "RELEASE_dontTag")) {
                tagMessage = "";
            }
            else if (customtagMessage != null) {
                //probably repeating the release finish after a merge
                tagMessage = customtagMessage;
            }
            else {
                tagMessageDraft = Messages.showInputDialog(myProject, "Enter the tag message:", "Finish Release", Messages.getQuestionIcon(), tagMessageTemplate, null);
                if (tagMessageDraft == null) {
                    cancelAction = true;
                    tagMessage = "";
                }
                else {
                    tagMessage = tagMessageDraft;
                }
            }


            if (!cancelAction) {

                new Task.Backgroundable(myProject, "Finishing release " + releaseName, false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.finishRelease(myRepo, releaseName, tagMessage, errorLineHandler);

                        if (result.success()) {
                            String finishedReleaseMessage = String.format("The release branch '%s%s' was merged into '%s' and '%s'", branchUtil.getPrefixRelease(), releaseName, branchUtil.getBranchnameDevelop(), branchUtil.getBranchnameMaster());
                            NotifyUtil.notifySuccess(myProject, releaseName, finishedReleaseMessage);
                        }
                        else if (errorLineHandler.hasMergeError) {
                            // (merge errors are handled in the onSuccess handler)
                        }
                        else {
                            NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                        }

                        myRepo.update();

                    }

                    @RequiredUIAccess
                    @Override
                    public void onSuccess() {
                        super.onSuccess();

                        //merge conflicts if necessary
                        if (errorLineHandler.hasMergeError) {
                            if (handleMerge((consulo.project.Project) myProject)) {
                                FinishReleaseAction completeFinisReleaseAction = new FinishReleaseAction(releaseName, tagMessage);
                                completeFinisReleaseAction.actionPerformed(event);
                            }
                        }
                    }

                }.queue();

            }
        }

    }

}