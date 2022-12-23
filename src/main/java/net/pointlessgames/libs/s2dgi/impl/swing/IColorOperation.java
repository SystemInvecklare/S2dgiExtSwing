package net.pointlessgames.libs.s2dgi.impl.swing;

/*package-protected*/ interface IColorOperation {
	void apply(int[] pixel, int offset, int bands);
	boolean isNoop();
}
