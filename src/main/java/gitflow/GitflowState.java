package gitflow;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.task.Task;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Persistent state for Gitflow component
 * Created by opherv on 8/17/16.
 */

@State(
        name = "GitflowState", storages = {
        @Storage(
                file = "$APP_CONFIG$/GitflowState.xml")
})
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@Singleton
public class GitflowState implements PersistentStateComponent<GitflowState> {

    private HashMap<String, String> taskBranches;


    public GitflowState() {
        taskBranches = new HashMap<String, String>();
    }


    public HashMap<String, String> getTaskBranches() {
        return taskBranches;
    }

    public void setTaskBranches(HashMap<String, String> taskBranches) {
        this.taskBranches = taskBranches;
    }

    @Nullable
    @Override
    public GitflowState getState() {
        return this;
    }

    @Override
    public void loadState(GitflowState state) {
        XmlSerializerUtil.copyBean(state, this);

    }

    public String getTaskBranch(Task task) {
        return taskBranches.get(task.getId());
    }


    public void setTaskBranch(Task task, String branchName) {
        taskBranches.put(task.getId(), branchName);
    }
}
