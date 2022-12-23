package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.pointlessgames.libs.s2dgi.color.IColorInterpreter;

/*package-private*/ class SwingCanvas extends JPanel {
	private static final long serialVersionUID = -2305897994242116157L;
	
	private final ColorOpComposite colorOpComposite = new ColorOpComposite();
	private final List<IPaintCommand> commands = new ArrayList<SwingCanvas.IPaintCommand>();
	
	private boolean resized = false;
	private BufferedImage image;
	private BufferedImage offImage;
	private final RenderState renderState = new RenderState();

	private final int pixelDepth;
	
	public SwingCanvas(int pixelDepth) {
		super(true);
		this.pixelDepth = pixelDepth;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resized = true;
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		synchronized (this) {
			if(image != null) {
				g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			}
		}
	}
	

	
	public void clearCommands() {
		commands.clear();
	}
	
	public void addCommand(IPaintCommand command) {
		commands.add(command);
	}
	
	public interface IRenderState {
		<C> void setTint(C color, IColorInterpreter<C> colorInterpreter);
		<C> void setAdditive(C color, IColorInterpreter<C> colorInterpreter);
		<C> Color getFinalColor(C color, IColorInterpreter<C> colorInterpreter);
	}
	
	public interface IPaintCommand {
		void execute(IRenderState renderState, Graphics2D g2d);
	}

	public void applyCommands() {
		if(resized) {
			int width = getWidth()/pixelDepth;
			int height = getHeight()/pixelDepth;
			int imageType = BufferedImage.TYPE_INT_RGB;
			offImage = new BufferedImage(width, height, imageType);
			image = new BufferedImage(width, height, imageType);
			resized = false;
		}
		Graphics2D g2d = offImage.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, offImage.getWidth(), offImage.getHeight());
		
		//Turn on Antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Composite oldComposite = g2d.getComposite();
        
        ColorOpComposite specialComposite = colorOpComposite.reset(oldComposite, renderState.reset());
        
        boolean renderingSpecial = false;
		for(IPaintCommand command : commands) {
			final boolean specialRender = !renderState.isNoop();
			if(specialRender != renderingSpecial) {
				renderingSpecial = specialRender;
				g2d.setComposite(specialRender ? specialComposite : oldComposite);
			}
			command.execute(renderState, g2d);
		}
		
		g2d.dispose();
		synchronized (this) {
			BufferedImage oldImage = image;
			image = offImage;
			offImage = oldImage;
		}
	}
	
	private static class RenderState implements IRenderState, IColorOperation {
		private final int[] tint = SwingColorUtil.toArray(Color.WHITE);
		private final int[] additive = new int[] {0,0,0,0};
		private boolean noop = true;
		
		@Override
		public void apply(int[] pixel, int offset, int bands) {
			SwingColorUtil.applyColorEffects(tint, 0, 4, additive, 0, 4, pixel, offset, bands);
		}
		
		@Override
		public boolean isNoop() {
			return noop;
		}
		
		private void updateNoop() {
			noop = tint[0] == 255 && tint[1] == 255 && tint[2] == 255 && tint[3] == 255
				&& additive[0] == 0 && additive[1] == 0 && additive[2] == 0 && additive[3] == 0; 
		}

		public RenderState reset() {
			tint[0] = tint[1] = tint[2] = tint[3] = 255;
			additive[0] = additive[1] = additive[2] = additive[3] = 0;
			updateNoop();
			return this;
		}

		@Override
		public <C> void setTint(C color, IColorInterpreter<C> colorInterpreter) {
			SwingColorUtil.setArray(tint, color, colorInterpreter);
			updateNoop();
		}
		

		@Override
		public <C> void setAdditive(C color, IColorInterpreter<C> colorInterpreter) {
			SwingColorUtil.setArray(additive, color, colorInterpreter);
			updateNoop();
		}
		
		private final int[] TEMP_FINAL_COLOR = new int[4];
		@Override
		public <C> Color getFinalColor(C color, IColorInterpreter<C> colorInterpreter) {
			SwingColorUtil.setArray(TEMP_FINAL_COLOR, color, colorInterpreter);
			SwingColorUtil.applyColorEffects(tint, additive, TEMP_FINAL_COLOR);
			return SwingColorUtil.toColor(TEMP_FINAL_COLOR);
		}
	}
}
