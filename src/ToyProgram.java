

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

public class ToyProgram extends JFrame implements ActionListener {
  private ImageBox imageBox;
  private ImageBox.ImageColor currentColor = ImageBox.ImageColor.RED;
  private ImageBox.ImageColor currentFillColor = ImageBox.ImageColor.RED;
  private ImageBox.ImageShape currentShape = ImageBox.ImageShape.SQUARE;
  private boolean isDashed = false;
  private boolean isChecked = false;
  private JRadioButton bgBlueBtn;
  private JRadioButton bgGreenBtn;
  private JRadioButton bgRedBtn;
  private JRadioButton solidLineBtn;
  private JRadioButton dashedLineBtn;
  private JButton penColorBtn;
  private JButton clearBtn;
  private JButton checkBtn;

  public ToyProgram(String title) {
    super(title);

    addMenu();

    JPanel contentPanel = new JPanel(new BorderLayout());

    final JComboBox shapeCombo = new JComboBox(new String[] { "Square", "Circle", "Line" });
    shapeCombo.setBounds(0, 0, 100, 50);
    shapeCombo.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        int index = shapeCombo.getSelectedIndex();
        switch (index) {
        case 0:
          currentShape = ImageBox.ImageShape.SQUARE;
          enableFillColorBtn(true);
          break;
        case 1:
          currentShape = ImageBox.ImageShape.CIRCLE;
          enableFillColorBtn(true);
          break;
        case 2:
          currentShape = ImageBox.ImageShape.LINE;
          enableFillColorBtn(false);
        }

      }

    });

    Box shapePanel = new Box(BoxLayout.Y_AXIS);
    shapePanel.setBorder(BorderFactory.createTitledBorder("Shape"));
    shapePanel.add(shapeCombo);

    solidLineBtn = new JRadioButton("Solid");
    solidLineBtn.addActionListener(this);
    solidLineBtn.setSelected(true);

    dashedLineBtn = new JRadioButton("Dashed");
    dashedLineBtn.addActionListener(this);

    ButtonGroup strokeGroup = new ButtonGroup();
    strokeGroup.add(solidLineBtn);
    strokeGroup.add(dashedLineBtn);

    Box strokePanel = new Box(BoxLayout.Y_AXIS);
    strokePanel.setBorder(BorderFactory.createTitledBorder("Stroke"));
    strokePanel.add(solidLineBtn);
    strokePanel.add(dashedLineBtn);

    Box leftSelectPanel = new Box(BoxLayout.Y_AXIS);
    leftSelectPanel.add(shapePanel);
    leftSelectPanel.add(strokePanel);

    checkBtn = new JButton("Check");
    checkBtn.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        isChecked = true;

      }
    });
    leftSelectPanel.add(checkBtn);

    penColorBtn = new JButton("Color");
    penColorBtn.addActionListener(this);

    Box colorPanel = new Box(BoxLayout.Y_AXIS);
    colorPanel.setBorder(BorderFactory.createTitledBorder("Pen Color"));
    colorPanel.add(penColorBtn);

    bgRedBtn = new JRadioButton("bgRed");
    bgRedBtn.setActionCommand("bgRed");
    bgRedBtn.addActionListener(this);
    bgRedBtn.setSelected(true);

    bgGreenBtn = new JRadioButton("bgGreen");
    bgGreenBtn.setActionCommand("bgGreen");
    bgGreenBtn.addActionListener(this);

    bgBlueBtn = new JRadioButton("bgBlue");
    bgBlueBtn.setActionCommand("bgBlue");
    bgBlueBtn.addActionListener(this);

    ButtonGroup bgColorGroup = new ButtonGroup();
    bgColorGroup.add(bgRedBtn);
    bgColorGroup.add(bgGreenBtn);
    bgColorGroup.add(bgBlueBtn);

    Box bgColorPanel = new Box(BoxLayout.Y_AXIS);
    bgColorPanel.setBorder(BorderFactory.createTitledBorder("Fill Color"));
    bgColorPanel.add(bgRedBtn);
    bgColorPanel.add(bgGreenBtn);
    bgColorPanel.add(bgBlueBtn);

    Box rightSelectPanel = new Box(BoxLayout.Y_AXIS);

    rightSelectPanel.add(colorPanel);
    rightSelectPanel.add(bgColorPanel);

    clearBtn = new JButton("Check2");
    clearBtn.addActionListener(this);
    rightSelectPanel.add(clearBtn);

    imageBox = new ImageBox();
    imageBox.setBorder(BorderFactory.createTitledBorder("Object"));

    JButton drawBtn = new JButton("Draw");
    drawBtn.addActionListener(this);

    contentPanel.add(imageBox, BorderLayout.CENTER);
    contentPanel.add(leftSelectPanel, BorderLayout.WEST);
    contentPanel.add(rightSelectPanel, BorderLayout.EAST);
    contentPanel.add(drawBtn, BorderLayout.SOUTH);

    enableFillColorBtn(true);

    setContentPane(contentPanel);
    setBounds(0, 0, 400, 300);

    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  public static class ShowAction extends AbstractAction {
    Component parentComponent;

    public ShowAction(Component parentComponent) {
      super("About");
      putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
      this.parentComponent = parentComponent;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      Runnable runnable = new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(parentComponent, "About Event", "About Event V1.0",
              JOptionPane.INFORMATION_MESSAGE);
        }
      };
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static class ImageBox extends JPanel {
    enum ImageShape {
      SQUARE, CIRCLE, LINE
    }

    enum ImageColor {
      RED, GREEN, BLUE
    }

    private final static float dash[] = { 7.0f };
    private final static BasicStroke dashed = new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND, 5.0f,
        dash, 0.0f);
    private final static BasicStroke normalStroke = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        5.0f);
    private ImageShape shape = ImageShape.SQUARE;
    private ImageColor color = ImageColor.RED;
    private ImageColor fillColor = ImageColor.RED;
    private boolean isDashed = false;

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Graphics2D g2 = (Graphics2D) g;

      Color penColor = null, fillColor = null;
      Paint oldColor = g2.getPaint();
      Stroke oldStroke = g2.getStroke();

      Rectangle rect = getVisibleRect();// getBounds();//g2.getClipBounds();
      rect.grow(-20, -20);

      switch (getColor()) {
      case RED:
        penColor = Color.RED;
        break;
      case GREEN:
        penColor = Color.GREEN;
        break;
      case BLUE:
        penColor = Color.BLUE;
        break;
      }

      if (getShape() != ImageShape.LINE) {
        switch (getFillColor()) {
        case RED:
          fillColor = Color.RED;
          break;
        case GREEN:
          fillColor = Color.GREEN;
          break;
        case BLUE:
          fillColor = Color.BLUE;
          break;
        }
        g2.setPaint(fillColor);
        switch (getShape()) {
        case SQUARE:
          g2.fill(new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
          break;
        case CIRCLE:
          g2.fill(new Ellipse2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
          break;
        }
      }

      g2.setPaint(penColor);
      if (isDashed)
        g2.setStroke(dashed);
      else
        g2.setStroke(normalStroke);

      switch (getShape()) {
      case SQUARE:
        g2.draw(new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
        break;
      case CIRCLE:
        g2.draw(new Ellipse2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
        break;
      case LINE:
        g2.draw(new Line2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
        break;
      }

      // special case
      if (getShape() == ImageShape.CIRCLE) {
        if (getFillColor() == ImageColor.GREEN) {
          if (getColor() == ImageColor.BLUE) {
            g2.setPaint(Color.BLACK);
            g2.setColor(Color.BLACK);
            g2.drawString(new AttributedString("Hard to cover").getIterator(), (float) rect.getX() + 10,
                (float) rect.getCenterY());
          }
        }
      }

      g2.setPaint(oldColor);
      g2.setStroke(oldStroke);

    }

    public void setShape(ImageShape shape) {
      this.shape = shape;
    }

    public ImageShape getShape() {
      return shape;
    }

    public void setColor(ImageColor color) {
      this.color = color;
    }

    public ImageColor getColor() {
      return color;
    }

    public void setFillColor(ImageColor fillColor) {
      this.fillColor = fillColor;
    }

    public ImageColor getFillColor() {
      return fillColor;
    }

    public void setDashed(boolean isDashed) {
      this.isDashed = isDashed;
    }

    public boolean isDashed() {
      return isDashed;
    }
  }

  public static class PenColorDialog extends JDialog implements ActionListener {
    ToyProgram parent;
    ImageBox.ImageColor selectedColor = ImageBox.ImageColor.RED;

    public PenColorDialog(ToyProgram parent) {
      this.parent = parent;

      setTitle("Select Color");
      JPanel contentPanel = new JPanel(new BorderLayout());

      JRadioButton redBtn = new JRadioButton("Red");
      redBtn.addActionListener(this);
      if (parent.currentColor == ImageBox.ImageColor.RED)
        redBtn.setSelected(true);

      JRadioButton greenBtn = new JRadioButton("Green");
      greenBtn.addActionListener(this);
      if (parent.currentColor == ImageBox.ImageColor.GREEN)
        greenBtn.setSelected(true);

      JRadioButton blueBtn = new JRadioButton("Blue");
      blueBtn.addActionListener(this);
      if (parent.currentColor == ImageBox.ImageColor.BLUE)
        blueBtn.setSelected(true);

      ButtonGroup colorGroup = new ButtonGroup();
      colorGroup.add(redBtn);
      colorGroup.add(greenBtn);
      colorGroup.add(blueBtn);

      JPanel centerPanel = new JPanel();
      centerPanel.add(redBtn);
      centerPanel.add(greenBtn);
      centerPanel.add(blueBtn);

      contentPanel.add(centerPanel, BorderLayout.CENTER);

      JButton okBtn = new JButton("OK");
      okBtn.addActionListener(this);
      JButton cancelBtn = new JButton("Cancel");
      cancelBtn.addActionListener(this);

      JPanel bottomPanel = new JPanel();
      bottomPanel.add(okBtn);
      bottomPanel.add(cancelBtn);

      contentPanel.add(bottomPanel, BorderLayout.SOUTH);

      setContentPane(contentPanel);
      setBounds(0, 0, 300, 100);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String actionCommand = e.getActionCommand();
      if (actionCommand.equals("Red")) {
        selectedColor = ImageBox.ImageColor.RED;
      } else if (actionCommand.equals("Green")) {
        selectedColor = ImageBox.ImageColor.GREEN;
      } else if (actionCommand.equals("Blue")) {
        selectedColor = ImageBox.ImageColor.BLUE;
      } else if (actionCommand.equals("OK")) {
        if (parent.currentColor == selectedColor) {
          int result = JOptionPane.showConfirmDialog(this, "Same color is selected. continue?", "Confirmation",
              JOptionPane.OK_CANCEL_OPTION);
          if (result == JOptionPane.OK_OPTION) {
            final JDialog dialog = this;
            dialog.dispose();
          }
        } else {
          parent.currentColor = selectedColor;
          dispose();
        }
      } else if (e.getActionCommand().equals("Cancel")) {
        dispose();
      }
    }

  }

  private void enableFillColorBtn(boolean b) {
    bgBlueBtn.setEnabled(b);
    bgGreenBtn.setEnabled(b);
    bgRedBtn.setEnabled(b);
    checkBtn.setEnabled(!b);
  }

  private void addMenu() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('f');
    JMenuItem exitMenuItem = new JMenuItem("Exit", 'X');
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);
    menuBar.add(fileMenu);
    setJMenuBar(menuBar);
  }

  public static void main(String[] args) {
    final JFrame f = new ToyProgram("Toy Program");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String actionCommand = e.getActionCommand();
    if (e.getActionCommand().equals("Exit")) {
      int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Close Window", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION) {
        // System.exit(0);
      }
    } else if (e.getActionCommand().equals("Color")) {
      JDialog penColorDialog = new PenColorDialog(this);
      penColorDialog.setModal(true);
      penColorDialog.setVisible(true);

    } else if (e.getActionCommand().equals("bgRed")) {

      currentFillColor = ImageBox.ImageColor.RED;
    } else if (e.getActionCommand().equals("bgGreen")) {
      currentFillColor = ImageBox.ImageColor.GREEN;
    } else if (e.getActionCommand().equals("bgBlue")) {
      currentFillColor = ImageBox.ImageColor.BLUE;
    } else if (actionCommand.equals("Solid")) {
      isDashed = false;
    } else if (actionCommand.equals("Dashed")) {
      isDashed = true;
    } else if (actionCommand.equals("Draw")) {
      imageBox.setColor(currentColor);
      imageBox.setShape(currentShape);
      if (currentShape != ImageBox.ImageShape.LINE)
        imageBox.setFillColor(currentFillColor);
      imageBox.setDashed(isDashed);
      draw();
    } else if (actionCommand.equals("Check2")) {
      if (isChecked) {
        JOptionPane.showMessageDialog(this, "Checked!");
      }

    }
  }

  private void draw() {
    repaint();
  }
}
