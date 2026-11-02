package fsm;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class EMFToPlantUMLConverter {
	public static String generatePlantUMLText(EObject model) {
		FSMModel fsm = (FSMModel) model;
		if (fsm == null) {
            return "@startuml\n@enduml";
        }
 
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");

        // Optional title based on FSMModel name or target LightSwitch
        if (fsm.getName() != null && !fsm.getName().isEmpty()) {
            sb.append("title ").append(fsm.getName()).append(" State Machine\n\n");
        }
        if (fsm.getAppliesTo() != null) {
            sb.append("' Applies to LightSwitch: ").append(fsm.getAppliesTo()).append("\n\n");
        }

        // Map states to valid PlantUML identifiers (replacing spaces/special characters)
        Map<State, String> stateIds = new HashMap<>();
        int stateCounter = 1;

        // 1. Declare all states
        for (State state : fsm.getState()) {
            String rawName = state.getName() != null ? state.getName() : "State" + stateCounter;
            String safeId = sanitizeIdentifier(rawName, stateCounter++);
            stateIds.put(state, safeId);

            //sb.append("state \"").append(rawName).append("\" as ").append(safeId).append("\n");
			sb.append(String.format("state \"[[%s %s]]\" as %s\n", rawName, rawName, safeId));
        }

        sb.append("\n");
        
        // 2. Render Initial State Marker ([*] --> initialState)
        State initial = fsm.getInitialState();
        if (initial != null && stateIds.containsKey(initial)) {
            sb.append("[*] --> ").append(stateIds.get(initial)).append(" : start\n\n");
        }

        // 3. Declare all transitions between states
        for (Transition transition : fsm.getTransition()) {
            State source = transition.getSourceState();
            State target = transition.getTargetState();

            if (source != null && target != null) {
                String sourceId = stateIds.get(source);
                String targetId = stateIds.get(target);

                sb.append(sourceId).append(" --> ").append(targetId);

                // Add transition label (Transition Name + Event operation call)
                String label = buildTransitionLabel(transition);
                if (!label.isEmpty()) {
                    sb.append(" : ").append(label);
                }

                sb.append("\n");
            }
        }

        sb.append("\n@enduml");
        return sb.toString();
    }

    /**
     * Constructs a readable label showing the event/operation name.
     */
    private static String buildTransitionLabel(Transition transition) {
        StringBuilder label = new StringBuilder();

        // Include transition name if defined
        if (transition.getName() != null && !transition.getName().isEmpty()) {
            label.append(transition.getName());
        }

        // Include associated Event and its operationName
        Event event = transition.getEvent();
        if (event != null) {
            if (label.length() > 0) {
                label.append(" / ");
            }
            if (event.getOperationName() != null && !event.getOperationName().isEmpty()) {
                label.append(event.getOperationName()).append("()");
            } else if (event.getName() != null) {
                label.append(event.getName());
            }
        }

        return label.toString();
    }

    /**
     * Sanitizes strings for valid PlantUML state alias names.
     */
    private static String sanitizeIdentifier(String name, int fallbackId) {
        if (name == null || name.trim().isEmpty()) {
            return "S" + fallbackId;
        }
        String sanitized = name.replaceAll("[^a-zA-Z0-9_]", "_");
        return sanitized.matches("^[0-9].*") ? "S_" + sanitized : sanitized;
    }
    
	/**
	 * Recursively traverses an EMF model tree starting from the root element to
	 * find an EObject with a "name" attribute matching targetName.
	 * 
	 * @param root       The root EObject of the EMF model resource.
	 * @param targetName The name of the requirement to locate.
	 * @return The matching EObject requirement, or null if not found.
	 */
	public static EObject findObjectByName(EObject root, String targetName) {
		if (root == null || targetName == null || targetName.isBlank()) {
			return null;
		}

		// 1. Check if the current EObject itself matches targetName
		if (hasMatchingName(root, targetName)) {
			return root;
		}

		// 2. Iterate through all child contents recursively
		var iterator = root.eAllContents();
		while (iterator.hasNext()) {
			EObject element = iterator.next();
			if (hasMatchingName(element, targetName)) {
				return element;
			}
		}

		return null;
	}
	
	/**
	 * Helper method to check if an EObject has a "name" feature that matches the
	 * target.
	 */
	private static boolean hasMatchingName(EObject element, String targetName) {
		EClass eClass = element.eClass();

		// Find structural feature named "name"
		EStructuralFeature nameFeature = eClass.getEStructuralFeature("name");

		if (nameFeature instanceof EAttribute) {
			Object value = element.eGet(nameFeature);
			if (value instanceof String) {
				String actualName = (String) value;
				return targetName.equalsIgnoreCase(actualName);
			}
		}

		return false;
	}
}
