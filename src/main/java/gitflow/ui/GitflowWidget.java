/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gitflow.ui;

import consulo.application.Application;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.event.FileEditorManagerEvent;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.project.ui.wm.StatusBarWidgetFactory;
import consulo.project.ui.wm.WindowManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.MessageDialogBuilder;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.ListPopup;
import consulo.versionControlSystem.ProjectLevelVcsManager;
import consulo.versionControlSystem.root.VcsRoot;
import consulo.virtualFileSystem.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import git4idea.ui.branch.GitBranchWidget;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.GitflowVersionTester;
import gitflow.actions.GitflowPopupGroup;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import java.util.Optional;

/**
 * Status bar widget which displays actions for git flow
 *
 * @author Kirill Likhodedov, Opher Vishnia, Alexander von Bremen-Kühne
 */
public class GitflowWidget extends GitBranchWidget implements GitRepositoryChangeListener {
    private volatile String myText = "";
    @Nonnull
    private volatile LocalizeValue myTooltip = LocalizeValue.empty();

    private GitflowPopupGroup popupGroup;

    public GitflowWidget(@Nonnull Project project, @Nonnull StatusBarWidgetFactory factory) {
        super(project, factory);

        project.getMessageBus().connect().subscribe(GitRepositoryChangeListener.class, this);
    }

    @Override
    public StatusBarWidget copy() {
        return new GitflowWidget(getProject(), myFactory);
    }

    @Override
    public StatusBarWidget.WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
        //updateAsync();
    }

    @Override
    public void fileOpened(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
        //updateAsync();
    }

    @Override
    public void fileClosed(@Nonnull FileEditorManager source, @Nonnull VirtualFile file) {
        //updateAsync();
    }

    @Override
    public void repositoryChanged(@Nonnull GitRepository repository) {
        initVersionCheck();
        updateAsync();
    }

    @Nullable
    @Override
    @RequiredUIAccess
    public ListPopup getPopupStep(@Nonnull DataContext dataContext) {
        if (!getIsSupportedVersion()) {
            showUnsupportedVersionDialog();
            return null;
        }

        GitRepository repo = GitBranchUtil.getCurrentRepository(getProject());
        GitflowPopupGroup group = popupGroup;
        if (repo == null || group == null) {
            return null;
        }

        return JBPopupFactory.getInstance()
            .createActionGroupPopup("Gitflow Actions", group.getActionGroup(), dataContext, false, false, true, null, -1, null);
    }

    @RequiredUIAccess
    private static void showUnsupportedVersionDialog() {
        MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo(
                "Unsupported Git Flow version",
                "The Git Flow CLI version installed isn't supported by the Git Flow Integration plugin"
            )
            .yesText("More information (open browser)")
            .noText("no");
        if (builder.show() == Messages.OK) {
            Platform.current().openInBrowser("https://github.com/OpherV/gitflow4idea/blob/develop/GITFLOW_VERSION.md");
        }
    }

    @Nonnull
    @Override
    @RequiredUIAccess
    public String getSelectedValue() {
        if (getHasVersionBeenTested() && !getIsSupportedVersion()) {
            return "Unsupported Git Flow Version";
        }
        return myText;
    }

    @Nonnull
    @Override
    public LocalizeValue getTooltipText() {
        if (!getIsSupportedVersion()) {
            return LocalizeValue.localizeTODO("Click for details");
        }
        return myTooltip;
    }

    public void updateAsync() {
        Application.get().invokeLater(() -> {
            update();

            if (myStatusBar != null) {
                myStatusBar.updateWidget(getId());
            }
        });
    }

    private void update() {
        Project project = getProject();

        //repopulate the branchUtil
        GitflowBranchUtilManager.update(project);

        GitRepository repo = GitBranchUtil.getCurrentRepository(project);
        if (repo == null) { // the file is not under version control => display nothing
            emptyTextAndTooltip();
            return;
        }

        //No advanced features in the status-bar widget
        popupGroup = new GitflowPopupGroup(project, false);

        GitflowBranchUtil gitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(repo);
        boolean hasGitflow = gitflowBranchUtil.hasGitflow();

        myText = hasGitflow ? "Gitflow" : "No Gitflow";
        myTooltip = getDisplayableBranchTooltip(repo);
    }

    private void emptyTextAndTooltip() {
        myText = "";
        myTooltip = LocalizeValue.empty();
    }

    @Nonnull
    private static LocalizeValue getDisplayableBranchTooltip(GitRepository repo) {
        String text = GitBranchUtil.getDisplayableBranchText(repo);
        if (!GitUtil.justOneGitRepository(repo.getProject())) {
            return LocalizeValue.localizeTODO(text + "\n" + "Root: " + repo.getRoot().getName());
        }
        return LocalizeValue.localizeTODO(text);
    }

    /**
     * This method looks up the widget instance for a specific project
     *
     * @param project The project for which the widget instance should be looked up
     * @return The widget instance for the provided project or null if no instance is available
     */
    @Nullable
    public static GitflowWidget findWidgetInstance(@Nullable Project project) {
        if (project != null) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);

            if (statusBar != null) {
                Optional<GitflowWidget> possibleWidget = statusBar.findWidget(widget -> widget instanceof GitflowWidget);
                return possibleWidget.orElse(null);
            }
        }

        return null;
    }

    /**
     * Shows the action popup of this widget in the center of the provided frame. If there are no
     * actions available for this widget, the popup will not be shown.
     *
     * @param frame The frame that will be used for display
     */
    @RequiredUIAccess
    public void showPopupInCenterOf(@Nonnull JFrame frame) {
        update();
        ListPopup popupStep = getPopupStep(DataManager.getInstance().getDataContext(frame));
        if (popupStep != null) {
            popupStep.showInCenterOf(frame);
        }
    }

    public boolean getIsSupportedVersion() {
        GitflowVersionTester versionTester = GitflowVersionTester.forProject(myProject);
        return versionTester.hasVersionBeenTested() && versionTester.isSupportedVersion();
    }

    public boolean getHasVersionBeenTested() {
        return GitflowVersionTester.forProject(myProject).hasVersionBeenTested();
    }

    private void initVersionCheck() {
        // init the gitflow cli version check in a new thread and not on the EDT
        String version = GitflowVersionTester.forProject(myProject).getVersion();
        if (version == null) {
            Runnable runnable = () -> {
                VcsRoot[] vcsRoots = ProjectLevelVcsManager.getInstance(myProject).getAllVcsRoots();
                if (vcsRoots.length > 0 && vcsRoots[0].getVcs() instanceof GitVcs) {
                    GitflowVersionTester.forProject(myProject).init();
                }
            };
            Application.get().executeOnPooledThread(runnable);
        }
    }
}
