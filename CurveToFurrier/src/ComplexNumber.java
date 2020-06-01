

public class ComplexNumber implements Cloneable {
	
	public static ComplexNumber rotation (double angle) {
		return new ComplexNumber(Math.cos(angle), Math.sin(angle));
	}
	
	private double real;
	private double imaginary;
	
	public ComplexNumber() {
		this.real = 0d;
		this.imaginary = 0d;
	}
	
	public ComplexNumber(int real, int imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public ComplexNumber(double real, double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public void add(ComplexNumber addition) {
		this.real += addition.real;
		this.imaginary += addition.imaginary;
	}
	
	public void add(int real, int imaginary) {
		this.real += real;
		this.imaginary += imaginary;
	}
	
	private double tmp;
	public void multiply (ComplexNumber multiplier) {
		tmp = this.real*multiplier.real - this.imaginary*multiplier.imaginary;
		this.imaginary = this.imaginary*multiplier.real + this.real*multiplier.imaginary;
		this.real = tmp;
	}
	
	public void multiply (int real, int imaginary) {
		tmp = this.real*real - this.imaginary*imaginary;
		this.imaginary = this.imaginary*real + this.real*imaginary;
		this.real = tmp;
	}
	
	public void divide (int divisor) {
		this.real /= divisor;
		this.imaginary /= divisor;
	}
	
	public void divide (ComplexNumber divisor) {
		tmp = (this.real*divisor.real + this.imaginary*divisor.imaginary)/(divisor.real*divisor.real - divisor.imaginary*divisor.imaginary);
		this.imaginary = (-this.real*divisor.imaginary + this.imaginary*divisor.real)/(divisor.real*divisor.real - divisor.imaginary*divisor.imaginary);
		this.real = tmp;
	}
	
	public void divide (int real, int imaginary) {
		tmp = (this.real*real + this.imaginary*imaginary)/(real*real - imaginary*imaginary);
		this.imaginary = (-this.real*imaginary + this.imaginary*real)/(real*real - imaginary*imaginary);
		this.real = tmp;
	}
	
	public double getArgument() {
		return Math.atan(this.imaginary/this.real);
	}
	
	public double getAbsolute() {
		return Math.sqrt(real*real + imaginary*imaginary);
	}
	
	private double trimCoefficient;
	public void trimAbsolute(double trim) {
		trimCoefficient = trim/getAbsolute();
		real *= trimCoefficient;
		imaginary *= trimCoefficient;
	}
	
	public void turn(double angle) {
		real = Math.cos(angle) * real - Math.sin(angle) * imaginary;
		imaginary = Math.sin(angle) * real + Math.cos(angle) * imaginary;
	}
	
	public int getReal() {
		return (int) Math.round(this.real);
	}
	
	public int getImaginary() {
		return (int) Math.round(this.imaginary);
	}
	
	@Override
	public ComplexNumber clone() {
		return new ComplexNumber(real, imaginary);
	}
	
	@Override
	public String toString() {
		return real + " + " + imaginary + "i";
	}
}
