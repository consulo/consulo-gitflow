package gitflow;

import consulo.disposer.Disposer;
import consulo.project.Project;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps repos to their corresponding branch utils
 * Note that the static class is used across projects
 */

public class GitflowBranchUtilManager {
    private static Map<String, GitflowBranchUtil> repoBranchUtilMap;

    static public GitflowBranchUtil getBranchUtil(GitRepository repo){
        if (repo != null && repoBranchUtilMap != null) {
            return repoBranchUtilMap.get(repo.getPresentableUrl());
        } else {
            return null;
        }
    }

    static public void setupBranchUtil(Project project, GitRepository repo){
        GitflowBranchUtil gitflowBranchUtil = new GitflowBranchUtil(project, repo);
        repoBranchUtilMap.put(repo.getPresentableUrl(), gitflowBranchUtil);
        // clean up
        Disposer.register(repo, () -> repoBranchUtilMap.remove(repo));
    }

    /**
     * Repopulates the branchUtils for each repo
     * @param project
     */
    static public void update(Project project){
        if (repoBranchUtilMap == null){
            repoBranchUtilMap = new HashMap<>();
        }

        for (GitRepository repo : GitUtil.getRepositoryManager(project).getRepositories()) {
            GitflowBranchUtilManager.setupBranchUtil(project, repo);
        }
    }
}
