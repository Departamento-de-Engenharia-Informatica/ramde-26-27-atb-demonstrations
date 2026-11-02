package org.xtext.example.fsmdsl.ui.diagram;

import org.eclipse.dsl.diagram.ui.api.IDslDiagramProvider;
import org.eclipse.emf.ecore.EObject;

import fsm.EMFToPlantUMLConverter;

public class FsmDiagramProvider implements IDslDiagramProvider {

    @Override
    public boolean isSupported(EObject rootModel) {
        return rootModel != null && "FSMModel".equals(rootModel.eClass().getName());
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
