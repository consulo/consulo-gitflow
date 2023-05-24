package gitflow.ui;

import consulo.ide.impl.idea.openapi.ui.MessageDialogBuilder;
import consulo.platform.Platform;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.ui.ex.awt.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class UnsupportedVersionWidgetPresentation implements StatusBarWidget.TextPresentation {

	@NotNull
	@Override
	public String getText() {
		return "Unsupported Git Flow Version";
	}

	@Override
	public float getAlignment() {
		return 0;
	}

	@Nullable
	@Override
	public String getTooltipText() {
		return "Click for details";
	}

	@Nullable
	@Override
	public Consumer<MouseEvent> getClickConsumer() {
		return mouseEvent -> {
			MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo("Unsupported Git Flow version", "The Git Flow CLI version installed isn't supported by the Git Flow Integration plugin")
					.yesText("More information (open browser)")
					.noText("no");
			if (builder.show() == Messages.OK) {
				Platform.current().openInBrowser("https://github.com/OpherV/gitflow4idea/blob/develop/GITFLOW_VERSION.md");
			}
		};

	}
}
