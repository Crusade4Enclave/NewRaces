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


/**
 * <code>Vector3f</code> defines a Vector for a three float value tuple.
 * <code>Vector3f</code> can represent any three dimensional value, such as a
 * vertex, a normal, etc. Utility methods are also included to aid in
 * mathematical calculations.
 *
 */

public class Vector3f {
	public final static Vector3f ZERO = new Vector3f(0, 0, 0);

	public final static Vector3f UNIT_X = new Vector3f(1, 0, 0);
	public final static Vector3f UNIT_Y = new Vector3f(0, 1, 0);
	public final static Vector3f UNIT_Z = new Vector3f(0, 0, 1);
	public final static Vector3f UNIT_XYZ = new Vector3f(1, 1, 1);

	/**
	 * the x value of the vector.
	 */
	public float x;

	/**
	 * the y value of the vector.
	 */
	public float y;

	/**
	 * the z value of the vector.
	 */
	public float z;

	/**
	 * Constructor instantiates a new <code>Vector3f</code> with default values
	 * of (0,0,0).
	 *
	 */
	public Vector3f() {
		x = y = z = 0.0f;
	}

	/**
	 * Constructor instantiates a new <code>Vector3f</code> with provides
	 * values.
	 *
	 * @param x
	 *            the x value of the vector.
	 * @param y
	 *            the y value of the vector.
	 * @param z
	 *            the z value of the vector.
	 */
	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f(Vector3fImmutable original) {
		this.x = original.x;
		this.y = original.y;
		this.z = original.z;
	}

	/**
	 * Constructor instantiates a new <code>Vector3f</code> that is a copy of
	 * the provided vector
	 *
	 * @param copy
	 *            The Vector3f to copy
	 */
	public Vector3f(Vector3f copy) {
		this.set(copy);
	}

	/**
	 * <code>set</code> sets the x,y,z values of the vector based on passed
	 * parameters.
	 *
	 * @param x
	 *            the x value of the vector.
	 * @param y
	 *            the y value of the vector.
	 * @param z
	 *            the z value of the vector.
	 * @return this vector
	 */
	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * <code>set</code> sets the x,y,z values of the vector by copying the
	 * supplied vector.
	 *
	 * @param vect
	 *            the vector to copy.
	 * @return this vector
	 */
	public Vector3f set(Vector3f vect) {
		this.x = vect.x;
		this.y = vect.y;
		this.z = vect.z;
		return this;
	}

	/**
	 *
	 * <code>add</code> adds a provided vector to this vector creating a
	 * resultant vector which is returned. If the provided vector is null, null
	 * is returned.
	 *
	 * Neither 'this' nor 'vec' are modified.
	 *
	 * @param vec
	 *            the vector to add to this.
	 * @return the resultant vector.
	 */
	public Vector3f add(Vector3f vec) {
		if (null == vec) {
			return null;
		}
		return new Vector3f(x + vec.x, y + vec.y, z + vec.z);
	}

	/**
	 *
	 * <code>add</code> adds the values of a provided vector storing the values
	 * in the supplied vector.
	 *
	 * @param vec
	 *            the vector to add to this
	 * @param result
	 *            the vector to store the result in
	 * @return result returns the supplied result vector.
	 */
	public Vector3f add(Vector3f vec, Vector3f result) {
		result.x = x + vec.x;
		result.y = y + vec.y;
		result.z = z + vec.z;
		return result;
	}

	/**
	 * <code>addLocal</code> adds a provided vector to this vector internally,
	 * and returns a handle to this vector for easy chaining of calls. If the
	 * provided vector is null, null is returned.
	 *
	 * @param vec
	 *            the vector to add to this vector.
	 * @return this
	 */
	public Vector3f addLocal(Vector3f vec) {
		if (null == vec) {
			return null;
		}
		x += vec.x;
		y += vec.y;
		z += vec.z;
		return this;
	}

	/**
	 *
	 * <code>add</code> adds the provided values to this vector, creating a new
	 * vector that is then returned.
	 *
	 * @param addX
	 *            the x value to add.
	 * @param addY
	 *            the y value to add.
	 * @param addZ
	 *            the z value to add.
	 * @return the result vector.
	 */
	public Vector3f add(float addX, float addY, float addZ) {
		return new Vector3f(x + addX, y + addY, z + addZ);
	}

	/**
	 * <code>addLocal</code> adds the provided values to this vector internally,
	 * and returns a handle to this vector for easy chaining of calls.
	 *
	 * @param addX
	 *            value to add to x
	 * @param addY
	 *            value to add to y
	 * @param addZ
	 *            value to add to z
	 * @return this
	 */
	public Vector3f addLocal(float addX, float addY, float addZ) {
		x += addX;
		y += addY;
		z += addZ;
		return this;
	}

	/**
	 *
	 * <code>scaleAdd</code> multiplies this vector by a scalar then adds the
	 * given Vector3f.
	 *
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @param add
	 *            the value to add
	 */
	public void scaleAdd(float scalar, Vector3f add) {
		x = x * scalar + add.x;
		y = y * scalar + add.y;
		z = z * scalar + add.z;
	}

	/**
	 *
	 * <code>scaleAdd</code> multiplies the given vector by a scalar then adds
	 * the given vector.
	 *
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @param mult
	 *            the value to multiply the scalar by
	 * @param add
	 *            the value to add
	 */
	public void scaleAdd(float scalar, Vector3f mult, Vector3f add) {
		this.x = mult.x * scalar + add.x;
		this.y = mult.y * scalar + add.y;
		this.z = mult.z * scalar + add.z;
	}

	/**
	 *
	 * <code>dot</code> calculates the dot product of this vector with a
	 * provided vector. If the provided vector is null, 0 is returned.
	 *
	 * @param vec
	 *            the vector to dot with this vector.
	 * @return the resultant dot product of this vector and a given vector.
	 */
	public float dot(Vector3f vec) {
		if (null == vec) {
			return 0;
		}
		return x * vec.x + y * vec.y + z * vec.z;
	}

	/**
	 * Returns a new vector which is the cross product of this vector with the
	 * specified vector.
	 * <P>
	 * Neither 'this' nor v are modified. The starting value of 'result'
	 * </P>
	 *
	 * @param v
	 *            the vector to take the cross product of with this.
	 * @return the cross product vector.
	 */
	public Vector3f cross(Vector3f v) {
		return cross(v, null);
	}

	/**
	 * <code>cross</code> calculates the cross product of this vector with a
	 * parameter vector v. The result is stored in <code>result</code>
	 * <P>
	 * Neither 'this' nor v are modified. The starting value of 'result' (if
	 * any) is ignored.
	 * </P>
	 *
	 * @param v
	 *            the vector to take the cross product of with this.
	 * @param result
	 *            the vector to store the cross product result.
	 * @return result, after receiving the cross product vector.
	 */
	public Vector3f cross(Vector3f v, Vector3f result) {
		return cross(v.x, v.y, v.z, result);
	}

	/**
	 * <code>cross</code> calculates the cross product of this vector with a
	 * Vector comprised of the specified other* elements. The result is stored
	 * in <code>result</code>, without modifying either 'this' or the 'other*'
	 * values.
	 *
	 * @param otherX
	 *            x component of the vector to take the cross product of with
	 *            this.
	 * @param otherY
	 *            y component of the vector to take the cross product of with
	 *            this.
	 * @param otherZ
	 *            z component of the vector to take the cross product of with
	 *            this.
	 * @param result
	 *            the vector to store the cross product result.
	 * @return result, after receiving the cross product vector.
	 */
	public Vector3f cross(float otherX, float otherY, float otherZ, Vector3f result) {
		if (result == null)
			result = new Vector3f();
		float resX = ((y * otherZ) - (z * otherY));
		float resY = ((z * otherX) - (x * otherZ));
		float resZ = ((x * otherY) - (y * otherX));
		result.set(resX, resY, resZ);
		return result;
	}

	/**
	 * <code>crossLocal</code> calculates the cross product of this vector with
	 * a parameter vector v.
	 *
	 * @param v
	 *            the vector to take the cross product of with this.
	 * @return this.
	 */
	public Vector3f crossLocal(Vector3f v) {
		return crossLocal(v.x, v.y, v.z);
	}

	/**
	 * <code>crossLocal</code> calculates the cross product of this vector with
	 * a parameter vector v.
	 *
	 * @param otherX
	 *            x component of the vector to take the cross product of with
	 *            this.
	 * @param otherY
	 *            y component of the vector to take the cross product of with
	 *            this.
	 * @param otherZ
	 *            z component of the vector to take the cross product of with
	 *            this.
	 * @return this.
	 */
	public Vector3f crossLocal(float otherX, float otherY, float otherZ) {
		float tempx = (y * otherZ) - (z * otherY);
		float tempy = (z * otherX) - (x * otherZ);
		z = (x * otherY) - (y * otherX);
		x = tempx;
		y = tempy;
		return this;
	}

	/**
	 * <code>length</code> calculates the magnitude of this vector.
	 *
	 * @return the length or magnitude of the vector.
	 */
	public float length() {
		return FastMath.sqrt(lengthSquared());
	}

	/**
	 * <code>lengthSquared</code> calculates the squared value of the magnitude
	 * of the vector.
	 *
	 * @return the magnitude squared of the vector.
	 */
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * <code>distanceSquared</code> calculates the distance squared between this
	 * vector and vector v.
	 *
	 * @param v
	 *            the second vector to determine the distance squared.
	 * @return the distance squared between the two vectors.
	 */
	public float distanceSquared(Vector3f v) {
		double dx = x - v.x;
		double dy = y - v.y;
		double dz = z - v.z;
		return (float) (dx * dx + dy * dy + dz * dz);
	}

	public float distanceSquared2D(Vector3f v) {
		double dx = x - v.x;
		double dz = z - v.z;
		return (float) (dx * dx + dz * dz);
	}

	/**
	 * <code>distance</code> calculates the distance between this vector and
	 * vector v.
	 *
	 * @param v
	 *            the second vector to determine the distance.
	 * @return the distance between the two vectors.
	 */
	public float distance(Vector3f v) {
		return FastMath.sqrt(distanceSquared(v));
	}

	public float distance2D(Vector3f v) {
		return FastMath.sqrt(distanceSquared2D(v));
	}

	/**
	 * <code>mult</code> multiplies this vector by a scalar. The resultant
	 * vector is returned. "this" is not modified.
	 *
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @return the new vector.
	 */
	public Vector3f mult(float scalar) {
		return new Vector3f(x * scalar, y * scalar, z * scalar);
	}

	/**
	 *
	 * <code>mult</code> multiplies this vector by a scalar. The resultant
	 * vector is supplied as the second parameter and returned. "this" is not
	 * modified.
	 *
	 * @param scalar
	 *            the scalar to multiply this vector by.
	 * @param product
	 *            the product to store the result in.
	 * @return product
	 */
	public Vector3f mult(float scalar, Vector3f product) {
		if (null == product) {
			product = new Vector3f();
		}

		product.x = x * scalar;
		product.y = y * scalar;
		product.z = z * scalar;
		return product;
	}

	/**
	 * <code>multLocal</code> multiplies this vector by a scalar internally, and
	 * returns a handle to this vector for easy chaining of calls.
	 *
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @return this
	 */
	public Vector3f multLocal(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * <code>multLocal</code> multiplies a provided vector to this vector
	 * internally, and returns a handle to this vector for easy chaining of
	 * calls. If the provided vector is null, null is returned. The provided
	 * 'vec' is not modified.
	 *
	 * @param vec
	 *            the vector to mult to this vector.
	 * @return this
	 */
	public Vector3f multLocal(Vector3f vec) {
		if (null == vec) {
			return null;
		}
		x *= vec.x;
		y *= vec.y;
		z *= vec.z;
		return this;
	}

	/**
	 * Returns a new Vector instance comprised of elements which are the product
	 * of the corresponding vector elements. (N.b. this is not a cross product).
	 * <P>
	 * Neither 'this' nor 'vec' are modified.
	 * </P>
	 *
	 * @param vec
	 *            the vector to mult to this vector.
	 */
	public Vector3f mult(Vector3f vec) {
		if (null == vec) {
			return null;
		}
		return mult(vec, null);
	}

	/**
	 * Multiplies a provided 'vec' vector with this vector. If the specified
	 * 'store' is null, then a new Vector instance is returned. Otherwise,
	 * 'store' with replaced values will be returned, to facilitate chaining.
	 * </P>
	 * <P>
	 *'This' is not modified; and the starting value of 'store' (if any) is
	 * ignored (and over-written).
	 * <P>
	 * The resultant Vector is comprised of elements which are the product of
	 * the corresponding vector elements. (N.b. this is not a cross product).
	 * </P>
	 *
	 * @param vec
	 *            the vector to mult to this vector.
	 * @param store
	 *            result vector (null to create a new vector)
	 * @return 'store', or a new Vector3f
	 */
	public Vector3f mult(Vector3f vec, Vector3f store) {
		if (null == vec) {
			return null;
		}
		if (store == null)
			store = new Vector3f();
		return store.set(x * vec.x, y * vec.y, z * vec.z);
	}

	/**
	 * <code>divide</code> divides the values of this vector by a scalar and
	 * returns the result. The values of this vector remain untouched.
	 *
	 * @param scalar
	 *            the value to divide this vectors attributes by.
	 * @return the result <code>Vector</code>.
	 */
	public Vector3f divide(float scalar) {
		scalar = 1f / scalar;
		return new Vector3f(x * scalar, y * scalar, z * scalar);
	}

	/**
	 * <code>divideLocal</code> divides this vector by a scalar internally, and
	 * returns a handle to this vector for easy chaining of calls. Dividing by
	 * zero will result in an exception.
	 *
	 * @param scalar
	 *            the value to divides this vector by.
	 * @return this
	 */
	public Vector3f divideLocal(float scalar) {
		scalar = 1f / scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * <code>divide</code> divides the values of this vector by a scalar and
	 * returns the result. The values of this vector remain untouched.
	 *
	 * @param scalar
	 *            the value to divide this vectors attributes by.
	 * @return the result <code>Vector</code>.
	 */
	public Vector3f divide(Vector3f scalar) {
		return new Vector3f(x / scalar.x, y / scalar.y, z / scalar.z);
	}

	/**
	 * <code>divideLocal</code> divides this vector by a scalar internally, and
	 * returns a handle to this vector for easy chaining of calls. Dividing by
	 * zero will result in an exception.
	 *
	 * @param scalar
	 *            the value to divides this vector by.
	 * @return this
	 */
	public Vector3f divideLocal(Vector3f scalar) {
		x /= scalar.x;
		y /= scalar.y;
		z /= scalar.z;
		return this;
	}

	/**
	 *
	 * <code>negate</code> returns the negative of this vector. All values are
	 * negated and set to a new vector.
	 *
	 * @return the negated vector.
	 */
	public Vector3f negate() {
		return new Vector3f(-x, -y, -z);
	}

	/**
	 *
	 * <code>negateLocal</code> negates the internal values of this vector.
	 *
	 * @return this.
	 */
	public Vector3f negateLocal() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 *
	 * <code>subtract</code> subtracts the values of a given vector from those
	 * of this vector creating a new vector object. If the provided vector is
	 * null, null is returned.
	 *
	 * @param vec
	 *            the vector to subtract from this vector.
	 * @return the result vector.
	 */
	public Vector3f subtract(Vector3f vec) {
		return new Vector3f(x - vec.x, y - vec.y, z - vec.z);
	}

	public Vector3f subtract2D(Vector3f vec) {
		return new Vector3f(x - vec.x, 0, z - vec.z);
	}

	/**
	 * <code>subtractLocal</code> subtracts a provided vector to this vector
	 * internally, and returns a handle to this vector for easy chaining of
	 * calls. If the provided vector is null, null is returned.
	 *
	 * @param vec
	 *            the vector to subtract
	 * @return this
	 */
	public Vector3f subtractLocal(Vector3f vec) {
		if (null == vec) {
			return null;
		}
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		return this;
	}

	/**
	 *
	 * <code>subtract</code>
	 *
	 * @param vec
	 *            the vector to subtract from this
	 * @param result
	 *            the vector to store the result in
	 * @return result
	 */
	public Vector3f subtract(Vector3f vec, Vector3f result) {
		if (result == null) {
			result = new Vector3f();
		}
		result.x = x - vec.x;
		result.y = y - vec.y;
		result.z = z - vec.z;
		return result;
	}

	/**
	 *
	 * <code>subtract</code> subtracts the provided values from this vector,
	 * creating a new vector that is then returned.
	 *
	 * @param subtractX
	 *            the x value to subtract.
	 * @param subtractY
	 *            the y value to subtract.
	 * @param subtractZ
	 *            the z value to subtract.
	 * @return the result vector.
	 */
	public Vector3f subtract(float subtractX, float subtractY, float subtractZ) {
		return new Vector3f(x - subtractX, y - subtractY, z - subtractZ);
	}

	/**
	 * <code>subtractLocal</code> subtracts the provided values from this vector
	 * internally, and returns a handle to this vector for easy chaining of
	 * calls.
	 *
	 * @param subtractX
	 *            the x value to subtract.
	 * @param subtractY
	 *            the y value to subtract.
	 * @param subtractZ
	 *            the z value to subtract.
	 * @return this
	 */
	public Vector3f subtractLocal(float subtractX, float subtractY, float subtractZ) {
		x -= subtractX;
		y -= subtractY;
		z -= subtractZ;
		return this;
	}

	/**
	 * <code>normalize</code> returns the unit vector of this vector.
	 *
	 * @return unit vector of this vector.
	 */
	public Vector3f normalize() {
		float length = length();
		if (length != 0) {
			return divide(length);
		}

		return divide(1);
	}

	/**
	 * <code>normalizeLocal</code> makes this vector into a unit vector of
	 * itself.
	 *
	 * @return this.
	 */
	public Vector3f normalizeLocal() {
		float length = length();
		if (length != 0) {
			return divideLocal(length);
		}

		return this;
	}

	/**
	 * <code>zero</code> resets this vector's data to zero internally.
	 */
	public void zero() {
		x = y = z = 0;
	}

	/**
	 * <code>angleBetween</code> returns (in radians) the angle between two
	 * vectors. It is assumed that both this vector and the given vector are
	 * unit vectors (iow, normalized).
	 *
	 * @param otherVector
	 *            a unit vector to find the angle against
	 * @return the angle in radians.
	 */
	public float angleBetween(Vector3f otherVector) {
		float dotProduct = dot(otherVector);
        return FastMath.acos(dotProduct);
	}

	/**
	 * Sets this vector to the interpolation by changeAmnt from this to the
	 * finalVec this=(1-changeAmnt)*this + changeAmnt * finalVec
	 *
	 * @param finalVec
	 *            The final vector to interpolate towards
	 * @param changeAmnt
	 *            An amount between 0.0 - 1.0 representing a percentage change
	 *            from this towards finalVec
	 */
	public void interpolate(Vector3f finalVec, float changeAmnt) {
		this.x = (1 - changeAmnt) * this.x + changeAmnt * finalVec.x;
		this.y = (1 - changeAmnt) * this.y + changeAmnt * finalVec.y;
		this.z = (1 - changeAmnt) * this.z + changeAmnt * finalVec.z;
	}
	
	
	public Vector3f lerp(Vector3f finalVec, float changeAmnt) {
		float x = (1 - changeAmnt) * this.x + changeAmnt * finalVec.x;
		float y = (1 - changeAmnt) * this.y + changeAmnt * finalVec.y;
		float z = (1 - changeAmnt) * this.z + changeAmnt * finalVec.z;
		return new Vector3f(x,y,z);
	}

	/**
	 * Sets this vector to the interpolation by changeAmnt from beginVec to
	 * finalVec this=(1-changeAmnt)*beginVec + changeAmnt * finalVec
	 *
	 * @param beginVec
	 *            the beginning vector (changeAmnt=0)
	 * @param finalVec
	 *            The final vector to interpolate towards
	 * @param changeAmnt
	 *            An amount between 0.0 - 1.0 representing a percentage change
	 *            from beginVec towards finalVec
	 */
	public void interpolate(Vector3f beginVec, Vector3f finalVec, float changeAmnt) {
		this.x = (1 - changeAmnt) * beginVec.x + changeAmnt * finalVec.x;
		this.y = (1 - changeAmnt) * beginVec.y + changeAmnt * finalVec.y;
		this.z = (1 - changeAmnt) * beginVec.z + changeAmnt * finalVec.z;
	}

	/**
	 * Check a vector... if it is null or its floats are NaN or infinite, return
	 * false. Else return true.
	 *
	 * @param vector
	 *            the vector to check
	 * @return true or false as stated above.
	 */
	public static boolean isValidVector(Vector3f vector) {
		if (vector == null)
			return false;
		if (Float.isNaN(vector.x) || Float.isNaN(vector.y) || Float.isNaN(vector.z))
			return false;
        return !Float.isInfinite(vector.x) && !Float.isInfinite(vector.y) && !Float.isInfinite(vector.z);
    }

	public static void generateOrthonormalBasis(Vector3f u, Vector3f v, Vector3f w) {
		w.normalizeLocal();
		generateComplementBasis(u, v, w);
	}

	public static void generateComplementBasis(Vector3f u, Vector3f v, Vector3f w) {
		float fInvLength;

		if (FastMath.abs(w.x) >= FastMath.abs(w.y)) {
			// w.x or w.z is the largest magnitude component, swap them
			fInvLength = FastMath.invSqrt(w.x * w.x + w.z * w.z);
			u.x = -w.z * fInvLength;
			u.y = 0.0f;
			u.z = +w.x * fInvLength;
			v.x = w.y * u.z;
			v.y = w.z * u.x - w.x * u.z;
			v.z = -w.y * u.x;
		} else {
			// w.y or w.z is the largest magnitude component, swap them
			fInvLength = FastMath.invSqrt(w.y * w.y + w.z * w.z);
			u.x = 0.0f;
			u.y = +w.z * fInvLength;
			u.z = -w.y * fInvLength;
			v.x = w.y * u.z - w.z * u.y;
			v.y = -w.x * u.z;
			v.z = w.x * u.y;
		}
	}

	@Override
	public Vector3f clone() {
		try {
			return (Vector3f) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // can not happen
		}
	}

	/**
	 * Saves this Vector3f into the given float[] object.
	 *
	 * @param floats
	 *            The float[] to take this Vector3f. If null, a new float[3] is
	 *            created.
	 * @return The array, with X, Y, Z float values in that order
	 */
	public float[] toArray(float[] floats) {
		if (floats == null) {
			floats = new float[3];
		}
		floats[0] = x;
		floats[1] = y;
		floats[2] = z;
		return floats;
	}

	/**
	 * are these two vectors the same? they are is they both have the same x,y,
	 * and z values.
	 *
	 * @param o
	 *            the object to compare for equality
	 * @return true if they are equal
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Vector3f)) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Vector3f comp = (Vector3f) o;
		if (Float.compare(x, comp.x) != 0)
			return false;
		if (Float.compare(y, comp.y) != 0)
			return false;
        return Float.compare(z, comp.z) == 0;
    }

	/**
	 * <code>hashCode</code> returns a unique code for this vector object based
	 * on it's values. If two vectors are logically equivalent, they will return
	 * the same hash code value.
	 *
	 * @return the hash code value of this vector.
	 */
	@Override
	public int hashCode() {
		int hash = 37;
		hash += 37 * hash + Float.floatToIntBits(x);
		hash += 37 * hash + Float.floatToIntBits(y);
		hash += 37 * hash + Float.floatToIntBits(z);
		return hash;
	}

	/**
	 * Used with serialization. Not to be called manually.
	 *
	 * @param in
	 *            input
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @see java.io.Externalizable
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}

	/**
	 * Used with serialization. Not to be called manually.
	 *
	 * @param out
	 *            output
	 * @throws IOException
	 * @see java.io.Externalizable
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	/**
	 * @param index
	 * @return x value if index == 0, y value if index == 1 or z value if index
	 *         == 2
	 * @throws IllegalArgumentException
	 *             if index is not one of 0, 1, 2.
	 */
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

	/**
	 * @param index
	 *            which field index in this vector to set.
	 * @param value
	 *            to set to one of x, y or z.
	 * @throws IllegalArgumentException
	 *             if index is not one of 0, 1, 2.
	 */
	public void set(int index, float value) {
		switch (index) {
		case 0:
			x = value;
			return;
		case 1:
			y = value;
			return;
		case 2:
			z = value;
			return;
		}
		throw new IllegalArgumentException("index must be either 0, 1 or 2");
	}

	/**
	 * Gets an offset from this position based on rotation around Y(up/down)-axis.
	 *
	 * @param rotation
	 *            Rotation in radians
	 * @param xOffset
	 *            Amount to offset along x axis (left negative, right positive)
	 * @param yOffset
	 *            Amount to offset along y axis (down negative, up positive)
	 * @param zOffset
	 *            Amount to offset along z axis (backwards negative, forwards positive)
	 * @param invertZ
	 *            whether to invert the z axis
	 */
	public Vector3f getOffset(float rotation, float xOffset, float yOffset, float zOffset, boolean invertZ) {
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
		return loc;
	}

	/**
	 * Returns the 2D face direction from rotation.
	 *
	 * @param rotation
	 *            Rotation in radians
	 */
	public static Vector3f getFaceDir(float rotation) {
		return new Vector3f(FastMath.sin(rotation), 0f, FastMath.cos(rotation));
	}

	/**
	 * Returns the 2D cross direction (perpendicular face direction) from rotation.
	 *
	 * @param rotation
	 *            Rotation in radians
	 */
	public static Vector3f getCrossDir(float rotation) {
		return new Vector3f(FastMath.cos(rotation), 0f, FastMath.sin(rotation));
	}

	/**
	 * Returns the 2D rotation (around Y-axis) in radians.
	 *
	 * @return
	 */
	public float getRotation() {
		return 3.14f + FastMath.atan2(-x, -z);
	}

	/**
	 * Gets the XYZ component of this Vector3f
	 *
	 * @return
	 */
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

	public synchronized void setLat(float lat) {
		this.x = lat;
	}

	public synchronized void setLong(float lon) {
		this.z = lon;
	}

	public synchronized void setAlt(float alt) {
		this.y = alt;
	}
	
	public static Vector3f rotateAroundPoint(Vector3f origin, Vector3f point, double angle) {

		float angleRadians;
		double modifiedAngle;

		// Convert angle to radians

		modifiedAngle = angle;

		if (angle < 0)
			modifiedAngle = 360 + modifiedAngle;

		angleRadians = (float) Math.toRadians(modifiedAngle);

		return rotateAroundPoint(origin, point, angleRadians);
	}

	public static Vector3f rotateAroundPoint(Vector3f origin, Vector3f point, float radians) {

		Vector3f outVector;
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

	@Override
	public String toString() {
		String out = "";
		out += "x=" + x + ", ";
		out += "y=" + y + ", ";
		out += "z=" + z;
		return out;
	}

	public static Vector3f min(Vector3f vectorA, Vector3f vectorB) {

		return new Vector3f(Math.min(vectorA.x, vectorB.x),
				Math.min(vectorA.y, vectorB.y),
				Math.min(vectorA.z, vectorB.z));
	}

	public static Vector3f max(Vector3f vectorA, Vector3f vectorB) {

		return new Vector3f(Math.max(vectorA.x, vectorB.x),
				Math.max(vectorA.y, vectorB.y),
				Math.max(vectorA.z, vectorB.z));
	}
	
	public static Vector3f rotateAroundPoint(Vector3f origin, Vector3f point,Quaternion angleRotation) {

		Vector3f outVector;
		Vector3f directionVector;
		// Build direction vector relative to origin
		directionVector = new Vector3f(point.subtract(origin));
		directionVector = angleRotation.mult(directionVector);

		// Translate from origin back to new rotated point

		outVector = origin.add(directionVector);

		return outVector;

	}

}
