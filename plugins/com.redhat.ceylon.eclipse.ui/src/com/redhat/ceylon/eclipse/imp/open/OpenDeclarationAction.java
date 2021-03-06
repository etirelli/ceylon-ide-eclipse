package com.redhat.ceylon.eclipse.imp.open;

import static com.redhat.ceylon.eclipse.imp.core.CeylonReferenceResolver.getCompilationUnit;
import static com.redhat.ceylon.eclipse.imp.core.CeylonReferenceResolver.getReferencedNode;
import static com.redhat.ceylon.eclipse.imp.parser.CeylonSourcePositionLocator.gotoNode;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.eclipse.imp.builder.CeylonBuilder;
import com.redhat.ceylon.eclipse.imp.editor.CeylonEditor;
import com.redhat.ceylon.eclipse.imp.editor.Util;
import com.redhat.ceylon.eclipse.imp.parser.CeylonParseController;
import com.redhat.ceylon.eclipse.ui.CeylonPlugin;
import com.redhat.ceylon.eclipse.ui.ICeylonResources;

public class OpenDeclarationAction extends Action {
    private final IEditorPart editor;
    
    public OpenDeclarationAction(IEditorPart editor) {
        this("Open Ceylon Declaration...", editor);
    }
    
    public OpenDeclarationAction(String text, IEditorPart editor) {
        super(text);
        this.editor = editor;
        setActionDefinitionId("com.redhat.ceylon.eclipse.ui.action.openDeclaration");
        setImageDescriptor(CeylonPlugin.getInstance().getImageRegistry()
                .getDescriptor(ICeylonResources.CEYLON_OPEN_DECLARATION));
    }
    
    @Override
    public void run() {
        Shell shell = CeylonPlugin.getInstance().getWorkbench()
                .getActiveWorkbenchWindow().getShell();
        OpenCeylonDeclarationDialog dialog = new OpenCeylonDeclarationDialog(shell, editor);
        dialog.setTitle("Open Ceylon Declaration");
        dialog.setMessage("Select a Ceylon declaration to open:");
        if (editor instanceof ITextEditor) {
            dialog.setInitialPattern(Util.getSelectionText((ITextEditor) editor));
        }
        dialog.open();
        Object[] types = dialog.getResult();
        if (types != null && types.length > 0) {
            gotoDeclaration((DeclarationWithProject) types[0]);
        }
    }

    public void gotoDeclaration(DeclarationWithProject dwp) {
        IProject project = dwp.getProject();
        Declaration dec = dwp.getDeclaration();
        if (editor instanceof CeylonEditor) {
            CeylonEditor ce = (CeylonEditor) editor;
            ISourceProject ep = ce.getParseController().getProject();
            if (ep!=null && ep.equals(project)) {
                CeylonParseController cpc = ce.getParseController();
                Tree.Declaration node = getReferencedNode(dec, getCompilationUnit(cpc, dec));
                if (node!=null) {
                    gotoNode(node, cpc.getTypeChecker());
                    return;
                }
            }
        }
        Tree.Declaration node = getReferencedNode(dec, getCompilationUnit(project, dec));
        if (node!=null) {
            gotoNode(node, CeylonBuilder.getProjectTypeChecker(project));
        }
    }

}