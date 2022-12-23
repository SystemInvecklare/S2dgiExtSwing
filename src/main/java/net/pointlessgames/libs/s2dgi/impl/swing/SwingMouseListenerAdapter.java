package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import net.pointlessgames.libs.s2dgi.mouse.IMouseListener;
import net.pointlessgames.libs.s2dgi.mouse.IMouseListener.MouseButton;

/*package-private*/ class SwingMouseListenerAdapter implements MouseListener, MouseMotionListener, MouseWheelListener {
	private final SwingInputCache inputCache;
	private final List<IMouseListener> mouseListeners;
	private final int pixelDepth;
	
	public SwingMouseListenerAdapter(List<IMouseListener> mouseListeners, SwingInputCache inputCache, int pixelDepth) {
		this.mouseListeners = mouseListeners;
		this.inputCache = inputCache;
		this.pixelDepth = pixelDepth;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
		for(IMouseListener mouseListener : mouseListeners) {
			mouseListener.onScroll(convertedX, convertedY, e.getWheelRotation());
		}
	}

	private int convertPixel(int p) {
		return p/pixelDepth;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
		for(IMouseListener mouseListener : mouseListeners) {
			mouseListener.onDragged(convertedX, convertedY);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
		for(IMouseListener mouseListener : mouseListeners) {
			mouseListener.onMoved(convertedX, convertedY);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
		IMouseListener.MouseButton button = convertButton(e.getButton());
		inputCache.setMouseButtonState(button, true);
		for(IMouseListener mouseListener : mouseListeners) {
			mouseListener.onPressed(convertedX, convertedY, button);
		}
	}

	private static MouseButton convertButton(int button) {
		if(button == MouseEvent.BUTTON1) {
			return MouseButton.BUTTON_1;
		} else if(button == MouseEvent.BUTTON2) {
			return MouseButton.BUTTON_2;
		} else if(button == MouseEvent.BUTTON3) {
			return MouseButton.BUTTON_3;
		} else {
			return MouseButton.UNKNOWN_BUTTON;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
		IMouseListener.MouseButton button = convertButton(e.getButton());
		inputCache.setMouseButtonState(button, false);
		for(IMouseListener mouseListener : mouseListeners) {
			mouseListener.onReleased(convertedX, convertedY, button);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		int convertedX = convertPixel(e.getX());
		int convertedY = convertPixel(e.getY());
		inputCache.setMousePosition(convertedX, convertedY);
	}
}
