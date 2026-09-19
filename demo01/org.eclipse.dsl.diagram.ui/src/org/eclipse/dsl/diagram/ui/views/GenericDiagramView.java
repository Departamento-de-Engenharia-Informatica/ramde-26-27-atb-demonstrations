package org.eclipse.dsl.diagram.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.dsl.diagram.ui.listeners.GenericDiagramListener;
import org.eclipse.dsl.diagram.ui.renderer.PurePlantUMLSVGRenderer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import net.sourceforge.plantuml.SourceStringReader;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationAdapter;
 
/**
 * Step 4: Custom Eclipse Diagram View (RequirementsDiagramView.java)
	Create a custom Eclipse view in org.example.requirements.ui.views to display the generated image:
 */
public class GenericDiagramView extends ViewPart {

    public static final String ID = "org.eclipse.dsl.diagram.ui.views.GenericDiagramView";
    
    private Browser browser;
    private String lastPlantUmlSource;
    private double zoomFactor = 1.0;

    @Override
    public void createPartControl(Composite parent) {
        // SWT Browser renders SVG vector graphs natively with crisp scaling & clickable links
        browser = new Browser(parent, SWT.NONE);

        // Intercept SVG hyperlink clicks to perform jump-to-code
        browser.addLocationListener(new LocationAdapter() {
            @Override
            public void changing(LocationEvent event) {
                String location = event.location;

                // Ignore blank or initial browser initialization URLs
                if (location != null && !location.equals("about:blank") && !location.startsWith("data:")) {
                    event.doit = false; // Cancel actual browser web navigation

                    // Extract the Node name/id from href URL
                    String nodeName = location;
                    if (nodeName.contains("/")) {
                        nodeName = nodeName.substring(nodeName.lastIndexOf('/') + 1);
                    }

                    // Jump directly to line in Xtext Editor
                    GenericDiagramListener.jumpToNode(nodeName);
                }
            }
        });
        
        // 2. Attach GenericDiagramListener to the active workbench page
        if (getSite() != null && getSite().getPage() != null) {
            getSite().getPage().addPartListener(GenericDiagramListener.INSTANCE);

            // 3. Force immediate check for any already open/active Xtext editor
            var activeRef = getSite().getWorkbenchWindow().getPartService().getActivePartReference();
            if (activeRef != null) {
                GenericDiagramListener.INSTANCE.partActivated(activeRef);
            }
        }

        contributeToToolBar();
    }
    
    @Override
    public void dispose() {
        // Clean up listener when the view closes
        if (getSite() != null && getSite().getPage() != null) {
            getSite().getPage().removePartListener(GenericDiagramListener.INSTANCE);
        }
        super.dispose();
    }

    public void updateDiagram(String plantUmlText) {
        if (browser == null || browser.isDisposed()) {
            return;
        }

        this.lastPlantUmlSource = plantUmlText;
        refreshSvgInBrowser();
    }

    private void refreshSvgInBrowser() {
        if (lastPlantUmlSource == null || lastPlantUmlSource.isBlank()) {
            return;
        }

        // Render PlantUML source to SVG string
        String svgContent = PurePlantUMLSVGRenderer.renderToSVG(lastPlantUmlSource);

        // Wrap SVG with CSS transform scale to handle zooming
        String htmlContent = "<html><head><style>"
                + "body { margin: 0; padding: 20px; overflow: auto; background-color: #ffffff; }"
                + "#container { display: flex; justify-content: center; align-items: center; transform-origin: top center; transform: scale(" + zoomFactor + "); }"
                + "svg { height: auto; }"
                + "</style></head><body>"
                + "<div id='container'>" + svgContent + "</div>"
                + "</body></html>";

        browser.setText(htmlContent);
    }

    private void contributeToToolBar() {
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBar = actionBars.getToolBarManager();

        // Zoom In Action
        Action zoomInAction = new Action("Zoom In") {
            @Override
            public void run() {
                if (zoomFactor < 4.0) {
                    zoomFactor += 0.2;
                    refreshSvgInBrowser();
                }
            }
        };
        zoomInAction.setToolTipText("Zoom In (+20%)");

        // Zoom Out Action
        Action zoomOutAction = new Action("Zoom Out") {
            @Override
            public void run() {
                if (zoomFactor > 0.3) {
                    zoomFactor -= 0.2;
                    refreshSvgInBrowser();
                }
            }
        };
        zoomOutAction.setToolTipText("Zoom Out (-20%)");

        // Reset Zoom Action
        Action resetZoomAction = new Action("100%") {
            @Override
            public void run() {
                zoomFactor = 1.0;
                refreshSvgInBrowser();
            }
        };
        resetZoomAction.setToolTipText("Reset Zoom to 100%");

        // Export PNG Action
        Action exportPngAction = new Action("Export to PNG") {
            @Override
            public void run() {
                exportToPNG();
            }
        };
        exportPngAction.setToolTipText("Save diagram as PNG file");

        // Add actions to view toolbar
        toolBar.add(zoomInAction);
        toolBar.add(zoomOutAction);
        toolBar.add(resetZoomAction);
        toolBar.add(exportPngAction);
    }

    private void exportToPNG() {
        if (lastPlantUmlSource == null || lastPlantUmlSource.isBlank()) {
            MessageDialog.openWarning(getSite().getShell(), "Export Failed", "No active diagram to export.");
            return;
        }

        FileDialog fileDialog = new FileDialog(getSite().getShell(), SWT.SAVE);
        fileDialog.setFilterExtensions(new String[] { "*.png" });
        fileDialog.setFileName("dsl_diagram.png");
        String filePath = fileDialog.open();

        if (filePath != null) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                SourceStringReader reader = new SourceStringReader(lastPlantUmlSource);
                reader.outputImage(os);

                try (ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { new ImageData(is) };
                    loader.save(filePath, SWT.IMAGE_PNG);
                }

                MessageDialog.openInformation(getSite().getShell(), "Export Successful", 
                    "Diagram saved to:\n" + filePath);

            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "Export Error", 
                    "Failed to save image: " + e.getMessage());
            }
        }
    }

    @Override
    public void setFocus() {
        if (browser != null && !browser.isDisposed()) {
            browser.setFocus();
        }
    }}