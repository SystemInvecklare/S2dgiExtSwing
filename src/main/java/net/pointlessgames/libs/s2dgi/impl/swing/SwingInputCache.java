package net.pointlessgames.libs.s2dgi.impl.swing;

import java.util.HashSet;
import java.util.Set;

import net.pointlessgames.libs.s2dgi.mouse.IMouseListener.MouseButton;

/*package-private*/ class SwingInputCache {
	private int mouseX;
	private int mouseY;
	
	private final Set<Integer> pressedKeys = new HashSet<>();
	private final Set<MouseButton> pressedMouseButtons = new HashSet<>();

	public boolean isMouseButtonDown(MouseButton mouseButton) {
		return pressedMouseButtons.contains(mouseButton);
	}

	public boolean isKeyDown(int keyCode) {
		return pressedKeys.contains(keyCode);
	}

	public void setMousePosition(int x, int y) {
		this.mouseX = x;
		this.mouseY = y;
	}

	public void setMouseButtonState(MouseButton button, boolean pressed) {
		if(pressed) {
			pressedMouseButtons.add(button);
		} else {
			pressedMouseButtons.remove(button);
		}
	}

	public void setKeyState(int keyCode, boolean pressed) {
		if(pressed) {
			pressedKeys.add(keyCode);
		} else {
			pressedKeys.remove(keyCode);
		}
	}

	public int getMouseX() {
		return mouseX;
	}
	
	public int getMouseY() {
		return mouseY;
	}
}
