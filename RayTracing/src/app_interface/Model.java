package app_interface;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joml.Vector3f;

/**
 * This class represents a model object containing information about spheres,
 * lights, materials, skybox image, sphere texture filenames, and a comment.
 */
public class Model {
	public String comment; // comment

	public float fovXdegree; // Field of view
	public String skyBoxImageFileName; // Sky box image filename
	
	public List<ModelLight> lights = new ArrayList<>(); // List of lights

	public List<String> sphereTextureFileNames = new ArrayList<>(); // List of sphere texture filenames

	public List<ModelMaterial> materials = new ArrayList<>(); // List of materials

	public List<ModelSphere> spheres = new ArrayList<>(); // List of spheres


	
	public List<SphereTexture> skyBoxImageSphereTextures = new ArrayList<>();// List of sphere texture objects
	
	/**
	 * This constructor creates a Model object by parsing a string representation of
	 * the model.
	 * 
	 * @param modelFilename The filename of the model file.
	 * @throws IOException          If there is an error reading the model file.
	 * @throws FileParsingException If there is an error parsing the model file
	 *                              format.
	 */
	public Model(String modelFilename) throws IOException, FileParsingException {
		this(Files.readString(Paths.get(modelFilename)), false);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("comment: " + comment + "\n");
		sb.append("\n");
		sb.append("fovXdegree: " + fovXdegree + "\n");
		sb.append("skyBoxImageFileName: " + skyBoxImageFileName + "\n");
		sb.append("\n");
		for (ModelLight light : lights) {
			sb.append(light + "\n");
		}
		sb.append("\n");
		for (String sphereTextureFileName : sphereTextureFileNames) {
			sb.append("sphereTextureFileName: " + sphereTextureFileName + "\n");
		}
		sb.append("\n");
		for (ModelMaterial material : materials) {
			sb.append(material + "\n");
		}
		sb.append("\n");
		for (ModelSphere sphere : spheres) {
			sb.append(sphere + "\n");
		}
		return sb.toString();
	}

	Model() {
		this.fovXdegree = 0; // Initialize with default values
		this.skyBoxImageFileName = "";
		this.comment = "";
	}

	Model(String toStringStr, boolean stam) throws FileParsingException {
		try (Scanner allStringScanner = new Scanner(toStringStr)) {
			String line;
			while (allStringScanner.hasNextLine()) {
				line = allStringScanner.nextLine();
				if (line.isEmpty()) {
					continue;
				}
				try (Scanner lineScanner = new Scanner(line)) { // Try-with-resources for lineScanner
					String lineType = line.substring(0, line.indexOf(':'));
					switch (lineType) {
					case "comment":
						comment = Utilities.parseTokenRestOfString(lineScanner, "comment");
						break;
					case "fovXdegree":
						fovXdegree = Utilities.parseTokenFloat(lineScanner, "fovXdegree");
						break;
					case "skyBoxImageFileName":
						skyBoxImageFileName = Utilities.parseTokenRestOfString(lineScanner, "skyBoxImageFileName");
						break;
					case "Light":
						ModelLight light = new ModelLight(line);
						lights.add(light);
						break;
					case "sphereTextureFileName":
						String sphereTextureFileName = Utilities.parseTokenRestOfString(lineScanner,
								"sphereTextureFileName");
						sphereTextureFileNames.add(sphereTextureFileName);
						break;
					case "Material":
						ModelMaterial material = new ModelMaterial(line);
						materials.add(material);
						break;
					case "Sphere":
						ModelSphere sphere = new ModelSphere(line);
						spheres.add(sphere);
						break;
					default:
						throw new RuntimeException("At Model constructor from toStringStr, line type \"" + lineType
								+ "\" not legal. Failed to parse model file. line: \"" + line + "\" failed.");
					}
				}
			}
		} catch (FileParsingException e) {
			String errorMessage = e.getMessage()
					+ "\nAt Model constructor from toStringStr.\n Fail to load parse string:\n" + toStringStr;
			System.err.println(errorMessage);
			throw new FileParsingException(errorMessage);
		}

		for (String sphereTextureFileName : sphereTextureFileNames) {
			try {
				skyBoxImageSphereTextures.add(new SphereTexture(sphereTextureFileName));
			} catch (IOException e) {
				String errorMessage = "At Model constructor from toStringStr.\n Fail to load texture file: \""
						+ sphereTextureFileName + "\"";
				System.err.println(errorMessage);
				throw new RuntimeException("At Model constructor from toStringStr.\n Fail to load texture file: \""
						+ sphereTextureFileName + "\"");
			}
		}
	}

	static void writeModelToFile(String fileName, Model modelToWrite) {
		try (Writer writer = new FileWriter(fileName)) {
			writer.write(modelToWrite.toString());
		} catch (IOException e) {
			String errorMessage = "At writeModelToFile of Model class.\n Fail to write model to file: \"" + fileName
					+ "\"";
			System.err.println(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		System.out.println("File \"" + fileName + "\" was created.");
	}

	static void writeModelToFileIfChanged(String fileName, Model modelToWrite) {
		boolean modelAreEquals = false;
		try {
			Model modelInFile = new Model(fileName);
			String modelToWriteToString = modelToWrite.toString();
			String modelInFileToString = modelInFile.toString();
			modelAreEquals = modelToWriteToString.equals(modelInFileToString);
			if (!modelAreEquals) {
				System.err.println("model in file " + fileName + " is different from model to write:");
				printDiffLines(modelToWriteToString, modelInFileToString);
			}

		} catch (Exception e) {
			modelAreEquals = false;
			String errorMessage = "At writeModelToFileIfChanged of Model class.\n Fail to load model from file or compare model to the model in file: \""
					+ fileName + "\"";
			System.err.println(errorMessage);
		}

		if (!modelAreEquals) {
			writeModelToFile(fileName, modelToWrite);
		}
	}

	static void printDiffLines(String str1, String str2) {
		// Split the strings into lists of lines
		String[] lines1 = str1.split("\n");
		String[] lines2 = str2.split("\n");

		int maxLength = Math.max(lines1.length, lines2.length); // The maximum number of lines

		for (int i = 0; i < maxLength; i++) {
			String line1 = i < lines1.length ? lines1[i] : "";
			String line2 = i < lines2.length ? lines2[i] : "";

			// If the lines are different, print them along with the line number
			if (!line1.equals(line2)) {
				System.out.println("Line " + (i + 1) + " (First string ): " + line1);
				System.out.println("Line " + (i + 1) + " (Second string): " + line2);
				System.out.println(); // Add a blank line to separate different lines
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Testing toString and parsing toString back to the object");
		System.out.println("===========================================================");
		ModelSphere s1 = new ModelSphere(new Vector3f(1, 2, 3), 4.1f, 5, 7);
		System.out.println(s1);
		ModelSphere s2 = new ModelSphere(s1.toString());
		System.out.println(s2);
		if (!s1.toString().equals(s2.toString())) {
			System.err.println(
					"toString of an object and the toString of the constucted object from its toString are not equal");
			throw new Exception();
		} else
			System.out.println("success - strings are equals.");

		ModelMaterial mirror1 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color
				0.3f, new Vector3f(0.25f, 0.20725f, 0.20725f), new Vector3f(1.0f, 0.829f, 0.829f),
				new Vector3f(0.296648f, 0.296648f, 0.296648f), 1000.0f, // Direct light
				0.7f, // Reflection
				0.0f, 1.52f, // Refraction
				0.0f, // Texture
				"Mirror" // Comment
		);
		System.out.println(mirror1);
		ModelMaterial mirror2 = new ModelMaterial(mirror1.toString());
		System.out.println(mirror2);
		if (!mirror1.toString().equals(mirror2.toString())) {
			System.err.println(
					"toString of an object and the toString of the constucted object from its toString are not equal");
			throw new Exception();
		} else
			System.out.println("success - strings are equals.");

		ModelLight light1 = new ModelLight(new Vector3f(1, 2, 3), 4.0f, "ccccccc");
		System.out.println(light1);
		ModelLight light2 = new ModelLight(light1.toString());
		System.out.println(light2);
		if (!light1.toString().equals(light2.toString())) {
			System.err.println(
					"toString of an object and the toString of the constucted object from its toString are not equal");
			throw new Exception();
		} else
			System.out.println("success - strings are equals.");

		ModelMaterial gold1 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color
				0.9f, new Vector3f(0.24725f, 0.1995f, 0.0745f), new Vector3f(0.75164f, 0.60648f, 0.22648f),
				new Vector3f(0.628281f, 0.555802f, 0.366065f), 100.0f, // Direct light
				0.0f, // Reflection
				0.0f, 1.52f, // Refraction
				0.1f, // Texture
				"gold" // Comment
		);
		ModelMaterial glass1 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), 0.1f, new Vector3f(1.0f),
				new Vector3f(1.0f), new Vector3f(1.0f), 1000.0f, 0.1f, 0.8f, 1.52f, 0.0f, "glass");
		Model model1 = new Model();
		model1.comment = "Spheres_from_few_materials";
		model1.fovXdegree = 20;
		model1.skyBoxImageFileName = "./Models/sky.jpg";
		model1.lights.add(new ModelLight(new Vector3f(6, 10, 0), 1.0f, "1"));
		model1.lights.add(new ModelLight(new Vector3f(-6, -10, 0), 2.0f, "2"));
		model1.materials.add(mirror1);
		model1.materials.add(gold1);
		model1.materials.add(glass1);
		model1.sphereTextureFileNames.add("./Models/2k_moon.jpg");
		model1.sphereTextureFileNames.add("./Models/1454054_texture_1-1.jpg");

		model1.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 0.2f, 0, 0));
		model1.spheres.add(new ModelSphere(new Vector3f(0, 0, -20), 1f, 1, 0));
		model1.spheres.add(new ModelSphere(new Vector3f(0, 0, -30), 2.5f, 2, 0));

		System.out.println(model1);
		System.out.println();

		Model model2 = new Model(model1.toString(), false);
		System.out.println(model2);
		if (!model1.toString().equals(model2.toString())) {
			System.err.println(
					"toString of an object and the toString of the constucted object from its toString are not equal");
			throw new Exception();
		} else
			System.out.println("success - strings are equals.");

		System.out.println();
		System.out.println("Success in testing toString and parsing toString back to the object");
		System.out.println("===========================================================");
		System.out.println();

		System.out.println();
		System.out.println("Writing file models if changed");
		System.out.println("===========================================================");

		// materials
		/////////////////////////////////////////////////////////////////////////////////////////////////
		ModelMaterial simplePastelBlue = new ModelMaterial();
		simplePastelBlue.kColor = 1f;
		simplePastelBlue.color = new Vector3f(0.10588235294117647f, 0.5215686274509804f, 0.7215686274509804f);
		simplePastelBlue.comment = "simplePastelBlue";

		ModelMaterial simplePastelBlack = new ModelMaterial();
		simplePastelBlack.kColor = 1f;
		simplePastelBlack.color = new Vector3f(0.35294117647058826f, 0.3215686274509804f, 0.3333333333333333f);
		simplePastelBlack.comment = "simplePastelBlack";

		ModelMaterial simplePastelGreen = new ModelMaterial();
		simplePastelGreen.kColor = 1f;
		simplePastelGreen.color = new Vector3f(0.3333333333333333f, 0.6196078431372549f, 0.5137254901960784f);
		simplePastelGreen.comment = "simplePastelGreen";

		ModelMaterial simplePastelBrown = new ModelMaterial();
		simplePastelBrown.kColor = 1f;
		simplePastelBrown.color = new Vector3f(0.6823529411764706f, 0.35294117647058826f, 0.2549019607843137f);
		simplePastelBrown.comment = "simplePastelBrown";

		ModelMaterial simplePastelLightGreen = new ModelMaterial();
		simplePastelLightGreen.kColor = 1f;
		simplePastelLightGreen.color = new Vector3f(0.7647058823529411f, 0.796078431372549f, 0.44313725490196076f);
		simplePastelLightGreen.comment = "simplePastelLightGreen";

		ModelMaterial simplePastelPink = new ModelMaterial();
		simplePastelPink.kColor = 1f;
		simplePastelPink.color = new Vector3f(0.9450980392156862f, 0.796078431372549f, 1f);
		simplePastelPink.comment = "simplePastelPink";

		ModelMaterial mirror = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.3f, new Vector3f(0.25f, 0.20725f, 0.20725f), new Vector3f(1.0f, 0.829f, 0.829f),
				new Vector3f(0.296648f, 0.296648f, 0.296648f), 1000.0f, // Direct light - kDirect, ka, kd, ks, shininess
				0.7f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Mirror" // Comment
		);

		ModelMaterial gold = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.9f, new Vector3f(0.24725f, 0.1995f, 0.0745f), new Vector3f(0.75164f, 0.60648f, 0.22648f),
				new Vector3f(0.628281f, 0.555802f, 0.366065f), 100.0f, // Direct light - kDirect, ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.1f, // Texture - kTexture
				"gold" // Comment
		);

		ModelMaterial Pearl = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.9f, new Vector3f(0.25f, 0.20725f, 0.20725f), new Vector3f(1.0f, 0.829f, 0.829f),
				new Vector3f(0.296648f, 0.296648f, 0.296648f), 11.264f, // Direct light - kDirect, ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.1f, // Texture - kTexture
				"gold" // Comment
		);

		ModelMaterial PolishedBronze = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.9f, new Vector3f(0.25f, 0.148f, 0.06475f), new Vector3f(0.4f, 0.2368f, 0.1036f),
				new Vector3f(0.774597f, 0.458561f, 0.200621f), 76.8f, // Direct light - kDirect, ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.1f, // Texture - kTexture
				"gold" // Comment
		);
		ModelMaterial BlackPlastic = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.9f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.01f, 0.01f, 0.01f),
				new Vector3f(0.5f, 0.5f, 0.5f), 32f, // Direct light - kDirect, ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.1f, // Texture - kTexture
				"gold" // Comment
		);
		
		ModelMaterial direct = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				1.0f, new Vector3f(0.2f), new Vector3f(0.75f), new Vector3f(0.25f), 40.0f, // Direct light - kDirect,
																							// ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"direct" // Comment
		);

		ModelMaterial textureAndLight = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.5f, new Vector3f(0.2f), new Vector3f(0.75f), new Vector3f(0.25f), 40.0f, // Direct light - kDirect,
																							// ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.5f, // Texture - kTexture
				"texture" // Comment
		);

		ModelMaterial textureAndLightMoon = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				1.0f, new Vector3f(0.0f), new Vector3f(0.75f), new Vector3f(0.0f), 40.0f, // Direct light - kDirect,
																							// ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				1.0f, // Texture - kTexture
				"texture" // Comment
		);
		
		ModelMaterial textureAndLightBall = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				1.0f, new Vector3f(0.25f), new Vector3f(0.75f), new Vector3f(0.1f), 20.0f, // Direct light - kDirect,
																							// ka, kd, ks, shininess
				0.0f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				1.0f, // Texture - kTexture
				"texture" // Comment
		);

		ModelMaterial glass = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.1f, new Vector3f(0.0f), new Vector3f(1.0f), new Vector3f(1.0f), 1000.0f, // Direct light - kDirect,
																							// ka, kd, ks, shininess
				0.1f, // Reflection - kReflection
				0.8f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Glass" // Comment
		);

		Vector3f plasticColorR = new Vector3f(1.0f, 0.0f, 0.0f);
		ModelMaterial plasticR = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.0f), plasticColorR, plasticColorR, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColorG = new Vector3f(0.0f, 1.0f, 0.0f);
		ModelMaterial plasticG = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.0f), plasticColorG, plasticColorG, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColorB = new Vector3f(0.0f, 0.0f, 1.0f);
		ModelMaterial plasticB = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.0f), plasticColorB, plasticColorB, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColor1 = new Vector3f(0.4f, 0.4f, 0.6f);
		ModelMaterial plastic1 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.15f), plasticColor1, plasticColor1, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColor2 = new Vector3f(0.4f, 0.5f, 0.4f);
		ModelMaterial plastic2 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.15f), plasticColor2, plasticColor2, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColor3 = new Vector3f(0.4f, 0.6f, 0.5f);
		ModelMaterial plastic3 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.15f), plasticColor3, plasticColor3, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Vector3f plasticColor4 = new Vector3f(0.5f, 0.4f, 0.2f);
		ModelMaterial plastic4 = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color - kColor, color
				0.95f, new Vector3f(0.15f), plasticColor4, plasticColor4, 100.0f, // Direct light - kDirect, ka, kd, ks,
																					// shininess
				0.05f, // Reflection - kReflection
				0.0f, 1.52f, // Refraction - kRefraction, refractiveIndex
				0.0f, // Texture - kTexture
				"Plastic with color" // Comment
		);

		Model model;
		String fileName;

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_02___skybox_with_direction_labels.model";
		model = new Model();
		model.comment = "panorama_image_with_direction_labels.jpg";
		model.fovXdegree = 80;
		model.skyBoxImageFileName = "./Models/panorama_image_with_direction_labels.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));
		model.materials.add(simplePastelBlue);

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, 10), 5f, 0, 0));
		
		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_02___skybox_1.model";
		model = new Model();
		model.comment = "skybox_1.model - Model with aerial-drone-panorama-view skybox";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/aerial-drone-panorama-view-chisinau-multiple-buildings-roads-snow-bare-trees.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));
		model.materials.add(simplePastelBlue);

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, 10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_02___skybox_2.model";
		model = new Model();
		model.comment = "Outer_space_skybox.model - Model with aerial city skybox";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/lok-yiu-cheung-o6k0ZH1eOwg-unsplash.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));
		model.materials.add(simplePastelBlue);

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, 10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_03_1_OneSphere_simpleColor.model";
		model = new Model();
		model.comment = "OneSphere_simpleBlue.model - Model of one sphere with simple blue color (no diifuse, specular, ambinet cooficents).";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/panorama_image_with_direction_labels.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));
		model.materials.add(simplePastelBlue);

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_03_3_FewSpheres_simpleColors.model";
		model = new Model();
		model.comment = "FewSpheres_simpleColors.model - Model of few spheres with simple colors (no diifuse, specular, ambinet cooficents).";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/panorama_image_with_direction_labels.jpg";
		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(simplePastelBlue);
		model.materials.add(simplePastelBlack);
		model.materials.add(simplePastelGreen);
		model.materials.add(simplePastelBrown);
		model.materials.add(simplePastelLightGreen);
		model.materials.add(simplePastelPink);

		model.spheres.add(new ModelSphere(new Vector3f(5, 0, -5f), 1f, 5, 0));
		model.spheres.add(new ModelSphere(new Vector3f(4, 0, -6.5f), 1f, 4, 0));
		model.spheres.add(new ModelSphere(new Vector3f(3, 0, -8f), 1f, 3, 0));
		model.spheres.add(new ModelSphere(new Vector3f(2, 0, -9.5f), 1f, 2, 0));
		model.spheres.add(new ModelSphere(new Vector3f(1, 0, -11f), 1f, 1, 0));
		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -12.5f), 1f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-1, 0, -11f), 1f, 1, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-2, 0, -9.5f), 1f, 2, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-3, 0, -8f), 1f, 3, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-4, 0, -6.5f), 1f, 4, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-5, 0, -5f), 1f, 5, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_04___OneSphere_direct.model";
		model = new Model();
		model.comment = "OneSphere_direct.model - Model of one sphere with direct light.";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/DefaultSkyBoxImage.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(direct);

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_04___4SpheresFromFewMaterialsDirectLight.model";
		model = new Model();
		model.comment = "Spheres_from_few_materials - New version of the model with updated materials.";
		model.fovXdegree = 50;
		model.skyBoxImageFileName = "./Models/sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(3, 10, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(gold);
		model.materials.add(Pearl);
		model.materials.add(PolishedBronze);
		model.materials.add(BlackPlastic);

		model.spheres.add(new ModelSphere(new Vector3f(-2, 2, -10f), 2f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-2, -2, -10f), 2f, 1, 0));
		model.spheres.add(new ModelSphere(new Vector3f(2, 2, -10f), 2f, 2, 0));
		model.spheres.add(new ModelSphere(new Vector3f(2, -2, -10f), 2f, 3, 0));

		writeModelToFileIfChanged(fileName, model);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_05___OneSphere_texture_and_direct_light.model";
		model = new Model();
		model.comment = "091_OneSphere_texture_and_direct_light.model - Model of one sphere with texture and direct light.";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/outer-space-background s.jpg";

		model.lights.add(new ModelLight(new Vector3f(10, 3, 0), 1.0f, ""));
		model.materials.add(textureAndLightMoon);
		model.sphereTextureFileNames.add("./Models/2k_moon.jpg");

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_06___OneSphereWithGround.model";
		model = new Model();
		model.comment = "OneSphereWithGround.model - Model of one sphere with a ground below the sphere.";
		model.fovXdegree = 40;
		model.skyBoxImageFileName = "./Models/panoramic-view-field-covered-grass-trees-sunlight-cloudy-sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(0, 17, -20), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(gold);
		model.materials.add(mirror);

		model.spheres.add(new ModelSphere(new Vector3f(0, -1f, -20f), 2f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(0, -100003f, -20f), 100000f, 1, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_07___OneSphere_mirror.model";
		model = new Model();
		model.comment = "OneSphere_mirror.model - Model of one sphere with mirror reflection.";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/DefaultSkyBoxImage.jpg";

		model.lights.add(new ModelLight(new Vector3f(3, 10, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(mirror);

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_07___TwoMirrorSpheresOnGround.model";
		model = new Model();
		model.comment = "130_TwoSphereOnGround.model - Model of two spheres on ground.";
		model.fovXdegree = 40;
		model.skyBoxImageFileName = "./Models/panoramic-view-field-covered-grass-trees-sunlight-cloudy-sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(5, 17, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(mirror); // 0

		model.spheres.add(new ModelSphere(new Vector3f(0f, -100003f, -12f), 100000f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(3.5f, 0f, -12f), 3f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-3.5f, 0f, -12f), 3f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_07___Lattice_mirros.model";
		model = new Model();
		model.fovXdegree = 45;
		model.skyBoxImageFileName = "./Models/sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(5, 3, 0), 1.0f, ""));

		for (int i = 0; i < 14; i++) {
			for (int j = 30; j >= 0; j--) {
				float r = (float) (Math.random() * 256) / 255;
				float g = (float) (Math.random() * 256) / 255;
				float b = (float) (Math.random() * 256) / 255;
				Vector3f plasticColor = new Vector3f(r, g, b);
				ModelMaterial plastic = new ModelMaterial(0.0f, new Vector3f(1.0f, 1.0f, 1.0f), // Color
						0.95f, new Vector3f(0), plasticColor, plasticColor, 100.0f, // Direct light
						0.05f, // Reflection
						0.0f, 1.52f, // Refraction
						0.0f, // Texture
						"Plastic with color" // Comment
				);
				model.materials.add(plastic); // Only direct
			}
		}
		model.materials.add(mirror);

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		int materialIndex = 0;
		for (int i = 0; i < 14; i++) {
			for (int j = 30; j >= 0; j--) {
				model.spheres.add(new ModelSphere(new Vector3f(0.5f * i - 3.5f, 0.15f + 1.0f, -1f - j), 0.15f,
						materialIndex++, 0));
			}
		}

		// materialIndex = 0;
		for (int i = 0; i < 14; i++) {
			for (int j = 30; j >= 0; j--) {
				model.spheres.add(
						new ModelSphere(new Vector3f(0.5f * i - 3.5f, 0.15f - 1.0f, -1f - j), 0.15f, materialIndex, 0));
			}
		}

		writeModelToFile(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_08___OneSphere_glass.model";
		model = new Model();
		model.comment = "OneSphere_glass.model - Model of one sphere made of transparent glass.";
		model.fovXdegree = 90;
		model.skyBoxImageFileName = "./Models/DefaultSkyBoxImage.jpg";

		model.lights.add(new ModelLight(new Vector3f(3, 10, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(glass);

		model.spheres.add(new ModelSphere(new Vector3f(0, 0, -10), 5f, 0, 0));

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_08___FewSphereWithGround.model";
		model = new Model();
		model.comment = "FewSphereWithGround.model - Model of several spheres with ground.";
		model.fovXdegree = 40;
		model.skyBoxImageFileName = "./Models/panoramic-view-field-covered-grass-trees-sunlight-cloudy-sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(5, 10, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/1454054_texture_1-1.jpg");
		model.sphereTextureFileNames.add("./Models/basketballTexture.jpg");
		model.sphereTextureFileNames.add("./Models/yellow-wall-texture-with-scratches.jpg");

		model.materials.add(mirror); // 0
		model.materials.add(glass); // 1
		model.materials.add(direct); // 2
		// gold.textureIndex = 2;
		model.materials.add(gold); // 3
		model.materials.add(plastic1); // 4
		model.materials.add(plastic2); // 5
		model.materials.add(plastic3); // 6
		model.materials.add(plastic4); // 7
		// textureAndLight.textureIndex = 0;
		model.materials.add(textureAndLightBall); // 8
		// textureAndLight.textureIndex = 1;
		model.materials.add(textureAndLightBall); // 9

		model.spheres.add(new ModelSphere(new Vector3f(3, 1, -25f), 4f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-1.5f, -1, -16f), 2f, 1, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-7, -1, -29f), 2f, 3, 2));
		model.spheres.add(new ModelSphere(new Vector3f(-4, -2.5f, -13f), 0.5f, 4, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-2, -2.5f, -10f), 0.5f, 8, 0));
		model.spheres.add(new ModelSphere(new Vector3f(0.5f, -2.5f, -12f), 0.5f, 5, 0));
		model.spheres.add(new ModelSphere(new Vector3f(1.5f, -2.5f, -9f), 0.5f, 7, 0));
		model.spheres.add(new ModelSphere(new Vector3f(3.5f, -1.5f, -15f), 1.5f, 9, 1));

		model.spheres.add(new ModelSphere(new Vector3f(-1, -100003, -20f), 100000f, 0, 0));
		// model.spheres.add(new Sphere(new Vector3f(-4, -1.25f, -16f), 1.5f, 9, 0)); //
		// Optional sphere

		writeModelToFileIfChanged(fileName, model);

		/////////////////////////////////////////////////////////////////////////////////////////////////
		fileName = "./Models/ex_08___4SpheresFromFewMaterials.model";
		model = new Model();
		model.comment = "Spheres_from_few_materials - New version of the model with updated materials.";
		model.fovXdegree = 50;
		model.skyBoxImageFileName = "./Models/sky.jpg";

		model.lights.add(new ModelLight(new Vector3f(3, 10, 0), 1.0f, ""));

		model.sphereTextureFileNames.add("./Models/EmptyTexture.jpg");

		model.materials.add(glass);
		model.materials.add(mirror);
		model.materials.add(gold);
		model.materials.add(plastic1);

		model.spheres.add(new ModelSphere(new Vector3f(-2, 2, -10f), 2f, 0, 0));
		model.spheres.add(new ModelSphere(new Vector3f(-2, -2, -10f), 2f, 1, 0));
		model.spheres.add(new ModelSphere(new Vector3f(2, 2, -10f), 2f, 2, 0));
		model.spheres.add(new ModelSphere(new Vector3f(2, -2, -10f), 2f, 3, 0));

		writeModelToFileIfChanged(fileName, model);



		
		
		
		
		System.out.println("Finished writing models.");
	}

}
