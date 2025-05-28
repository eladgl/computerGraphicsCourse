//אלעד גולדנברג 315040519 //
//318400165 דביר חייט //
package your_code;

import java.io.IOException;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import app_interface.DisplayTypeEnum;
import app_interface.ExerciseEnum;
import app_interface.IntBufferWrapper;
import app_interface.OBJLoader;
import app_interface.TriangleFace;

public class ObjectModel {
	WorldModel worldModel;

	private int imageWidth;
	private int imageHeight;

	private List<VertexData> verticesData;
	private List<TriangleFace> faces;
	private IntBufferWrapper textureImageIntBufferWrapper;

	private Matrix4f modelM = new Matrix4f();
	private Matrix4f lookatM = new Matrix4f();
	private Matrix4f projectionM = new Matrix4f();
	private Matrix4f viewportM = new Matrix4f();
	private Vector3f boundingBoxDimensions;
	private Vector3f boundingBoxCenter;

	private Vector3f lightPositionEyeCoordinates = new Vector3f();
	
	public static ExerciseEnum exercise = ExerciseEnum.EX_9___Lighting;
	
	public ObjectModel(WorldModel worldModel, int imageWidth, int imageHeight) {
		this.worldModel = worldModel;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	void initTransfomations() {
		this.modelM.identity();
		this.modelM.identity();
		this.lookatM.identity();
		this.projectionM.identity();
		this.viewportM.identity();
	}
	
	void setModelM(Matrix4f modelM) {
		this.modelM = modelM;
	}

	void setLookatM(Matrix4f lookatM) {
		this.lookatM = lookatM;
	}

	void setProjectionM(Matrix4f projectionM) {
		this.projectionM = projectionM;
	}

	void setViewportM(Matrix4f viewportM) {
		this.viewportM = viewportM;
	}

	public Vector3f getBoundingBoxDimensions() {
		return boundingBoxDimensions;
	}

	public Vector3f getBoundingBoxCenter() {
		return boundingBoxCenter;
	}

	public boolean load(String fileName) {
		OBJLoader objLoader = new OBJLoader();
		try {
			objLoader.loadOBJ(fileName);
			verticesData = objLoader.getVertices();
			faces = objLoader.getFaces();
			boundingBoxDimensions = objLoader.getBoundingBoxDimensions();
			boundingBoxCenter = objLoader.getBoundingBoxCenter();
			textureImageIntBufferWrapper = objLoader.getTextureImageIntBufferWrapper();
			return true;
		} catch (IOException e) {
			//System.err.println("Failed to load the OBJ file.");
			return false;
		}
	}
	
	public boolean objectHasTexture() {
		return textureImageIntBufferWrapper != null;
	}

	public void render(IntBufferWrapper intBufferWrapper) { //render
		exercise = worldModel.exercise;

		
		if (verticesData != null) {
			for (VertexData vertexData : verticesData) {
				vertexProcessing(intBufferWrapper, vertexData);
			}
			for (TriangleFace face : faces) {
				rasterization(intBufferWrapper,	
						verticesData.get(face.indices[0]), 
						verticesData.get(face.indices[1]), 
						verticesData.get(face.indices[2]), 
						face.color);
			}
		}
	}

	private void vertexProcessing(IntBufferWrapper intBufferWrapper, VertexData vertex) {


			// Initialize a 4D vector from the 3D vertex point
			Vector4f t = new Vector4f(vertex.pointObjectCoordinates, 1f);
	
			// Transform only model transformation
			modelM.transform(t);
			lookatM.transform(t);
			vertex.pointEyeCoordinates = new Vector3f(t.x, t.y, t.z);
			projectionM.transform(t);
			if (t.w != 0f) {
		        t.mul(1.0f / t.w);
		    }
			viewportM.transform(t);
			vertex.pointWindowCoordinates = new Vector3f(t.x, t.y, t.z);

		// transformation normal from object coordinates to eye coordinates v->normal
		///////////////////////////////////////////////////////////////////////////////////
		transformNormalFromObjectCoordToEyeCoordAndDrawIt(intBufferWrapper, vertex);


	}

	private void transformNormalFromObjectCoordToEyeCoordAndDrawIt(IntBufferWrapper intBufferWrapper, VertexData vertex) {
		// transformation normal from object coordinates to eye coordinates v->normal
		///////////////////////////////////////////////////////////////////////////////////
		// --> v->NormalEyeCoordinates
		Matrix4f modelviewM = new Matrix4f(lookatM).mul(modelM);
		Matrix3f modelviewM3x3 = new Matrix3f();
		modelviewM.get3x3(modelviewM3x3);
		vertex.normalEyeCoordinates = new Vector3f();
		modelviewM3x3.transform(vertex.normalObjectCoordinates, vertex.normalEyeCoordinates);
		if (worldModel.displayNormals) {
			// drawing normals
			Vector3f t1 = new Vector3f(vertex.normalEyeCoordinates);
			Vector4f point_plusNormal_eyeCoordinates = new Vector4f(t1.mul(0.1f).add(vertex.pointEyeCoordinates),
					1);
			Vector4f t2 = new Vector4f(point_plusNormal_eyeCoordinates);
			// modelviewM.transform(t2);
			projectionM.transform(t2);
			if (t2.w != 0) {
				t2.mul(1 / t2.w);
			} else {
				System.err.println("Division by w == 0 in vertexProcessing normal transformation");
			}
			viewportM.transform(t2);
			Vector3f point_plusNormal_screen = new Vector3f(t2.x, t2.y, t2.z);
			drawLineDDA(intBufferWrapper, vertex.pointWindowCoordinates, point_plusNormal_screen, 0, 0, 1f);
		}
		
	}
	
	
	private void rasterization(IntBufferWrapper intBufferWrapper, VertexData vertex1, VertexData vertex2, VertexData vertex3, Vector3f faceColor) {

		Vector3f faceNormal = new Vector3f(vertex2.pointEyeCoordinates).sub(vertex1.pointEyeCoordinates)
					.cross(new Vector3f(vertex3.pointEyeCoordinates).sub(vertex1.pointEyeCoordinates))
					.normalize();

		if (worldModel.displayType == DisplayTypeEnum.FACE_EDGES) {
			drawLineDDA(intBufferWrapper, vertex1.pointWindowCoordinates, vertex2.pointWindowCoordinates, 1f, 1f, 1f);
			drawLineDDA(intBufferWrapper, vertex1.pointWindowCoordinates, vertex3.pointWindowCoordinates, 1f, 1f, 1f);
			drawLineDDA(intBufferWrapper, vertex2.pointWindowCoordinates, vertex3.pointWindowCoordinates, 1f, 1f, 1f);

		} else {
			BarycentricCoordinates baycentricCoordinates = new BarycentricCoordinates(vertex1.pointWindowCoordinates, vertex2.pointWindowCoordinates, vertex3.pointWindowCoordinates);
			Vector4i boundingBox = calcBoundingBox(vertex1.pointWindowCoordinates, vertex2.pointWindowCoordinates, vertex3.pointWindowCoordinates, imageWidth, imageHeight);
			for (int x = boundingBox.get(0); x <= boundingBox.get(1); x++) {
				for( int y = boundingBox.get(2); y <= boundingBox.get(3); y++) {
					baycentricCoordinates.calcCoordinatesForPoint(x, y);
					if (baycentricCoordinates.isPointInside()) {
						FragmentData fragmentData = new FragmentData(); 
						if (worldModel.displayType == DisplayTypeEnum.FACE_COLOR) { 
							fragmentData.pixelColor = faceColor; 
						} 
						else if (worldModel.displayType == DisplayTypeEnum.INTERPOlATED_VERTEX_COLOR) { 
							fragmentData.pixelColor = baycentricCoordinates.interpolate(vertex1.color, vertex2.color, vertex3.color);
						} 
						else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_FLAT) { 
							
						} 
						else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_GOURARD) { 
							
						} 
						else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_PHONG) { 
						} 
						else if (worldModel.displayType == DisplayTypeEnum.TEXTURE) { 
							
						} 
						else if (worldModel.displayType == DisplayTypeEnum.TEXTURE_LIGHTING) { 
							
						} 
						Vector3f pixelColor = fragmentProcessing(fragmentData); 
						intBufferWrapper.setPixel((int)x, (int)y, pixelColor); 
					}
				}
			}
		}
		
	}


	private Vector3f fragmentProcessing(FragmentData fragmentData) {
		
		if (worldModel.displayType == DisplayTypeEnum.FACE_COLOR) {
			return fragmentData.pixelColor;
		} else if (worldModel.displayType == DisplayTypeEnum.INTERPOlATED_VERTEX_COLOR) {
			return fragmentData.pixelColor;
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_FLAT) {
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_GOURARD) {
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_PHONG) {
		} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE) {
		} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE_LIGHTING) {
		}
		return new Vector3f();
		
	}

	

	static void drawLineDDA(IntBufferWrapper intBufferWrapper, Vector3f p1, Vector3f p2, float r, float g, float b) {
		int x1round = Math.round(p1.x);
		int y1round = Math.round(p1.y);
		int x2round = Math.round(p2.x);
		int y2round = Math.round(p2.y);
		
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		float a = dx/dy;;

		if((dy < -dx) || (dy == -dx && dx < 0)) {
			x1round = x1round + x2round;
			x2round = x1round - x2round;
			x1round = x1round - x2round;
			
			y1round = y1round + y2round;
			y2round = y1round - y2round;
			y1round = y1round - y2round;
			
			dx = -dx;
			dy = -dy;
		}
		
		if(Math.abs(dy) <= Math.abs(dx)){
			a = dy / dx;
			float y = y1round;
			for(int t = x1round; t <= x2round; t++) {
				intBufferWrapper.setPixel(t, Math.round(y), r, g, b);
				y += a;
				
			}
		} else {
			float x = x1round;
			for(int t = y1round; t <= y2round; t++) {
				intBufferWrapper.setPixel(Math.round(x), t, r, g, b);
				x += a;
			}
		}
	}



	static Vector4i calcBoundingBox(Vector3f p1, Vector3f p2, Vector3f p3, int imageWidth, int imageHeight) { 
		int minX = (int)Math.floor(Math.max(0, 
				Math.min(p1.x, 
						Math.min(p2.x,p3.x))
				));
		int minY = (int)Math.floor(Math.max(0, 
				Math.min(p1.y, 
						Math.min(p2.y,p3.y))
				));
		int maxX = (int)Math.ceil(Math.min(imageWidth - 1, 
				Math.max(p1.x,
						Math.max(p2.x,  p3.x))
				));
		int maxY = (int)Math.ceil(Math.min(imageWidth - 1, 
				Math.max(p1.y,
						Math.max(p2.y,  p3.y))
				));
		return new Vector4i(minX, maxX, minY, maxY);
	}

	
	float lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, float Kd, float Ks, float Ka, float shininess) {

		Vector3f color = lightingEquation(point, PointNormal, LightPos, 
				                          new Vector3f(Kd), new Vector3f(Ks), new Vector3f(Ka), shininess);
		return color.get(0);
	}
	
	
	private static Vector3f lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, Vector3f Kd,
			Vector3f Ks, Vector3f Ka, float shininess) {

		Vector3f returnedColor = new Vector3f();


		return returnedColor;
	}	
}


