package sdp.pc.vision.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sdp.pc.vision.WorldStateListener;
import sdp.pc.vision.WorldStatePainter;
import sdp.pc.vision.WorldStatePainter.HighlightMode;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class SettingsGUI extends JFrame {

	/* WindowBuilder fields */
	private JPanel contentPane;
	public SliderPanel sliderPanel;
	private JRadioButton btnBall;
	private JRadioButton rdbtnMain;
	private JRadioButton rdbtnSide;
	private JRadioButton rdbtnBlue;
	private JRadioButton rdbtnYellow;
	private JRadioButton rdbtnRight;
	private JRadioButton rdbtnLeft;
	private JSlider slRgbMin;
	private JSlider slRgbDelta;
	private JCheckBox chkWhiteOverlay;
	private JLabel lblPitchStuff;
	private JButton btnSave;
	private JButton btnForcePreprocess;
	private JCheckBox chkShowRawBorders;
	private JButton btnSetRawBorders;

	/* Other fields */

	/**
	 * The underlying settings manager to write values to.
	 */
	private SettingsManager settingsManager;

	/**
	 * The associated world painter.
	 * <p>
	 * When available allows changing pixel overlay style.
	 */
	private WorldStatePainter worldPainter;

	/**
	 * The associated world listener.
	 * <p>
	 * If available allows for forcing preprocessing
	 */
	private WorldStateListener worldListener;

	private boolean reloading = false;

	/* Test-only main function */
	public static void main(String[] args) {

		SettingsGUI n = new SettingsGUI();
		n.setSettingsManager(SettingsManager.defaultSettings);
		n.setVisible(true);
	}

	/* Event handlers */

	RadioActionListener ColourCodeListener = new RadioActionListener() {

		@Override
		public void selectedChanged(JRadioButton newButton, String command,
				boolean forced) {

			// avoid doing anything if we don't have a settings manager attached
			if (settingsManager == null)
				return;

			// get relevant button id
			lastButtonTag = SettingsManager.getColorCode(command);
			if (lastButtonTag < 0) {
				JOptionPane
						.showMessageDialog(
								contentPane,
								"Unable to find the requested color code "
										+ "'"
										+ command
										+ "' in SettingsManager. \n"
										+ "Please make sure the radio button's action command is appropriately set!");
				return;
			}

			// update the slider panel
			int[] minColors = settingsManager.getMinValues(lastButtonTag);
			int[] maxColors = settingsManager.getMaxValues(lastButtonTag);
			sliderPanel.setColorSource(minColors, maxColors);

			// update the world painter highlighting
			if (!forced && worldPainter != null)
				worldPainter.setHighlightMode(SettingsManager
						.getHighlightMode(lastButtonTag));
		}
	};

	RadioActionListener PitchIdListener = new RadioActionListener() {
		@Override
		public void selectedChanged(JRadioButton newButton, String command,
				boolean forced) {
			if (!forced) {
				settingsManager.setCurrentPitchId(newButton == rdbtnMain ? 0
						: 1);
				reloadSettings();
			}
		}
	};

	RadioActionListener DirectionListener = new RadioActionListener() {
		@Override
		public void selectedChanged(JRadioButton newButton, String command,
				boolean forced) {
			if (!forced) {
				if(worldListener != null)
					worldListener.getWorldState().setDirection(
						newButton == rdbtnLeft ? 0 : 1);
				if(settingsManager != null)
					settingsManager.setShootingDirection(newButton == rdbtnLeft ? 0 : 1);
			}
		}

	};

	RadioActionListener TeamListener = new RadioActionListener() {
		@Override
		public void selectedChanged(JRadioButton newButton,
				String actionCommand, boolean forced) {
			if (!forced) {
				if(worldListener != null)
					worldListener.getWorldState().setOurColor(
						newButton == rdbtnYellow ? 0 : 1);
				if(settingsManager != null)
					settingsManager.setOurTeam(newButton == rdbtnYellow ? 0 : 1);
			}
		}
	};

	ActionListener SettingsManagerChangedListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean hasChange = settingsManager.isChanged();
			SettingsGUI.this.setTitle("Control GUI"
					+ (hasChange ? " *" : ""));

			btnSave.setEnabled(hasChange);
//			if(hasChange)
//				reloadSettings();
		}
	};
	private JCheckBox chkMulticore;
	private JCheckBox chkUseFisheye;
	private JSlider slFisheyePower;
	private JCheckBox chkAltColors;
	private JSlider slMedianSize;
	private JCheckBox chkDemoMode;

	/**
	 * Executed when the "Show white pixels" checkbox is clicked
	 */
	protected void onWhiteRgbDisplayChanged() {
		if (worldPainter == null)
			return;

		if (chkWhiteOverlay.isSelected())
			worldPainter.setHighlightMode(HighlightMode.White);
		else
			worldPainter.setHighlightMode(HighlightMode.None);
	}

	/**
	 * Executed when the white RGB delta slider's value changes
	 */
	protected void onRgbDeltaChanged(ChangeEvent e) {
		if (settingsManager != null && !reloading)
			settingsManager.setWhiteRgbDelta(slRgbDelta.getValue());
	}

	protected void onFisheyePowerChange() {
		if (settingsManager != null && !reloading)
			settingsManager.setFisheyePower(slFisheyePower.getValue());
	}

	/**
	 * Executed when the white RGB threshold slider's value changes
	 */
	protected void onRgbMinChanged(ChangeEvent e) {
		if (settingsManager != null && !reloading)
			settingsManager.setWhiteRgbThreshold(slRgbMin.getValue());
	}

	/**
	 * Executed when the Save button is clicked
	 */
	protected void onSaveClick() {
		if (settingsManager != null)
			settingsManager.save();
	}

	/**
	 * Executed when the "Show Raw Borders" checkbox is clicked
	 */
	protected void onChkShowRawBordersClick() {
		if (worldPainter != null)
			worldPainter.setRawBoundaryShown(chkShowRawBorders.isSelected());
	}

	/**
	 * Executed when a tab page in the main JTabbedPane is turned
	 * 
	 * @param tabId
	 *            The new tab page
	 */
	protected void onTabChanged(int tabId) {

		if (settingsManager != null && tabId == 1) {
			String pitchName = (settingsManager.getCurrentPitchId() == 0 ? "Main"
					: "Side");
			lblPitchStuff.setText(pitchName + " pitch");
		}

		if (worldPainter == null)
			return;

		if (tabId == 1) {
			int ccode = this.ColourCodeListener.lastButtonTag;
			if (ccode >= 0)
				worldPainter.setHighlightMode(SettingsManager
						.getHighlightMode(ccode));
			return;
		}

		if ((tabId == 0 && chkWhiteOverlay.isSelected()) || tabId == 2 && chkUseFisheye.isSelected())
			worldPainter.setHighlightMode(HighlightMode.White);
		else
			worldPainter.setHighlightMode(HighlightMode.None);
	}


	protected void onMulticoreCheckedChanged() {
		if(settingsManager != null && !reloading)
			settingsManager.setMulticoreProcessing(chkMulticore.isSelected());
	}

	protected void onChkAltColorsChanged() {
		if (settingsManager != null && !reloading && settingsManager.isUseAltColors() != chkAltColors.isSelected()) {
			sliderPanel.setUseAltColors(chkAltColors.isSelected());
			settingsManager.setUseAltColors(chkAltColors.isSelected());
		}
		
	}

	protected void onMedianSizeChanged() {
		if (settingsManager != null && !reloading)
			settingsManager.setMedianFilterSize(slMedianSize.getValue());
	}
	
	/**
	 * Executed when the window starts closing.
	 */
	protected void onWindowClosing() {

		if (settingsManager == null || !settingsManager.isChanged()) {
			this.dispose();
			return;
		}

		int saveChoice = JOptionPane
				.showConfirmDialog(
						this,
						"There are unsaved changes. Would you like to save them before closing?",
						"hrr", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);

		if (saveChoice == JOptionPane.CANCEL_OPTION)
			return;

		if (saveChoice == JOptionPane.YES_OPTION && settingsManager != null) {
			settingsManager.save();
		}

		this.dispose();
	}

	/**
	 * Executed when the "Force Preprocessing" button is clicked
	 */
	protected void onBtnForcePreprocessingClick() {
		if (worldListener != null)
			worldListener.forcePreprocess();
	}

	protected void onFisheyeToggle() {
		if(settingsManager != null)
			settingsManager.setFisheyeEnabled(chkUseFisheye.isSelected());
		if(worldPainter != null)
			worldPainter.setHighlightMode(chkUseFisheye.isSelected() ? HighlightMode.White : HighlightMode.None);
	}

	protected void onDemoModeChanged() {
		if (settingsManager != null && !reloading)
			settingsManager.setScreenshotMode(chkDemoMode.isSelected());
		
	}

	/**
	 * Executed when the "Set Raw Borders" button is clicked
	 */
	protected void onBtnSetRawBordersClick() {
		if (settingsManager != null)
			settingsManager.resetBoundary();
	}

	/**
	 * Creates the frame. Do not edit manually!
	 */
	public SettingsGUI() {
		setMinimumSize(new Dimension(0, 420));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onWindowClosing();
			}
		});
		setTitle("Control GUI");
		setIconImage(new ImageIcon("resource/settings.png").getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 467, 402);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setMaximumSize(new Dimension(32767, 200));
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onTabChanged(tabbedPane.getSelectedIndex());
			}
		});
		contentPane.add(tabbedPane);

		JPanel pGeneral = new JPanel();
		tabbedPane.addTab("General", null, pGeneral, null);
		pGeneral.setLayout(new BoxLayout(pGeneral, BoxLayout.Y_AXIS));

		JLabel lblGeneral = new JLabel("General");
		lblGeneral.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGeneral.setPreferredSize(new Dimension(80, 25));
		lblGeneral.setHorizontalAlignment(SwingConstants.LEFT);
		lblGeneral.setAlignmentX(0.5f);
		pGeneral.add(lblGeneral);

		JPanel pControlBox = new JPanel();
		pGeneral.add(pControlBox);
		pControlBox.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnForcePreprocess = new JButton("Force preprocessing");
		btnForcePreprocess.setEnabled(false);
		btnForcePreprocess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onBtnForcePreprocessingClick();
			}
		});
		pControlBox.add(btnForcePreprocess);

		btnSetRawBorders = new JButton("Set raw borders");
		btnSetRawBorders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onBtnSetRawBordersClick();
			}
		});
		btnSetRawBorders.setEnabled(false);
		pControlBox.add(btnSetRawBorders);

		chkShowRawBorders = new JCheckBox("Show raw borders");
		chkShowRawBorders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChkShowRawBordersClick();
			}
		});
		pGeneral.add(chkShowRawBorders);

		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(32767, 130));
		pGeneral.add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("175px"),
				ColumnSpec.decode("pref:grow"), ColumnSpec.decode("pref:grow"),
				FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC, }));

		JLabel lblPitchId = new JLabel("Pitch ID:");
		panel.add(lblPitchId, "2, 2");

		rdbtnMain = new JRadioButton("Main");
		rdbtnMain.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnMain, "3, 2");

		rdbtnSide = new JRadioButton("Side");
		rdbtnSide.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnSide, "4, 2");

		JLabel lblOurColour = new JLabel("Our colour:");
		panel.add(lblOurColour, "2, 4");

		rdbtnYellow = new JRadioButton("Yellow");
		rdbtnYellow.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnYellow, "3, 4");

		rdbtnBlue = new JRadioButton("Blue");
		rdbtnBlue.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnBlue, "4, 4");

		JLabel lblShootingDirection = new JLabel("Shooting direction:");
		panel.add(lblShootingDirection, "2, 6");

		rdbtnLeft = new JRadioButton("Left");
		rdbtnLeft.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnLeft, "3, 6");

		rdbtnRight = new JRadioButton("Right");
		rdbtnRight.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(rdbtnRight, "4, 6");

		JLabel lblNewLabel = new JLabel("White Detection");
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		lblNewLabel.setPreferredSize(new Dimension(80, 25));
		pGeneral.add(lblNewLabel);

		JPanel pWhiteDetection = new JPanel();
		pWhiteDetection.setMaximumSize(new Dimension(32767, 130));
		pGeneral.add(pWhiteDetection);
		pWhiteDetection.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("48dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("pref:grow"),
				FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));

		JLabel lblMinRgb = new JLabel("Min RGB:");
		pWhiteDetection.add(lblMinRgb, "2, 2");

		slRgbMin = new JSlider();
		slRgbMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onRgbMinChanged(e);
			}
		});
		slRgbMin.setMajorTickSpacing(50);
		slRgbMin.setMinorTickSpacing(10);
		slRgbMin.setPaintLabels(true);
		slRgbMin.setPaintTicks(true);
		slRgbMin.setMaximum(255);
		pWhiteDetection.add(slRgbMin, "4, 2");

		JLabel lblDeltaRgb = new JLabel("Delta RGB:");
		pWhiteDetection.add(lblDeltaRgb, "2, 4");

		slRgbDelta = new JSlider();
		slRgbDelta.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onRgbDeltaChanged(e);
			}
		});
		slRgbDelta.setPaintTicks(true);
		slRgbDelta.setPaintLabels(true);
		slRgbDelta.setMinorTickSpacing(10);
		slRgbDelta.setMaximum(255);
		slRgbDelta.setMajorTickSpacing(50);
		pWhiteDetection.add(slRgbDelta, "4, 4");

		chkWhiteOverlay = new JCheckBox("Display white pixels");
		chkWhiteOverlay.setEnabled(false);
		chkWhiteOverlay.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onWhiteRgbDisplayChanged();
			}
		});
		chkWhiteOverlay.setHorizontalAlignment(SwingConstants.RIGHT);
		pWhiteDetection.add(chkWhiteOverlay, "4, 6");

		JPanel pColors = new JPanel();
		tabbedPane.addTab("Colors", null, pColors, null);
		pColors.setLayout(new BoxLayout(pColors, BoxLayout.Y_AXIS));

		lblPitchStuff = new JLabel("New label");
		lblPitchStuff.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblPitchStuff.setHorizontalAlignment(SwingConstants.CENTER);
		pColors.add(lblPitchStuff);

		JPanel pColorCodes = new JPanel();
		pColorCodes.setMaximumSize(new Dimension(32767, 80));
		pColorCodes.setPreferredSize(new Dimension(200, 80));
		pColors.add(pColorCodes);
		pColorCodes.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnBall = new JRadioButton("Ball");
		pColorCodes.add(btnBall);

		JRadioButton btnBlue = new JRadioButton("Blue");
		pColorCodes.add(btnBlue);

		JRadioButton btnPlate = new JRadioButton("Plate");

		JRadioButton btnYellow = new JRadioButton("Yellow");
		pColorCodes.add(btnYellow);
		pColorCodes.add(btnPlate);

		JRadioButton btnGray = new JRadioButton("Gray");
		pColorCodes.add(btnGray);

		sliderPanel = new SliderPanel();
		pColors.add(sliderPanel);
		
		JPanel pExtras = new JPanel();
		tabbedPane.addTab("Extras", null, pExtras, null);
		pExtras.setLayout(new BoxLayout(pExtras, BoxLayout.Y_AXIS));
		
		JPanel pFisheye = new JPanel();
		pFisheye.setBorder(new LineBorder(new Color(0, 0, 0)));
		pExtras.add(pFisheye);
		pFisheye.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.UNRELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:7dlu"),
				ColumnSpec.decode("left:max(65dlu;pref)"),
				ColumnSpec.decode("max(32dlu;default):grow"),
				FormFactory.UNRELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.PREF_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:pref"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chkDemoMode = new JCheckBox("Pretty Mode");
		chkDemoMode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onDemoModeChanged();
			}
		});
		chkDemoMode.setToolTipText("Make nice screenshots at the cost of some FPS. ");
		chkDemoMode.setAlignmentX(0.5f);
		pFisheye.add(chkDemoMode, "2, 6, 3, 1");
		
		JLabel lblFisheyeCorrection = new JLabel("Fisheye correction");
		pFisheye.add(lblFisheyeCorrection, "2, 8, 2, 1");
		
		slFisheyePower = new JSlider();
		slFisheyePower.setMaximum(200);
		slFisheyePower.setMaximumSize(new Dimension(32767, 32767));
		slFisheyePower.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onFisheyePowerChange();
			}
		});
		
		chkUseFisheye = new JCheckBox("Enabled");
		chkUseFisheye.setAlignmentX(Component.CENTER_ALIGNMENT);
		chkUseFisheye.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onFisheyeToggle();
			}
		});
		pFisheye.add(chkUseFisheye, "3, 10, left, center");
		
		JLabel lblFisheyePower = new JLabel("Strength:");
		pFisheye.add(lblFisheyePower, "3, 12");
		slFisheyePower.setMajorTickSpacing(40);
		slFisheyePower.setMinorTickSpacing(5);
		slFisheyePower.setPaintLabels(true);
		slFisheyePower.setPaintTicks(true);
		slFisheyePower.setValue(10);
		pFisheye.add(slFisheyePower, "4, 12, fill, fill");
		
		JLabel lblMedianFilter = new JLabel("Median Filter");
		pFisheye.add(lblMedianFilter, "2, 14, 2, 1");
		
		JLabel lblSize = new JLabel("Size:");
		pFisheye.add(lblSize, "3, 16");
		
		slMedianSize = new JSlider();
		slMedianSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				onMedianSizeChanged();
			}
		});
		slMedianSize.setMaximum(16);
		slMedianSize.setValue(2);
		slMedianSize.setPaintTicks(true);
		slMedianSize.setPaintLabels(true);
		slMedianSize.setMinorTickSpacing(1);
		slMedianSize.setMaximumSize(new Dimension(32767, 32767));
		slMedianSize.setMajorTickSpacing(2);
		pFisheye.add(slMedianSize, "4, 16");
		
		chkMulticore = new JCheckBox("Concurrent world processing");
		pFisheye.add(chkMulticore, "2, 2, 3, 1");
		chkMulticore.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		chkAltColors = new JCheckBox("Use alternative color metric");
		chkAltColors.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onChkAltColorsChanged();
			}
		});
		chkAltColors.setToolTipText("If enabled, assigns each color type a base RGB \nand maximum euclidean distance a pixel can deviate from it.\n(a sphere in the RGB space) \n\nTraditional color metric uses RGB and HSB ranges for each color type. \nFor a pixel to match its value must be in the given R/G/B/H/S/Br range.\n(2 cubes in RGB/HSB space)  ");
		chkAltColors.setAlignmentX(0.5f);
		pFisheye.add(chkAltColors, "2, 4, 3, 1");
		chkMulticore.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				onMulticoreCheckedChanged();
			}
		});

		btnBall.addActionListener(ColourCodeListener);
		btnBlue.addActionListener(ColourCodeListener);
		btnPlate.addActionListener(ColourCodeListener);
		btnYellow.addActionListener(ColourCodeListener);
		btnGray.addActionListener(ColourCodeListener);

		rdbtnMain.addActionListener(PitchIdListener);
		rdbtnSide.addActionListener(PitchIdListener);

		rdbtnLeft.addActionListener(DirectionListener);
		rdbtnRight.addActionListener(DirectionListener);

		rdbtnYellow.addActionListener(TeamListener);
		rdbtnBlue.addActionListener(TeamListener);

		btnSave = new JButton("Save changes");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onSaveClick();
			}
		});
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(btnSave);
	}

	/**
	 * Gets the currently attached settings manager object.
	 */
	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	/**
	 * Sets the currently attached settings manager object.
	 */
	public void setSettingsManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;
		settingsManager.changeListener = this.SettingsManagerChangedListener;
		this.btnSetRawBorders.setEnabled(settingsManager != null);
		reloadSettings();
	}

	/**
	 * Gets the currently attached world painter object.
	 */
	public WorldStatePainter getWorldPainter() {
		return worldPainter;
	}

	/**
	 * Sets the currently attached world painter object.
	 */
	public void setWorldPainter(WorldStatePainter worldPainter) {
		this.worldPainter = worldPainter;
		chkWhiteOverlay.setEnabled(worldPainter != null);
		if (worldPainter != null)
			chkWhiteOverlay.setSelected(worldPainter.isRawBoundaryShown());
	}

	/**
	 * Gets the currently attached world listener object.
	 */
	public WorldStateListener getWorldListener() {
		return worldListener;
	}

	/**
	 * Sets the currently attached world listener object.
	 */
	public void setWorldListener(WorldStateListener worldListener) {
		this.worldListener = worldListener;
		this.btnForcePreprocess.setEnabled(worldListener != null);
	}

	/**
	 * Updates all the UI values as if we've just reloaded the settings
	 */
	private void reloadSettings() {
		if (settingsManager != null) {
			reloading = true;
			JRadioButton pitch = (settingsManager.getCurrentPitchId() == 0) ? rdbtnMain
					: rdbtnSide;
			JRadioButton team = (settingsManager.getOurTeam() == 0) ? rdbtnYellow
					: rdbtnBlue;
			JRadioButton direction = (settingsManager.getShootingDirection() == 0) ? rdbtnLeft
					: rdbtnRight;

			PitchIdListener.setSelectedButton(pitch);
			TeamListener.setSelectedButton(team);
			DirectionListener.setSelectedButton(direction);
			ColourCodeListener.setSelectedButton(btnBall);

			slRgbMin.setValue(settingsManager.getWhiteRgbThreshold());
			slRgbDelta.setValue(settingsManager.getWhiteRgbDelta());

			SettingsManagerChangedListener.actionPerformed(null);

			chkMulticore.setSelected(settingsManager.isMulticoreProcessing());
			chkUseFisheye.setSelected(settingsManager.isFisheyeEnabled());
			slFisheyePower.setValue(settingsManager.getFisheyePower());
			
			chkAltColors.setSelected(settingsManager.isUseAltColors());
			chkDemoMode.setSelected(settingsManager.isScreenshotMode());
			slMedianSize.setValue(settingsManager.getMedianFilterSize());
			
			reloading = false;
		}
	}
}
