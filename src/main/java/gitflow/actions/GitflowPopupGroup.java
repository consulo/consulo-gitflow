package gitflow.actions;

import consulo.project.Project;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.DefaultActionGroup;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * ActionGroup for the Gitflow popup, constructs the actions for each repo source
 */
public class GitflowPopupGroup {
    final Project myProject;
    DefaultActionGroup actionGroup;
    final GitRepositoryManager myRepositoryManager;
    final List<GitRepository> gitRepositories;

    public GitflowPopupGroup(@Nonnull Project project, boolean includeAdvanced) {
        myProject = project;

        //fetch all the git repositories from the project;
        myRepositoryManager = GitUtil.getRepositoryManager(project);
        gitRepositories = myRepositoryManager.getRepositories();

        createActionGroup(includeAdvanced);
    }

    /**
     * Generates the popup actions for the widget
     */
    private void createActionGroup(boolean includeAdvanced) {
        actionGroup = new DefaultActionGroup();

        if (gitRepositories.size() == 1) {
            ActionGroup repoActions = (new RepoActions(myProject, gitRepositories.get(0))).getRepoActionGroup(includeAdvanced);
            actionGroup.addAll(repoActions);
        }
        else {
            for (GitRepository repo : gitRepositories) {
                RepoActions repoActions = new RepoActions(myProject, repo);
                actionGroup.add(repoActions);
            }
        }
    }

    public ActionGroup getActionGroup() {

        return actionGroup;
    }
}
