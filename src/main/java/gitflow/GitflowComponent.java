package gitflow;

import consulo.component.messagebus.MessageBus;
import consulo.disposer.Disposable;
import consulo.project.Project;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.WindowManager;
import consulo.versionControlSystem.ProjectLevelVcsManager;
import consulo.versionControlSystem.VcsListener;
import consulo.versionControlSystem.VcsMappingListener;
import consulo.versionControlSystem.root.VcsRoot;
import git4idea.GitVcs;
import gitflow.ui.GitflowWidget;

import java.util.Optional;

/**
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 * One instance per project
 */
public class GitflowComponent implements VcsListener, Disposable {
    final Project myProject;
    GitflowWidget myGitflowWidget;
    final MessageBus messageBus;

    public GitflowComponent(Project project) {
        myProject = project;
        messageBus = myProject.getMessageBus();
        messageBus.connect().subscribe(VcsMappingListener.class, this);
        // Seems the event triggering this component happens after the directory mapping change
        directoryMappingChanged();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void directoryMappingChanged() {
        VcsRoot[] vcsRoots = ProjectLevelVcsManager.getInstance(myProject).getAllVcsRoots();
        if (vcsRoots.length > 0 && vcsRoots[0].getVcs() instanceof GitVcs) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            Optional<GitflowWidget> optional = statusBar.findWidget(it -> it instanceof GitflowWidget);
            optional.ifPresent(GitflowWidget::updateAsync);
        }
    }
}
