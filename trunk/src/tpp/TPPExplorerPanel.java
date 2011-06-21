package tpp;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.PreprocessPanel;
import weka.gui.explorer.Explorer.ExplorerPanel;

/**
 * An ExplorerPanel for Targeted Projection Pursuit
 */
public class TPPExplorerPanel extends JPanel implements ExplorerPanel,
		ComponentListener, TechnicalInformationHandler, ActionListener,
		TPPModelEventListener {

	private static final String HELP_URL = "http://code.google.com/p/targeted-projection-pursuit/wiki/Introduction";

	private ScatterPlotModel model;

	ScatterPlotViewPanel viewPanel;

	ScatterPlotControlPanel controlPanel = null;

	private DataViewer dataViewer;

	private Explorer explorer;

	private JSplitPane splitPane;

	private JPanel rhPanel;

	private JComboBox fileCombo;

	private JComboBox viewCombo;

	private JButton helpButton;

	private boolean dataStructureChangeTriggeredByTPP;

	private void showDataViewer(boolean show) {
		if (show) {
			dataViewer = new DataViewer(model);
		} else {
			if (dataViewer != null) {
				dataViewer.setVisible(false);
				dataViewer.dispose();
			}
		}

	}

	public TPPModel getModel() {
		return model;
	}

	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public String getTabTitle() {
		return "Projection Plot";
	}

	@Override
	public String getTabTitleToolTip() {
		return "Explore data using Targeted Projection Pursuit";
	}

	@Override
	public void setExplorer(Explorer e) {
		explorer = e;

	}

	@Override
	public void setInstances(Instances in) {
		// only respond to changes that are triggered by other panels
		if (dataStructureChangeTriggeredByTPP)
			return;
		try {
			removeAll();
			setLayout(new BorderLayout());
			model = new ScatterPlotModel(2);
			model.setInstances(in);
			viewPanel = new ScatterPlotViewPanel();
			viewPanel.setModel(model);
			ScatterPlotViewPanelMouseListener l = new ScatterPlotViewPanelMouseListener(
					viewPanel, model);
			viewPanel.addMouseListener(l);
			viewPanel.addMouseMotionListener(l);
			controlPanel = new ScatterPlotControlPanel();
			controlPanel.setModel(model);
			rhPanel = new JPanel();
			rhPanel.setLayout(new BoxLayout(rhPanel, BoxLayout.Y_AXIS));
			rhPanel.add(getToolBar());
			rhPanel.add(controlPanel);
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewPanel,
					rhPanel);
			splitPane.setDividerLocation(getParent().getSize().width - 250);
			splitPane.setResizeWeight(0.8);
			Dimension minimumSize = new Dimension(250, 250);
			viewPanel.setMinimumSize(minimumSize);
			rhPanel.setMinimumSize(minimumSize);
			add(splitPane);
			model.addListener(this);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"There was a problem reading that data");
		}

	}

	private JComponent getToolBar() {
		// we'd like to implement these options as menu items, but you can't add
		// menu bars to a panel
		JPanel toolbar = new JPanel();
		toolbar.setLayout(new GridLayout(1, 3, 6, 2));
		toolbar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
		fileCombo = new WideComboBox(new String[] { "File", " Save projection",
				" Save view", " Save view as EPS image",
				" Save view as SVG image" });
		viewCombo = new WideComboBox(new String[] { "View",
				" Project onto first two principal components",
				" Resize view to fit to window (right mouse button)",
				" Show/Hide axes", " Random projection" });
		helpButton = new JButton("Help");
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseHelp();
			}
		});
		Dimension min = new Dimension(80, 20);
		fileCombo.setMinimumSize(min);
		viewCombo.setMinimumSize(min);
		helpButton.setMinimumSize(min);
		toolbar.add(fileCombo);
		toolbar.add(viewCombo);
		toolbar.add(helpButton);
		fileCombo.addActionListener(this);
		viewCombo.addActionListener(this);
		helpButton.addActionListener(this);
		return toolbar;
	}

	public void componentResized(ComponentEvent e) {
		repaint();
	}

	public void componentMoved(ComponentEvent e) {
		repaint();
	}

	public void componentShown(ComponentEvent e) {
		// rebuild the entire panel every tiem it is reshown, in case there have
		// been any changes to the data
		model.fireModelChanged(TPPModelEvent.DATA_SET_CHANGED);
	}

	public void componentHidden(ComponentEvent e) {
	}

	public TPPExplorerPanel() {
		super();
		addComponentListener(this);
	}

	public TPPExplorerPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		addComponentListener(this);
	}

	public TPPExplorerPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		addComponentListener(this);
	}

	public TPPExplorerPanel(LayoutManager layout) {
		super(layout);
		addComponentListener(this);
	}

	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation info = new TechnicalInformation(Type.ARTICLE);
		info.setValue(Field.AUTHOR, "Faith,J");
		info.setValue(Field.YEAR, "2007");
		info.setValue(
				Field.TITLE,
				"Targeted Projection Pursuit for Interactive Exploration of High-Dimensional Data Sets");
		info.setValue(
				Field.JOURNAL,
				"Proceedings of 11th International Conference on Information Visualisation (IV07)");
		return info;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fileCombo) {
			switch (fileCombo.getSelectedIndex()) {
			case 1:
				Exporter.saveCurrentProjection(model, null);
				break;
			case 2:
				Exporter.saveCurrentViewData(model, null);
				break;
			case 3:
				Exporter.saveViewAsEPSImage(viewPanel, model, null);
				break;
			case 4:
				Exporter.saveViewAsSVGImage(viewPanel, model, null);
				break;
			}
			fileCombo.setSelectedIndex(0);
		}
		if (e.getSource() == viewCombo) {
			switch (viewCombo.getSelectedIndex()) {
			case 1:
				model.PCA();
				model.resizePlot();
				break;
			case 2:
				model.resizePlot();
				break;
			case 3:
				model.setShowAxes(!(model.showAxes()));
				break;
			case 4:
				model.randomProjection();
				model.resizePlot();
				break;
			}
			viewCombo.setSelectedIndex(0);
		}
	}

	// A combobox whose menu is wider than its label
	// http://www.jroller.com/santhosh/entry/make_jcombobox_popup_wide_enough
	// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4618607
	private class WideComboBox extends JComboBox {

		public WideComboBox(final Object items[]) {
			super(items);
		}

		private boolean layingOut = false;

		public void doLayout() {
			try {
				layingOut = true;
				super.doLayout();
			} finally {
				layingOut = false;
			}
		}

		public Dimension getSize() {
			Dimension dim = super.getSize();
			if (!layingOut)
				dim.width = Math.max(dim.width, getPreferredSize().width);
			return dim;
		}
	}

	public void modelChanged(TPPModelEvent e) {
		if (e.getType() == TPPModelEvent.DATA_STRUCTURE_CHANGED) {
			// set a flag indicating that the change in data structure comes
			// from the TPP application, to prevent infinite loops
			dataStructureChangeTriggeredByTPP = true;
			getExplorer().getPreprocessPanel().setInstances(
					model.getInstances());
			dataStructureChangeTriggeredByTPP = false;
		}
	}

    private static void browseHelp() {
        if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                        desktop.browse(new URI(HELP_URL));
                } catch (Exception e) {
                        // TODO: error handling
                }
        } else {
                // TODO: error handling
        }
    }
}