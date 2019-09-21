import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class svm_toy extends Applet {
   static final String DEFAULT_PARAM = "-t 2 -c 100";
   int XLEN;
   int YLEN;
   Image buffer;
   Graphics buffer_gc;
   static final Color[] colors = new Color[]{new Color(0, 0, 0), new Color(0, 120, 120), new Color(120, 120, 0), new Color(120, 0, 120), new Color(0, 200, 200), new Color(200, 200, 0), new Color(200, 0, 200)};
   Vector<svm_toy.point> point_list = new Vector();
   byte current_value = 1;

   public svm_toy() {
   }

   public void init() {
      this.setSize(this.getSize());
      final Button button_change = new Button("Change");
      Button button_run = new Button("Run");
      Button button_clear = new Button("Clear");
      Button button_save = new Button("Save");
      Button button_load = new Button("Load");
      final TextField input_line = new TextField("-t 2 -c 100");
      BorderLayout layout = new BorderLayout();
      this.setLayout(layout);
      Panel p = new Panel();
      GridBagLayout gridbag = new GridBagLayout();
      p.setLayout(gridbag);
      GridBagConstraints c = new GridBagConstraints();
      c.fill = 2;
      c.weightx = 1.0D;
      c.gridwidth = 1;
      gridbag.setConstraints(button_change, c);
      gridbag.setConstraints(button_run, c);
      gridbag.setConstraints(button_clear, c);
      gridbag.setConstraints(button_save, c);
      gridbag.setConstraints(button_load, c);
      c.weightx = 5.0D;
      c.gridwidth = 5;
      gridbag.setConstraints(input_line, c);
      button_change.setBackground(colors[this.current_value]);
      p.add(button_change);
      p.add(button_run);
      p.add(button_clear);
      p.add(button_save);
      p.add(button_load);
      p.add(input_line);
      this.add(p, "South");
      button_change.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_change_clicked();
            button_change.setBackground(svm_toy.colors[svm_toy.this.current_value]);
         }
      });
      button_run.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_run_clicked(input_line.getText());
         }
      });
      button_clear.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_clear_clicked();
         }
      });
      button_save.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_save_clicked();
         }
      });
      button_load.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_load_clicked();
         }
      });
      input_line.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            svm_toy.this.button_run_clicked(input_line.getText());
         }
      });
      this.enableEvents(16L);
   }

   void draw_point(svm_toy.point p) {
      Color c = colors[p.value + 3];
      Graphics window_gc = this.getGraphics();
      this.buffer_gc.setColor(c);
      this.buffer_gc.fillRect((int)(p.x * (double)this.XLEN), (int)(p.y * (double)this.YLEN), 4, 4);
      window_gc.setColor(c);
      window_gc.fillRect((int)(p.x * (double)this.XLEN), (int)(p.y * (double)this.YLEN), 4, 4);
   }

   void clear_all() {
      this.point_list.removeAllElements();
      if (this.buffer != null) {
         this.buffer_gc.setColor(colors[0]);
         this.buffer_gc.fillRect(0, 0, this.XLEN, this.YLEN);
      }

      this.repaint();
   }

   void draw_all_points() {
      int n = this.point_list.size();

      for(int i = 0; i < n; ++i) {
         this.draw_point((svm_toy.point)this.point_list.elementAt(i));
      }

   }

   void button_change_clicked() {
      ++this.current_value;
      if (this.current_value > 3) {
         this.current_value = 1;
      }

   }

   private static double atof(String s) {
      return Double.valueOf(s);
   }

   private static int atoi(String s) {
      return Integer.parseInt(s);
   }

   void button_run_clicked(String args) {
      if (!this.point_list.isEmpty()) {
         svm_parameter param = new svm_parameter();
         param.svm_type = 0;
         param.kernel_type = 2;
         param.degree = 3;
         param.gamma = 0.0D;
         param.coef0 = 0.0D;
         param.nu = 0.5D;
         param.cache_size = 40.0D;
         param.C = 1.0D;
         param.eps = 0.001D;
         param.p = 0.1D;
         param.shrinking = 1;
         param.probability = 0;
         param.nr_weight = 0;
         param.weight_label = new int[0];
         param.weight = new double[0];
         StringTokenizer st = new StringTokenizer(args);
         String[] argv = new String[st.countTokens()];

         int i;
         for(i = 0; i < argv.length; ++i) {
            argv[i] = st.nextToken();
         }

         for(i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
            ++i;
            if (i >= argv.length) {
               System.err.print("unknown option\n");
               break;
            }

            switch(argv[i - 1].charAt(1)) {
            case 'b':
               param.probability = atoi(argv[i]);
               break;
            case 'c':
               param.C = atof(argv[i]);
               break;
            case 'd':
               param.degree = atoi(argv[i]);
               break;
            case 'e':
               param.eps = atof(argv[i]);
               break;
            case 'f':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'o':
            case 'q':
            case 'u':
            case 'v':
            default:
               System.err.print("unknown option\n");
               break;
            case 'g':
               param.gamma = atof(argv[i]);
               break;
            case 'h':
               param.shrinking = atoi(argv[i]);
               break;
            case 'm':
               param.cache_size = atof(argv[i]);
               break;
            case 'n':
               param.nu = atof(argv[i]);
               break;
            case 'p':
               param.p = atof(argv[i]);
               break;
            case 'r':
               param.coef0 = atof(argv[i]);
               break;
            case 's':
               param.svm_type = atoi(argv[i]);
               break;
            case 't':
               param.kernel_type = atoi(argv[i]);
               break;
            case 'w':
               ++param.nr_weight;
               int[] old = param.weight_label;
               param.weight_label = new int[param.nr_weight];
               System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
               double[] old = param.weight;
               param.weight = new double[param.nr_weight];
               System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
               param.weight_label[param.nr_weight - 1] = atoi(argv[i - 1].substring(2));
               param.weight[param.nr_weight - 1] = atof(argv[i]);
            }
         }

         svm_problem prob = new svm_problem();
         prob.l = this.point_list.size();
         prob.y = new double[prob.l];
         if (param.kernel_type != 4) {
            svm_toy.point p;
            int p;
            int i;
            svm_model model;
            svm_node[] x;
            if (param.svm_type != 3 && param.svm_type != 4) {
               if (param.gamma == 0.0D) {
                  param.gamma = 0.5D;
               }

               prob.x = new svm_node[prob.l][2];

               for(i = 0; i < prob.l; ++i) {
                  p = (svm_toy.point)this.point_list.elementAt(i);
                  prob.x[i][0] = new svm_node();
                  prob.x[i][0].index = 1;
                  prob.x[i][0].value = p.x;
                  prob.x[i][1] = new svm_node();
                  prob.x[i][1].index = 2;
                  prob.x[i][1].value = p.y;
                  prob.y[i] = (double)p.value;
               }

               model = svm.svm_train(prob, param);
               x = new svm_node[]{new svm_node(), new svm_node()};
               x[0].index = 1;
               x[1].index = 2;
               Graphics window_gc = this.getGraphics();

               for(int i = 0; i < this.XLEN; ++i) {
                  for(p = 0; p < this.YLEN; ++p) {
                     x[0].value = (double)i / (double)this.XLEN;
                     x[1].value = (double)p / (double)this.YLEN;
                     double d = svm.svm_predict(model, x);
                     if (param.svm_type == 2 && d < 0.0D) {
                        d = 2.0D;
                     }

                     this.buffer_gc.setColor(colors[(int)d]);
                     window_gc.setColor(colors[(int)d]);
                     this.buffer_gc.drawLine(i, p, i, p);
                     window_gc.drawLine(i, p, i, p);
                  }
               }
            } else {
               if (param.gamma == 0.0D) {
                  param.gamma = 1.0D;
               }

               prob.x = new svm_node[prob.l][1];

               for(i = 0; i < prob.l; ++i) {
                  p = (svm_toy.point)this.point_list.elementAt(i);
                  prob.x[i][0] = new svm_node();
                  prob.x[i][0].index = 1;
                  prob.x[i][0].value = p.x;
                  prob.y[i] = p.y;
               }

               model = svm.svm_train(prob, param);
               x = new svm_node[]{new svm_node()};
               x[0].index = 1;
               int[] j = new int[this.XLEN];
               Graphics window_gc = this.getGraphics();

               for(p = 0; p < this.XLEN; ++p) {
                  x[0].value = (double)p / (double)this.XLEN;
                  j[p] = (int)((double)this.YLEN * svm.svm_predict(model, x));
               }

               this.buffer_gc.setColor(colors[0]);
               this.buffer_gc.drawLine(0, 0, 0, this.YLEN - 1);
               window_gc.setColor(colors[0]);
               window_gc.drawLine(0, 0, 0, this.YLEN - 1);
               p = (int)(param.p * (double)this.YLEN);

               for(int i = 1; i < this.XLEN; ++i) {
                  this.buffer_gc.setColor(colors[0]);
                  this.buffer_gc.drawLine(i, 0, i, this.YLEN - 1);
                  window_gc.setColor(colors[0]);
                  window_gc.drawLine(i, 0, i, this.YLEN - 1);
                  this.buffer_gc.setColor(colors[5]);
                  window_gc.setColor(colors[5]);
                  this.buffer_gc.drawLine(i - 1, j[i - 1], i, j[i]);
                  window_gc.drawLine(i - 1, j[i - 1], i, j[i]);
                  if (param.svm_type == 3) {
                     this.buffer_gc.setColor(colors[2]);
                     window_gc.setColor(colors[2]);
                     this.buffer_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);
                     window_gc.drawLine(i - 1, j[i - 1] + p, i, j[i] + p);
                     this.buffer_gc.setColor(colors[2]);
                     window_gc.setColor(colors[2]);
                     this.buffer_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
                     window_gc.drawLine(i - 1, j[i - 1] - p, i, j[i] - p);
                  }
               }
            }
         }

         this.draw_all_points();
      }
   }

   void button_clear_clicked() {
      this.clear_all();
   }

   void button_save_clicked() {
      FileDialog dialog = new FileDialog(new Frame(), "Save", 1);
      dialog.setVisible(true);
      String filename = dialog.getDirectory() + dialog.getFile();
      if (filename != null) {
         try {
            DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            int n = this.point_list.size();

            for(int i = 0; i < n; ++i) {
               svm_toy.point p = (svm_toy.point)this.point_list.elementAt(i);
               fp.writeBytes(p.value + " 1:" + p.x + " 2:" + p.y + "\n");
            }

            fp.close();
         } catch (IOException var7) {
            System.err.print(var7);
         }

      }
   }

   void button_load_clicked() {
      FileDialog dialog = new FileDialog(new Frame(), "Load", 0);
      dialog.setVisible(true);
      String filename = dialog.getDirectory() + dialog.getFile();
      if (filename != null) {
         this.clear_all();

         try {
            BufferedReader fp = new BufferedReader(new FileReader(filename));

            String line;
            while((line = fp.readLine()) != null) {
               StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
               byte value = (byte)atoi(st.nextToken());
               st.nextToken();
               double x = atof(st.nextToken());
               st.nextToken();
               double y = atof(st.nextToken());
               this.point_list.addElement(new svm_toy.point(x, y, value));
            }

            fp.close();
         } catch (IOException var11) {
            System.err.print(var11);
         }

         this.draw_all_points();
      }
   }

   protected void processMouseEvent(MouseEvent e) {
      if (e.getID() == 501) {
         if (e.getX() >= this.XLEN || e.getY() >= this.YLEN) {
            return;
         }

         svm_toy.point p = new svm_toy.point((double)e.getX() / (double)this.XLEN, (double)e.getY() / (double)this.YLEN, this.current_value);
         this.point_list.addElement(p);
         this.draw_point(p);
      }

   }

   public void paint(Graphics g) {
      if (this.buffer == null) {
         this.buffer = this.createImage(this.XLEN, this.YLEN);
         this.buffer_gc = this.buffer.getGraphics();
         this.buffer_gc.setColor(colors[0]);
         this.buffer_gc.fillRect(0, 0, this.XLEN, this.YLEN);
      }

      g.drawImage(this.buffer, 0, 0, this);
   }

   public Dimension getPreferredSize() {
      return new Dimension(this.XLEN, this.YLEN + 50);
   }

   public void setSize(Dimension d) {
      this.setSize(d.width, d.height);
   }

   public void setSize(int w, int h) {
      super.setSize(w, h);
      this.XLEN = w;
      this.YLEN = h - 50;
      this.clear_all();
   }

   public static void main(String[] argv) {
      new AppletFrame("svm_toy", new svm_toy(), 500, 550);
   }

   class point {
      double x;
      double y;
      byte value;

      point(double x, double y, byte value) {
         this.x = x;
         this.y = y;
         this.value = value;
      }
   }
}
