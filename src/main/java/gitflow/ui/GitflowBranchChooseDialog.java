package gitflow.ui;

import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;

/**
 * Dialog for choosing branches
 *
 * @author Opher Vishnia / opherv.com / opherv@gmail.com
 */

public class GitflowBranchChooseDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField searchField;
    //using a single column table because it has built in sorting and filtering capabilities.
    private JTable branchList;
    private JScrollPane scrollpane;
    private JPanel branchPanel;

    public GitflowBranchChooseDialog(Project project, List<String> branchNames) {
        super(project, true);

        setModal(true);

        setTitle("Choose Branch");
        initBranchList(branchNames);
        init();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (branchList.getSelectedRow() == -1) {
            return new ValidationInfo("No branch selected!");
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getSelectedBranchName() {
        int selectedRow = branchList.getSelectedRow();
        return (String) branchList.getValueAt(selectedRow, 0);
    }

    private void initBranchList(List<String> branchNames) {
        branchList.setTableHeader(null);
        DefaultTableModel model = (DefaultTableModel) branchList.getModel();
        //only one column. No Header.
        model.setColumnCount(1);
        for (String branchName : branchNames) {
            model.addRow(new String[]{branchName});
        }
        //sort on first (and only column)
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>();
        rowSorter.setModel(model);
        branchList.setRowSorter(rowSorter);
        branchList.getRowSorter().toggleSortOrder(0);
        //add filtering capabilities.
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent documentEvent) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent documentEvent) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent documentEvent) {
                        filter();
                    }

                    private void filter() {
                        String text = searchField.getText();
                        if (text.trim().length() == 0) {
                            rowSorter.setRowFilter(null);
                        } else {
                            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                        }
                    }
                }
        );
    }


}
