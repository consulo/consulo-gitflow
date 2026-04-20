package gitflow.ui;

import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.ValidationInfo;
import git4idea.repo.GitRepository;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Base class for a "start" dialog. Such a dialog prompts the user to enter a name for a new branch
 * and select a base branch. See {@link GitflowStartFeatureDialog} for an example implementation.
 */
public abstract class AbstractBranchStartDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField branchNameTextField;
    private JComboBox branchFromCombo;
    private JLabel branchNameLabel;
    private JLabel spacesLabel;
    private JLabel branchFromTitle;

    private final Project project;
    protected final GitRepository myRepo;
    private final GitflowBranchUtil myGitflowBranchUtil;

    public AbstractBranchStartDialog(Project project, GitRepository repo) {
        super(project, false);
        this.project = project;
        this.myRepo = repo;
        this.myGitflowBranchUtil = GitflowBranchUtilManager.getBranchUtil(repo);

        init();
        String label = getLabel();
        setTitle("New " + label + "...");
        branchNameLabel.setText(String.format("Enter a name for the new %s...", label));
        setModal(true);

        //set the base branch combo
        branchFromCombo.setModel(myGitflowBranchUtil.createBranchComboModel(getDefaultBranch()));

        branchNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateBranchName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateBranchName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateBranchName();
            }

            public void validateBranchName() {
                if (branchNameTextField.getText().contains(" ")) {
                    spacesLabel.setVisible(true);
                }
                else {
                    spacesLabel.setVisible(false);
                }

                if (branchNameTextField.getText().contains("&")) {
                    AbstractBranchStartDialog.super.setOKActionEnabled(false);
                }
                else {
                    AbstractBranchStartDialog.super.setOKActionEnabled(true);
                }
            }
        });

        if (!showBranchFromCombo()) {
            branchFromTitle.setVisible(false);
            branchFromCombo.setVisible(false);
        }
    }

    protected boolean showBranchFromCombo() {
        return true;
    }

    @Override
    @RequiredUIAccess
    public JComponent getPreferredFocusedComponent() {
        return branchNameTextField;
    }

    /**
     * @return The name of the new branch as specified by the user
     */
    public String getNewBranchName() {
        return branchNameTextField.getText().trim().replaceAll(" ", "_");
    }

    /**
     * @return The name of the base branch (the branch on which the new hotfix or feature should be
     * based on)
     */
    public String getBaseBranchName() {
        GitflowBranchUtil.ComboEntry selectedBranch = (GitflowBranchUtil.ComboEntry) branchFromCombo.getModel().getSelectedItem();
        return selectedBranch.getBranchName();
    }

    /**
     * @return The label for this dialog (e.g. "hotfix" or "feature"). Will be used for the window
     * title and other labels.
     */
    protected abstract String getLabel();

    /**
     * @return The name of the default branch, i.e. the branch that is selected by default when
     * opening the dialog.
     */
    protected abstract String getDefaultBranch();

    protected Project getProject() {
        return this.project;
    }

    @Override
    @RequiredUIAccess
    protected ValidationInfo doValidate() {
        boolean isBranchNameSpecified = branchNameTextField.getText().trim().length() > 0;
        if (!isBranchNameSpecified) {
            return new ValidationInfo("No name specified", branchNameTextField);
        }
        else {
            return null;
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
