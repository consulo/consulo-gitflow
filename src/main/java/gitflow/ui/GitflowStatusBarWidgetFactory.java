package gitflow.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.project.ui.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl(id = "gitflowWidget", order = "before gitWidget")
public class GitflowStatusBarWidgetFactory implements StatusBarWidgetFactory {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "GitFlow status bar";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return new GitflowWidget(project, this);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
