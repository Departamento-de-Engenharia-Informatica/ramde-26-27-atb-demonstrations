package ls;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import ls.LSModel;
import ls.Room;

public class EMFToPlantUMLConverter {

		public static String generatePlantUMLText(EObject lsModel) {
			StringBuilder sb = new StringBuilder();
			sb.append("@startuml\n");
			sb.append("left to right direction\n\n");

			// Retrieve Rooms from the model root
			LSModel reqModel = (LSModel) lsModel;
			List<Room> rooms = reqModel.getRooms();

			// 1. Declare Objects with Attributes
			for (Room room : rooms) {
				String name = room.getName();
				if (name != null) {
					//String description = req.getDescription();

					// Escape strings and format object node
					// [[name]] creates the clickable SVG hyperlink
					sb.append(String.format("object \"[[%s %s]]\" as %s {\n", name, name, sanitize(name)));
					//sb.append(String.format("  description = %s\n", description));
					sb.append("}\n");
					
					// Add lights
					for (Light l: room.getLights()) {
						String lName = l.getName();
						if (lName != null) {
							//String description = req.getDescription();

							// Escape strings and format object node
							// [[name]] creates the clickable SVG hyperlink
							sb.append(String.format("object \"<&sun> [[%s %s]]\" as %s {\n", lName, lName, sanitize(lName)));
							//sb.append(String.format("  description = %s\n", description));
							sb.append("}\n");
							
							sb.append(String.format("%s .> %s : light\n", name, lName));
						}
					}
					
					// Add lightswitches
					for (LightSwitch ls: room.getLightswitchs()) {
						String lsName = ls.getName();
						if (lsName != null) {
							//String description = req.getDescription();

							// Escape strings and format object node
							// [[name]] creates the clickable SVG hyperlink
							sb.append(String.format("object \"<&power-standby> [[%s %s]]\" as %s {\n", lsName, lsName, sanitize(lsName)));
							//sb.append(String.format("  description = %s\n", description));
							sb.append("}\n");
							
							sb.append(String.format("%s .> %s : switch\n", name, lsName));
							
							// Connect switchs and lights
							for (Light l2: ls.getLights()) {
								String l2Name = l2.getName();
								if (l2Name != null) {
									sb.append(String.format("%s .> %s : light\n", lsName, l2Name));
								}
							}
						}
					}
				}
			} 

			sb.append("\n");
			
			

			// 2. Declare Dependency Relationships
//			for (Requirement req : requirements) {
//				String sourceName = req.getName();
//				List<Requirement> dependencies = req.getDependsOn();
//
//				if (dependencies != null) {
//					for (Requirement dep : dependencies) {
//						if (dep.getName() != null) {
//							String targetName = sanitize((String) dep.getName());
//							sb.append(String.format("%s .> %s : dependsOn\n", sourceName, targetName));
//						}
//					}
//				}
//			}

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
