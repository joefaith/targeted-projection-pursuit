package tpp;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import weka.core.Instance;
import weka.core.matrix.Matrix;

/** Smooth series in a scatter plot */
public class SmoothSeries implements PerturbationPursuit {

	private static final int X = 0;
	private static final int Y = 1;

	private static final double EXPANSION = 1.2d;
	private ScatterPlotModel model;

	public SmoothSeries(ScatterPlotModel model) {
		this.model = model;
	}

	/**
	 * Find the perturbations necessary to untangle the currently viewed series
	 * by trying to map each series onto a straight line
	 * 
	 * @throws TPPException
	 */
	public void pursuePerturbation() throws TPPException {
		if (model.getNumViewDimensions() == 2) {
			Matrix target = new Matrix(model.getNumDataPoints(), 2);
			Collection<TreeSet<Instance>> allSeries = model.getSeries().getAllSeries().values();
			int numPoints;
			int indexInModel, indexInSeries;
			double firstX, firstY, dX, dY;
			// for each series
			for (TreeSet<Instance> series : allSeries) {
				// find the start and end point, and the length of the line
				firstX = model.getView().get(model.indexOf(series.first()), X);
				firstY = model.getView().get(model.indexOf(series.first()), Y);
				dX = model.getView().get(model.indexOf(series.last()), X) - firstX;
				dY = model.getView().get(model.indexOf(series.last()), Y) - firstY;
				numPoints = series.size();
				indexInSeries = 0;
				// the target points should be a on a straight line between the first and last
				for (Instance p : series) {
					indexInModel = model.indexOf(p);
					target.set(indexInModel, X, firstX + (EXPANSION * dX * indexInSeries / numPoints));
					target.set(indexInModel, Y, firstX + (EXPANSION * dY * indexInSeries / numPoints));
					indexInSeries++;
				}
			}
			model.setTarget(target);
			model.pursueTarget();
			model.resizePlot();
		}
	}

	/**
	 * Find the perturbations necessary to untangle the currently viewed series
	 * by moving each point nearer to any connected ones.
	 * 
	 * @throws TPPException
	 */
	public void pursuePerturbation1() throws TPPException {
		if (model.getNumViewDimensions() == 2) {
			Matrix target = new Matrix(model.getNumDataPoints(), 2);
			int previous, next, numPoints = 0;
			double totaly, totalx;
			// Set the target for each position to be the mean of the current,
			// the next, and the previous points
			for (int p = 0; p < model.getNumDataPoints(); p++) {
				// find the next and prev points
				previous = model.getSeries().previous(p);
				next = model.getSeries().next(p);
				numPoints = 1;
				totalx = model.getView().get(p, 0);
				totaly = model.getView().get(p, 1);
				if (previous != -1) {
					totalx += model.getView().get(previous, 0);
					totaly += model.getView().get(previous, 1);
					numPoints++;
				}
				if (next != -1) {
					totalx += model.getView().get(next, 0);
					totaly += model.getView().get(next, 1);
					numPoints++;
				}
				target.set(p, 0, totalx / numPoints);
				target.set(p, 1, totaly / numPoints);
				// System.out.println("view = "+model.getView().get(p,0)+","+model.getView().get(p,1)+"; target = "+target.get(p,0)+","+target.get(p,1));
			}

			model.setTarget(target);
			model.pursueTarget();
			model.resizePlot();

		} else
			throw new TPPException("2D smoother can only be applied to a 2D target");

	}

	public void setModel(TPPModel model) {
		this.model = (ScatterPlotModel) model;
	}

}
