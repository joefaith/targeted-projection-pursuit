package tpp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;
import weka.gui.explorer.ExplorerDefaults;

import weka.clusterers.Clusterer;

/*
 * Created on 23-May-2006
 *
 */

/**
 * An extension of JPanel that includes the controls and projections for a data
 * set
 * 
 * @author Joe
 * 
 */
public class ScatterPlotControlPanel extends JPanel implements TPPModelEventListener, ActionListener, ChangeListener {

	private JButton clusterButton;

	private JComboBox clusterNumberCombo;

	private SelectionPanel selectionPanel;

	private AttributeCombo seriesIdCombo;

	private AttributeCombo sizeCombo;

	private AttributeCombo selectCombo;

	private AttributeCombo seriesIndexCombo;

	private JButton createSeriesButton;

	private SmoothButton smoothSeriesButton;

	private JButton createTestSetButton;

	private JComboBox createTestSetKCombo;

	private JButton removeTestSetButton;

	private SeparatePointsButton separateButton;

	private JButton removeAttributeButton;

	private JButton normaliseButton;

	private AttributeCombo fillCombo;

	private AttributeCombo shapeCombo;

	private AbstractButton applyClassifierButton;

	private AttributeCombo classificationTargetCombo;

	// private JComboBox classificationCombo;

	private JSlider markerSlider;

	private JComboBox pointSelectorCombo;

	private JButton pointSelectorButton;

	private JButton removeSeriesButton;

	protected ProjectionTable projectionTable = null;

	protected ScatterPlotModel model;

	private GenericObjectEditor classifierChooser;

	private PropertyPanel classifierChooserPanel;

	private Dimension min = new Dimension(100, 20);

	private JButton undoButton;

	public ScatterPlotControlPanel() {
		super();
	}

	public void setModel(ScatterPlotModel tpp) throws TPPException {
		this.model = tpp;
		model.addListener(this);
		init();
	}

	/**
	 * Initialise this panel, creating the controls necessary to manipulate this
	 * scatter plot
	 * 
	 * @throws TPPException
	 */
	public void init() {

		removeAll();
		revalidate();
		setLayout(new GridBagLayout());
		GridBagConstraints grid = new GridBagConstraints();
		grid.fill = GridBagConstraints.BOTH;

		grid.insets = new Insets(0, 0, 0, 0);
		grid.weightx = 1.0;
		grid.gridy = 0;

		// create a panel for holding the view options
		JPanel viewOptionsPanel = new JPanel();
		viewOptionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints viewOptionsGrid = new GridBagConstraints();
		viewOptionsGrid.fill = GridBagConstraints.BOTH;
		viewOptionsGrid.weightx = 1.0;
		viewOptionsGrid.gridy = 0;
		viewOptionsGrid.insets = new Insets(2, 2, 2, 0);
		viewOptionsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("View options"),
				BorderFactory.createEmptyBorder(0, 2, 2, 2)));

		addSelectionPanel(viewOptionsPanel, viewOptionsGrid);
		addSizeAttributeSelector(viewOptionsPanel, viewOptionsGrid);
		addFillAttributeSelector(viewOptionsPanel, viewOptionsGrid);
		addShapeAttributeSelector(viewOptionsPanel, viewOptionsGrid);
		addMarkerSizeSlider(viewOptionsPanel, viewOptionsGrid);

		// and add these options to the control panel
		grid.gridy = 0;
		grid.gridx = 0;
		grid.gridwidth = GridBagConstraints.REMAINDER;
		add(viewOptionsPanel, grid);

		// create a panel for holding the available actions
		JPanel actionsPanel = new JPanel();
		actionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints actionsGrid = new GridBagConstraints();
		actionsGrid.fill = GridBagConstraints.BOTH;
		actionsGrid.weightx = 1.0;
		actionsGrid.gridy = 0;
		actionsGrid.insets = new Insets(2, 2, 2, 0);
		actionsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Cluster and Classify"), BorderFactory.createEmptyBorder(0, 2, 2, 2)));

		addClustererButton(actionsPanel, actionsGrid);
		addClassificationButton(actionsPanel, actionsGrid);

		// experimental UI elements:
		// addPointSelector(actionsPanel, actionsGrid);
		// addSeriesCreationButton(actionsPanel, actionsGrid);
		// addTestSetCreationButton(actionsPanel, actionsGrid);

		// and add these options to the control panel
		grid.gridy++;
		add(actionsPanel, grid);

		// add the projection table and attribute selection actions
		addProjectionTableAndAttributeSelection(grid);

		revalidate();

	}

	private void addProjectionTableAndAttributeSelection(GridBagConstraints grid) {
		// add projection table
		projectionTable = new ProjectionTable(model);
		projectionTable
				.setToolTipText(
						"<html><p width=\"300px\">This table shows the components of the linear projection used to produce the view of the data. The x and y components of each attribute (axis) are shown, and the 'Significance' column shows the overall length. The rows can be ordered by each of the columns (click on the header to re-order). By clicking on the 'Significance' column you can find which attributes are most significant in producing the view. Values from the table can be copied to the clipboard and imported to Excel etc. Or these values can be saved from the File menu.</p></html>");
		JScrollPane tablePane = new JScrollPane(projectionTable);
		grid.gridx = 0;
		grid.gridwidth = 3;
		grid.gridy++;
		grid.weighty = 1;
		add(tablePane, grid);

		normaliseButton = new JButton("Normalise");
		normaliseButton.addActionListener(this);
		normaliseButton.setToolTipText("Normalise numeric attributes to [0,1]");
		grid.gridy++;
		grid.gridx = 0;
		grid.weighty = 0;
		grid.gridwidth = 1;
		add(normaliseButton, grid);

		removeAttributeButton = new JButton("Remove");
		removeAttributeButton.addActionListener(this);
		removeAttributeButton.setToolTipText("Remove the selected attributes");
		grid.gridx = 1;
		grid.weighty = 0;
		grid.gridwidth = 1;
		add(removeAttributeButton, grid);

		undoButton = new JButton("Undo");
		undoButton.addActionListener(this);
		undoButton.setToolTipText("Restore the removed attributes");
		undoButton.setEnabled(model.canUndo());
		grid.gridx = 2;
		grid.gridwidth = 1;
		add(undoButton, grid);
	}

	private void addClassificationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// Add classification crossvalidation, but only if there are any nominal
		// attributes
		if (model.getNominalAttributes() != null && model.getNominalAttributes().size() > 0) {

			// Lets the user configure the classifier.
			classifierChooser = new GenericObjectEditor();

			// The panel showing the current classifier selection.
			classifierChooserPanel = new PropertyPanel(classifierChooser);
			classifierChooser.setClassType(Classifier.class);
			classifierChooser.setValue(ExplorerDefaults.getClassifier());

			classificationTargetCombo = AttributeCombo.buildCombo(model, AttributeCombo.NOMINAL_ATTRIBUTES, false);
			classificationTargetCombo.setToolTipText("Choose the target attribute for the classifier");
			applyClassifierButton = new JButton("Apply classifier:");
			applyClassifierButton.addActionListener(this);
			applyClassifierButton
					.setToolTipText(
							"<html><p width=\"300px\">Test the performance of a classification algorithm on this data. The chosen classifier is applied using 10-fold cross-validation, and the resulting predicted classifications and error are shown.</p></html>");
			classifierChooserPanel.setToolTipText("Choose a classifier from the Weka toolkit");
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			actionsPanel.add(applyClassifierButton, actionsGrid);
			actionsGrid.gridx = 1;
			actionsPanel.add(classifierChooserPanel, actionsGrid);
			actionsGrid.gridx = 2;
			actionsPanel.add(classificationTargetCombo, actionsGrid);
		}
	}

	private void addTestSetCreationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// Add test set creation button
		Vector<Integer> testSetKValues = new Vector<Integer>();
		for (int k = 2; k < 11; k++)
			testSetKValues.add(Integer.valueOf(k));
		createTestSetKCombo = new JComboBox(testSetKValues);
		createTestSetKCombo
				.setToolTipText(
						"<html><p width=\"300px\">What proportion of the data will be used as a test set (1/k)</p></html>");
		createTestSetButton = new JButton("Create test set");
		createTestSetButton.addActionListener(this);
		createTestSetButton
				.setToolTipText(
						"<html><p width=\"300px\">Create a test set. The points in the test set will not be affected by projection pursuit operations (dragging or separating etc), so this shows the generalisability and robustness of the projection pursuit operations</p></html>");
		removeTestSetButton = new JButton("Remove test set");
		removeTestSetButton.addActionListener(this);
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		actionsPanel.add(createTestSetKCombo, actionsGrid);
		actionsGrid.gridx = 1;
		actionsPanel.add(createTestSetButton, actionsGrid);
		actionsGrid.gridx = 2;
		actionsPanel.add(removeTestSetButton, actionsGrid);
	}

	private void addSeriesCreationButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// !!TODO it should be possible to identify series by string attributes
		if (model.getSeries() == null) {

			seriesIdCombo = AttributeCombo.buildCombo(model, AttributeCombo.NOMINAL_ATTRIBUTES, true);
			seriesIdCombo
					.setToolTipText(
							"<html><p width=\"300px\">Choose the attribute that identifies which series each point is a member of. If no attribute is chosen then include all points in a single series</p></html>");
			seriesIndexCombo = AttributeCombo.buildCombo(model, AttributeCombo.ORDERED_ATTRIBUTES, false);
			seriesIndexCombo.setMinimumSize(min);
			seriesIndexCombo
					.setToolTipText(
							"<html><p width=\"300px\">Choose the attribute used to order points in the series, such as a date.</p></html>");
			seriesIdCombo.setMinimumSize(min);
			if (model.getSeries() != null) {
				seriesIdCombo.setSelectedAttribute(model.getSeries().getIdAttribute());
				seriesIndexCombo.setSelectedAttribute(model.getSeries().getIndexAttribute());
			}
			createSeriesButton = new JButton("Show series (id & order)");
			createSeriesButton.addActionListener(this);
			createSeriesButton.setToolTipText("Divide the points into series, connected by lines");
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			actionsPanel.add(createSeriesButton, actionsGrid);
			actionsGrid.gridx = 1;
			actionsPanel.add(seriesIdCombo, actionsGrid);
			actionsGrid.gridx = 2;
			actionsGrid.gridwidth = GridBagConstraints.REMAINDER;
			actionsPanel.add(seriesIndexCombo, actionsGrid);
		}

		// if there are series currently defined then add a button for
		// denoising and removing them
		if (model.getSeries() != null) {
			smoothSeriesButton = new SmoothButton(model);
			smoothSeriesButton.setText("Smooth Series");
			smoothSeriesButton
					.setToolTipText(
							"<html><p width=\"300px\">Try to find a view of the data that removes low frequency noise and shows longer-term evolution of the system</p></html>");
			actionsGrid.gridy++;
			actionsGrid.gridx = 0;
			actionsGrid.gridwidth = 1;
			actionsPanel.add(smoothSeriesButton, actionsGrid);

			removeSeriesButton = new JButton("Remove Series");
			removeSeriesButton.addActionListener(this);
			removeSeriesButton.setToolTipText("No longer show the series lines");
			actionsGrid.gridx = 1;
			actionsGrid.gridwidth = 1;
			actionsPanel.add(removeSeriesButton, actionsGrid);
		}
	}

	private void addClustererButton(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// add clusterer button
		Vector<String> clusters = new Vector<String>();
		clusters.add("Use CV");
		for (int k = 2; k < 9; k++)
			clusters.add(" N=" + k);
		clusterNumberCombo = new JComboBox(clusters);
		clusterNumberCombo.setToolTipText(
				"Choose the number of clusters to find in the data, or let the clustering algorithm decide by cross validation");
		clusterButton = new JButton("Create clusters:");
		clusterButton.addActionListener(this);
		clusterButton
				.setToolTipText(
						"<html><p width=\"300px\">Use an unsupervised clustering algorithm (DBSCAN) to divide the points into clusters based on the value of the numeric attributes. You can either choose the number of clusters to create, or let DBSCAN decide by cross validation.</p></html>");
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		actionsPanel.add(clusterButton, actionsGrid);
		actionsGrid.gridx = 1;
		actionsGrid.gridwidth = 1;
		actionsPanel.add(clusterNumberCombo, actionsGrid);
	}

	private void addPointSelector(JPanel actionsPanel, GridBagConstraints actionsGrid) {
		// add point selector
		pointSelectorCombo = new JComboBox(model.getPointDescriptions());
		pointSelectorButton = new JButton("Add point to selection: ");
		pointSelectorButton.addActionListener(this);
		pointSelectorButton.setToolTipText("Add another point to the selection");
		actionsGrid.gridy++;
		actionsGrid.gridx = 0;
		actionsGrid.gridwidth = 1;
		actionsPanel.add(pointSelectorButton, actionsGrid);
		actionsGrid.gridx = 1;
		actionsGrid.gridwidth = 2;
		actionsPanel.add(pointSelectorCombo, actionsGrid);
	}

	private void addMarkerSizeSlider(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add marker slider
		JLabel markerSizeLabel = new JLabel("Marker size: ", JLabel.RIGHT);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(markerSizeLabel, viewOptionsGrid);
		markerSlider = new JSlider(1, (int) (model.MARKER_DEFAULT * 2000), (int) (model.MARKER_DEFAULT * 1000));
		markerSlider.setValue((int) (model.getMarkerSize() * 1000));
		markerSlider.addChangeListener(this);
		markerSlider.setToolTipText("Change the average size of the points");
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		viewOptionsPanel.add(markerSlider, viewOptionsGrid);
	}

	private void addShapeAttributeSelector(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add shape attribute selector
		shapeCombo = AttributeCombo.buildCombo(model, AttributeCombo.NON_NUMERIC_ATTRIBUTES, true);
		shapeCombo.setMinimumSize(min);
		shapeCombo.setSelectedAttribute(model.getShapeAttribute());
		shapeCombo.addActionListener(this);
		shapeCombo.setToolTipText("Choose which attribute is used to determine the shape of each point");
		JLabel shapeAttributeSelectorLabel = new JLabel("Shape points by: ", JLabel.RIGHT);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(shapeAttributeSelectorLabel, viewOptionsGrid);
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		viewOptionsPanel.add(shapeCombo, viewOptionsGrid);
	}

	private void addFillAttributeSelector(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add fill attribute selector
		fillCombo = AttributeCombo.buildCombo(model, AttributeCombo.NOMINAL_ATTRIBUTES, true);
		fillCombo.setMinimumSize(min);
		fillCombo.setSelectedAttribute(model.getFillAttribute());
		fillCombo.addActionListener(this);
		fillCombo.setToolTipText("Choose which attribute is used to determine whether each point is filled");
		JLabel fillAttributeSelectorLabel = new JLabel("Fill points by: ", JLabel.RIGHT);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(fillAttributeSelectorLabel, viewOptionsGrid);
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		viewOptionsPanel.add(fillCombo, viewOptionsGrid);
	}

	private void addSizeAttributeSelector(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add size attribute selector
		sizeCombo = AttributeCombo.buildCombo(model, AttributeCombo.NUMERIC_ATTRIBUTES, true);
		sizeCombo.setMinimumSize(min);
		sizeCombo.setSelectedAttribute(model.getSizeAttribute());
		sizeCombo.addActionListener(this);
		sizeCombo.setToolTipText("Choose which attribute is used to determine the size of each point");
		JLabel sizeAttributeSelectorLabel = new JLabel("Size points by: ", JLabel.RIGHT);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(sizeAttributeSelectorLabel, viewOptionsGrid);
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 2;
		viewOptionsPanel.add(sizeCombo, viewOptionsGrid);
	}

	private void addSelectionPanel(JPanel viewOptionsPanel, GridBagConstraints viewOptionsGrid) {
		// add selection attribute selector, with the current selection
		// attribute shown
		selectCombo = AttributeCombo.buildCombo(model, AttributeCombo.ALL_ATTRIBUTES, true);
		selectCombo.setMinimumSize(min);
		selectCombo.setSelectedAttribute(model.getSelectAttribute());
		selectCombo.addActionListener(this);
		selectCombo.setToolTipText("Choose which attribute is used to color and select points");
		JLabel selectAttributeSelectorLabel = new JLabel("Color points by: ", JLabel.RIGHT);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 0;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(selectAttributeSelectorLabel, viewOptionsGrid);
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(selectCombo, viewOptionsGrid);
		separateButton = new SeparatePointsButton(model, selectCombo);
		separateButton.setText("Separate points");
		separateButton
				.setToolTipText(
						"<html><p width=\"300px\">Try to find a projection in which the points are separated on the basis of the chosen attribute. If the attribute is nominal then each of the classes will be separated as far as possible. If the attribute is numeric then the distance between points in will try to approximate to the difference in value of this attribute</p></html>");
		// can only separate points by a nominal attribute
		// (SeparatePoints contains code to separate by numerical attributes as
		// well but it doesn't work very well)
		separateButton.setEnabled(selectCombo.getSelectedAttribute() != null
				&& selectCombo.getSelectedAttribute().isNominal());
		viewOptionsGrid.gridx = 2;
		viewOptionsGrid.gridwidth = 1;
		viewOptionsPanel.add(separateButton, viewOptionsGrid);

		// add selection panel
		selectionPanel = new SelectionPanel(model);
		viewOptionsGrid.gridy++;
		viewOptionsGrid.gridx = 1;
		viewOptionsGrid.gridwidth = GridBagConstraints.REMAINDER;
		viewOptionsPanel.add(selectionPanel, viewOptionsGrid);
	}

	public void actionPerformed(ActionEvent event) {

		if (event.getSource() == clusterButton) {
			int index = ((JComboBox) clusterNumberCombo).getSelectedIndex();
			int n;
			if (index == 0) {
				n = -1; // -1 indicates that the clustering algorithm should choose the number of
						// clusters
			} else {
				n = index + 1; // Number of clusters starts at N=2
			}
			Attribute cluster = model.cluster(n);
			model.setSelectAttribute(cluster);
			model.setColourAttribute(cluster);
			init();
		}

		if (event.getSource() == createSeriesButton) {
			model.createSeries(seriesIndexCombo.getSelectedAttribute(), seriesIdCombo.getSelectedAttribute());
		}

		if (event.getSource() == removeSeriesButton) {
			model.removeSeries();
		}

		if (event.getSource() == sizeCombo)
			model.setSizeAttribute(sizeCombo.getSelectedAttribute());

		if (event.getSource() == fillCombo)
			model.setFillAttribute(fillCombo.getSelectedAttribute());

		if (event.getSource() == shapeCombo)
			model.setShapeAttribute(shapeCombo.getSelectedAttribute());

		if (event.getSource() == selectCombo) {
			// by default we should color the points by the same attribute we
			// are selecting by
			model.setSelectAttribute(selectCombo.getSelectedAttribute());
			model.setColourAttribute(selectCombo.getSelectedAttribute());
			selectionPanel.initialiseSelectionButtons();
			separateButton.setEnabled(model.getSelectAttribute() != null && model.getSelectAttribute().isNominal());
			revalidate();
			repaint();
		}

		if (event.getSource() == createTestSetButton)
			model.createTestSet(((Integer) createTestSetKCombo.getSelectedItem()).intValue());

		if (event.getSource() == removeTestSetButton)
			model.removeTestSet();

		if (event.getSource() == removeAttributeButton && projectionTable.getSelectedAttributeIndices() != null
				&& projectionTable.getSelectedAttributeIndices().length > 0)
			model.removeAttributes(projectionTable.getSelectedAttributes());

		if (event.getSource() == undoButton)
			model.undo();

		if (event.getSource() == normaliseButton)
			try {
				model.normalizeDataUnit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (event.getSource() == pointSelectorButton) {
			model.selectPoint(pointSelectorCombo.getSelectedIndex());
			model.drawRectangleAroundSelectedPoints();
		}

		if (event.getSource() == applyClassifierButton) {
			try {

				Attribute[] classification = model.createCrossValidation(
						classificationTargetCombo.getSelectedAttribute(), (Classifier) classifierChooser.getValue());
				// and fill the points by the error
				model.setColourAttribute(classification[0]);
				model.setSelectAttribute(classification[0]);
				model.setFillAttribute(classification[1]);
				init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tpp.ControlPanel#getClassificationPanel()
	 */
	public SelectionPanel getClassificationPanel() {
		return selectionPanel;
	}

	/** Marker slider state has changed */
	public void stateChanged(ChangeEvent e) {
		if (markerSlider == (JSlider) e.getSource())
			model.setMarkerSize(markerSlider.getValue() / 1000d);
	}

	public void setColours(ColourScheme colours) {
		selectionPanel = new SelectionPanel(model);
	}

	public void modelChanged(TPPModelEvent e) {
		switch (e.getType()) {
			case (TPPModelEvent.DATA_SET_CHANGED):
				init();
				break;
			case (TPPModelEvent.DATA_STRUCTURE_CHANGED):
				init();
				break;
			default:
				repaint();
		}
	}

	public ProjectionTable getProjectionTable() {
		return projectionTable;
	}

}