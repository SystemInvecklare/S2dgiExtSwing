package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Color;

import net.pointlessgames.libs.s2dgi.color.IColorInterpreter;

/*package-protected*/ class SwingColorUtil {
	public static void applyColorEffects(int[] tint, int[] additive, int[] color) {
		applyColorEffects(tint, 0, 4, additive, 0, 4, color, 0, 4);
	}
	
	public static void applyColorEffects(int[] tint, int tintOffset, int tintBands, int[] additive, int additiveOffset, int additiveBands, int[] color, int colorOffset, int colorBands) {
		if(color == null) {
			return;
		}
		if(tint != null && !isWhite(tint, tintOffset, tintBands)) {
			combine(color, colorOffset, colorBands, tint, tintOffset, tintBands, MULTIPLY, color, colorOffset, colorBands);
		}
		if(additive != null && !isZero(additive, additiveOffset, additiveBands)) {
			combine(color, colorOffset, colorBands, additive, additiveOffset, additiveBands, ADD, color, colorOffset, colorBands);
		}
	}

	private static boolean isZero(int[] color, int colorOffset, int colorBands) {
		switch (colorBands) {
			case 1: return color[colorOffset] == 0;
			case 2: return color[colorOffset] == 0 && color[colorOffset+1] == 0;
			case 3: return color[colorOffset] == 0 && color[colorOffset+1] == 0 && color[colorOffset+2] == 0;
		default:
			return color[colorOffset] == 0 && color[colorOffset+1] == 0 && color[colorOffset+2] == 0 && color[colorOffset+3] == 0;
		}
	}

	private static boolean isWhite(int[] color, int colorOffset, int colorBands) {
		switch (colorBands) {
		case 1: return color[colorOffset] == 255;
		case 2: return color[colorOffset] == 255 && color[colorOffset+1] == 255;
		case 3: return color[colorOffset] == 255 && color[colorOffset+1] == 255 && color[colorOffset+2] == 255;
	default:
		return color[colorOffset] == 255 && color[colorOffset+1] == 255 && color[colorOffset+2] == 255 && color[colorOffset+3] == 255;
	}
	}

	private static void combine(int[] color1, int color1Offset, int color1Bands, int[] color2, int color2Offset, int color2Bands, IComponentOperation op, int[] result, int resultOffset, int resultBands) {
		for(int i = 0; i < resultBands; ++i) {
			int color1Value = i < color1Bands ? color1[color1Offset + i] : IComponentOperation.MISSING;
			int color2Value = i < color2Bands ? color2[color2Offset + i] : IComponentOperation.MISSING;
			result[resultOffset + i] = op.apply(color1Value, color2Value, i == 3);
		}
	}
	
	private static final IComponentOperation ADD = new IComponentOperation() {
		@Override
		public int apply(int c1, int c2, boolean isAlpha) {
			if(c1 == MISSING) {
				c1 = 0;
			}
			if(c2 == MISSING) {
				c2 = 0;
			}
			return Math.min(c1 + c2, 255);
		}
	};
	
	private static final IComponentOperation MULTIPLY = new IComponentOperation() {
		@Override
		public int apply(int c1, int c2, boolean isAlpha) {
			if(c1 == MISSING) {
				c1 = 255;
			}
			if(c2 == MISSING) {
				c2 = 255;
			}
			return c1*c2/255;
		}
	};
	
	private interface IComponentOperation {
		public static final int MISSING = Integer.MIN_VALUE;
		int apply(int c1, int c2, boolean isAlpha);
	}
	
	// Util for Color
	public static Color applyColorEffects(Color tint, Color additive, Color color) {
		int[] tintArray = toArray(tint);
		int[] additiveArray = toArray(additive);
		int[] colorArray = toArray(color);
		SwingColorUtil.applyColorEffects(tintArray, additiveArray, colorArray);
		return toColor(colorArray);
	}

	public static Color toColor(int[] array) {
		if(array == null) {
			return null;
		}
		return new Color(array[0], array[1], array[2], array[3]);
	}
	
	public static int[] toArray(Color color, int[] result) {
		result[0] = color.getRed();
		result[1] = color.getGreen();
		result[2] = color.getBlue();
		result[3] = color.getAlpha();
		return result;
	}

	public static int[] toArray(Color color) {
		if(color == null) {
			return null;
		}
		return toArray(color, new int[4]);
	}

	public static  <C> Color convertColor(C color, IColorInterpreter<C> colorInterpreter) {
		if(color instanceof Color) {
			return (Color) color;
		}
		return new Color(colorInterpreter.getRed(color),
						 colorInterpreter.getGreen(color),
						 colorInterpreter.getBlue(color),
						 colorInterpreter.getAlpha(color));
	}

	public static <C> void setArray(int[] array, C color, IColorInterpreter<C> colorInterpreter) {
		array[0] = colorInterpreter.getRed(color);
		array[1] = colorInterpreter.getGreen(color);
		array[2] = colorInterpreter.getBlue(color);
		array[3] = colorInterpreter.getAlpha(color);
	}
}
