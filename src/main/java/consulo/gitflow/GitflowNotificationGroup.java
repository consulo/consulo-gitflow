package consulo.gitflow;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;
import gitflow.ui.NotifyUtil;
import jakarta.annotation.Nonnull;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 24/05/2023
 */
@ExtensionImpl
public class GitflowNotificationGroup implements NotificationGroupContributor {
    @Override
    public void contribute(@Nonnull Consumer<NotificationGroup> consumer) {
        consumer.accept(NotifyUtil.BALLOON_NOTIFICATION);
        consumer.accept(NotifyUtil.STICKY_NOTIFICATION);
        consumer.accept(NotifyUtil.TOOLWINDOW_NOTIFICATION);
    }
}
