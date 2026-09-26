package org.xtext.example.ucdsl.ui.diagram;

import org.eclipse.dsl.diagram.ui.api.IDslDiagramProvider;
import org.eclipse.emf.ecore.EObject;

import uc.EMFToPlantUMLConverter;

public class RequirementsDiagramProvider implements IDslDiagramProvider {

    @Override
    public boolean isSupported(EObject rootModel) {
        return rootModel != null && "UCModel".equals(rootModel.eClass().getName());
    } 

    @Override
    public String generatePlantUMLText(EObject rootModel) {
        // Your PlantUML string generation logic
        return EMFToPlantUMLConverter.generatePlantUMLText(rootModel);
    }

    @Override
    public EObject findTargetElement(EObject rootModel, String nodeId) {
        // Finds target Requirement node by name for click navigation
        return EMFToPlantUMLConverter.findObjectByName(rootModel, nodeId);
    }
}