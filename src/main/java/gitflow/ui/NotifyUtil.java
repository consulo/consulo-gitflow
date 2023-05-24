package gitflow.ui;

import consulo.component.ComponentManager;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationDisplayType;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.wm.ToolWindowId;

public class NotifyUtil {
    private static final NotificationGroup TOOLWINDOW_NOTIFICATION = NotificationGroup.toolWindowGroup(
            "Gitflow Errors", ToolWindowId.VCS, true);
    private static final NotificationGroup STICKY_NOTIFICATION = new NotificationGroup(
            "Gitflow Errors", NotificationDisplayType.STICKY_BALLOON, true);
    private static final NotificationGroup BALLOON_NOTIFICATION = new NotificationGroup(
            "Gitflow Notifications", NotificationDisplayType.BALLOON, true);

    public static void notifySuccess(ComponentManager project, String title, String message) {
        notify(NotificationType.INFORMATION, BALLOON_NOTIFICATION, project, title, message);
    }

    public static void notifyInfo(ComponentManager project, String title, String message) {
        notify(NotificationType.INFORMATION, TOOLWINDOW_NOTIFICATION, project, title, message);
    }

    public static void notifyError(ComponentManager project, String title, String message) {
        notify(NotificationType.ERROR, TOOLWINDOW_NOTIFICATION, project, title, message);
    }

    public static void notifyError(ComponentManager project, String title, Exception exception) {
        notify(NotificationType.ERROR, STICKY_NOTIFICATION, project, title, exception.getMessage());
    }

    private static void notify(NotificationType type, NotificationGroup group, ComponentManager project, String title, String message) {
        group.createNotification(title, message, type, null).notify((Project) project);
    }
}
