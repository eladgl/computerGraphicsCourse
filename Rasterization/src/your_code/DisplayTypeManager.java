package your_code;

import java.util.EnumMap;
import java.util.Map;

import app_interface.DisplayTypeEnum;
import app_interface.IntBufferWrapper;

import org.joml.Vector3f;

public class DisplayTypeManager {

    private static final Map<DisplayTypeEnum, DisplayTypeShader> displayTypeMap =
            new EnumMap<>(DisplayTypeEnum.class);

    static {
    	
    	displayTypeMap.put(DisplayTypeEnum.FACE_EDGES, context -> {
    		IntBufferWrapper intBufferWrapper = context.intBufferWrapper;
    		VertexData v1 = context.v1, v2 = context.v2, v3 = context.v3;
    		ObjectModel.drawLineDDA(intBufferWrapper, v1.pointWindowCoordinates, v2.pointWindowCoordinates, 1f, 1f, 1f);
    		ObjectModel.drawLineDDA(intBufferWrapper, v1.pointWindowCoordinates, v3.pointWindowCoordinates, 1f, 1f, 1f);
    		ObjectModel.drawLineDDA(intBufferWrapper, v2.pointWindowCoordinates, v3.pointWindowCoordinates, 1f, 1f, 1f);
    	});
    	
        displayTypeMap.put(DisplayTypeEnum.FACE_COLOR, context -> {
            context.fragmentData.pixelColor = context.faceColor;
        });

        displayTypeMap.put(DisplayTypeEnum.INTERPOlATED_VERTEX_COLOR, context -> {
            Vector3f c1 = context.v1.color;
            Vector3f c2 = context.v2.color;
            Vector3f c3 = context.v3.color;

            context.fragmentData.pixelColor = new Vector3f(
                context.barycentricCoordinates.interpolate(c1.x, c2.x, c3.x),
                context.barycentricCoordinates.interpolate(c1.y, c2.y, c3.y),
                context.barycentricCoordinates.interpolate(c1.z, c2.z, c3.z)
            );
        });

        displayTypeMap.put(DisplayTypeEnum.LIGHTING_FLAT, context -> {
            // Add your logic here
        });

        displayTypeMap.put(DisplayTypeEnum.LIGHTING_GOURARD, context -> {
            // Add your logic here
        });

        displayTypeMap.put(DisplayTypeEnum.LIGHTING_PHONG, context -> {
            // Add your logic here
        });

        displayTypeMap.put(DisplayTypeEnum.TEXTURE, context -> {
            // Add your logic here
        });

        displayTypeMap.put(DisplayTypeEnum.TEXTURE_LIGHTING, context -> {
            // Add your logic here
        });
    }

    public static void applyDisplay(ContextObject context) {
        DisplayTypeEnum type = context.worldModel.displayType;
        DisplayTypeShader shader = displayTypeMap.get(type);
        if (shader != null) {
            shader.apply(context);
        } else {
            throw new IllegalArgumentException("Unknown DisplayType: " + type);
        }
    }
}
