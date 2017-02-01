import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.text.DecimalFormat;

@SuppressWarnings("serial")
public class mainPaint extends JFrame {

	JButton brushBut, lineBut, ellipseBut, rectBut, strokeBut, fillBut;
	JSlider transSlider;
	JLabel transLabel;

	DecimalFormat dec = new DecimalFormat("#.##");

	Graphics2D graphSettings;

	int currentAction = 1;

	float transparentVal = 1.0f;

	Color strokeColor = Color.BLACK, fillColor = Color.BLACK;

	public static void main(String[] args) {
		new mainPaint();
	}

	public mainPaint() {

		this.setSize(800, 600);
		this.setTitle("Java Paint");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel buttonPanel = new JPanel();

		Box theBox = Box.createHorizontalBox();

		brushBut = makeMeButtons("./src/brush.png", 1);
		lineBut = makeMeButtons("./src/Line.png", 2);
		ellipseBut = makeMeButtons("./src/Ellipse.png", 3);
		rectBut = makeMeButtons("./src/Rectangle.png", 4);

		strokeBut = makeMeColorButton("./src/Stroke.png", 5, true);
		fillBut = makeMeColorButton("./src/Fill.png", 6, false);

		theBox.add(brushBut);
		theBox.add(lineBut);
		theBox.add(ellipseBut);
		theBox.add(rectBut);
		theBox.add(strokeBut);
		theBox.add(fillBut);

		transLabel = new JLabel("Transparent: 1");
		transSlider = new JSlider(1, 99, 99);
		ListenForSlider lForSlider = new ListenForSlider();
		transSlider.addChangeListener(lForSlider);

		theBox.add(transLabel);
		theBox.add(transSlider);

		buttonPanel.add(theBox);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(new DrawingBoard(), BorderLayout.CENTER);
		this.setVisible(true);

	}

	public JButton makeMeButtons(String iconFile, final int actionNum) {

		JButton theBut = new JButton();
		Icon butIcon = new ImageIcon(iconFile);
		theBut.setIcon(butIcon);

		theBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentAction = actionNum;
			}
		});

		return theBut;
	}

	public JButton makeMeColorButton(String iconFile, final int actionNum,
			final boolean stroke) {

		JButton theBut = new JButton();
		Icon butIcon = new ImageIcon(iconFile);
		theBut.setIcon(butIcon);

		theBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (stroke) {
					strokeColor = JColorChooser.showDialog(null,
							"Pick a Stroke", Color.BLACK);
				} else {
					fillColor = JColorChooser.showDialog(null, "Pick a Stroke",
							Color.BLACK);
				}
			}
		});

		return theBut;
	}

	private class DrawingBoard extends JComponent {

		ArrayList<Shape> shapes = new ArrayList<Shape>();
		ArrayList<Color> shapeFill = new ArrayList<Color>();
		ArrayList<Color> shapeStroke = new ArrayList<Color>();
		ArrayList<Float> transPercent = new ArrayList<Float>();

		Point drawStart, drawEnd;

		public DrawingBoard() {

			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {

					if (currentAction != 1) {
						drawStart = new Point(e.getX(), e.getY());
						drawEnd = drawStart;
						repaint();
					}
				}

				public void mouseReleased(MouseEvent e) {
					if (currentAction != 1) {
						Shape aShape = null;

						if (currentAction == 2) {
							aShape = drawLine(drawStart.x, drawStart.y,
									e.getX(), e.getY());
						} else if (currentAction == 3) {
							aShape = drawEllipse(drawStart.x, drawStart.y,
									e.getX(), e.getY());
						} else if (currentAction == 4) {
							aShape = drawRectangle(drawStart.x, drawStart.y,
									e.getX(), e.getY());
						}

						shapes.add(aShape);
						shapeFill.add(fillColor);
						shapeStroke.add(strokeColor);
						transPercent.add(transparentVal);

						drawStart = null;
						drawEnd = null;
						repaint();
					}

				}
			}); // END OF addMouseListener

			this.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					if (currentAction == 1) {
						int x = e.getX();
						int y = e.getY();
						Shape aShape = null;

						strokeColor = fillColor;
						aShape = drawBrush(x, y, 5, 5);

						shapes.add(aShape);
						shapeFill.add(fillColor);
						shapeStroke.add(strokeColor);
						transPercent.add(transparentVal);
					}

					drawEnd = new Point(e.getX(), e.getY());
					repaint();
				}
			}); // END OF addMouseMotionListener
		}

		public void paint(Graphics g) {

			graphSettings = (Graphics2D) g;
			graphSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graphSettings.setStroke(new BasicStroke(2));

			Iterator<Color> strokeCounter = shapeStroke.iterator();
			Iterator<Color> fillCounter = shapeFill.iterator();
			Iterator<Float> transCounter = transPercent.iterator();

			for (Shape s : shapes) {
				graphSettings.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, transCounter.next()));
				graphSettings.setPaint(strokeCounter.next());
				graphSettings.draw(s);
				graphSettings.setPaint(fillCounter.next());
				graphSettings.fill(s);
			}

			if (drawStart != null && drawEnd != null) {
				graphSettings.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.4f));
				graphSettings.setPaint(Color.GRAY);

				Shape aShape = null;

				if (currentAction == 2) {
					aShape = drawLine(drawStart.x, drawStart.y, drawEnd.x,
							drawEnd.y);
				} else if (currentAction == 3) {
					aShape = drawEllipse(drawStart.x, drawStart.y, drawEnd.x,
							drawEnd.y);
				} else if (currentAction == 4) {
					aShape = drawRectangle(drawStart.x, drawStart.y, drawEnd.x,
							drawEnd.y);
				}

				graphSettings.draw(aShape);
			}
		}

		private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);

			int width = Math.abs(x1 - x2);
			int height = Math.abs(y1 - y2);

			return new Rectangle.Float(x, y, width, height);
		}

		private Ellipse2D.Float drawEllipse(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);

			int width = Math.abs(x1 - x2);
			int height = Math.abs(y1 - y2);

			return new Ellipse2D.Float(x, y, width, height);
		}

		private Line2D.Float drawLine(int x1, int y1, int x2, int y2) {
			return new Line2D.Float(x1, y1, x2, y2);
		}

		private Ellipse2D.Float drawBrush(int x1, int y1, int brushStrokeWidth,
				int brushStrokeHeight) {
			return new Ellipse2D.Float(x1, y1, brushStrokeWidth,
					brushStrokeHeight);
		}
	}

	private class ListenForSlider implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == transSlider) {
				transLabel.setText("Transparent: "
						+ dec.format(transSlider.getValue() * .01));
				transparentVal = (float) (transSlider.getValue() * .01);
			}
		}
	}

}
