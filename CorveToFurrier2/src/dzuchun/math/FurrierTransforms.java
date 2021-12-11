package dzuchun.math;

import java.util.Collection;

public class FurrierTransforms {
	public static ComplexNumber[] setDescrete(Double[] freq, Collection<ComplexNumber> data) {
		ComplexNumber[] res = new ComplexNumber[freq.length];
		int no;
		double f, angleDelta, norm = 1.0 / data.size();
		ComplexNumber sum;
		for (int i = 0; i < freq.length; i++) {
			f = freq[i];
			sum = new ComplexNumber();
			angleDelta = -2.0 * Math.PI * f / data.size();
			no = 0;
			for (ComplexNumber cn : data) {
				no++;
				sum.add(cn.turn(angleDelta * no, true), false);
			}
			res[i] = sum.multiply(norm);
		}
		return res;
	}
}
