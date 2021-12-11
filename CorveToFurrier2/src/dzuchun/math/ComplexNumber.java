package dzuchun.math;

/**
 * Simple complex number (CN) implementation
 *
 * @author dzu
 *
 */
public class ComplexNumber implements Cloneable {

	/**
	 * @param angle
	 * @return exp(i*angle)
	 */
	public static ComplexNumber rotation(double angle) {
		return new ComplexNumber(Math.cos(angle), Math.sin(angle));
	}

	/**
	 * CN real part (Re)
	 */
	private double real;
	/**
	 * CN imaginary part (Im)
	 */
	private double imaginary;

	/**
	 * Creates zero CN
	 */
	public ComplexNumber() {
		this.real = 0d;
		this.imaginary = 0d;
	}

	/**
	 * Creates CN with specified integer Re and Im
	 *
	 * @param real
	 * @param imaginary
	 */
	public ComplexNumber(int real, int imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}

	/**
	 * Creates CN with specified double Re and Im
	 *
	 * @param real
	 * @param imaginary
	 */
	public ComplexNumber(double real, double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}

	/**
	 * Adds one CN to another. If createNew is true, result is saved to a new CN.
	 *
	 * @param addition
	 * @param createNew
	 * @return result of the operation
	 */
	public ComplexNumber add(ComplexNumber addition, boolean createNew) {
		if (createNew) {
			return new ComplexNumber(this.real + addition.real, this.imaginary + addition.imaginary);
		} else {
			this.real += addition.real;
			this.imaginary += addition.imaginary;
			this.cachedArgumentFlag = false;
			this.cachedAbsoluteFlag = false;
			return this;
		}
	}

	/**
	 * Adds one CN to another.
	 *
	 * @param addition
	 * @return
	 */
	public ComplexNumber add(ComplexNumber addition) {
		return this.add(addition, false);
	}

	/**
	 * Adds specified Re and Im to CN
	 *
	 * @param real
	 * @param imaginary
	 */
	public void add(int real, int imaginary) {
		this.real += real;
		this.imaginary += imaginary;
		this.cachedArgumentFlag = false;
		this.cachedAbsoluteFlag = false;
	}

	private double tmp1;

	/**
	 * Multiplies one CN by another. If createNew is true, result is saved to a new
	 * CN.
	 *
	 * @param multiplier
	 * @param createNew
	 * @return result of the operation
	 */
	public ComplexNumber multiply(ComplexNumber multiplier, boolean createNew) {
		if (createNew) {
			return new ComplexNumber(this.real * multiplier.real - this.imaginary * multiplier.imaginary,
					this.imaginary * multiplier.real + this.real * multiplier.imaginary);
		} else {
			tmp1 = this.real * multiplier.real - this.imaginary * multiplier.imaginary;
			this.imaginary = this.imaginary * multiplier.real + this.real * multiplier.imaginary;
			this.real = tmp1;
			this.cachedArgumentFlag = false;
			this.cachedAbsoluteFlag = false;
			return this;
		}
	}

	/**
	 * Adds specified Re and Im to CN
	 *
	 * @param real
	 * @param imaginary
	 */
	public ComplexNumber multiply(ComplexNumber multiplier) {
		return this.multiply(multiplier, false);
	}

	private double tmp2;

	/**
	 * Multiplies CN by specified Re and Im
	 *
	 * @param real
	 * @param imaginary
	 */
	public ComplexNumber multiply(int real, int imaginary) {
		tmp2 = this.real * real - this.imaginary * imaginary;
		this.imaginary = this.imaginary * real + this.real * imaginary;
		this.real = tmp2;
		this.cachedArgumentFlag = false;
		this.cachedAbsoluteFlag = false;
		return this;
	}

	/**
	 * Multiplies CN by specified real value
	 *
	 * @param real
	 */
	public ComplexNumber multiply(double real) {
		this.real *= real;
		this.imaginary *= real;
		this.cachedArgumentFlag = false;
		this.cachedAbsoluteFlag = false;
		return this;
	}

	/**
	 * Indicates, if Arg is valid
	 */
	private boolean cachedArgumentFlag = false;
	/**
	 * Last valid Arg
	 */
	private double cachedArgument;

	/**
	 * Returns argument (Arg) of a CN
	 *
	 * @return Arg
	 */
	public double getArgument() {
		if (cachedArgumentFlag) {
			return cachedArgument;
		} else {
			cachedArgumentFlag = true;
			return cachedArgument = Math.atan2(this.imaginary, this.real);
		}
	}

	/**
	 * Indicates, if Abs is valid
	 */
	private boolean cachedAbsoluteFlag = false;
	/**
	 * Last valid Abs
	 */
	private double cachedAbsolute;

	/**
	 * Returns absolute (Abs) of a CN
	 *
	 * @return Abs
	 */
	public double getAbsolute() {
		if (cachedAbsoluteFlag) {
			return cachedAbsolute;
		} else {
			cachedAbsoluteFlag = true;
			return cachedAbsolute = Math.sqrt(this.real * this.real + this.imaginary * this.imaginary);
		}
	}

	private double trimCoefficient;

	/**
	 * Scales CN so it's Abs is trim
	 *
	 * @param trim
	 */
	public void trimAbsolute(double trim) {
		trimCoefficient = trim / getAbsolute();
		real *= trimCoefficient;
		imaginary *= trimCoefficient;
		this.cachedAbsoluteFlag = false;
	}

	/**
	 * Turns CN to angle radians. If createNew is true, result is saved to a new CN
	 *
	 * @param angle
	 * @param createNew
	 * @return
	 */
	public ComplexNumber turn(double angle, boolean createNew) {
		this.cachedArgumentFlag = false;
		return this.multiply(ComplexNumber.rotation(angle), createNew);
	}

	/**
	 * @return Int-rounded CN Re
	 */
	public int getIntReal() {
		return (int) Math.round(this.real);
	}

	/**
	 * @return Int-rounded CN Im
	 */
	public int getIntImaginary() {
		return (int) Math.round(this.imaginary);
	}

	/**
	 * Creates CN clone
	 */
	@Override
	public ComplexNumber clone() {
		return new ComplexNumber(this.real, this.imaginary);
	}

	/**
	 * String representation of CN
	 */
	@Override
	public String toString() {
		return String.format("%.4f + %.4fi", this.real, this.imaginary);
	}
}