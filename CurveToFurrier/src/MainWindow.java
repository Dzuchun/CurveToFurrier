

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static Dimension size = new Dimension(1920, 1080);
	private static Dimension center = new Dimension(size.width/2, size.height/2);
	private static Color curveColor = new Color(255, 0, 0);
	private static Color drawnCurveColor = new Color(0, 255, 0);
	private static Color circlesColor = new Color(0, 0, 255);
	private static long cycleLength;
	private static long pointsTimeInterval = 1;
	private static long repaintTimeInterval = 16;
	private static int maxCircleFrequency = 40;
	
	public DrawData data;
	
	private final Object LOCK = new Object();
	
	private long lastTime; 
	private State programState;
	private Canvas canvas;
	
	private MainWindow() {
		this.setSize(size);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.lastTime = System.currentTimeMillis();
		this.programState = State.IDLE;
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.add(this.canvas = new Canvas() {
			private static final long serialVersionUID = 1L;
			private Graphics2D graphics;
			{
				this.setBackground(Color.black);
				this.addMouseListener(new MouseListener() {
					
					@Override
					public void mouseReleased(MouseEvent e) {}
					
					@Override
					public void mousePressed(MouseEvent e) {}
					
					@Override
					public void mouseExited(MouseEvent e) {}
					
					@Override
					public void mouseEntered(MouseEvent e) {}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.isControlDown()) {
							switch (programState) {
							case DRAWING:
							case DRAWN:
								stopDrawing();
								break;
							case IDLE:
								startListening();
								break;
							case LISTENING:
								endListening();
								data.calculateResult();
								startDrawing();
								break;
							}
						}
					}
				});
			}
			@Override
			public void paint(Graphics g) {

				synchronized(LOCK) {
//					System.out.println("Repainting!");
					if (programState != State.LISTENING && programState != State.IDLE) {
						data.forward(System.currentTimeMillis() - lastTime);
						lastTime = System.currentTimeMillis();
					}
					
					graphics = (Graphics2D) g.create();
		
					if (programState != State.LISTENING && programState != State.IDLE) {
	
						graphics.translate(center.width, center.height);
						graphics.setColor(circlesColor);
//						double i=0;
						for (MainWindow.DrawData.DrawingCircle circle : data.circles) {
							graphics.drawOval((int)-circle.radius, (int)-circle.radius, (int)(2 * circle.radius), (int)(2 * circle.radius));
							graphics.drawLine(0, 0, circle.pointer.getReal(), circle.pointer.getImaginary());
							graphics.translate(circle.pointer.getReal(), circle.pointer.getImaginary());
//							i++;
//							data.curves.setRGB((int)graphics.getTransform().getTranslateX(), (int)graphics.getTransform().getTranslateY(), getRainbowColor(i/data.circles.size()).getRGB());
						}
						
						if (programState == State.DRAWING || true) {
							try {
								data.curves.setRGB((int)graphics.getTransform().getTranslateX(), (int)graphics.getTransform().getTranslateY(), drawnCurveColor.getRGB());
//								System.out.println("Global pointer at - " + graphics.getTransform().getTranslateX() + ", " + graphics.getTransform().getTranslateY());
							} catch (ArrayIndexOutOfBoundsException e) { }
						}
					}
				}
				
				Toolkit.getDefaultToolkit().sync();
			}
			
			@Override
			public void update(Graphics g) {
				g.drawImage(data.curves, 0, 0, null);
				paint(g);
			}
		});
		this.data = new DrawData();
		this.setVisible(true);
		this.data.refresh();
	}
	
	private void startRepainting() {
		new Thread() {
			
			@Override
			public void run() {
				this.setName("Repainting-Thread");
				try {
					while (programState != MainWindow.State.IDLE) {
						canvas.repaint();
						sleep(repaintTimeInterval);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void startListening() {
		this.startRepainting();
		this.programState = State.LISTENING;
		data.refresh();
		cycleLength = System.currentTimeMillis();
		new Thread() {
			
			@Override
			public void run() {
				this.setName("Listening-Thread");
				
				try {
					while (programState == MainWindow.State.LISTENING) {	
						synchronized(LOCK) {
							if (canvas.getMousePosition() != null) {
								data.curves.setRGB(canvas.getMousePosition().x, canvas.getMousePosition().y, curveColor.getRGB());
								data.inputCurve.add(new ComplexNumber(canvas.getMousePosition().x-center.width, canvas.getMousePosition().y-center.height));
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
		this.programState = State.DRAWING;
	}

	private void startDrawing() {
		programState = State.DRAWING;
		this.lastTime = System.currentTimeMillis();
	}

	private void stopDrawing() {
		programState = State.IDLE;
	}
	
	public static void main(String[] args) {
		new MainWindow();
	}
	
	class DrawData {
		
		//Curves as buffered image
		public BufferedImage curves;
		//Curve to represent
		public Vector<DrawingCircle> circles;
		//Curve drawn
		public Vector<ComplexNumber> inputCurve;
		
		private long timeSpent;
		private double rotationAngle;
//		private ComplexNumber tmp;
		public void forward(double milis) {
//			System.out.println("Executing forward");
			timeSpent += milis;
			if (timeSpent >= cycleLength) {
				if (programState == State.DRAWING) {
						System.out.println("Drawn state set, time spent - " + timeSpent);
						programState = State.DRAWN;
				}
				timeSpent -= cycleLength;
			}
//			if ((timeSpent/cycleLength)%1 <0.1) {
//				System.out.println("Triming circles");
//				for (DrawingCircle c : circles) {
//					c.pointer.trimAbsolute(c.radius);
//				}
//			}
			rotationAngle = 2*Math.PI*timeSpent/cycleLength;
//			System.out.println("Rotation angle - " + rotationAngle);
			for (DrawingCircle circle : this.circles) {
				tmp = circle.pointerSave.clone();
				tmp.multiply(ComplexNumber.rotation(rotationAngle * circle.frequency));
				circle.pointer = tmp;
				circle.pointer.trimAbsolute(circle.radius);
//				System.out.println("Multiplying " + circle.pointerSave + " and " + ComplexNumber.rotation(rotationAngle) + " gives us - " + circle.pointer);
//				System.out.println("Multiplying " + circle.pointerSave.getAbsolute() + " and " + ComplexNumber.rotation(rotationAngle).getAbsolute() + " gives us - " + circle.pointer.getAbsolute());
			}
			
		}
		
		public void refresh() {
//			System.out.println("Executing refresh");
			synchronized (LOCK) {
				this.circles = new Vector<DrawingCircle>(0);
				this.curves = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_BGR);
				this.inputCurve = new Vector<ComplexNumber>(0);
				this.timeSpent = 0;
			}
		}
		
		private int f;
		private ComplexNumber sum, tmp;
		private int pointsCount;
		private double rotationAngleSum, rotationAngleInc;
		public void calculateResult() {
			synchronized (LOCK) {
				pointsCount = inputCurve.size();
				for (f=-maxCircleFrequency; f<=maxCircleFrequency; f++) {
					sum = new ComplexNumber();
					rotationAngleSum = 0;
					rotationAngleInc = -2.0 * Math.PI * f / pointsCount;
					for (ComplexNumber n : inputCurve) {
						rotationAngleSum += rotationAngleInc;
						tmp = n.clone();
						tmp.multiply(ComplexNumber.rotation(rotationAngleSum));
						sum.add(tmp);
					}
					sum.divide(pointsCount);
					circles.add(new DrawingCircle(sum, f));
					System.out.println("Creating circle with argument " + sum.toString());
				}
				System.out.println("Calculated circles");
				//TODO syncronize
				circles.sort((DrawingCircle o1, DrawingCircle o2) -> (int) (o2.radius-o1.radius));
			}
		}

		class DrawingCircle	{
			public double radius;
			public int frequency;
			public ComplexNumber pointer, pointerSave;
			
			public DrawingCircle() {
				this.radius = 0d;
				this.frequency = 1;
				this.pointer = new ComplexNumber();
				pointerSave = pointer;
			}
			
			public DrawingCircle(ComplexNumber arg, int frequency) {
				this.frequency = frequency;
				this.radius = arg.getAbsolute();
				this.pointer = arg;
				pointerSave = pointer;
			}
		}
	}
	
	enum State {
		IDLE,
		LISTENING, 
		DRAWING,
		DRAWN
	}
	
	public static Color getRainbowColor(double d) {
		return new Color(0.5f*(float)Math.cos(2 * Math.PI * d)+0.5f, 0.5f*(float)Math.cos(2 * Math.PI * d+2*Math.PI/3)+0.5f, 0.5f*(float)Math.cos(2 * Math.PI * d-2*Math.PI/3)+0.5f); 
	}
}
