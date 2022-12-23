package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import net.pointlessgames.libs.s2dgi.clipboard.ISimpleClipboard;

public class SwingClipboard implements ISimpleClipboard {
	public static boolean DEBUG = false;
	
	private static final DataFlavor FLAVOR = DataFlavor.stringFlavor;
	private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	@Override
	public void setText(CharSequence text) {
		clipboard.setContents(new StringSelection(text.toString()), null);
	}

	@Override
	public String getText(String fallback) {
		if(clipboard.isDataFlavorAvailable(FLAVOR)) {
			try {
				return (String) clipboard.getData(FLAVOR);
			} catch (UnsupportedFlavorException e) {
				if(DEBUG) {
					e.printStackTrace();
				}
				return fallback;
			} catch (IOException e) {
				if(DEBUG) {
					e.printStackTrace();
				}
				return fallback;
			}
		} else {
			return fallback;
		}
	}
}
