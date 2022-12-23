package net.pointlessgames.libs.s2dgi.impl.swing;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*package-private*/ class ColorOpComposite implements Composite {
	private Composite wrapped = null;
	private IColorOperation colorOp = null;
	private final IRasterPool rasterPool = new RasterPool();
	private final Context reusedContext = new Context(rasterPool);
	
	public ColorOpComposite reset(Composite wrapped, IColorOperation colorOp) {
		this.wrapped = wrapped;
		this.colorOp = colorOp;
		return this;
	}

	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel,
			RenderingHints hints) {
		return reusedContext.reset(wrapped.createContext(srcColorModel, dstColorModel, hints), colorOp);
	}
	
	private static class Context implements CompositeContext {
		private final IRasterPool rasterPool;
		private CompositeContext wrapped;
		private IColorOperation colorOp;

		public Context(IRasterPool rasterPool) {
			this.rasterPool = rasterPool;
		}

		public Context reset(CompositeContext wrapped, IColorOperation colorOp) {
			this.wrapped = wrapped;
			this.colorOp = colorOp;
			return this;
		}

		@Override
		public void dispose() {
			wrapped.dispose();
		}

		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
			if(colorOp.isNoop()) {
				wrapped.compose(src, dstIn, dstOut);
			} else {
				PoolableRaster borrowed = rasterPool.obtain(src);
				
				final int bands = src.getNumBands();
				final int srcPixels = src.getWidth()*src.getHeight()*bands;
				
				src.getPixels(src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight(), borrowed.data);
				
				for(int i = 0; i < srcPixels; i+= bands) {
					colorOp.apply(borrowed.data, i, bands);
				}
				
				borrowed.raster.setPixels(borrowed.raster.getMinX(), borrowed.raster.getMinY(), src.getWidth(), src.getHeight(), borrowed.data);
				
				wrapped.compose(borrowed.raster, dstIn, dstOut);
				
				rasterPool.free(borrowed);
			}
		}
	}
	
	private interface IRasterPool {
		/**
		 * Promises to return a WritableRaster that is at least as big as the raster. May return the same object (raster).
		 */
		PoolableRaster obtain(Raster minSizeRaster);
		void free(PoolableRaster raster);
	}
	
	private static class RasterPool implements IRasterPool {
		private final int maxPoolSize = 10;
		private final List<PoolableRaster> pool = new ArrayList<>();
		
		public RasterPool() {
		}

		@Override
		public PoolableRaster obtain(Raster minSizeRaster) {
			Iterator<PoolableRaster> poolIterator = pool.iterator();
			while(poolIterator.hasNext()) {
				PoolableRaster writableRaster = poolIterator.next();
				if(isCompatible(writableRaster.raster, minSizeRaster)) {
					poolIterator.remove();
					return writableRaster;
				}
			}
			return new PoolableRaster(minSizeRaster.createCompatibleWritableRaster(Math.max(minSizeRaster.getWidth(),1024), Math.max(minSizeRaster.getHeight(),1024)));
		}

		private boolean isCompatible(WritableRaster writableRaster, Raster raster) {
			if(writableRaster.getWidth() < raster.getWidth() || writableRaster.getHeight() < raster.getHeight()) {
				return false;
			}
			return isCompatible(writableRaster.getSampleModel(), raster.getSampleModel());
		}
		
		private boolean isCompatible(SampleModel existingSampleModel, SampleModel sampleModel) {
			return existingSampleModel.getDataType() == sampleModel.getDataType()
				&& existingSampleModel.getNumBands() == sampleModel.getNumBands();
		}

		@Override
		public void free(PoolableRaster raster) {
			PoolableRaster replace = null;
			for(PoolableRaster existing : pool) {
				if(isCompatible(raster.raster, existing.raster)) {
					replace = existing;
					break;
				}
			}
			if(replace == null &&  pool.size() >= maxPoolSize) {
				System.err.println("Pool overflow!");
			} else {
				if(replace != null) {
					pool.remove(replace);
				}
				pool.add(raster);
			}
		}
	}
	
	private static class PoolableRaster {
		public final WritableRaster raster;
		public final int[] data;
		
		public PoolableRaster(WritableRaster raster) {
			this.raster = raster;
			this.data = new int[raster.getWidth()*raster.getHeight()*raster.getNumBands()];
		}
	}
}
