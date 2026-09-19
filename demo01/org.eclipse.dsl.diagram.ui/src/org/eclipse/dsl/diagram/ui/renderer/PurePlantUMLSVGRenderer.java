package org.eclipse.dsl.diagram.ui.renderer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
 
public class PurePlantUMLSVGRenderer {

    /**
     * Converts PlantUML source into raw SVG XML string.
     */
    public static String renderToSVG(String plantUmlSource) {
        if (plantUmlSource == null || plantUmlSource.isBlank()) {
            return "";
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantUmlSource);
            reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
            return os.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}