package gitflow.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.project.ui.wm.StatusBarWidgetFactory;
import jakarta.annotation.Nonnull;

@ExtensionImpl(id = "gitflowWidget", order = "before gitWidget")
public class GitflowStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @Nonnull
    @Override
    public String getDisplayName() {
        return "GitFlow status bar";
    }

    @Override
    public boolean isAvailable(@Nonnull Project project) {
        return true;
    }

    @Nonnull
    @Override
    public StatusBarWidget createWidget(@Nonnull Project project) {
        return new GitflowWidget(project, this);
    }

    @Override
    public boolean canBeEnabledOn(@Nonnull StatusBar statusBar) {
        return true;
    }
}
