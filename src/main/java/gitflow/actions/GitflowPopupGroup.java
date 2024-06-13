package gitflow.actions;

import consulo.project.Project;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.DefaultActionGroup;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * ActionGroup for the Gitflow popup, constructs the actions for each repo source
 */
public class GitflowPopupGroup {

    Project myProject;
    DefaultActionGroup actionGroup;
    GitRepositoryManager myRepositoryManager;
    List<GitRepository> gitRepositories;

    public GitflowPopupGroup(@NotNull Project project, boolean includeAdvanced) {
        myProject = project;

        //fetch all the git repositories from the project;
        myRepositoryManager = GitUtil.getRepositoryManager(project);
        gitRepositories = myRepositoryManager.getRepositories();

        createActionGroup(includeAdvanced);
    }

    /**
     * Generates the popup actions for the widget
     */
    private void createActionGroup(boolean includeAdvanced){
        actionGroup = new DefaultActionGroup();


        if (gitRepositories.size() == 1){
            ActionGroup repoActions =
                    (new RepoActions(myProject, gitRepositories.get(0)))
                            .getRepoActionGroup(includeAdvanced);
            actionGroup.addAll(repoActions);
        }
        else{
            Iterator gitRepositoriesIterator = gitRepositories.iterator();
            while(gitRepositoriesIterator.hasNext()){
                GitRepository repo = (GitRepository) gitRepositoriesIterator.next();
                RepoActions repoActions = new RepoActions(myProject, repo);
                actionGroup.add(repoActions);
            }
        }
    }

    public ActionGroup getActionGroup (){

        return actionGroup;
    }
}
