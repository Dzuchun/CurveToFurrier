package dzuchun.util;

import java.awt.Color;
import java.util.function.Function;

public class Util {

	public static Color getRainbowColor(double d) {
		return new Color(0.5f*(float)Math.cos(2 * Math.PI * d)+0.5f, 0.5f*(float)Math.cos(2 * Math.PI * d+2*Math.PI/3)+0.5f, 0.5f*(float)Math.cos(2 * Math.PI * d-2*Math.PI/3)+0.5f);
	}

	public static Integer[] intArray(int size) {
		Integer[] res = new Integer[size];
		for (int i=0; i<size; i++) {
			res[i]=i;
		}
		return res;
	}

	public static <T> T[] reshaft(Integer[] shaft, T[] obj){
		T[] res = obj.clone();
		for (int i=0; i<shaft.length; i++) {
			res[i] = obj[shaft[i]];
		}
		return res;
	}

	public static <T> String iterableToString(Iterable<T> iterable) {
		return iterableToString(iterable, Object::toString);
	}

	public static <T> String iterableToString(Iterable<T> iterable, Function<T, String> customToString) {
		if (!iterable.iterator().hasNext()) {
			return String.format("%s[]", iterable.getClass().getName());
		}
		String res = "";
		for (T o : iterable) {
			res += customToString.apply(o) + ", ";
		}
		res = res.substring(0, res.length() - 2);
		return String.format("%s[%s]", iterable.getClass().getName(), res);
	}
}
