package your_code;

import org.joml.Vector3f;

import app_interface.IntBufferWrapper;

public class ContextObject {
	public FragmentData fragmentData;
	public WorldModel worldModel;
	
	public VertexData v1, v2, v3;
	public BarycentricCoordinates barycentricCoordinates;
	
	public Vector3f faceColor;
	
	IntBufferWrapper intBufferWrapper;
	
	public ContextObject(WorldModel worldModel, VertexData v1, VertexData v2, VertexData v3) {
		this.worldModel = worldModel;
		this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
	}
	
	public ContextObject(FragmentData fragmentData, 
			WorldModel worldModel, 
			VertexData v1, 
			VertexData v2, 
			VertexData v3, 
			BarycentricCoordinates barycentricCoordinates, 
			Vector3f faceColor) {
		this(worldModel, v1, v2, v3);
		this.fragmentData = fragmentData;
        this.barycentricCoordinates = barycentricCoordinates;
        this.faceColor = faceColor;
	}
	
	public ContextObject(
			WorldModel worldModel, 
			IntBufferWrapper intBufferWrapper, 
			VertexData v1, 
			VertexData v2,
			VertexData v3) {
		this(worldModel, v1, v2, v3);
		this.intBufferWrapper = intBufferWrapper;
	}
}
