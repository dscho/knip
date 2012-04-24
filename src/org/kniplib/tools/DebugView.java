package org.kniplib.tools;

///* ------------------------------------------------------------------
// * This source code, its documentation and all appendant files
// * are protected by copyright law. All rights reserved.
// *
// * Copyright, 2003 - 2009
// * University of Konstanz, Germany.
// * Chair for Bioinformatics and Information Mining
// * Prof. Dr. Michael R. Berthold
// *
// * You may not modify, publish, transmit, transfer or sell, reproduce,
// * create derivative works from, distribute, perform, display, or in
// * any way exploit any of the content, in whole or in part, except as
// * otherwise expressly permitted in writing by the copyright owner or
// * as specified in the license file distributed with this product.
// *
// * If you have any quesions please contact the copyright holder:
// * website: www.knime.org
// * email: contact@knime.org
// * --------------------------------------------------------------------- *
// *
// * History
// *   01.06.2006 (ohl): created
// */
//package org.imalib.tools;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Image;
//import java.awt.Point;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.image.ColorModel;
//import java.awt.image.ImageConsumer;
//import java.awt.image.ImageProducer;
//import java.util.Map;
//import java.util.Vector;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//
//import net.imglib2.RandomAccess;
//import net.imglib2.img.Img;
//import net.imglib2.type.numeric.RealType;
//
//import org.imalib.algorithm.VoronoiSegmentationAlgorithm.LabelInfo;
//import org.imalib.data.PointNeighbors;
//
///**
// * @author ohl
// * 
// */
//public class DebugView<T extends RealType<T>> extends JFrame implements
//		ImageProducer {
//
//	private int[][] m_labelArray;
//
//	private Map<Integer, LabelInfo> m_labelMap;
//
//	private Vector<ImageConsumer> m_imageConsumer;
//
//	private JLabel m_imageLabel;
//
//	private JButton m_stepButton;
//
//	private ColorModel m_colorModel;
//
//	private int[] m_pixels;
//
//	private Img<T> m_orgImg;
//	RandomAccess<T> m_orgImgRA;
//
//	private int m_lastYChanged;
//
//	private int m_lastXChanged;
//
//	private JButton m_showLastButton;
//
//	private JTextField m_editField;
//
//	private int m_stepsToGo;
//
//	private int m_backgroundTreshold;
//
//	private JLabel m_infoLabel;
//
//	private JLabel m_colorLabel;
//
//	/**
//	 * Creates a new instance of the debug view.
//	 * 
//	 * @param cellPic
//	 *            the original cell picture
//	 * @param labelArray
//	 *            array containing labels.
//	 * @param colorMap
//	 *            mapping label numbers to labelinfos
//	 * @param backgrndThresh
//	 *            all pixels with gray values less than this are bg.
//	 * @param brighteningFactor
//	 *            the factor to brighten the image.
//	 */
//	DebugView(final Img<T> cellPic, final int[][] labelArray,
//			final Map<Integer, LabelInfo> colorMap, final int backgrndThresh) {
//		super("DebugView for ImageSegmentation");
//
//		m_lastXChanged = -1;
//		m_lastYChanged = -1;
//		m_stepsToGo = 1;
//		/*
//		 * Do the imageproducer preparations before instantiating the label that
//		 * displays the image (thus uses the imageproducer)
//		 */
//		m_labelArray = labelArray;
//		m_labelMap = colorMap;
//		m_imageConsumer = new Vector<ImageConsumer>();
//		// m_colorModel = new ColorModelMultStd8bitColor(brighteningFactor);
//		m_pixels = new int[m_labelArray[0].length];
//		m_orgImg = cellPic;
//		m_orgImgRA = m_orgImg.randomAccess();
//		m_backgroundTreshold = backgrndThresh;
//
//		getContentPane().add(getMainPanel());
//		pack();
//		invalidate();
//		validate();
//		setVisible(true);
//	}
//
//	private JPanel getMainPanel() {
//		JPanel result = new JPanel();
//		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
//		result.add(getImagePanel());
//		result.add(getControlPanel());
//		return result;
//	}
//
//	private JPanel getImagePanel() {
//		m_imageLabel = new JLabel();
//		Image image = Toolkit.getDefaultToolkit().createImage(this);
//		m_imageLabel.setIcon(new ImageIcon(image));
//		m_imageLabel.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseReleased(final MouseEvent e) {
//				int label = m_labelArray[e.getY()][e.getX()];
//				m_infoLabel.setText("Label at (" + e.getX() + ", " + e.getY()
//						+ "): " + label);
//				LabelInfo li = m_labelMap.get(label);
//				if (li == null) {
//					m_colorLabel.setText("Color at (" + e.getX() + ", "
//							+ e.getY() + "): <none>");
//				} else {
//					m_colorLabel.setText("Color at (" + e.getX() + ", "
//							+ e.getY() + "): R"
//							+ Integer.toHexString(li.getColor().getRed()) + "G"
//							+ Integer.toHexString(li.getColor().getGreen())
//							+ "B"
//							+ Integer.toHexString(li.getColor().getBlue()));
//				}
//			}
//		});
//		JPanel result = new JPanel();
//		result.add(m_imageLabel);
//		return result;
//	}
//
//	private JPanel getControlPanel() {
//		JPanel result = new JPanel();
//		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
//		m_editField = new JTextField("1");
//		m_editField.setMaximumSize(new Dimension(250, 25));
//		m_editField.setMinimumSize(new Dimension(75, 25));
//		m_stepButton = new JButton("Step");
//		m_stepButton.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent e) {
//				wakeWaiters();
//			}
//		});
//		m_showLastButton = new JButton("Show last pixel");
//		m_showLastButton.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent e) {
//				flickerLastPixel();
//			}
//		});
//		m_infoLabel = new JLabel("Label at (XX, YY): -50000");
//		m_colorLabel = new JLabel("Color at (XX, YY): RrrGggBbb");
//
//		result.add(Box.createVerticalGlue());
//		result.add(m_editField);
//		result.add(Box.createVerticalStrut(5));
//		result.add(m_stepButton);
//		result.add(Box.createVerticalStrut(5));
//		result.add(m_showLastButton);
//		result.add(Box.createVerticalStrut(15));
//		result.add(Box.createHorizontalStrut(200));
//		result.add(m_infoLabel);
//		result.add(Box.createVerticalStrut(5));
//		result.add(m_colorLabel);
//		result.add(Box.createVerticalGlue());
//		result.add(Box.createVerticalGlue());
//		return result;
//	}
//
//	private void flickerLastPixel() {
//		if ((m_lastXChanged >= 0) && (m_lastYChanged >= 0)) {
//			new Thread() {
//				@Override
//				public void run() {
//					PointNeighbors nbh = new PointNeighbors(m_lastXChanged,
//							m_lastYChanged, true, (int) m_orgImg.dimension(0),
//							(int) m_orgImg.dimension(1));
//					try {
//						for (Point n : nbh) {
//							setPixelColor(n.x, n.y, Color.WHITE);
//						}
//						synchronized (this) {
//							wait(100);
//						}
//						for (Point n : nbh) {
//							setPixelColor(n.x, n.y, Color.BLACK);
//						}
//						synchronized (this) {
//							wait(100);
//						}
//					} catch (InterruptedException ie) {
//						// just continue
//					}
//					int lastX = m_lastXChanged;
//					int lastY = m_lastYChanged;
//					for (Point n : nbh) {
//						// newLabel modifies the lastX/YChanged members.
//						newLabel(n.x, n.y);
//					}
//					// reset the variables here
//					m_lastXChanged = lastX;
//					m_lastYChanged = lastY;
//				}
//			}.start();
//		}
//	}
//
//	private void wakeWaiters() {
//		try {
//			m_stepsToGo = Integer.parseInt(m_editField.getText());
//		} catch (NumberFormatException nfe) {
//			m_stepsToGo = 1;
//			m_editField.setText("1");
//		}
//
//		synchronized (m_stepButton) {
//			m_stepButton.notifyAll();
//		}
//	}
//
//	/**
//	 * Waits until user presses the Step button.
//	 */
//	public void grantAStep() {
//
//		m_stepsToGo--;
//
//		if (m_stepsToGo <= 0) {
//			try {
//				m_stepsToGo = Integer.parseInt(m_editField.getText());
//			} catch (NumberFormatException nfe) {
//				m_stepsToGo = 1;
//				m_editField.setText("1");
//			}
//			synchronized (m_stepButton) {
//				try {
//					m_stepButton.wait();
//				} catch (InterruptedException ie) {
//					// nothing.
//				}
//			}
//		}
//		return;
//	}
//
//	/***************************************************************************
//	 * ImageProducer Stuff
//	 **************************************************************************/
//	/**
//	 * {@inheritDoc}
//	 */
//	public synchronized void addConsumer(final ImageConsumer ic) {
//		// assert m_imageConsumer != null;
//		if (ic == null) {
//			// I don't know why this should happen - but it does!!
//			return;
//		}
//
//		if (m_imageConsumer.contains(ic)) {
//			return;
//		}
//		m_imageConsumer.add(ic);
//		try {
//			initConsumer(ic);
//
//			// we send each row of the picture separately
//			for (int r = 0; r < m_labelArray.length; r++) {
//				if (!m_imageConsumer.contains(ic)) {
//					return;
//				}
//				// translate labelNumbers in color values
//				for (int x = 0; x < m_labelArray[r].length; x++) {
//					int label = m_labelArray[r][x];
//					m_orgImgRA.setPosition(x, 0);
//					m_orgImgRA.setPosition(r, 1);
//					int origPixVal = (int) m_orgImgRA.get().getRealDouble();
//					int red = origPixVal & 0x0FF;
//					int grn = origPixVal & 0x0FF;
//					int blu = origPixVal & 0x0FF;
//					if (origPixVal < m_backgroundTreshold) {
//						// draw background blue. Full power blue.
//						red = 0;
//						grn = 0;
//						blu = 255;
//					}
//					LabelInfo lInfo = m_labelMap.get(label);
//					if (lInfo != null) {
//						red += lInfo.getColor().getRed();
//						if (red > 255) {
//							red = 255;
//						}
//						grn += lInfo.getColor().getGreen();
//						if (grn > 255) {
//							grn = 255;
//						}
//						blu += lInfo.getColor().getBlue();
//						if (blu > 255) {
//							blu = 255;
//						}
//					}
//					m_pixels[x] = 0xFF000000 | (red << 16) | (grn << 8) | blu;
//				}
//				ic.setPixels(0, r, m_labelArray[r].length, 1, m_colorModel,
//						m_pixels, 0, 0);
//			}
//			if (isConsumer(ic)) {
//				ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
//			}
//		} catch (Exception e) {
//			if (isConsumer(ic)) {
//				ic.imageComplete(ImageConsumer.IMAGEERROR);
//			}
//		}
//	}
//
//	/**
//	 * Call this whenever the label of a pixel was changed in the array initialy
//	 * passed to this producer. It will notify interested ImageConsumers to
//	 * update their view on the image.
//	 * 
//	 * @param x
//	 *            the x coord of the pixel that changed
//	 * @param y
//	 *            the y coord of the pixel that changed
//	 * 
//	 */
//	public synchronized void newLabel(final int x, final int y) {
//
//		LabelInfo lInfo = m_labelMap.get(m_labelArray[y][x]);
//		m_orgImgRA.setPosition(x, 0);
//		m_orgImgRA.setPosition(y, 1);
//
//		int origPixVal = (int) m_orgImgRA.get().getRealDouble();
//		int red = origPixVal & 0x0FF;
//		int grn = origPixVal & 0x0FF;
//		int blu = origPixVal & 0x0FF;
//		if (origPixVal < m_backgroundTreshold) {
//			red = 255;
//			grn = 0;
//			blu = 0;
//		}
//		if (lInfo != null) {
//			red += lInfo.getColor().getRed();
//			if (red > 255) {
//				red = 255;
//			}
//			grn += lInfo.getColor().getGreen();
//			if (grn > 255) {
//				grn = 255;
//			}
//			blu += lInfo.getColor().getBlue();
//			if (blu > 255) {
//				blu = 255;
//			}
//		}
//
//		m_pixels[0] = 0xFF000000 | (red << 16) | (grn << 8) | blu;
//		for (ImageConsumer ic : m_imageConsumer) {
//			ic.setPixels(x, y, 1, 1, m_colorModel, m_pixels, 0, 0);
//			ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
//		}
//
//		m_lastXChanged = x;
//		m_lastYChanged = y;
//	}
//
//	/**
//	 * Sends the pixel at the specified position with the specified color.
//	 */
//	private void setPixelColor(final int x, final int y, final Color color) {
//		m_pixels[0] = 0xFF000000 | color.getRGB();
//		for (ImageConsumer ic : m_imageConsumer) {
//			ic.setPixels(x, y, 1, 1, m_colorModel, m_pixels, 0, 0);
//			ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
//		}
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public synchronized boolean isConsumer(final ImageConsumer ic) {
//		return m_imageConsumer.contains(ic);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public synchronized void removeConsumer(final ImageConsumer ic) {
//		m_imageConsumer.remove(ic);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public void requestTopDownLeftRightResend(final ImageConsumer ic) {
//		// we do topdown leftright anyway!
//		startProduction(ic);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public void startProduction(final ImageConsumer ic) {
//		addConsumer(ic);
//	}
//
//	private void initConsumer(final ImageConsumer ic) {
//		ic.setDimensions(m_labelArray[0].length, m_labelArray.length);
//		ic.setColorModel(m_colorModel);
//		int hints = ImageConsumer.RANDOMPIXELORDER;
//		ic.setHints(hints);
//	}
//
// }
