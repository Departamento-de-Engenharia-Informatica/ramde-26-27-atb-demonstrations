package org.eclipse.dsl.diagram.ui.api;

import org.eclipse.emf.ecore.EObject;

public interface IDslDiagramProvider {

    /**
     * Checks if this provider handles the given root EMF EObject.
     */
    boolean isSupported(EObject rootModel);

    /**
     * Converts the EMF model instance into a PlantUML string.
     */
    String generatePlantUMLText(EObject rootModel);

    /**
     * (Optional) Finds the target EObject corresponding to a clicked node ID in the diagram.
     */
    EObject findTargetElement(EObject rootModel, String nodeId);
}