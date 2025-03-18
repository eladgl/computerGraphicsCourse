package your_code;

import java.io.IOException;

import org.joml.Vector3f;

import app_interface.Model;
import app_interface.ModelSphere;
import app_interface.SphereTexture;


public class UnitTests {

	private static void testCalcPixelDirection() {
	    int x = 127;
	    int y = 312;
	    int imageWidth = 600;
	    int imageHeight = 400;
	    float fovXdegree = 70.0f;
	    Vector3f result = WorldModel.calcPixelDirection(x, y, imageWidth, imageHeight, fovXdegree);
	    Vector3f expected = new Vector3f(-0.365f, 0.220f, -0.905f);
	    float epsilon = 1e-3f;
	    if (Math.abs(result.x - expected.x) < epsilon &&
	        Math.abs(result.y - expected.y) < epsilon &&
	        Math.abs(result.z - expected.z) < epsilon) {
	        System.out.println("testCalcPixelDirection passed.");
	    } else {
	        System.out.println("testCalcPixelDirection failed. Result: " + result + ", Expected: " + expected);
	    }
	}
	
	private static void testRayIntersection() {
		// Test case 1: No intersection (ray misses the sphere)
		Vector3f rayStart1 = new Vector3f(1, 1, 1);
		Vector3f rayDirection1 = new Vector3f(1, 1, -1).normalize();
		ModelSphere sphere1 = new ModelSphere(new Vector3f(0, 0, -10), 5, 0, 0);

		IntersectionResults result1 = WorldModel.rayIntersection(rayStart1, rayDirection1, sphere1);
		System.out.println("Test Case 1: No intersection (ray misses the sphere)");
		System.out.format("Input: rayStart = [%.2f, %.2f, %.2f], rayDirection = [%.2f, %.2f, %.2f], sphereCenter = [%.2f, %.2f, %.2f], sphereRadius = %.2f%n",
				rayStart1.x, rayStart1.y, rayStart1.z, rayDirection1.x, rayDirection1.y, rayDirection1.z, sphere1.center.x, sphere1.center.y, sphere1.center.z, sphere1.radius);
		System.out.format("Result  : intersected = %b%n", result1!=null);
		System.out.format("Expected: intersected = false%n%n");

		// Test case 2: One intersection (ray tangent to the sphere)
		Vector3f rayStart2 = new Vector3f(-5, 0, -5);
		Vector3f rayDirection2 = new Vector3f(1, 0, 0).normalize();
		ModelSphere sphere2 = new ModelSphere(new Vector3f(0, 0, -10), 5, 0, 0);

		IntersectionResults result2 = WorldModel.rayIntersection(rayStart2, rayDirection2, sphere2);
		System.out.println("Test Case 2: One intersection (ray tangent to the sphere)");
		System.out.format("Input: rayStart = [%.2f, %.2f, %.2f], rayDirection = [%.2f, %.2f, %.2f], sphereCenter = [%.2f, %.2f, %.2f], sphereRadius = %.2f%n",
				rayStart2.x, rayStart2.y, rayStart2.z, rayDirection2.x, rayDirection2.y, rayDirection2.z, sphere2.center.x, sphere2.center.y, sphere2.center.z, sphere2.radius);
		System.out.format("Result  : intersected = %b, intersectionPoint = [%.2f, %.2f, %.2f], normal = [%.2f, %.2f, %.2f], linePointOutside = %b%n",
				result2!=null, result2.intersectionPoint.x, result2.intersectionPoint.y, result2.intersectionPoint.z, result2.normal.x, result2.normal.y, result2.normal.z, result2.rayFromOutsideOfSphere);
		System.out.format("Expected: intersected = true, intersectionPoint = [0.00, 0.00, -5.00], normal = [0.00, 0.00, 1.00], linePointOutside = true%n%n");
		
		// Test case 3: Two intersections (ray passes through the sphere)
		Vector3f rayStart3 = new Vector3f(0, 0, -20);
		Vector3f rayDirection3 = new Vector3f(0f, 0f, 1).normalize();
		ModelSphere sphere3 = new ModelSphere(new Vector3f(0, 0, -10), 5, 0, 0);
		IntersectionResults result3 = WorldModel.rayIntersection(rayStart3, rayDirection3, sphere3);
		System.out.println("Test Case 3: Two intersections (ray passes through the sphere)");
		System.out.format("Input: rayStart = [%.2f, %.2f, %.2f], rayDirection = [%.2f, %.2f, %.2f], sphereCenter = [%.2f, %.2f, %.2f], sphereRadius = %.2f%n",
				rayStart3.x, rayStart3.y, rayStart3.z, rayDirection3.x, rayDirection3.y, rayDirection3.z, sphere3.center.x, sphere3.center.y, sphere3.center.z, sphere3.radius);
		System.out.format("Result  : intersected = %b, intersectionPoint = [%.2f, %.2f, %.2f], normal = [%.2f, %.2f, %.2f], linePointOutside = %b%n",
				result3!=null, result3.intersectionPoint.x, result3.intersectionPoint.y, result3.intersectionPoint.z, result3.normal.x, result3.normal.y, result3.normal.z, result3.rayFromOutsideOfSphere);
		System.out.format("Expected: intersected = true, intersectionPoint = [0.00, 0.00, -15.00], normal = [0.00, 0.00, -1.00], linePointOutside = true%n");
	}

	private static void testlightingEquation_Diffuse() {
	    // Test with light in the same direction as the normal
	    Vector3f point = new Vector3f(2, 2, -2);
	    Vector3f normal = new Vector3f(0.2f, 0.1f, 1).normalize();
	    Vector3f lightPos = new Vector3f(-5, 5, 1);
	    Vector3f kd = new Vector3f(0.5f, 0.6f, 0.7f);
	    Vector3f ks = new Vector3f(0, 0, 0);
	    Vector3f ka = new Vector3f(0, 0, 0);
	    float shininess = 0;
	    Vector3f result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    Vector3f expected = new Vector3f(0.113f, 0.136f, 0.159f); // Diffuse contribution only
	    float epsilon = 1e-3f;
        if (Math.abs(result.x - expected.x) < epsilon &&
            Math.abs(result.y - expected.y) < epsilon &&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testDiffuse 1 passed.");
	    } else {
	        System.out.println("testDiffuse 1 failed. Result: " + result + ", Expected: " + expected);
	    }

	    // Test with light in the opposite direction of the normal
	    lightPos = new Vector3f(-5, 5, -3);
	    result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    expected = new Vector3f(0, 0, 0); // No contribution from opposite direction
        if (Math.abs(result.x - expected.x) < epsilon&&
            Math.abs(result.y - expected.y) < epsilon&&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testDiffuse 2 (opposite direction) passed.");
	    } else {
	        System.out.println("testDiffuse 2 (opposite direction) failed. Result: " + result + ", Expected: " + expected);
	    }
	}

	private static void testlightingEquation_Ambient() {
	    // Test ambient lighting only
	    Vector3f point = new Vector3f(9, 10, 11);
	    Vector3f normal = new Vector3f(32, 13, 50).normalize();
	    Vector3f lightPos = new Vector3f(40, 12, 27);
	    Vector3f kd = new Vector3f(0, 0, 0);
	    Vector3f ks = new Vector3f(0, 0, 0);
	    Vector3f ka = new Vector3f(0.4f, 0.3f, 0.2f);
	    float shininess = 0;
	    Vector3f result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    Vector3f expected = new Vector3f(0.4f, 0.3f, 0.2f); // Ambient contribution only
	    float epsilon = 1e-3f;
        if (Math.abs(result.x - expected.x) < epsilon&&
            Math.abs(result.y - expected.y) < epsilon&&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testAmbient passed.");
	    } else {
	        System.out.println("testAmbient failed. Result: " + result + ", Expected: " + expected);
	    }
	}

	private static void testlightingEquation_Specular() {
	    // Test with light and eye in the same direction
	    Vector3f point = new Vector3f(1, 0, -3);
	    Vector3f normal = new Vector3f(0, 0, 1);
	    Vector3f lightPos = new Vector3f(2.5f, 0, 0);
	    Vector3f kd = new Vector3f(0, 0, 0);
	    Vector3f ks = new Vector3f(0.5f, 0.4f, 0.3f);
	    Vector3f ka = new Vector3f(0, 0, 0);
	    float shininess = 50;
	    Vector3f result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    Vector3f expected = new Vector3f(0.302f, 0.241f, 0.181f); // Specular contribution
	    float epsilon = 1e-3f;
        if (Math.abs(result.x - expected.x) < epsilon&&
            Math.abs(result.y - expected.y) < epsilon&&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testSpecular (same direction) passed.");
	    } else {
	        System.out.println("testSpecular (same direction) failed. Result: " + result + ", Expected: " + expected);
	    }

	    // Test with light opposite to the normal
	    lightPos = new Vector3f(0, 0, -5);
	    result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    expected = new Vector3f(0, 0, 0); // No contribution as light is opposite to normal
	    if (result.equals(expected)) {
	        System.out.println("testSpecular (opposite light) passed.");
	    } else {
	        System.out.println("testSpecular (opposite light) failed. Result: " + result + ", Expected: " + expected);
	    }

	    // Test with eye direction opposite to reflection
	    normal = new Vector3f(0, 0, -1);
	    lightPos = new Vector3f(-5.5f, 0, -5);
	    result = WorldModel.lightingEquation(point, normal, lightPos, kd, ks, ka, shininess);
	    expected = new Vector3f(0, 0, 0); // No contribution from specular due to eye direction
	    if (result.equals(expected)) {
	        System.out.println("testSpecular (opposite eye) passed.");
	    } else {
	        System.out.println("testSpecular (opposite eye) failed. Result: " + result + ", Expected: " + expected);
	    }
	}


	private static void testcalcKdCombinedWithTexture() throws IOException {
		Vector3f intersectionPoint = new Vector3f(3,2,-1); 
		Vector3f intersectedSphereCenter = new Vector3f(5,1,-3);
		SphereTexture intersectedSphereTexture;
		intersectedSphereTexture = new SphereTexture("./Models/DefaultSkyBoxImage.jpg");
		Vector3f intersectedSphereKd = new Vector3f(10000,10000,10000); 
		float kTexture = 1;
		Vector3f result = WorldModel.calcKdCombinedWithTexture(
				intersectionPoint,
				intersectedSphereCenter,
				intersectedSphereTexture,
				intersectedSphereKd,
				kTexture);
		Vector3f expected = new Vector3f(0.412f, 0.616f, 0.902f);
	    float epsilon = 1e-3f;
        if (Math.abs(result.x - expected.x) < epsilon&&
            Math.abs(result.y - expected.y) < epsilon&&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testcalcKdCombinedWithTexture 1 (kd should be the texture color at the point) passed.");
	    } else {
	        System.out.println("testcalcKdCombinedWithTexture 1 (kd should be the texture color at the point) failed. Result: " + result + ", Expected: " + expected);
	    }

		intersectedSphereKd = new Vector3f(0.7f,0.6f,0.5f); 
		kTexture = 0.5f;
		result = WorldModel.calcKdCombinedWithTexture(
				intersectionPoint,
				intersectedSphereCenter,
				intersectedSphereTexture,
				intersectedSphereKd,
				kTexture);
		expected = new Vector3f(0.556f, 0.608f, 0.701f); 
        if (Math.abs(result.x - expected.x) < epsilon&&
            Math.abs(result.y - expected.y) < epsilon&&
            Math.abs(result.z - expected.z) < epsilon) {	    
	        System.out.println("testcalcKdCombinedWithTexture 2 passed.");
	    } else {
	        System.out.println("testcalcKdCombinedWithTexture 2 failed. Result: " + result + ", Expected: " + expected);
	    }
	}	

	public static void main(String[] args) throws IOException {
		System.out.println("\nRay direction tests");
		System.out.println("=========================");
		testCalcPixelDirection();
		
		System.out.println("\nRay intersection tests");
		System.out.println("=========================");
		testRayIntersection();

		System.out.println("\nLight calculations tests");
		System.out.println("=========================");
	    testlightingEquation_Diffuse();
	    testlightingEquation_Ambient();
	    testlightingEquation_Specular();
	    
		System.out.println("\nTexture tests");
		System.out.println("=========================");
	    testcalcKdCombinedWithTexture();
	}
}