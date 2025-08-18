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

import consulo.application.ApplicationManager;
import consulo.application.ui.wm.ApplicationIdeFocusManager;
import consulo.application.ui.wm.FocusableFrame;
import consulo.application.ui.wm.IdeFocusManager;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.event.FileEditorManagerEvent;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.project.ui.wm.StatusBarWidgetFactory;
import consulo.project.ui.wm.WindowManager;
import consulo.ui.ex.RelativePoint;
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
import gitflow.IDEAUtils;
import gitflow.actions.GitflowPopupGroup;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Status bar widget which displays actions for git flow
 *
 * @author Kirill Likhodedov, Opher Vishnia, Alexander von Bremen-Kühne
 */
public class GitflowWidget extends GitBranchWidget implements GitRepositoryChangeListener, StatusBarWidget.TextPresentation {
    private volatile String myText = "";
    private volatile String myTooltip = "";

    private GitflowPopupGroup popupGroup;

    public GitflowWidget(@NotNull Project project, @Nonnull StatusBarWidgetFactory factory) {
        super(project, factory);

        project.getMessageBus().connect().subscribe(GitRepositoryChangeListener.class, this);
    }

    @Override
    public StatusBarWidget copy() {
        return new GitBranchWidget(getProject(), myFactory);
    }

    @Override
    public StatusBarWidget.WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
        //updateAsync();
    }

    @Override
    public void fileOpened(FileEditorManager source, VirtualFile file) {
        //updateAsync();
    }

    @Override
    public void fileClosed(FileEditorManager source, VirtualFile file) {
        //updateAsync();
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        initVersionCheck();
        updateAsync();
    }

    @Nullable
    @Override
    public ListPopup getPopupStep() {
        Project project = IDEAUtils.getActiveProject();

        if (project == null) {
            return null;
        }
        GitRepository repo = GitBranchUtil.getCurrentRepository(project);
        if (repo == null) {
            return null;
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) {
            IdeFocusManager focusManager = ApplicationIdeFocusManager.getInstance().getInstanceForProject(project);
            FocusableFrame frame = focusManager.getLastFocusedFrame();
            if (frame != null) {
                focusOwner = focusManager.getLastFocusedFor(frame);
            }
        }

        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        return JBPopupFactory.getInstance().createActionGroupPopup("Gitflow Actions", popupGroup.getActionGroup(), dataContext, false, false, true, null, -1, null);
    }

    @NotNull
    @Override
    public String getSelectedValue() {
        if (getHasVersionBeenTested() && !getIsSupportedVersion()) {
            return "Unsupported Git Flow Version";
        }
        return myText;
    }

    @Override
    public String getTooltipText() {
        if (!getIsSupportedVersion()) {
            return "Click for details";
        }
        return myTooltip;
    }

    @NotNull
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
            if (getIsSupportedVersion()) {
                final ListPopup popup = getPopupStep();
                if (popup == null) {
                    return;
                }
                final Dimension dimension = popup.getContent().getPreferredSize();
                final Point at = new Point(0, -dimension.height);
                popup.show(new RelativePoint(mouseEvent.getComponent(), at));
            }
            else {
                MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo("Unsupported Git Flow version", "The Git Flow CLI version installed isn't supported by the Git Flow Integration plugin")
                    .yesText("More information (open browser)")
                    .noText("no");
                if (builder.show() == Messages.OK) {
                    Platform.current().openInBrowser("https://github.com/OpherV/gitflow4idea/blob/develop/GITFLOW_VERSION.md");
                }
            }
        };

    }

    public void updateAsync() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                update();

                if (myStatusBar != null) {
                    myStatusBar.updateWidget(ID());
                }
            }
        });
    }

    private void update() {
        Project project = getProject();

        //repopulate the branchUtil
        GitflowBranchUtilManager.update(project);

        if (project == null) {
            emptyTextAndTooltip();
            return;
        }

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
        myTooltip = "";
    }

    @NotNull
    private static String getDisplayableBranchTooltip(GitRepository repo) {
        String text = GitBranchUtil.getDisplayableBranchText(repo);
        if (!GitUtil.justOneGitRepository(repo.getProject())) {
            return text + "\n" + "Root: " + repo.getRoot().getName();
        }
        return text;
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
    public void showPopupInCenterOf(@NotNull JFrame frame) {
        update();
        ListPopup popupStep = getPopupStep();
        if (popupStep != null) {
            popupStep.showInCenterOf(frame);
        }
    }

    @NotNull
    @Override
    public String getText() {
        return getSelectedValue();
    }

    @Override
    public float getAlignment() {
        return 0;
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
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    VcsRoot[] vcsRoots = ProjectLevelVcsManager.getInstance(myProject).getAllVcsRoots();
                    if (vcsRoots.length > 0 && vcsRoots[0].getVcs() instanceof GitVcs) {
                        GitflowVersionTester.forProject(myProject).init();
                    }

                }
            };
            new Thread(runnable).start();
        }
    }

}
