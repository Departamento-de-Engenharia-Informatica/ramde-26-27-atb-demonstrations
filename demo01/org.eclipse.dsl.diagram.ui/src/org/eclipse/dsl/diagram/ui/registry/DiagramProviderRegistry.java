package org.eclipse.dsl.diagram.ui.registry;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dsl.diagram.ui.api.IDslDiagramProvider;

public class DiagramProviderRegistry {

    private static final String EXTENSION_POINT_ID = "org.eclipse.dsl.diagram.ui.diagramProviders";
    private static List<IDslDiagramProvider> providers;

    public static synchronized List<IDslDiagramProvider> getProviders() {
        if (providers == null) {
            providers = new ArrayList<>();
            IConfigurationElement[] elements = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor(EXTENSION_POINT_ID);

            for (IConfigurationElement element : elements) {
                try {
                    // Instantiate the class specified in the plugin.xml dynamically
                    Object object = element.createExecutableExtension("class");
                    if (object instanceof IDslDiagramProvider) {
                        providers.add((IDslDiagramProvider) object);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return providers;
    }
}