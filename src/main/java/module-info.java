/**
 * @author VISTALL
 * @since 2023-05-02
 */
open module consulo.gitflow {
    requires consulo.ide.api;
    requires com.intellij.git;

    // TODO remove
    requires forms.rt;
    requires java.desktop;
}