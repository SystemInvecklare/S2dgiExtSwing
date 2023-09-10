package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import net.pointlessgames.libs.s2dgi.impl.swing.SwingCanvas.IPaintCommand;
import net.pointlessgames.libs.s2dgi.impl.swing.SwingCanvas.IRenderState;
import net.pointlessgames.libs.s2dgi.texture.ITexture;
import net.pointlessgames.libs.s2dgi.util.ImplementationUtil;

/*package-private*/ class SwingTexture implements ITexture {
	private final BufferedImage bufferedImage;
	private final int offsetX;
	private final int offsetY;
	private final int width;
	private final int height;
	
	public SwingTexture(BufferedImage bufferedImage) {
		this(bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
	}

	private SwingTexture(BufferedImage bufferedImage, int offsetX, int offsetY, int width, int height) {
		this.bufferedImage = bufferedImage;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void renderIn(SwingCanvas canvas, int sourceX, int sourceY, int sourceWidth, int sourceHeight, int x, int y, int width, int height) {
		canvas.addCommand(new IPaintCommand() {
			@Override
			public void execute(IRenderState renderState, Graphics2D g2d) {
				g2d.drawImage(bufferedImage, x, y, x + width, y + height, offsetX + sourceX, offsetY + sourceY, offsetX + sourceX + sourceWidth, offsetY + sourceY + sourceHeight, null);
			}
		});
	}
	
	public void renderIn(SwingCanvas canvas, int sourceX, int sourceY, int sourceWidth, int sourceHeight, int x, int y, int origWidth, int origHeight, int quarterRotations, boolean flipX, boolean flipY, boolean rotateDimensions) {
		canvas.addCommand(new IPaintCommand() {
			@Override
			public void execute(IRenderState renderState, Graphics2D g2d) {
				AffineTransform oldTransform = g2d.getTransform();
				int rotations = ImplementationUtil.mod(quarterRotations, 4);
				int width = origWidth;
				int height = origHeight;
				if(flipX || flipY) {
					g2d.translate(x + (flipX ? width : 0), y + (flipY ? height : 0));
					g2d.scale(flipX ? -1 : 1, flipY ? -1 : 1);
					g2d.translate(-x, -y);
				}
				if(rotations != 0) {
					if(!rotateDimensions && rotations%2 == 1) {
						width = origHeight;
						height = origWidth;
					}
					int xmove = rotations == 1 ? height : (rotations == 2 ? width : 0);
					int ymove = rotations == 2 ? height : (rotations == 3 ? width : 0);
					g2d.translate(x+xmove, y+ymove);
					g2d.rotate(rotations*Math.PI/2);
					g2d.translate(-x, -y);
				}
				g2d.drawImage(bufferedImage, x, y, x + width, y + height, offsetX + sourceX, offsetY + sourceY, offsetX + sourceX + sourceWidth, offsetY + sourceY + sourceHeight, null);
				g2d.setTransform(oldTransform);
			}
		});
	}

	@Override
	public ITexture createSubTexture(int sourceX, int sourceY, int width, int height) {
		return new SwingTexture(bufferedImage, offsetX + sourceX, offsetY + sourceY, width, height);
	}
}
