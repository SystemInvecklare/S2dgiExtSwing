package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import net.pointlessgames.libs.s2dgi.key.IKeyListener;

/*package-private*/ class SwingKeyListenerAdapter implements KeyListener {
	private final List<IKeyListener> keyListeners;
	private final SwingInputCache inputCache;
	
	public SwingKeyListenerAdapter(List<IKeyListener> keyListeners, SwingInputCache inputCache) {
		this.keyListeners = keyListeners;
		this.inputCache = inputCache;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		for(IKeyListener keyListener : keyListeners) {
			keyListener.onKeyTyped(e.getKeyChar());
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!inputCache.isKeyDown(e.getKeyCode())) {
			inputCache.setKeyState(e.getKeyCode(), true);
			for(IKeyListener keyListener : keyListeners) {
				keyListener.onKeyPressed(e.getKeyCode());
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(inputCache.isKeyDown(e.getKeyCode())) {
			inputCache.setKeyState(e.getKeyCode(), false);
			for(IKeyListener keyListener : keyListeners) {
				keyListener.onKeyReleased(e.getKeyCode());
			}
		}
	}

}
