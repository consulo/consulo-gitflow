package gitflow.actions;

import consulo.component.ComponentManager;
import consulo.util.dataholder.Key;
import git4idea.commands.GitLineHandlerListener;

import java.util.ArrayList;

//generic line handler (should handle errors etc)
public abstract class GitflowLineHandler implements GitLineHandlerListener {
    ArrayList<String> myErrors = new ArrayList<String>();
    ComponentManager myProject;

    @Override
    public void onLineAvailable(String line, Key outputType) {
        if (line.contains("fatal") || line.contains("Fatal")) {
            myErrors.add(line);
        }
    }

    @Override
    public void processTerminated(int exitCode) {
    }

    @Override
    public void startFailed(Throwable exception) {
    }
}