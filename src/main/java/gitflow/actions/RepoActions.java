package gitflow.actions;

import consulo.fileEditor.event.FileEditorManagerEvent;
import consulo.fileEditor.event.FileEditorManagerListener;
import consulo.project.Project;
import consulo.ui.ex.action.*;
import consulo.versionControlSystem.distributed.branch.BranchActionGroup;
import consulo.versionControlSystem.distributed.branch.PopupElementWithAdditionalInfo;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class RepoActions extends BranchActionGroup implements PopupElementWithAdditionalInfo, FileEditorManagerListener {
    @Nonnull
    final Project myProject;
    @Nonnull
    final GitRepository myRepo;

    RepoActions(@Nonnull Project project, @Nonnull GitRepository repo) {
        myProject = project;
        myRepo = repo;

        String repoName = repo.getRoot().getPresentableName();
        getTemplatePresentation().setText(repoName, false); // no mnemonics
        this.updateFavoriteIcon();
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.class, this);
    }

    public List<AnAction> getRepoActions(boolean includeAdvanced) {
        List<AnAction> actionList = new ArrayList<>();

        actionList.add(new InitRepoAction(myRepo));

        //FEATURE ACTIONS
        actionList.add(new AnSeparator("Feature"));
        actionList.add(new StartFeatureAction(myRepo));
        actionList.add(new FinishFeatureAction(myRepo));
        actionList.add(new PublishFeatureAction(myRepo));
        actionList.add(new TrackFeatureAction(myRepo));

        //RELEASE ACTIONS
        actionList.add(new AnSeparator("Release"));
        actionList.add(new StartReleaseAction(myRepo));
        actionList.add(new FinishReleaseAction(myRepo));
        actionList.add(new PublishReleaseAction(myRepo));
        actionList.add(new TrackReleaseAction(myRepo));

        //BUGFIX ACTIONS
        actionList.add(new AnSeparator("Bugfix"));
        actionList.add(new StartBugfixAction(myRepo));
        actionList.add(new FinishBugfixAction(myRepo));
        actionList.add(new PublishBugfixAction(myRepo));
        actionList.add(new TrackBugfixAction(myRepo));

        //HOTFIX ACTIONS
        actionList.add(new AnSeparator("Hotfix"));
        actionList.add(new StartHotfixAction(myRepo));
        actionList.add(new FinishHotfixAction(myRepo));
        actionList.add(new PublishHotfixAction(myRepo));

        if (includeAdvanced) {
            actionList.add(new AnSeparator("Advanced"));

            actionList.add(new ActionGroup("Advanced", true) {
                @Nonnull
                @Override
                public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
                    AnAction initRepoAction = new ReInitRepoAction(myRepo);
                    return new AnAction[]{initRepoAction};
                }
            });
        }

        return actionList;
    }

    public DefaultActionGroup getRepoActionGroup(boolean includeAdvanced) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        for (AnAction action : this.getRepoActions(includeAdvanced)) {
            actionGroup.add(action);
        }

        return actionGroup;
    }

    @Nonnull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        List<AnAction> children = this.getRepoActions(false);
        return children.toArray(new AnAction[children.size()]);
    }

    @Override
    @Nullable
    public String getInfoText() {
        return "what's this";
    }

    public void updateFavoriteIcon() {
        boolean isFavorite = GitBranchUtil.getCurrentRepository(myProject) == myRepo;
        setFavorite(isFavorite);
    }

    @Override
    public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
        this.updateFavoriteIcon();
    }
}

