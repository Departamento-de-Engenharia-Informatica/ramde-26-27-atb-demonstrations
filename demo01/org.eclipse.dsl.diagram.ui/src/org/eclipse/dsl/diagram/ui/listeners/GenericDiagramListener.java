package org.eclipse.dsl.diagram.ui.listeners;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dsl.diagram.ui.api.IDslDiagramProvider;
import org.eclipse.dsl.diagram.ui.registry.DiagramProviderRegistry;
import org.eclipse.dsl.diagram.ui.views.GenericDiagramView;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;

public class GenericDiagramListener implements IPartListener2 {

    public static final GenericDiagramListener INSTANCE = new GenericDiagramListener();
    private static DiagramUpdateJob updateJob = new DiagramUpdateJob();

    private static class DiagramUpdateJob extends Job {
        private String plantUmlSource;

        public DiagramUpdateJob() {
            super("Updating DSL Diagram");
            setSystem(true);
        }

        public void setSource(String source) {
            this.plantUmlSource = source;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (plantUmlSource != null) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                    try {
                        var page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        GenericDiagramView view = (GenericDiagramView) page.findView(GenericDiagramView.ID);
                        if (view != null) {
                            view.updateDiagram(plantUmlSource);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            return Status.OK_STATUS;
        }
    }

    private static IDslDiagramProvider findMatchingProvider(EObject rootModel) {
        // Triggers the extension point lookup dynamically
        List<IDslDiagramProvider> providers = DiagramProviderRegistry.getProviders();
        for (IDslDiagramProvider provider : providers) {
            if (provider.isSupported(rootModel)) {
                return provider;
            }
        }
        return null;
    }

    public static void jumpToNode(String nodeId) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
            IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (activeEditor instanceof XtextEditor) {
                XtextEditor editor = (XtextEditor) activeEditor;
                editor.getDocument().readOnly((XtextResource resource) -> {
                    if (resource != null && !resource.getContents().isEmpty()) {
                        EObject root = resource.getContents().get(0);
                        IDslDiagramProvider provider = findMatchingProvider(root);
                        if (provider != null) {
                            EObject target = provider.findTargetElement(root, nodeId);
                            if (target != null) {
                                ICompositeNode node = NodeModelUtils.findActualNodeFor(target);
                                if (node != null) {
                                    PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
                                        editor.selectAndReveal(node.getOffset(), node.getLength());
                                    });
                                }
                            }
                        }
                    }
                    return null;
                });
            }
        });
    }

    private void attachListener(IWorkbenchPartReference partRef) {
        IEditorPart editor = partRef.getPage().getActiveEditor();
        if (editor instanceof XtextEditor) {
            XtextEditor xtextEditor = (XtextEditor) editor;
            if (xtextEditor.getDocument() != null) {
                // Listen for document edits
                xtextEditor.getDocument().addModelListener((XtextResource resource) -> {
                    if (resource != null && !resource.getContents().isEmpty()) {
                        EObject root = resource.getContents().get(0);
                        IDslDiagramProvider provider = findMatchingProvider(root);
                        if (provider != null) {
                            String source = provider.generatePlantUMLText(root);
                            updateJob.cancel();
                            updateJob.setSource(source);
                            updateJob.schedule(300);
                        }
                    }
                });

                // Trigger initial render immediately on file open/focus
                xtextEditor.getDocument().readOnly(resource -> {
                    if (resource != null && !resource.getContents().isEmpty()) {
                        EObject root = resource.getContents().get(0);
                        IDslDiagramProvider provider = findMatchingProvider(root);
                        if (provider != null) {
                            String source = provider.generatePlantUMLText(root);
                            updateJob.cancel();
                            updateJob.setSource(source);
                            updateJob.schedule(0);
                        }
                    }
                    return null;
                });
            }
        }
    }

    @Override public void partOpened(IWorkbenchPartReference partRef) { attachListener(partRef); }
    @Override public void partActivated(IWorkbenchPartReference partRef) { attachListener(partRef); }
    @Override public void partClosed(IWorkbenchPartReference partRef) {}
    @Override public void partDeactivated(IWorkbenchPartReference partRef) {}
    @Override public void partBroughtToTop(IWorkbenchPartReference partRef) {}
    @Override public void partHidden(IWorkbenchPartReference partRef) {}
    @Override public void partVisible(IWorkbenchPartReference partRef) {}
    @Override public void partInputChanged(IWorkbenchPartReference partRef) {}
}