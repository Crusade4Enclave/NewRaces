// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.math;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.ThreadLocalRandom;

import static engine.math.FastMath.sqr;

public class Vector3fImmutable {

	public final float x, y, z;

	public Vector3fImmutable() {
		x = y = z = 0.0f;
	}

	public Vector3fImmutable(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3fImmutable(Vector3f original) {
		this.x = original.x;
		this.y = original.y;
		this.z = original.z;
	}

	public Vector3fImmutable(Vector3fImmutable original) {
		this.x = original.x;
		this.y = original.y;
		this.z = original.z;
	}

	public boolean isInsideCircle(Vector3fImmutable circleCenter, float radius) {

		return (circleCenter.distanceSquared2D(this) < sqr(radius));
	}

	public Vector3fImmutable add(Vector3f vec) {
		if (null == vec)
			return null;
		return new Vector3fImmutable(x + vec.x, y + vec.y, z + vec.z);
	}

	public Vector3fImmutable add(Vector3fImmutable vec) {
		if (null == vec)
			return null;
		return new Vector3fImmutable(x + vec.x, y + vec.y, z + vec.z);
	}

	public Vector3fImmutable add(float x, float y, float z) {
		return new Vector3fImmutable(this.x + x, this.y + y, this.z + z);
	}

	public Vector3fImmutable scaleAdd(float scalar, Vector3fImmutable add) {
		return new Vector3fImmutable(x * scalar + add.x, y * scalar + add.y , z
				* scalar + add.z);
	}

	public static Vector3fImmutable scaleAdd(float scalar, Vector3fImmutable mult,
			Vector3fImmutable add) {
		return new Vector3fImmutable(mult.x * scalar + add.x, mult.y * scalar
				+ add.y, mult.z * scalar + add.z);
	}

	public float dot(Vector3fImmutable vec) {
		if (null == vec) {
			return 0.0f;
		}

		return x * vec.x + y * vec.y + z * vec.z;
	}
	
	public float dot2D(Vector3fImmutable vec) {
		if (null == vec) {
			return 0.0f;
		}

		return x * vec.x  + z * vec.z;
	}

	public Vector3fImmutable cross(Vector3fImmutable v) {
		return cross(v.x, v.y, v.z);
	}

	public Vector3fImmutable cross(float x, float y, float z) {
		return new Vector3fImmutable(this.y * z - this.z * y, this.z * x
				- this.x * z, this.x * y - this.y * x);
	}

	public float length() {
		return FastMath.sqrt(lengthSquared());
	}

	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	public float distanceSquared(Vector3fImmutable v) {
		double dx = x - v.x;
		double dy = y - v.y;
		double dz = z - v.z;
		return (float) (dx * dx + dy * dy + dz * dz);
	}

	public float magnitude() {
		return FastMath.sqrt(sqrMagnitude());
	}

	public float sqrMagnitude() {
		return x * x + y * y + z * z;
	}

	public Vector3fImmutable moveTowards (Vector3fImmutable target, float maxDistanceDelta)
	{
		Vector3fImmutable outVector;

		Vector3fImmutable direction = target.subtract2D(this);
		float magnitude = direction.magnitude();

		if (magnitude <= maxDistanceDelta || magnitude == 0f)
		{
			return target;
		}

		outVector = direction.divide(magnitude).mult(maxDistanceDelta);
		outVector = this.add(outVector);
		return outVector;
	}

	public float distanceSquared2D(Vector3fImmutable v) {
		double dx = x - v.x;
		double dz = z - v.z;
		return (float) (dx * dx + dz * dz);
	}

	public float distance(Vector3fImmutable v) {
		return FastMath.sqrt(distanceSquared(v));
	}

	public float distance2D(Vector3fImmutable v) {
		return FastMath.sqrt(distanceSquared2D(v));
	}

	public Vector3fImmutable mult(float scalar) {
		return new Vector3fImmutable(x * scalar, y * scalar, z * scalar);
	}

	public Vector3fImmutable mult(Vector3fImmutable vec) {
		if (null == vec) {
			return null;
		}

		return new Vector3fImmutable(x * vec.x, y * vec.y, z * vec.z);
	}

	public Vector3fImmutable divide(float scalar) {
		scalar = 1f / scalar;
		return new Vector3fImmutable(x * scalar, y * scalar, z * scalar);
	}

	public Vector3fImmutable divide(Vector3fImmutable scalar) {
		return new Vector3fImmutable(x / scalar.x, y / scalar.y, z / scalar.z);
	}

	public Vector3fImmutable negate() {
		return new Vector3fImmutable(-x, -y, -z);
	}

	public Vector3fImmutable subtract(Vector3fImmutable vec) {
		return new Vector3fImmutable(x - vec.x, y - vec.y, z - vec.z);
	}

	public Vector3fImmutable subtract2D(Vector3fImmutable vec) {
		return new Vector3fImmutable(x - vec.x, 0, z - vec.z);
	}

	public Vector3fImmutable subtract(float x, float y, float z) {
		return new Vector3fImmutable(this.x - x, this.y - y, this.z - z);
	}

	public Vector3fImmutable normalize() {
		float length = length();
		if (length != 0) {
			return divide(length);
		}

		return divide(1);
	}

	public float angleBetween(Vector3fImmutable otherVector) {
		float dotProduct = dot(otherVector);
		return FastMath.acos(dotProduct);
	}
	
	public float angleBetween2D(Vector3fImmutable otherVector) {
		float dotProduct = dot(otherVector);
		return FastMath.acos(dotProduct);
	}

	public Vector3fImmutable interpolate(Vector3f finalVec, float changeAmnt) {
		return new Vector3fImmutable((1 - changeAmnt) * this.x + changeAmnt
				* finalVec.x, (1 - changeAmnt) * this.y + changeAmnt
				* finalVec.y, (1 - changeAmnt) * this.z + changeAmnt
				* finalVec.z);
	}
	
	public Vector3fImmutable interpolate(Vector3fImmutable finalVec, float changeAmnt) {
		return new Vector3fImmutable((1 - changeAmnt) * this.x + changeAmnt
				* finalVec.x, (1 - changeAmnt) * this.y + changeAmnt
				* finalVec.y, (1 - changeAmnt) * this.z + changeAmnt
				* finalVec.z);
	}


	public static boolean isValidVector(Vector3fImmutable vector) {
		if (vector == null)
			return false;
		if (Float.isNaN(vector.x) || Float.isNaN(vector.y)
				|| Float.isNaN(vector.z))
			return false;
		return !Float.isInfinite(vector.x) && !Float.isInfinite(vector.y)
				&& !Float.isInfinite(vector.z);
	}

	@Override
	public Vector3fImmutable clone() throws CloneNotSupportedException {
		return (Vector3fImmutable) super.clone();
	}

	public float[] toArray(float[] floats) {
		if (floats == null) {
			floats = new float[3];
		}
		floats[0] = x;
		floats[1] = y;
		floats[2] = z;
		return floats;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Vector3fImmutable)) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Vector3fImmutable comp = (Vector3fImmutable) o;
		if (Float.compare(x, comp.x) != 0)
			return false;
		if (Float.compare(y, comp.y) != 0)
			return false;
		return Float.compare(z, comp.z) == 0;
	}

	@Override
	public int hashCode() {
		int hash = 37;
		hash += 37 * hash + Float.floatToIntBits(x);
		hash += 37 * hash + Float.floatToIntBits(y);
		hash += 37 * hash + Float.floatToIntBits(z);
		return hash;
	}

	public static Vector3fImmutable readExternal(ObjectInput in)
			throws IOException, ClassNotFoundException {
		return new Vector3fImmutable(in.readFloat(), in.readFloat(), in
				.readFloat());
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
	}

	public Vector3fImmutable getOffset(float rotation, float xOffset, float yOffset, float zOffset, boolean invertZ) {
		float sin = FastMath.sin(rotation);
		float cos = FastMath.cos(rotation);
		Vector3f faceDir = new Vector3f(sin, 0f, cos);
		Vector3f crossDir = new Vector3f(cos, 0f, sin);
		faceDir.multLocal(zOffset);
		crossDir.multLocal(xOffset);
		if (invertZ) {
			faceDir.z = -faceDir.z;
			crossDir.z = -crossDir.z;
		}
		Vector3f loc = new Vector3f(this);
		loc.addLocal(faceDir);
		loc.addLocal(crossDir);
		loc.y += yOffset;
		return new Vector3fImmutable(loc);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public float get(int index) {
		switch (index) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		}
		throw new IllegalArgumentException("index must be either 0, 1 or 2");
	}

	public Vector2f getLatLong() {
		return new Vector2f(this.x, this.z);
	}

	public synchronized float getLat() {
		return x;
	}

	public synchronized float getLong() {
		return z;
	}

	public synchronized float getAlt() {
		return y;
	}

	public Vector3fImmutable setX(float x) {
		return new Vector3fImmutable(x, y, z);
	}

	public Vector3fImmutable setY(float y) {
		return new Vector3fImmutable(x, y, z);
	}

	public Vector3fImmutable setZ(float z) {
		return new Vector3fImmutable(x, y, z);
	}

	public float getRotation() {
		return 3.14f + FastMath.atan2(-x, -z);
	}
	public boolean inRange2D(Vector3fImmutable otherVec, float range){
		float distance = this.distanceSquared2D(otherVec);
		return !(distance > range * range);
	}

	public static String toString(Vector3fImmutable vector) {

		return vector.toString();
	}

	@Override
	public String toString() {

		String outString;

		outString = "(" + this.x + '/' + this.y + '/' + this.z;
		return outString;

	}

	public String toString2D() {

		String outString;

		outString = "( " + (int)this.x + " , " + (int)(this.z *-1) +" )";
		return outString;

	}

	public static Vector3fImmutable ClosestPointOnLine(Vector3fImmutable lineStart, Vector3fImmutable lineEnd, Vector3fImmutable sourcePoint) {

		Vector3fImmutable closestPoint;
		Vector3fImmutable lineStartToTarget;
		Vector3fImmutable lineDirection;
		float lineLength;
		float dotProduct;

		lineStartToTarget = sourcePoint.subtract(lineStart);
		lineDirection = lineEnd.subtract(lineStart).normalize();
		lineLength = lineStart.distance2D(lineEnd);

		dotProduct = lineDirection.dot(lineStartToTarget);

		if (dotProduct <= 0)
			return lineStart;

		if (dotProduct >= lineLength)
			return lineEnd;

		// Project the point by advancing it along the line from
		// the starting point.

		closestPoint = lineDirection.mult(dotProduct);
		closestPoint = lineStart.add(closestPoint);

		return closestPoint;
	}

	public Vector3fImmutable ClosestPointOnLine(Vector3fImmutable lineStart, Vector3fImmutable lineEnd) {

		Vector3fImmutable closestPoint;
		Vector3fImmutable lineStartToTarget;
		Vector3fImmutable lineDirection;
		float lineLength;
		float dotProduct;

		lineStartToTarget = this.subtract(lineStart);
		lineDirection = lineEnd.subtract(lineStart).normalize();
		lineLength = lineStart.distance2D(lineEnd);

		dotProduct = lineDirection.dot(lineStartToTarget);

		if (dotProduct <= 0)
			return lineStart;

		if (dotProduct >= lineLength)
			return lineEnd;

		// Project the point by advancing it along the line from
		// the starting point.

		closestPoint = lineDirection.mult(dotProduct);
		closestPoint = lineStart.add(closestPoint);

		return closestPoint;
	}

	public static Vector3fImmutable rotateAroundPoint(Vector3fImmutable origin, Vector3fImmutable point, int angle) {

		float angleRadians;
		int modifiedAngle;

		// Convert angle to radians

		      modifiedAngle = angle;

		    if (angle < 0)
		      modifiedAngle = 360 + modifiedAngle;

		angleRadians = (float) Math.toRadians(modifiedAngle);

		return rotateAroundPoint(origin, point, angleRadians);
	}

	public static Vector3fImmutable rotateAroundPoint(Vector3fImmutable origin, Vector3fImmutable point, float radians) {

		Vector3fImmutable outVector;
		Vector3f directionVector;
		Quaternion angleRotation;

		// Build direction vector relative to origin

		directionVector = new Vector3f(point.subtract(origin));

		// Build quaternion rotation

		angleRotation = new Quaternion().fromAngleAxis(radians, new Vector3f(0,1,0));
		// Apply rotation to direction vector

		directionVector = angleRotation.mult(directionVector);

		// Translate from origin back to new rotated point

		outVector = origin.add(directionVector);

		return outVector;

	}
	
	public static Vector3fImmutable rotateAroundPoint(Vector3fImmutable origin, Vector3fImmutable point,Quaternion angleRotation) {

		Vector3fImmutable outVector;
		Vector3f directionVector;
		// Build direction vector relative to origin
		directionVector = new Vector3f(point.subtract(origin));

		// Build quaternion rotation


		// Apply rotation to direction vector
		

		directionVector = angleRotation.mult(directionVector);

		// Translate from origin back to new rotated point

		outVector = origin.add(directionVector);

		return outVector;

	}
	
	public static Vector3fImmutable rotateAroundPoint(Vector3fImmutable origin, Vector3fImmutable point, float w, Vector3f axis) {

		Vector3fImmutable outVector;
		Vector3f directionVector;
		Quaternion angleRotation;

		// Build direction vector relative to origin

		directionVector = new Vector3f(point.subtract(origin));

		// Build quaternion rotation

		angleRotation = new Quaternion().fromAngleAxis(w, axis);
		// Apply rotation to direction vector

		directionVector = angleRotation.mult(directionVector);

		// Translate from origin back to new rotated point

		outVector = origin.add(directionVector);

		return outVector;

	}

	public static Vector3fImmutable getRandomPointInCircle(Vector3fImmutable origin, float radius) {
		// Member variables

		float targetAngle;
		float targetRadius;
		Vector3fImmutable targetPosition;

		targetAngle = (float) (ThreadLocalRandom.current().nextFloat() * Math.PI * 2);
		targetRadius = (float) (Math.sqrt(ThreadLocalRandom.current().nextFloat()) * radius);
		targetPosition = new Vector3fImmutable((float) (origin.x + targetRadius * Math.cos(targetAngle)), origin.y, (float) (origin.z + targetRadius * Math.sin(targetAngle)));
		return targetPosition;
	}
	
	public static Vector3fImmutable getLocBetween(Vector3fImmutable start, Vector3fImmutable end) {
		// Member variables

		Vector3fImmutable faceDirection = end.subtract(start).normalize();
		float distance = end.distance(start) * .5f;
		return faceDirection.scaleAdd(distance, start);
	}

	public static Vector3fImmutable getRandomPointOnCircle(Vector3fImmutable origin, float radius) {

		// Member variables

		int randomAngle;
		Vector3fImmutable targetPosition;

		randomAngle = ThreadLocalRandom.current().nextInt(360);

		targetPosition = new Vector3fImmutable((float) (origin.x + radius * Math.cos(randomAngle)), origin.y, (float) (origin.z + radius * Math.sin(randomAngle)));
		return targetPosition;
	}

	public static final Vector3fImmutable ZERO = new Vector3fImmutable(0,0,0);
	
	public static Vector3fImmutable transform(Vector3fImmutable origin,Vector3fImmutable point, float angle){
		
		//TRANSLATE TO ORIGIN
        float x1 = point.x - origin.x;
        float y1 = point.z - origin.z;

		//APPLY ROTATION
		float temp_x1 = (float) (x1 * Math.cos(angle) - y1 * Math.sin(angle));
		float temp_z1 = (float) (x1 * Math.sin(angle) + y1 * Math.cos(angle));
		
		temp_x1 += origin.x;
		temp_z1 += origin.z;
		
		return new Vector3fImmutable(temp_x1,point.y,temp_z1);
	}
	public float Lerp(Vector3fImmutable dest, float lerpFactor)
	{
		return dest.subtract(this).mult(lerpFactor).add(this).y;
	}
}
