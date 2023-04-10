package tpp;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JPanel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.Matrix;

/*
 * Created on 16-Feb-2006
 */

/**
 * Panel with a view of a set of instances that can be manipulated thru
 * projection pursuit
 * 
 * @author Joe Faith
 */
public class ScatterPlotViewPanel extends JPanel implements TPPModelEventListener, ComponentListener {

	protected ScatterPlotModel spModel = null;

	protected static final double LINE_WIDTH = 1.5;

	private static final int X = 0;
	private static final int Y = 1;
	
	private static final int BASE_FONT_SIZE=12;

	/** Noise added to the view to better separate the points */
	private Matrix jitter;

	/** whether to add jitter to the view */
	private boolean showJitter = false;

	public ScatterPlotViewPanel() {
		super();
		initialize();
	}

	/**
	 *
	 */
	private void initialize() {
		addComponentListener(this);
	}

	/**
	 * Load the data panel with the instances to be displayed
	 */
	public void setModel(ScatterPlotModel spModel) {
		this.spModel = spModel;
		if (spModel == null)
			removeAll();
		else {
			spModel.addListener(this);
			spModel.setColours(ColourScheme.DARK);
			jitter = new Matrix(spModel.getNumDataPoints(), spModel.getNumDataDimensions());
			spModel.initRetinalAttributes();
			spModel.resizePlot(getWidth(), getHeight());
		}
	}

	/**
	 * Find the indices of the nearest points to the given coordinates in data
	 * space. If no point is found then zero length array returned
	 */
	public int[] findNearestPoints(Point2D.Double pt) {
		double margin = spModel.markerSize * getWidth() / spModel.getTransform().getScaleX();
		double distance;
		Vector<Integer> points = new Vector<Integer>();
		for (int i = 0; i < spModel.getNumDataPoints(); i++) {
			distance = pt.distance(new Point2D.Double(spModel.getView().get(i, X), spModel.getView().get(i, Y)));
			if (distance < margin)
				points.add(Integer.valueOf(i));
		}
		int[] aPoints = new int[points.size()];
		for (int i = 0; i < points.size(); i++)
			aPoints[i] = points.get(i).intValue();
		return aPoints;

	}

	/**
	 * Find the indices of the nearest axes to the given coordinates in data
	 * space. If no axis is found then zero length array returned
	 */
	public int[] findNearestAxes(java.awt.geom.Point2D.Double pt) {
		double margin = spModel.markerSize * getWidth() / spModel.getTransform().getScaleX();
		double distance;
		Vector<Integer> axes = new Vector<Integer>();
		for (int i = 0; i < spModel.getNumDataDimensions(); i++) {
			distance = pt.distance(new Point2D.Double(spModel.getProjection().get(i, X), spModel.getProjection().get(i,
					1)));
			if (distance < margin)
				axes.add(Integer.valueOf(i));
		}
		int[] aAxes = new int[axes.size()];
		for (int i = 0; i < axes.size(); i++)
			aAxes[i] = axes.get(i).intValue();
		return aAxes;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// add a transform so that we can specify coordinates in data space
		// rather than device space
		Graphics2D g2 = (Graphics2D) g;
		paintView(g2, spModel.getTransform(), getWidth(), getHeight());
	}

	/**
	 * Paint the scatter plot to the given Graphics, using the given mapping
	 * from data (aka user) space to device space, and with markers of the given
	 * size (in pixels) If transform is null then use the default one. If
	 * markerSize=0 use the default size.
	 */
	public void paintView(Graphics2D g2, AffineTransform transform, int width, int height) {

		if (spModel != null && spModel.getData() != null) {

			// if a transform is specified then use it, saving the original
			AffineTransform saveAT = null;
			if (transform != null) {
				saveAT = g2.getTransform();
				g2.transform(transform);
			} else {
				transform = g2.getTransform();
			}
			g2.setStroke(new BasicStroke((float) (LINE_WIDTH / transform.getScaleX())));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			setBackground(spModel.getColours().getBackgroundColor());

			// find out how big the markers need to be in data space in order to
			// appear the right size in device space
			// nb this assumes that the same scale is used for both x and y
			// nb actual markers (and labels) may be further scaled depending on the size attribute
			double markerRadius = spModel.markerSize * width / transform.getScaleX();

			// If the axes are shown and there are points currently selected
			// then colour the axes based on the values of those attributes for
			// the selected points
			double[] col = null;
			if (spModel.showAxes() && (spModel.numPointsSelected() > 0)) {
				col = defineAxesColors();
			}

			// draw series lines;
			if (spModel.showSeries() && (spModel.getSeries() != null))
				drawSeriesLines(g2);

			// draw the graph
			if (spModel.showGraph())
				drawGraph(g2);

			// draw clustering
			if (spModel.showHierarchicalClustering())
				drawClustering(g2);

			// draw the target
			if (spModel.showTarget())
				drawTarget(g2, markerRadius);

			// draw the points
			drawPoints(g2, transform, markerRadius);

			// plot the axes or just the origin
			drawAxesOrOrigin(g2, transform,  markerRadius, col);

			// draw the rectangle?
			if (spModel.rectangle != null)
				spModel.rectangle.draw(g2);

			// restore original transform
			if (saveAT != null)
				g2.setTransform(saveAT);

			if (showJitter)
				updateJitter();

		}

	}

	private void drawPoints(Graphics2D g2, AffineTransform transform, double markerRadius) {
		Shape marker = null;
		int i;
		Graphics labelGraphics = getGraphics();

		for (i = 0; i < spModel.getNumDataPoints(); i++) {

			// if shaping the point using a string attribute and the point isn't
			// selected then draw a label, otherwise a marker
			if (spModel.getShapeAttribute() != null && spModel.getShapeAttribute().isString()
					&& !spModel.isPointSelected(i))
				drawLabelAtPoint(transform, i, labelGraphics);
			else
				drawMarkerAtPoint(g2, transform, markerRadius, marker, i);
		}
	}

	private void drawMarkerAtPoint(Graphics2D g2, AffineTransform transform,
			double markerRadius, Shape marker, int i) {
		double x;
		double y;
		double size;
		// so we are showing points using a shaped marker rather than text
		setColourOfPoint(g2, i);

		// Size of the marker depends on size attribute
		if (spModel.getSizeAttribute() == null)
			size = markerRadius;
		else
			size = ( 0.5 + (spModel.getInstances().instance(i).value(spModel.getSizeAttribute()) - spModel.sizeAttributeLowerBound)
					/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))*markerRadius;

		// position of marker
		x = spModel.getView().get(i, X) + jitter.get(i, X);
		y = spModel.getView().get(i, Y) + jitter.get(i, Y);

		// if the point is selected then draw cross hairs
		if (spModel.isPointSelected(i)) {
			g2.draw(new Line2D.Double(x - size, y, x + size, y));
			g2.draw(new Line2D.Double(x, y - size, x, y + size));
			return;
		}
		
		// if the shape attribute is numeric, then choose a
		// shape based on it
		if (spModel.getShapeAttribute() != null && spModel.getShapeAttribute().isNumeric())
			marker = MarkerFactory.buildMarker((int) spModel.instances.instance(i).value(spModel.getShapeAttribute()),
					x, y, size);

		// if there's no shape marker, then just use the default
		// shape
		if (spModel.getShapeAttribute() == null)
			marker = MarkerFactory.buildMarker(0, x, y, size);

		// if there's no fill attribute, use the default fill
		if (spModel.getFillAttribute() == null) {
			g2.fill(marker);
		} else {
			// otherwise the type of fill depends on the value
			// of the fill attribute
			switch ((int) spModel.instances.instance(i).value(spModel.getFillAttribute())) {
			case 0: {
				g2.fill(marker);
				break;
			}
			case 1: {
				g2.draw(marker);
				break;
			}
			default: {
				// TODO add more textures for filling points
				// (shaded
				// lines etc)
				g2.draw(marker);

			}
			}
		}
	}

	private void drawLabelAtPoint(AffineTransform transform,int i, Graphics labelGraphics) {
		// if we are shaping the points by a string attribute then
		// write label centered on the position of the marker
		// NB we write the labels in device space rather than in data space,
		// since fonts don't scale nicely under a transform
		double size;
		String label;
		double textWidth;
		double textHeight;
		// write label to the right of the marker
		Point2D pointLocationInDeviceSpace = null;
		pointLocationInDeviceSpace = transform.transform(new Point2D.Double(spModel.getView().get(i, X), spModel
				.getView().get(i, Y)), pointLocationInDeviceSpace);

		// Size of the marker depends on size attribute
		if (spModel.getSizeAttribute() == null)
			size = BASE_FONT_SIZE*spModel.getMarkerSize()/spModel.MARKER_DEFAULT;
		else
			size = (0.5+(spModel.getInstances().instance(i).value(spModel.getSizeAttribute()) - spModel.sizeAttributeLowerBound)
					/ (spModel.sizeAttributeUpperBound - spModel.sizeAttributeLowerBound))*BASE_FONT_SIZE*spModel.getMarkerSize()/spModel.MARKER_DEFAULT;

		setColourOfPoint(labelGraphics, i);
		label = spModel.getDescriptionOfInstance(i);
		labelGraphics.setFont(labelGraphics.getFont().deriveFont((float) size));
		textWidth = labelGraphics.getFontMetrics().getStringBounds(label, labelGraphics).getWidth();
		textHeight = labelGraphics.getFontMetrics().getStringBounds(label, labelGraphics).getHeight();
		labelGraphics.drawString(spModel.getDescriptionOfInstance(i),
				(int) (pointLocationInDeviceSpace.getX() - (textWidth / 2)),
				(int) (pointLocationInDeviceSpace.getY() + (textHeight / 2)));
	}

	/**
	 * Color of the point depends on whether we are coloring by a numeric or
	 * nominal attribute.
	 */
	private void setColourOfPoint(Graphics g, int i) {
		if (spModel.getColourAttribute() == null)
			g.setColor(spModel.getColours().getForegroundColor());
		else {
			if (spModel.getColourAttribute().isNominal())
				g.setColor(spModel.getColours().getClassificationColor(
						(int) spModel.getInstances().instance(i).value(spModel.getColourAttribute())));
			if (spModel.getColourAttribute().isNumeric())
				g.setColor(spModel.getColours().getColorFromSpectrum(
						spModel.getInstances().instance(i).value(spModel.getColourAttribute()),
						spModel.colorAttributeLowerBound, spModel.colorAttributeUpperBound));
		}
	}

	/**
	 * If the axes are shown and there are points currently selected then colour
	 * the axes based on the values of those attributes for the selected points
	 * 
	 * col = (means-min) / (max-min) where col = used to colour the axis (0,Y)
	 * means = mean of this attribute for the selected points min,max = min,max
	 * of this attribute for all points
	 */
	private double[] defineAxesColors() {
		int p;
		double[] col;
		Matrix mPointsSelected = new Matrix(1, spModel.getNumDataPoints());
		for (p = 0; p < spModel.getNumDataPoints(); p++)
			if (spModel.isPointSelected(p))
				mPointsSelected.set(0, p, Y);
		Matrix means = mPointsSelected.times(spModel.getData()).times(1d / spModel.numPointsSelected());
		Matrix min = MatrixUtils.columnMin(spModel.getData());
		Matrix max = MatrixUtils.columnMax(spModel.getData());
		col = means.minus(min).arrayRightDivide(max.minus(min)).getArray()[0];
		return col;
	}

	private void drawAxesOrOrigin(Graphics2D g2, AffineTransform transform, double markerRadius, double[] col) {
		int i;
		if (spModel.showAxes()) {

			Graphics labelGraphics = null;
			if (spModel.showAxisLabels()) {
				// We draw labels in device space rather than user space,
				// since
				// fonts may not scale correctly
				labelGraphics = getGraphics();
				labelGraphics.setColor(spModel.getColours().getAxesColor());
			}

			for (i = 0; i < spModel.getNumDataDimensions(); i++) {

				// If there are any point(s) selected then color the axes by
				// their (average) weight with the selected point(s)
				if (spModel.numPointsSelected() > 0)
					g2.setColor(spModel.getColours().getColorFromSpectrum(col[i], 0, 1));
				// otherwise highlight the axis if it is selected
				else
					g2.setColor((spModel.isAxisSelected(i) ? spModel.getColours().getForegroundColor() : spModel
							.getColours().getAxesColor()));
				g2.draw(new Line2D.Double(0, 0, spModel.getProjection().get(i, X), spModel.getProjection().get(i, Y)));
				if (spModel.isAxisSelected(i))
					g2.fill(new Ellipse2D.Double(spModel.getProjection().get(i, X) - markerRadius, spModel
							.getProjection().get(i, Y) - markerRadius, markerRadius * 2, markerRadius * 2));

				if (spModel.showAxisLabels()) {

					// write label to the right of the marker
					Point2D labelLocationInDeviceSpace = null;
					labelLocationInDeviceSpace = transform.transform(
							new Point2D.Double(spModel.getProjection().get(i, X), spModel.getProjection().get(i, Y)),
							labelLocationInDeviceSpace);
					try {
						labelGraphics.drawString(spModel.getNumericAttributes().get(i).name(),
								(int) labelLocationInDeviceSpace.getX(), (int) labelLocationInDeviceSpace.getY());
					} catch (Exception e) {
						System.out.println(e);
					}
				}

			}
		} else {
			double originSize = markerRadius;
			g2.setColor(spModel.getColours().getAxesColor());
			g2.draw(new Line2D.Double(-originSize, 0, originSize, X));
			g2.draw(new Line2D.Double(0, -originSize, 0, originSize));
		}
	}

	private void drawTarget(Graphics2D g2, double markerRadius) {
		double x;
		double y;
		Shape circle;
		int i;
		g2.setColor(spModel.getColours().getAxesColor());
		for (i = 0; i < spModel.getNumDataPoints(); i++) {
			x = spModel.getTarget().get(i, X);
			y = spModel.getTarget().get(i, Y);
			circle = new Ellipse2D.Double(x - markerRadius, y - markerRadius, markerRadius * 2, markerRadius * 2);
			g2.draw(circle);
		}
	}

	private void drawClustering(Graphics2D g2) {
		// recursively draw lines between the centroids of each cluster
		// nb this assumes the this is a binary HC -- ie that each
		// cluster contains two members
		g2.setColor(spModel.getColours().getAxesColor());
		HierarchicalCluster cluster = spModel.getHierarchicalCluster();
		drawClusterArc(cluster, g2);
	}

	private void drawGraph(Graphics2D g2) {
		double x1;
		double y1;
		double x2;
		double y2;
		Line2D line;
		int i;
		int j;
		g2.setColor(spModel.getColours().getAxesColor());

		Iterator<Connection> allConnections = spModel.getGraph().getAllConnections().iterator();

		String sourceNode = null;
		String targetNode = null;
		Instances ins = spModel.getInstances();

		Connection cnxn = null;

		while (allConnections.hasNext()) {

			cnxn = allConnections.next();

			sourceNode = cnxn.getSourceNode();
			// System.out.println("Source Node: "+ sourceNode);
			targetNode = cnxn.getTargetNode();
			// System.out.println("Target Node: "+ targetNode);

			Instance source = cnxn.getNodeInstance(ins, sourceNode);
			// System.out.println(source.stringValue(0));
			Instance target = cnxn.getNodeInstance(ins, targetNode);
			// System.out.println(target.stringValue(0));

			i = spModel.indexOf(source);
			x1 = spModel.getView().get(i, X) + jitter.get(i, X);
			y1 = spModel.getView().get(i, Y) + jitter.get(i, Y);

			j = spModel.indexOf(target);
			x2 = spModel.getView().get(j, X) + jitter.get(j, X);
			y2 = spModel.getView().get(j, Y) + jitter.get(j, Y);
			line = new Line2D.Double(x1, y1, x2, y2);
			g2.draw(line);
			// g2.fill(MarkerFactory.buildArrowHead(line,
			// markerRadius*2));
		}
	}

	private void drawSeriesLines(Graphics2D g2) {
		double x1;
		double y1;
		double x2;
		double y2;
		Line2D line;
		int i;
		g2.setColor(spModel.getColours().getAxesColor());
		Iterator<TreeSet<Instance>> allSeries = spModel.getSeries().getAllSeries().values().iterator();
		Iterator<Instance> nextSeries;
		// for all the series
		while (allSeries.hasNext()) {
			nextSeries = allSeries.next().iterator();
			if (nextSeries.hasNext()) {

				// find the start point
				i = spModel.indexOf(nextSeries.next());
				x1 = spModel.getView().get(i, X) + jitter.get(i, X);
				y1 = spModel.getView().get(i, Y) + jitter.get(i, Y);
				while (nextSeries.hasNext()) {

					// and draw a line to the next point
					i = spModel.indexOf(nextSeries.next());
					x2 = spModel.getView().get(i, X) + jitter.get(i, X);
					y2 = spModel.getView().get(i, Y) + jitter.get(i, Y);
					line = new Line2D.Double(x1, y1, x2, y2);
					g2.draw(line);
					// g2.fill(MarkerFactory.buildArrowHead(line,
					// markerRadius, true));

					x1 = x2;
					y1 = y2;
				}
			}
		}
	}

	/**
	 * Recursively draw an arc between the centroids of the members of this
	 * (binary) cluster
	 * 
	 * @param g2
	 */
	private void drawClusterArc(HierarchicalCluster cluster, Graphics2D g2) {

		// if this cluster just contains another cluster then draw that
		if (cluster.size() == 1 && cluster.get(0) instanceof HierarchicalCluster)
			drawClusterArc((HierarchicalCluster) cluster.get(0), g2);

		// if this cluster contains two subclusters then draw arc between their
		// centroids
		if (cluster.size() == 2) {
			HierarchicalCluster c0, c1;
			Matrix p0, p1;
			c0 = (HierarchicalCluster) cluster.get(0);
			c1 = (HierarchicalCluster) cluster.get(1);
			p0 = spModel.projection.project(c0.getCentroid());
			p1 = spModel.projection.project(c1.getCentroid());
			Double line = new Line2D.Double(p0.get(0, X), p0.get(0, Y), p1.get(0, X), p1.get(0, Y));
			g2.draw(line);
			drawClusterArc(c0, g2);
			drawClusterArc(c1, g2);
		}

	}

	/** Whether to add noise to the current view */
	public void addJitter(boolean showJitter) {
		this.showJitter = showJitter;
		if (showJitter) {
			updateJitter();
		} else
			// reset noise to null
			jitter = new Matrix(spModel.getNumDataPoints(), spModel.getNumDataDimensions());

	}

	/** Change the noise */
	private void updateJitter() {

		double scale = spModel.getTransform().getScaleX();
		Random ran = new Random();
		for (int i = 0; i < jitter.getRowDimension(); i++)
			for (int j = 0; j < jitter.getColumnDimension(); j++)
				jitter.set(i, j, (ran.nextDouble() - 0.5d) * 20d / scale);

	}

	public void modelChanged(TPPModelEvent e) {
		repaint();
	}

	public void componentHidden(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent e) {
		spModel.resizePlot(getWidth(), getHeight());
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

}
