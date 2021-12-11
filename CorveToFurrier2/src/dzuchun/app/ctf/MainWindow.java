package dzuchun.app.ctf;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import dzuchun.math.ComplexNumber;
import dzuchun.math.FurrierTransforms;
import dzuchun.util.Util;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Dimension size = new Dimension(1920, 1080);
	private static final Dimension center = new Dimension(size.width / 2, size.height / 2);
	private static final Color bgColor = Color.black;
	private static final Color curveColor = new Color(255, 0, 0);
	private static final Color drawnCurveColor = new Color(0, 255, 0);
	private static final Color circlesColor = new Color(0, 0, 255);
	private static final long pointsTimeInterval = 1;
	private static final long repaintTimeInterval = 16;
	private static final int maxCircleFrequency = 40;
	private static final int RELATIVE_RESOLUTION = 10;
	private static long cycleLength;

	public static void main(String[] args) {
		new MainWindow();
	}

	/**
	 * State of a program
	 */
	private State state;
	/**
	 * Curves as image
	 */
	public BufferedImage image;
	/**
	 * Canvas program is drawing on
	 */
	public Canvas canvas;

	public Double[] freq;
	public ComplexNumber[] circles;
	public Vector<ComplexNumber> curve;

	private MainWindow() {
		this.setSize(size);
		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_BGR);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.state = State.IDLE;
		this.getContentPane().add(canvas = new Canvas() {
			private static final long serialVersionUID = 1L;
			{
				this.setBackground(bgColor);
				this.addMouseListener(new MouseListener() {

					@Override
					public void mouseReleased(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.isControlDown()) {
							switch (state) {
							case IDLE:
								startRecording();
								break;
							case RECORDING:
								endListening();
								calculateResult();
								startDrawing();
								break;
							case DRAWING:
								stopDrawing();
								break;
							}
						}
					}
				});
			}

			@Override
			public void update(Graphics g) {
				g.drawImage(image, 0, 0, null);
				paint(g);
			}

			private double phase;
			private ComplexNumber circlePointer;

			@Override
			public void paint(Graphics g) {

				synchronized (LOCK) {

					if (state == State.DRAWING) {

						phase = 2 * Math.PI * (System.currentTimeMillis() % cycleLength) / (cycleLength);

						Graphics2D graphics = (Graphics2D) g.create();
						graphics.translate(center.width, center.height);
						graphics.setColor(circlesColor);
						for (int i = 0; i < freq.length; i++) {
							circlePointer = circles[i].turn(phase * freq[i], true);
							graphics.drawOval((int) -circlePointer.getAbsolute(), (int) -circlePointer.getAbsolute(),
									(int) (2 * circlePointer.getAbsolute()), (int) (2 * circlePointer.getAbsolute()));
							graphics.drawLine(0, 0, circlePointer.getIntReal(), circlePointer.getIntImaginary());
							graphics.translate(circlePointer.getIntReal(), circlePointer.getIntImaginary());
						}

					}
				}

				Toolkit.getDefaultToolkit().sync();
			}
		});
		this.startRepainting();
		this.setVisible(true);
	}

	private void startRepainting() {
		new Thread() {
			{
				this.setName("Repaint-Thread");
				this.setDaemon(true);
			}

			@Override
			public void run() {
				try {
					while (true) {
						if (state != MainWindow.State.IDLE) {
							canvas.repaint(0);
						}
						sleep(repaintTimeInterval);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void startRecording() {
		this.state = State.RECORDING;
		Graphics g = image.getGraphics();
		g.setColor(bgColor);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		cycleLength = System.currentTimeMillis();
		this.curve = new Vector<>(0);
		new Thread() {
			{
				this.setName("Recording-Thread");
				this.setDaemon(true);
			}

			@Override
			public void run() {
				try {
					while (state == MainWindow.State.RECORDING) {
						synchronized (LOCK) {
							if (canvas.getMousePosition() != null) {
								image.setRGB(canvas.getMousePosition().x, canvas.getMousePosition().y,
										curveColor.getRGB());
								curve.add(new ComplexNumber(canvas.getMousePosition().x - center.width,
										canvas.getMousePosition().y - center.height));
							}
						}
						sleep(pointsTimeInterval);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void endListening() {
		cycleLength = System.currentTimeMillis() - cycleLength;
		this.state = State.DRAWING;
	}

	private final Object LOCK = new Object();

	public void calculateResult() {
		fillFreq();
		synchronized (LOCK) {
			circles = FurrierTransforms.setDescrete(freq, curve);
			//Filling order
			Integer[] tmp = Util.intArray(freq.length);
			Vector<Integer> order = new Vector<Integer>(0);
			for (int i : tmp) {
				order.add(i);
			} 
			order.sort((i1, i2) -> (int) (10000 * (circles[i2].getAbsolute() - circles[i1].getAbsolute())));
			Integer[] shaft = new Integer[freq.length];
			order.toArray(shaft);
			freq = Util.reshaft(shaft, freq);
			circles = Util.reshaft(shaft, circles);
		}
	}

	private void startDrawing() {
		// Painting curves on image
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setColor(bgColor);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.translate(center.width, center.height);
		g.setStroke(new BasicStroke(2));

		g.setColor(curveColor);
		PathIterator iter = new PathIterator() {
			int index = 0;

			@Override
			public void next() {
				index++;
			}

			@Override
			public boolean isDone() {
				return index == curve.size();
			}

			@Override
			public int getWindingRule() {
				return WIND_EVEN_ODD;
			}

			@Override
			public int currentSegment(double[] coords) {
				coords[0] = curve.get(index).getIntReal();
				coords[1] = curve.get(index).getIntImaginary();
				return (index != 0) ? SEG_LINETO : SEG_MOVETO;
			}

			@Override
			public int currentSegment(float[] coords) {
				return -100;
			}
		};
		Path2D.Double path = new Path2D.Double();
		path.append(iter, false);
		g.draw(path);

		g.setColor(drawnCurveColor);
		final double phaseStep = 2 * Math.PI / curve.size() / RELATIVE_RESOLUTION;
		iter = new PathIterator() {
			int index = 0;

			@Override
			public void next() {
				index++;
			}

			@Override
			public boolean isDone() {
				return index == curve.size() * RELATIVE_RESOLUTION;
			}

			@Override
			public int getWindingRule() {
				return WIND_EVEN_ODD;
			}

			@Override
			public int currentSegment(double[] coords) {
				ComplexNumber c = calculateForPhase(index * phaseStep);
				coords[0] = c.getIntReal();
				coords[1] = c.getIntImaginary();
				return (index != 0) ? SEG_LINETO : SEG_MOVETO;
			}

			@Override
			public int currentSegment(float[] coords) {
				return -100;
			}
		};
		path = new Path2D.Double();
		path.append(iter, false);
		g.draw(path);

		state = State.DRAWING;
	}

	private void fillFreq() {
		freq = new Double[maxCircleFrequency * 2 + 1];
		for (int f = -maxCircleFrequency; f <= maxCircleFrequency; f++) {
			freq[f + maxCircleFrequency] = (double) f;
		}
	}

	private ComplexNumber calculateForPhase(double phase) {
		ComplexNumber res = new ComplexNumber();
		for (int i = 0; i < freq.length; i++) {
			res.add(circles[i].turn(phase * freq[i], true), false);
		}
		return res;
	}

	private void stopDrawing() {
		state = State.IDLE;
	}

	enum State {
		/**
		 * Program awaits input.
		 */
		IDLE,
		/**
		 * Program records pointer positions as curve points
		 */
		RECORDING,
		/**
		 * Program animates inputed curve
		 */
		DRAWING
	}
}
