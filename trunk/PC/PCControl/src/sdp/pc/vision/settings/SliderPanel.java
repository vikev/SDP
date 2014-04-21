package sdp.pc.vision.settings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * A generic panel with sliders providing general colour range selection
 * capabilities.
 * 
 * @author s1141301
 * 
 */
@SuppressWarnings("serial")
public class SliderPanel extends JPanel {

	/**
	 * the min/max thresholds
	 */
	private int[] minColors, maxColors;

	private JLabel lblRed;
	private JLabel lblGreen;
	private JLabel lblBlue;
	private JLabel lblSat;
	private JLabel lblHue;
	private JLabel lblBr;

	private RangeSlider slRed;
	private RangeSlider slGreen;
	private RangeSlider slBlue;
	private RangeSlider slHue;
	private RangeSlider slSaturation;
	private RangeSlider slBrightness;

	private RangeSlider[] sliders;

	private boolean isAlt;

	public void setUseAltColors(boolean alt) {
		isAlt = alt;
		slSaturation.setEnabled(!alt);
		slBrightness.setEnabled(!alt);
		lblHue.setText(alt ? "Distance" : "Hue");
		lblSat.setEnabled(!alt);
		lblBr.setEnabled(!alt);
		if (maxColors != null) {
			maxColors[0] = 255;
			maxColors[1] = 255;
			maxColors[2] = 255;
			maxColors[3] = 100;
		}
		System.out.println("Changed colorz!");
	}

	public void update() {
		for (int i = 0; i < sliders.length; i++)
			if (minColors != null && maxColors != null) {
				sliders[i].setValue(minColors[i]);
				sliders[i].setUpperValue(maxColors[i]);
			}
	}

	/**
	 * Whether the control is currently updating its values and needs not save
	 * stuff to the backing storage (arrays)
	 */
	private boolean refreshing = false;

	/**
	 * The change listener for slider movement
	 */
	private ChangeListener sliderChange = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!(e.getSource() instanceof RangeSlider) || refreshing)
				return;

			RangeSlider sl = (RangeSlider) e.getSource();
			for (int i = 0; i < sliders.length; i++) {
				if (sl == sliders[i]) {
					if (minColors != null && maxColors != null) {
						minColors[i] = sl.getValue();
						if (!isAlt)
							maxColors[i] = sl.getUpperValue();
						else
							sl.setUpperValue(sl.getMaximum());
						SettingsManager.defaultSettings.registerChange();
					}
					// break;
				}
			}
		}

	};

	/**
	 * Create the panel. Do not edit this manually!
	 */
	public SliderPanel() {

		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("40dlu"),
				ColumnSpec.decode("max(32dlu;default):grow"),
				FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] {
				RowSpec.decode("12px"), RowSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
				FormFactory.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("pref:grow"),
				RowSpec.decode("12px"), }));

		lblRed = new JLabel("Red:");
		add(lblRed, "2, 2");

		slRed = new RangeSlider();
		slRed.setName("");
		slRed.setPaintLabels(true);
		slRed.setPaintTicks(true);
		slRed.setUpperValue(100);
		slRed.setMinorTickSpacing(10);
		slRed.setMaximum(255);
		slRed.setMajorTickSpacing(50);
		slRed.setAlignmentX(1.0f);
		slRed.addChangeListener(sliderChange);
		add(slRed, "3, 2");

		lblGreen = new JLabel("Green:");
		add(lblGreen, "2, 4");

		slGreen = new RangeSlider();
		slGreen.setPaintTicks(true);
		slGreen.setPaintLabels(true);
		slGreen.setUpperValue(100);
		slGreen.setMinorTickSpacing(10);
		slGreen.setMaximum(255);
		slGreen.setMajorTickSpacing(50);
		slGreen.setAlignmentX(1.0f);
		slGreen.addChangeListener(sliderChange);
		add(slGreen, "3, 4");

		lblBlue = new JLabel("Blue:");
		add(lblBlue, "2, 6");

		slBlue = new RangeSlider();
		slBlue.setPaintTicks(true);
		slBlue.setPaintLabels(true);
		slBlue.setUpperValue(100);
		slBlue.setMinorTickSpacing(10);
		slBlue.setMaximum(255);
		slBlue.setMajorTickSpacing(50);
		slBlue.setAlignmentX(1.0f);
		slBlue.addChangeListener(sliderChange);
		add(slBlue, "3, 6");

		lblHue = new JLabel("Hue:");
		add(lblHue, "2, 8");

		slHue = new RangeSlider();
		slHue.setValue(25);
		slHue.setPaintTicks(true);
		slHue.setPaintLabels(true);
		slHue.setUpperValue(75);
		slHue.setMinorTickSpacing(5);
		slHue.setMajorTickSpacing(20);
		slHue.setAlignmentX(1.0f);
		slHue.addChangeListener(sliderChange);
		add(slHue, "3, 8");

		lblSat = new JLabel("Saturation:");
		add(lblSat, "2, 10");

		slSaturation = new RangeSlider();
		slSaturation.setValue(25);
		slSaturation.setPaintTicks(true);
		slSaturation.setPaintLabels(true);
		slSaturation.setUpperValue(75);
		slSaturation.setMinorTickSpacing(5);
		slSaturation.setMajorTickSpacing(20);
		slSaturation.setAlignmentX(1.0f);
		slSaturation.addChangeListener(sliderChange);
		add(slSaturation, "3, 10");

		lblBr = new JLabel("Brightness:");
		add(lblBr, "2, 12");

		slBrightness = new RangeSlider();
		slBrightness.setValue(25);
		slBrightness.setPaintTicks(true);
		slBrightness.setPaintLabels(true);
		slBrightness.setUpperValue(75);
		slBrightness.setMinorTickSpacing(5);
		slBrightness.setMajorTickSpacing(20);
		slBrightness.setAlignmentX(1.0f);
		slBrightness.addChangeListener(sliderChange);
		add(slBrightness, "3, 12");

		sliders = new RangeSlider[] { slRed, slGreen, slBlue, slHue,
				slSaturation, slBrightness };

		setUseAltColors(SettingsManager.defaultSettings.isUseAltColors());
	}

	/**
	 * Sets the colour intervals' source.
	 * 
	 * @param minColors
	 *            an array of length 6 specifying the minimum components of the
	 *            allowed colours
	 * @param maxColors
	 *            an array of length 6 specifying the maximum components of the
	 *            allowed colours
	 */
	public void setColorSource(int[] minColors, int[] maxColors) {
		this.minColors = minColors;
		this.maxColors = maxColors;

		refreshing = true;
		for (int i = 0; i < 6; i++) {
			sliders[i].setValue(minColors[i]);
			sliders[i].setUpperValue(maxColors[i]);
		}
		refreshing = false;
	}

}
