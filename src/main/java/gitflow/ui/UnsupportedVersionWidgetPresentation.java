package gitflow.ui;

import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.ui.ex.awt.MessageDialogBuilder;
import consulo.ui.ex.awt.Messages;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class UnsupportedVersionWidgetPresentation implements StatusBarWidget.TextPresentation {
    @Nonnull
    @Override
    public String getText() {
        return "Unsupported Git Flow Version";
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Nonnull
    @Override
    public LocalizeValue getTooltipText() {
        return LocalizeValue.localizeTODO("Click for details");
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
            MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo(
                    "Unsupported Git Flow version",
                    "The Git Flow CLI version installed isn't supported by the Git Flow Integration plugin"
                )
                .yesText("More information (open browser)")
                .noText("no");
            if (builder.show() == Messages.OK) {
                Platform.current().openInBrowser("https://github.com/OpherV/gitflow4idea/blob/develop/GITFLOW_VERSION.md");
            }
        };
    }
}
