//אלעד גולדנברג 315040519 //
//318400165 דביר חייט //
package your_code;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import app_interface.DisplayTypeEnum;
import app_interface.ExerciseEnum;
import app_interface.IntBufferWrapper;
import app_interface.ProjectionTypeEnum;

public class WorldModel {

	// type of rendering
	public ProjectionTypeEnum projectionType;
	public DisplayTypeEnum displayType;
	public boolean displayNormals;
	public YourSelectionEnum yourSelection;
	
	// camera location parameters
	public Vector3f cameraPos = new Vector3f();
	public Vector3f cameraLookAtCenter = new Vector3f();
	public Vector3f cameraUp = new Vector3f();
	public float horizontalFOV;

	// transformation parameters
	public float modelScale;

	// lighting parameters
	public float lighting_Diffuse;
	public float lighting_Specular;
	public float lighting_Ambient;
	public float lighting_sHininess;
	public Vector3f lightPositionWorldCoordinates = new Vector3f();
	
	public ExerciseEnum exercise;

	private int imageWidth;
	private int imageHeight;

	private ObjectModel object1;
	
	float zBuffer[][];
	
	private int counter = 0;
	
	ErrorLogger errorLogger;
	
	float tx, ty;
	
	public WorldModel(int imageWidth, int imageHeight, ErrorLogger errorLogger) {
		this.tx = 0;
		this.ty = 0;
		this.imageWidth  = imageWidth;
		this.imageHeight = imageHeight;
		this.zBuffer = new float[imageWidth][imageHeight];
		this.errorLogger = errorLogger;
	}


	public boolean load(String fileName) {
		object1 = new ObjectModel(this, imageWidth, imageHeight);
		return object1.load(fileName);
	}
	
	public boolean modelHasTexture() {
		return object1.objectHasTexture();
	}
	
	
	public void render(IntBufferWrapper intBufferWrapper) {
		counter+=1;
		intBufferWrapper.imageClear();
		clearZbuffer();
		object1.initTransfomations();

		if (exercise.ordinal() == ExerciseEnum.EX_3_1_Object_transformation___translation.ordinal()) {
			Random random = new Random();
			this.tx += -5 + random.nextFloat() * (5 - (-5));
			this.ty += -5 + random.nextFloat() * (5 - (-5));
			Matrix4f randomTranslation = new Matrix4f().translate(this.tx, this.ty, 0.0F);
			object1.setModelM(randomTranslation);
		}
	
		if (exercise.ordinal() == ExerciseEnum.EX_3_2_Object_transformation___scale.ordinal()) {
			float oscilatedScale = 1.0F + 0.1F * (float)Math.sin((float)Math.PI / 2 * counter /10.0F);
			Matrix4f translationMatrix = new Matrix4f().translate(new Vector3f(300,300,0)).scale(oscilatedScale).translate(new Vector3f(-300,-300,0));
		    object1.setModelM(translationMatrix);
		}

		if (exercise.ordinal() == ExerciseEnum.EX_3_3_Object_transformation___4_objects.ordinal()) {
			Vector3f originOfAxes = new Vector3f(-300, -300, 0);
			List.of(
				    new Vector3f(450, 450, 0),
				    new Vector3f(150, 450, 0),
				    new Vector3f(150, 150, 0),
				    new Vector3f(450, 150, 0)
			).forEach((corner) -> {
				Matrix4f translationMatrix = new Matrix4f()
						.translate(corner)
						.scale(0.5F)
						.translate(originOfAxes);
				object1.setModelM(translationMatrix);
                object1.render(intBufferWrapper);
			});
		}


			if(projectionType==ProjectionTypeEnum.ORTHOGRAPHIC) {


			}

			
			if(projectionType==ProjectionTypeEnum.PERSPECTIVE) {


			}
			
		
		object1.render(intBufferWrapper);
	}
	
	private void clearZbuffer() {
		for(int i=0; i<imageHeight; i++)
			for(int j=0; j<imageWidth; j++)
				zBuffer[i][j] = 1;
	}	
}
