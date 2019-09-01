package mainPackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JApplet;
import javax.swing.JFrame;

public class FilledGeneralPath extends JApplet {

    public void init() {
        setBackground(Color.white);
        setForeground(Color.white);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int x = 5;
        int y = 7;

        // fill and stroke GeneralPath
        int xPoints[] = { x, 200, x, 200 };
        int yPoints[] = { y, 200, 200, y };
        GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
                xPoints.length);
        filledPolygon.moveTo(xPoints[0], yPoints[0]);
        for (int index = 1; index < xPoints.length; index++) {
            filledPolygon.lineTo(xPoints[index], yPoints[index]);
        }
        filledPolygon.closePath();
        g2.setPaint(Color.red);
        g2.fill(filledPolygon);
        g2.setPaint(Color.black);
        g2.draw(filledPolygon);
        g2.drawString("Filled and Stroked GeneralPath", x, 250);
    }

    public static void main(String s[]) {
        JFrame f = new JFrame("");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JApplet applet = new FilledGeneralPath();
        f.getContentPane().add("Center", applet);
        applet.init();
        f.pack();
        f.setSize(new Dimension(300, 300));
        f.show();
    }
}




