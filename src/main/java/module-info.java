/**
 * @author VISTALL
 * @since 05/02/2023
 */
open module consulo.gitflow {
    requires consulo.application.api;
    requires consulo.application.ui.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.datacontext.api;
    requires consulo.disposer.api;
    requires consulo.file.editor.api;
    requires consulo.ide.api;
    requires consulo.language.editor.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.project.ui.api;
    requires consulo.task.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.util.dataholder;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.version.control.system.api;
    requires consulo.version.control.system.distributed.api;
    requires consulo.virtual.file.system.api;

    requires com.intellij.git;

    // TODO remove
    requires forms.rt;
    requires java.desktop;
}
