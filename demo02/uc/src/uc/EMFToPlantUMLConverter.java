package uc;

import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import req.Requirement;
import uc.UCModel;
import uc.UseCase;

public class EMFToPlantUMLConverter {
	public static String generatePlantUMLText(EObject model) {
		StringBuilder sb = new StringBuilder();
		sb.append("@startuml\n");
		sb.append("left to right direction\n");
		sb.append("allowmixing\n\n");

		// Retrieve use cases from the model root
		UCModel ucModel = (UCModel) model;
		List<UseCase> usecases = ucModel.getUsecase();
		
		// Retrieve all referenced requirements
		HashSet<Requirement> reqs=new HashSet<Requirement>();
		for (UseCase uc : usecases) {
			Requirement satisfies = uc.getRefine();

			if (satisfies != null) {
				reqs.add(satisfies);
			}
		}
			
		// 1. Declare use cases 
		for (UseCase uc : usecases) {
			String name = uc.getName();
			if (name != null) {
				//String description = req.getDescription();

				// Escape strings and format object node
				// [[name]] creates the clickable SVG hyperlink
				sb.append(String.format("usecase \"%s\" as %s\n", name, sanitize(name)));
				//sb.append(String.format("  description = %s\n", description));
				//sb.append("}\n");
			}
		}
 
		sb.append("\n"); 
		
		// declare referenced requirements
		for (Requirement req: reqs) {
			sb.append(String.format("object %s\n", req.getName()));
		}
 

		// 2. Declare Satisfies Relationships
		for (UseCase uc : usecases) {
			String sourceName = uc.getName();
			Requirement satisfies = uc.getRefine();

			if (satisfies != null) {
					if (satisfies.getName() != null) {
						String targetName = sanitize((String) satisfies.getName());
						sb.append(String.format("%s ..> %s : refine\n", sourceName, targetName));
					}
				
			}
		}
		
		sb.append("@enduml");
		return sb.toString();
	}

	private static String sanitize(String id) {
		return id.replaceAll("[^a-zA-Z0-9_]", "_");
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
