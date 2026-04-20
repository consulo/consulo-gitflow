package gitflow;

import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.util.GitVcsConsoleWriter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class GitInitLineHandler extends GitLineHandler {
    private final GitVcsConsoleWriter consoleWriter;

    private BufferedWriter writer;
    final GitflowInitOptions myInitOptions;

    public GitInitLineHandler(
        GitflowInitOptions initOptions,
        @Nonnull Project project,
        @Nonnull VirtualFile vcsRoot,
        @Nonnull GitCommand command
    ) {
        super(project, vcsRoot, command);
        consoleWriter = GitVcsConsoleWriter.getInstance(project);
        myInitOptions = initOptions;
    }

//    @Override
//    protected OSProcessHandler createProcess(@Nonnull GeneralCommandLine commandLine) throws ExecutionException {
//        return new MyOSProcessHandler(commandLine,
//                this.myWithMediator && Registry
//                        .is("git.execute.with.mediator"));
//    }

    @Nullable
    @Override
    protected Process startProcess() throws ExecutionException {
        Process p = super.startProcess();
        writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        return p;
    }

    @Override
    protected void onTextAvailable(String s, Key key) {
        super.onTextAvailable(s, key);
        try {
            if (s.contains("name for production releases")) {
                consoleWriter.showCommandLine(myInitOptions.getProductionBranch());

                writer.write(myInitOptions.getProductionBranch());
                writer.write("\n");
                writer.flush();
            }

            if (s.contains("name for \"next release\"")) {
                consoleWriter.showCommandLine(myInitOptions.getDevelopmentBranch());

                writer.write(myInitOptions.getDevelopmentBranch());
                writer.write("\n");
                writer.flush();
            }

            if (s.contains("Feature branches")) {
                consoleWriter.showCommandLine(myInitOptions.getFeaturePrefix());

                writer.write(myInitOptions.getFeaturePrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Bugfix branches")) {
                consoleWriter.showCommandLine(myInitOptions.getBugfixPrefix());

                writer.write(myInitOptions.getBugfixPrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Release branches")) {
                consoleWriter.showCommandLine(myInitOptions.getReleasePrefix());

                writer.write(myInitOptions.getReleasePrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Hotfix branches")) {
                consoleWriter.showCommandLine(myInitOptions.getHotfixPrefix());

                writer.write(myInitOptions.getHotfixPrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Support branches")) {
                consoleWriter.showCommandLine(myInitOptions.getSupportPrefix());

                writer.write(myInitOptions.getSupportPrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Version tag")) {
                consoleWriter.showCommandLine(myInitOptions.getVersionPrefix());

                writer.write(myInitOptions.getVersionPrefix());
                writer.write("\n");
                writer.flush();
            }
            if (s.contains("Hooks and filters")) {
                writer.write("\n");
                writer.flush();
            }


        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

//    class MyOSProcessHandler extends GitTextHandler.MyOSProcessHandler {
//        MyOSProcessHandler(@Nonnull GeneralCommandLine commandLine,
//                boolean withMediator) throws ExecutionException {
//            super(commandLine, withMediator);
//        }
//    }
}
