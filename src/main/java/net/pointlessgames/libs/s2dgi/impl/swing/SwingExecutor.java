package net.pointlessgames.libs.s2dgi.impl.swing;

import java.util.concurrent.Executor;

import javax.swing.SwingUtilities;

public final class SwingExecutor {
	private static final Executor executor = new Executor() {
		@Override
		public void execute(Runnable command) {
			SwingUtilities.invokeLater(command);
		}
	};
	
	public static Executor get() {
		return executor;
	}
}
