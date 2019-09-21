/*
 * Decompiled with CFR 0.146.
 */
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.Vector;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class svm_toy
extends Applet {
    static final String DEFAULT_PARAM = "-t 2 -c 100";
    int XLEN;
    int YLEN;
    Image buffer;
    Graphics buffer_gc;
    static final Color[] colors = new Color[]{new Color(0, 0, 0), new Color(0, 120, 120), new Color(120, 120, 0), new Color(120, 0, 120), new Color(0, 200, 200), new Color(200, 200, 0), new Color(200, 0, 200)};
    Vector<point> point_list = new Vector();
    byte current_value = 1;

    public void init() {
        this.setSize(this.getSize());
        final Button button_change = new Button("Change");
        Button button_run = new Button("Run");
        Button button_clear = new Button("Clear");
        Button button_save = new Button("Save");
        Button button_load = new Button("Load");
        final TextField input_line = new TextField(DEFAULT_PARAM);
        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        Panel p = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = 2;
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints(button_change, c);
        gridbag.setConstraints(button_run, c);
        gridbag.setConstraints(button_clear, c);
        gridbag.setConstraints(button_save, c);
        gridbag.setConstraints(button_load, c);
        c.weightx = 5.0;
        c.gridwidth = 5;
        gridbag.setConstraints(input_line, c);
        button_change.setBackground(colors[this.current_value]);
        p.add(button_change);
        p.add(button_run);
        p.add(button_clear);
        p.add(button_save);
        p.add(button_load);
        p.add(input_line);
        this.add((Component)p, "South");
        button_change.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_change_clicked();
                button_change.setBackground(colors[svm_toy.this.current_value]);
            }
        });
        button_run.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_run_clicked(input_line.getText());
            }
        });
        button_clear.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_clear_clicked();
            }
        });
        button_save.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_save_clicked();
            }
        });
        button_load.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_load_clicked();
            }
        });
        input_line.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                svm_toy.this.button_run_clicked(input_line.getText());
            }
        });
        this.enableEvents(16L);
    }

    void draw_point(point p) {
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
        for (int i = 0; i < n; ++i) {
            this.draw_point(this.point_list.elementAt(i));
        }
    }

    void button_change_clicked() {
        this.current_value = (byte)(this.current_value + 1);
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
        int i;
        if (this.point_list.isEmpty()) {
            return;
        }
        svm_parameter param = new svm_parameter();
        param.svm_type = 0;
        param.kernel_type = 2;
        param.degree = 3;
        param.gamma = 0.0;
        param.coef0 = 0.0;
        param.nu = 0.5;
        param.cache_size = 40.0;
        param.C = 1.0;
        param.eps = 0.001;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        StringTokenizer st = new StringTokenizer(args);
        String[] argv = new String[st.countTokens()];
        for (i = 0; i < argv.length; ++i) {
            argv[i] = st.nextToken();
        }
        block16 : for (i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
            if (++i >= argv.length) {
                System.err.print("unknown option\n");
                break;
            }
            switch (argv[i - 1].charAt(1)) {
                case 's': {
                    param.svm_type = svm_toy.atoi(argv[i]);
                    continue block16;
                }
                case 't': {
                    param.kernel_type = svm_toy.atoi(argv[i]);
                    continue block16;
                }
                case 'd': {
                    param.degree = svm_toy.atoi(argv[i]);
                    continue block16;
                }
                case 'g': {
                    param.gamma = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'r': {
                    param.coef0 = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'n': {
                    param.nu = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'm': {
                    param.cache_size = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'c': {
                    param.C = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'e': {
                    param.eps = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'p': {
                    param.p = svm_toy.atof(argv[i]);
                    continue block16;
                }
                case 'h': {
                    param.shrinking = svm_toy.atoi(argv[i]);
                    continue block16;
                }
                case 'b': {
                    param.probability = svm_toy.atoi(argv[i]);
                    continue block16;
                }
                case 'w': {
                    ++param.nr_weight;
                    int[] old = param.weight_label;
                    param.weight_label = new int[param.nr_weight];
                    System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
                    double[] old2 = param.weight;
                    param.weight = new double[param.nr_weight];
                    System.arraycopy(old2, 0, param.weight, 0, param.nr_weight - 1);
                    param.weight_label[param.nr_weight - 1] = svm_toy.atoi(argv[i - 1].substring(2));
                    param.weight[param.nr_weight - 1] = svm_toy.atof(argv[i]);
                    continue block16;
                }
                default: {
                    System.err.print("unknown option\n");
                }
            }
        }
        svm_problem prob = new svm_problem();
        prob.l = this.point_list.size();
        prob.y = new double[prob.l];
        if (param.kernel_type != 4) {
            if (param.svm_type == 3 || param.svm_type == 4) {
                if (param.gamma == 0.0) {
                    param.gamma = 1.0;
                }
                prob.x = new svm_node[prob.l][1];
                for (int i2 = 0; i2 < prob.l; ++i2) {
                    point p = this.point_list.elementAt(i2);
                    prob.x[i2][0] = new svm_node();
                    prob.x[i2][0].index = 1;
                    prob.x[i2][0].value = p.x;
                    prob.y[i2] = p.y;
                }
                svm_model model = svm.svm_train(prob, param);
                svm_node[] x = new svm_node[]{new svm_node()};
                x[0].index = 1;
                int[] j = new int[this.XLEN];
                Graphics window_gc = this.getGraphics();
                for (int i3 = 0; i3 < this.XLEN; ++i3) {
                    x[0].value = (double)i3 / (double)this.XLEN;
                    j[i3] = (int)((double)this.YLEN * svm.svm_predict(model, x));
                }
                this.buffer_gc.setColor(colors[0]);
                this.buffer_gc.drawLine(0, 0, 0, this.YLEN - 1);
                window_gc.setColor(colors[0]);
                window_gc.drawLine(0, 0, 0, this.YLEN - 1);
                int p = (int)(param.p * (double)this.YLEN);
                for (int i4 = 1; i4 < this.XLEN; ++i4) {
                    this.buffer_gc.setColor(colors[0]);
                    this.buffer_gc.drawLine(i4, 0, i4, this.YLEN - 1);
                    window_gc.setColor(colors[0]);
                    window_gc.drawLine(i4, 0, i4, this.YLEN - 1);
                    this.buffer_gc.setColor(colors[5]);
                    window_gc.setColor(colors[5]);
                    this.buffer_gc.drawLine(i4 - 1, j[i4 - 1], i4, j[i4]);
                    window_gc.drawLine(i4 - 1, j[i4 - 1], i4, j[i4]);
                    if (param.svm_type != 3) continue;
                    this.buffer_gc.setColor(colors[2]);
                    window_gc.setColor(colors[2]);
                    this.buffer_gc.drawLine(i4 - 1, j[i4 - 1] + p, i4, j[i4] + p);
                    window_gc.drawLine(i4 - 1, j[i4 - 1] + p, i4, j[i4] + p);
                    this.buffer_gc.setColor(colors[2]);
                    window_gc.setColor(colors[2]);
                    this.buffer_gc.drawLine(i4 - 1, j[i4 - 1] - p, i4, j[i4] - p);
                    window_gc.drawLine(i4 - 1, j[i4 - 1] - p, i4, j[i4] - p);
                }
            } else {
                if (param.gamma == 0.0) {
                    param.gamma = 0.5;
                }
                prob.x = new svm_node[prob.l][2];
                for (int i5 = 0; i5 < prob.l; ++i5) {
                    point p = this.point_list.elementAt(i5);
                    prob.x[i5][0] = new svm_node();
                    prob.x[i5][0].index = 1;
                    prob.x[i5][0].value = p.x;
                    prob.x[i5][1] = new svm_node();
                    prob.x[i5][1].index = 2;
                    prob.x[i5][1].value = p.y;
                    prob.y[i5] = p.value;
                }
                svm_model model = svm.svm_train(prob, param);
                svm_node[] x = new svm_node[]{new svm_node(), new svm_node()};
                x[0].index = 1;
                x[1].index = 2;
                Graphics window_gc = this.getGraphics();
                for (int i6 = 0; i6 < this.XLEN; ++i6) {
                    for (int j = 0; j < this.YLEN; ++j) {
                        x[0].value = (double)i6 / (double)this.XLEN;
                        x[1].value = (double)j / (double)this.YLEN;
                        double d = svm.svm_predict(model, x);
                        if (param.svm_type == 2 && d < 0.0) {
                            d = 2.0;
                        }
                        this.buffer_gc.setColor(colors[(int)d]);
                        window_gc.setColor(colors[(int)d]);
                        this.buffer_gc.drawLine(i6, j, i6, j);
                        window_gc.drawLine(i6, j, i6, j);
                    }
                }
            }
        }
        this.draw_all_points();
    }

    void button_clear_clicked() {
        this.clear_all();
    }

    void button_save_clicked() {
        FileDialog dialog = new FileDialog(new Frame(), "Save", 1);
        dialog.setVisible(true);
        String filename = dialog.getDirectory() + dialog.getFile();
        if (filename == null) {
            return;
        }
        try {
            DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            int n = this.point_list.size();
            for (int i = 0; i < n; ++i) {
                point p = this.point_list.elementAt(i);
                fp.writeBytes(p.value + " 1:" + p.x + " 2:" + p.y + "\n");
            }
            fp.close();
        }
        catch (IOException e) {
            System.err.print(e);
        }
    }

    void button_load_clicked() {
        FileDialog dialog = new FileDialog(new Frame(), "Load", 0);
        dialog.setVisible(true);
        String filename = dialog.getDirectory() + dialog.getFile();
        if (filename == null) {
            return;
        }
        this.clear_all();
        try {
            String line;
            BufferedReader fp = new BufferedReader(new FileReader(filename));
            while ((line = fp.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
                byte value = (byte)svm_toy.atoi(st.nextToken());
                st.nextToken();
                double x = svm_toy.atof(st.nextToken());
                st.nextToken();
                double y = svm_toy.atof(st.nextToken());
                this.point_list.addElement(new point(x, y, value));
            }
            fp.close();
        }
        catch (IOException e) {
            System.err.print(e);
        }
        this.draw_all_points();
    }

    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == 501) {
            if (e.getX() >= this.XLEN || e.getY() >= this.YLEN) {
                return;
            }
            point p = new point((double)e.getX() / (double)this.XLEN, (double)e.getY() / (double)this.YLEN, this.current_value);
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

