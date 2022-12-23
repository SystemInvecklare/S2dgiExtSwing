package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import net.pointlessgames.libs.s2dgi.clipboard.ISimpleClipboard;
import net.pointlessgames.libs.s2dgi.color.IColorInterpreter;
import net.pointlessgames.libs.s2dgi.core.ISimple2DGraphics;
import net.pointlessgames.libs.s2dgi.impl.swing.SwingCanvas.IPaintCommand;
import net.pointlessgames.libs.s2dgi.impl.swing.SwingCanvas.IRenderState;
import net.pointlessgames.libs.s2dgi.key.IKeyListener;
import net.pointlessgames.libs.s2dgi.mouse.IMouseListener;
import net.pointlessgames.libs.s2dgi.mouse.IMouseListener.MouseButton;
import net.pointlessgames.libs.s2dgi.texture.ITexture;
import net.pointlessgames.libs.s2dgi.util.ImplementationUtil;
import net.pointlessgames.libs.s2dgi.window.IClippingRectangle;
import net.pointlessgames.libs.s2dgi.window.IGraphics;
import net.pointlessgames.libs.s2dgi.window.IWindow;

public class SwingSimple2DGraphics implements ISimple2DGraphics {
	public static final IResourceLocator DEFAULT_RESOURCE_LOCATOR = new SimpleResourceLocator(new File("src/main/resources"));
	
	private IResourceLocator resourceLocator = DEFAULT_RESOURCE_LOCATOR;
	private final ISimpleClipboard clipboard = new SwingClipboard();
	private Window mainWindow;

	
	public SwingSimple2DGraphics(int width, int height) {
		this(width, height, 1);
	}
	
	public SwingSimple2DGraphics(int width, int height, int pixelDepth) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		SwingCanvas canvas = new SwingCanvas(pixelDepth);
		canvas.setPreferredSize(new Dimension(width, height));
		canvas.setMaximumSize(canvas.getPreferredSize());
		canvas.setMinimumSize(canvas.getPreferredSize());
		frame.add(canvas);
		this.mainWindow = new Window(frame, canvas, width/pixelDepth, height/pixelDepth, pixelDepth);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public SwingSimple2DGraphics setResourceLocator(IResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
		return this;
	}
	

	public SwingSimple2DGraphics setResourceLocation(String directory) {
		return setResourceLocator(new SimpleResourceLocator(new File(directory)));
	}


	@Override
	public IWindow getMainWindow() {
		return mainWindow;
	}
	
	@Override
	public ISimpleClipboard getClipboard() {
		return clipboard;
	}

	@Override
	public ITexture loadTexture(String path) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(resourceLocator.locate(path));
		return new SwingTexture(bufferedImage);
	}

	
	private static class Window implements IWindow {
		private final JFrame frame;
		private final SwingCanvas canvas;
		private int width;
		private int height;
		private final MutableClip currentClip = new MutableClip();
		
		private final SwingInputCache inputCache = new SwingInputCache();
		private final List<IMouseListener> mouseListeners = new ArrayList<IMouseListener>();
		private final List<IKeyListener> keyListeners = new ArrayList<IKeyListener>();
		
		private final IGraphics graphics = new IGraphics() {
			
			@Override
			public void render(ITexture texture, int x, int y) {
				SwingTexture swingTexture = (SwingTexture) texture;
				swingTexture.renderIn(canvas, 0, 0, swingTexture.getWidth(), swingTexture.getHeight(), x, y, swingTexture.getWidth(), swingTexture.getHeight());
			}

			@Override
			public void render(ITexture texture, int x, int y, int width, int height) {
				SwingTexture swingTexture = (SwingTexture) texture;
				swingTexture.renderIn(canvas, 0, 0, swingTexture.getWidth(), swingTexture.getHeight(), x, y, width, height);
			}
			
			public void render(ITexture texture, int x, int y, int width, int height, int quarterRotations, boolean flipX, boolean flipY, boolean rotateDimensions) {
				SwingTexture swingTexture = (SwingTexture) texture;
				swingTexture.renderIn(canvas, 0, 0, swingTexture.getWidth(), swingTexture.getHeight(), x, y, width, height, quarterRotations, flipX, flipY, rotateDimensions);
			}

			@Override
			public void renderTiled(ITexture texture, int offsetX, int offsetY, int x, int y, int width,
					int height) {
				SwingTexture swingTexture = (SwingTexture) texture;
				final int tileWidth = swingTexture.getWidth();
				final int tileHeight = swingTexture.getHeight();
				int tileOffsetX = ImplementationUtil.mod(offsetX, tileWidth);
				int tileOffsetY = ImplementationUtil.mod(offsetY, tileHeight);
				int cx = x + tileOffsetX - tileWidth;
				while(cx < x + width) {
					if(cx + tileWidth > x) {
						int cy = y + tileOffsetY - tileHeight;
						while(cy < y + height) {
							if(cy + tileHeight > y) {
								renderPart(swingTexture, cx, cy, tileWidth, tileHeight, x, y, width, height);
							}
							cy += tileHeight;
						}
					}
					cx += tileWidth;
				}
			}
			
			private void renderPart(SwingTexture swingTexture, int x, int y, int width, int height, int boundsX, int boundsY, int boundsWidth, int boundsHeight) {
				int x1 = x;
				int y1 = y;
				int x2 = x + width;
				int y2 = y + height;
				if(x1 < boundsX) {
					x1 = boundsX;
				}
				if(y1 < boundsY) {
					y1 = boundsY;
				}
				if(x2 > boundsX + boundsWidth) {
					x2 = boundsX + boundsWidth;
				}
				if(y2 > boundsY + boundsHeight) {
					y2 = boundsY + boundsHeight;
				}
				if(x2 - x1 > 0 && y2 - y1 > 0) {
					swingTexture.renderIn(canvas, x1 - x, y1 - y, x2 - x1, y2 - y1, x1, y1, x2 - x1, y2 - y1);
				}
			}
			
			@Override
			public <C> void renderRectangle(int x, int y, int width, int height, C color,
					IColorInterpreter<C> colorInterpreter) {
				final Color immutableColor = toAwtColor(color, colorInterpreter);
				canvas.addCommand(new IPaintCommand() {
					@Override
					public void execute(IRenderState renderState, Graphics2D g2d) {
						g2d.setColor(renderState.getFinalColor(immutableColor, AWT_COLOR_INTERPRETER));
						g2d.fillRect(x, y, width, height);
					}
				});
			}
			
			@Override
			public <C> void renderLine(int x1, int y1, int x2, int y2, C color, IColorInterpreter<C> colorInterpreter) {
				final Color immutableColor = toAwtColor(color, colorInterpreter);
				canvas.addCommand(new IPaintCommand() {
					@Override
					public void execute(IRenderState renderState, Graphics2D g2d) {
						g2d.setColor(renderState.getFinalColor(immutableColor, AWT_COLOR_INTERPRETER));
						final Object antiAliasOld = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
						g2d.drawLine(x1, y1, x2, y2);
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasOld);
					}
				});
			}
			
			@Override
			public <C> void setTint(C color, IColorInterpreter<C> colorInterpreter) {
				final Color immutableColor = toAwtColor(color, colorInterpreter);
				canvas.addCommand(new IPaintCommand() {
					@Override
					public void execute(IRenderState renderState, Graphics2D g2d) {
						renderState.setTint(immutableColor, AWT_COLOR_INTERPRETER);
					}
				});
			}
			
			@Override
			public <C> void setAdditive(C color, IColorInterpreter<C> colorInterpreter) {
				final Color immutableColor = toAwtColor(color, colorInterpreter);
				canvas.addCommand(new IPaintCommand() {
					@Override
					public void execute(IRenderState renderState, Graphics2D g2d) {
						renderState.setAdditive(immutableColor, AWT_COLOR_INTERPRETER);
					}
				});
			}
			
			@Override
			public IClippingRectangle getClip() {
				return currentClip.createCopy();
			}
			
			@Override
			public void setClip(IClippingRectangle clippingRectangle) {
				if(clippingRectangle == null) {
					currentClip.setToNull();
					canvas.addCommand(new IPaintCommand() {
						@Override
						public void execute(IRenderState renderState, Graphics2D g2d) {
							g2d.setClip(null);
						}
					});	
				} else {
					setClip(clippingRectangle.getX(), clippingRectangle.getY(), clippingRectangle.getWidth(), clippingRectangle.getHeight());
				}
			}
			
			@Override
			public void setClip(final int x, final int y, final int width, final int height) {
				currentClip.setTo(x, y, width, height);
				canvas.addCommand(new IPaintCommand() {
					@Override
					public void execute(IRenderState renderState, Graphics2D g2d) {
						g2d.setClip(x, y, width, height);
					}
				});
			}
		};

		public Window(JFrame frame, SwingCanvas canvas, int width, int height, int pixelDepth) {
			this.frame = frame;
			this.canvas = canvas;
			this.width = width;
			this.height = height;
			SwingMouseListenerAdapter mouseListenerAdapter = new SwingMouseListenerAdapter(mouseListeners, inputCache, pixelDepth);
			canvas.addMouseListener(mouseListenerAdapter);
			canvas.addMouseMotionListener(mouseListenerAdapter);
			canvas.addMouseWheelListener(mouseListenerAdapter);
			frame.addKeyListener(new SwingKeyListenerAdapter(keyListeners, inputCache));
		}
		
		@Override
		public void renderBegin() {
			canvas.clearCommands();
			currentClip.setToNull();
		}

		@Override
		public void renderEnd() {
			canvas.applyCommands();
			frame.repaint();
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public IGraphics getGraphics() {
			return graphics;
		}

		@Override
		public void addMouseListener(IMouseListener clickListener) {
			this.mouseListeners.add(clickListener);
		}

		@Override
		public void removeMouseListener(IMouseListener clickListener) {
			this.mouseListeners.remove(clickListener);
		}

		@Override
		public void addKeyListener(IKeyListener keyListener) {
			this.keyListeners.add(keyListener);
		}

		@Override
		public void removeKeyListener(IKeyListener keyListener) {
			this.keyListeners.remove(keyListener);
		}
		
		@Override
		public boolean isKeyDown(int keyCode) {
			return inputCache.isKeyDown(keyCode);
		}
		
		@Override
		public boolean isMouseButtonDown(MouseButton mouseButton) {
			return inputCache.isMouseButtonDown(mouseButton);
		}
		
		@Override
		public int getMouseX() {
			return inputCache.getMouseX();
		}
		
		@Override
		public int getMouseY() {
			return inputCache.getMouseY();
		}
	}
	
	private static <C> Color toAwtColor(C color, IColorInterpreter<C> colorInterpreter) {
		return new Color(colorInterpreter.getRed(color), colorInterpreter.getGreen(color), colorInterpreter.getBlue(color), colorInterpreter.getAlpha(color));
	}
	
	private static IColorInterpreter<Color> AWT_COLOR_INTERPRETER = new IColorInterpreter<Color>() {
		@Override
		public int getRed(Color color) {
			return color.getRed();
		}
		
		@Override
		public int getGreen(Color color) {
			return color.getGreen();
		}
		
		@Override
		public int getBlue(Color color) {
			return color.getBlue();
		}
		
		@Override
		public int getAlpha(Color color) {
			return color.getAlpha();
		}
	};
	
	private static class MutableClip implements IClippingRectangle {
		private int x;
		private int y;
		private int width;
		private int height;
		private boolean isNull = true;
		
		public void setToNull() {
			this.isNull = true;
		}
		
		public void setTo(int x, int y, int width, int height) {
			isNull = false;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
		
		public IClippingRectangle createCopy() {
			 return isNull ? null : IClippingRectangle.create(this);
		}
	}
	
	public interface IResourceLocator {
		File locate(String path);
	}
	
	private static class SimpleResourceLocator implements IResourceLocator {
		private final File resourceDirectory;
		
		public SimpleResourceLocator(File resourceDirectory) {
			this.resourceDirectory = resourceDirectory;
		}

		@Override
		public File locate(String path) {
			return new File(resourceDirectory, path);
		}
	}
}
