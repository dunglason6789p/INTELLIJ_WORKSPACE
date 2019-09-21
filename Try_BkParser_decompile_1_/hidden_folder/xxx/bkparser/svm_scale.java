import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.StringTokenizer;

class svm_scale {
   private String line = null;
   private double lower = -1.0D;
   private double upper = 1.0D;
   private double y_lower;
   private double y_upper;
   private boolean y_scaling = false;
   private double[] feature_max;
   private double[] feature_min;
   private double y_max = -1.7976931348623157E308D;
   private double y_min = 1.7976931348623157E308D;
   private int max_index;
   private long num_nonzeros = 0L;
   private long new_num_nonzeros = 0L;

   svm_scale() {
   }

   private static void exit_with_help() {
      System.out.print("Usage: svm-scale [options] data_filename\noptions:\n-l lower : x scaling lower limit (default -1)\n-u upper : x scaling upper limit (default +1)\n-y y_lower y_upper : y scaling limits (default: no y scaling)\n-s save_filename : save scaling parameters to save_filename\n-r restore_filename : restore scaling parameters from restore_filename\n");
      System.exit(1);
   }

   private BufferedReader rewind(BufferedReader fp, String filename) throws IOException {
      fp.close();
      return new BufferedReader(new FileReader(filename));
   }

   private void output_target(double value) {
      if (this.y_scaling) {
         if (value == this.y_min) {
            value = this.y_lower;
         } else if (value == this.y_max) {
            value = this.y_upper;
         } else {
            value = this.y_lower + (this.y_upper - this.y_lower) * (value - this.y_min) / (this.y_max - this.y_min);
         }
      }

      System.out.print(value + " ");
   }

   private void output(int index, double value) {
      if (this.feature_max[index] != this.feature_min[index]) {
         if (value == this.feature_min[index]) {
            value = this.lower;
         } else if (value == this.feature_max[index]) {
            value = this.upper;
         } else {
            value = this.lower + (this.upper - this.lower) * (value - this.feature_min[index]) / (this.feature_max[index] - this.feature_min[index]);
         }

         if (value != 0.0D) {
            System.out.print(index + ":" + value + " ");
            ++this.new_num_nonzeros;
         }

      }
   }

   private String readline(BufferedReader fp) throws IOException {
      this.line = fp.readLine();
      return this.line;
   }

   private void run(String[] argv) throws IOException {
      BufferedReader fp = null;
      BufferedReader fp_restore = null;
      String save_filename = null;
      String restore_filename = null;
      String data_filename = null;

      int i;
      for(i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i) {
         ++i;
         switch(argv[i - 1].charAt(1)) {
         case 'l':
            this.lower = Double.parseDouble(argv[i]);
            break;
         case 'm':
         case 'n':
         case 'o':
         case 'p':
         case 'q':
         case 't':
         case 'v':
         case 'w':
         case 'x':
         default:
            System.err.println("unknown option");
            exit_with_help();
            break;
         case 'r':
            restore_filename = argv[i];
            break;
         case 's':
            save_filename = argv[i];
            break;
         case 'u':
            this.upper = Double.parseDouble(argv[i]);
            break;
         case 'y':
            this.y_lower = Double.parseDouble(argv[i]);
            ++i;
            this.y_upper = Double.parseDouble(argv[i]);
            this.y_scaling = true;
         }
      }

      if (this.upper <= this.lower || this.y_scaling && this.y_upper <= this.y_lower) {
         System.err.println("inconsistent lower/upper specification");
         System.exit(1);
      }

      if (restore_filename != null && save_filename != null) {
         System.err.println("cannot use -r and -s simultaneously");
         System.exit(1);
      }

      if (argv.length != i + 1) {
         exit_with_help();
      }

      data_filename = argv[i];

      try {
         fp = new BufferedReader(new FileReader(data_filename));
      } catch (Exception var21) {
         System.err.println("can't open file " + data_filename);
         System.exit(1);
      }

      this.max_index = 0;
      int idx;
      if (restore_filename != null) {
         try {
            fp_restore = new BufferedReader(new FileReader(restore_filename));
         } catch (Exception var20) {
            System.err.println("can't open file " + restore_filename);
            System.exit(1);
         }

         if (fp_restore.read() == 121) {
            fp_restore.readLine();
            fp_restore.readLine();
            fp_restore.readLine();
         }

         fp_restore.readLine();
         fp_restore.readLine();

         for(String restore_line = null; (restore_line = fp_restore.readLine()) != null; this.max_index = Math.max(this.max_index, idx)) {
            StringTokenizer st2 = new StringTokenizer(restore_line);
            idx = Integer.parseInt(st2.nextToken());
         }

         fp_restore = this.rewind(fp_restore, restore_filename);
      }

      int index;
      while(this.readline(fp) != null) {
         StringTokenizer st = new StringTokenizer(this.line, " \t\n\r\f:");
         st.nextToken();

         while(st.hasMoreTokens()) {
            index = Integer.parseInt(st.nextToken());
            this.max_index = Math.max(this.max_index, index);
            st.nextToken();
            ++this.num_nonzeros;
         }
      }

      try {
         this.feature_max = new double[this.max_index + 1];
         this.feature_min = new double[this.max_index + 1];
      } catch (OutOfMemoryError var19) {
         System.err.println("can't allocate enough memory");
         System.exit(1);
      }

      for(i = 0; i <= this.max_index; ++i) {
         this.feature_max[i] = -1.7976931348623157E308D;
         this.feature_min[i] = 1.7976931348623157E308D;
      }

      fp = this.rewind(fp, data_filename);

      double target;
      StringTokenizer st;
      double value;
      while(this.readline(fp) != null) {
         idx = 1;
         st = new StringTokenizer(this.line, " \t\n\r\f:");
         target = Double.parseDouble(st.nextToken());
         this.y_max = Math.max(this.y_max, target);

         for(this.y_min = Math.min(this.y_min, target); st.hasMoreTokens(); idx = index + 1) {
            index = Integer.parseInt(st.nextToken());
            value = Double.parseDouble(st.nextToken());

            for(i = idx; i < index; ++i) {
               this.feature_max[i] = Math.max(this.feature_max[i], 0.0D);
               this.feature_min[i] = Math.min(this.feature_min[i], 0.0D);
            }

            this.feature_max[index] = Math.max(this.feature_max[index], value);
            this.feature_min[index] = Math.min(this.feature_min[index], value);
         }

         for(i = idx; i <= this.max_index; ++i) {
            this.feature_max[i] = Math.max(this.feature_max[i], 0.0D);
            this.feature_min[i] = Math.min(this.feature_min[i], 0.0D);
         }
      }

      fp = this.rewind(fp, data_filename);
      if (restore_filename != null) {
         fp_restore.mark(2);
         StringTokenizer st;
         if (fp_restore.read() == 121) {
            fp_restore.readLine();
            st = new StringTokenizer(fp_restore.readLine());
            this.y_lower = Double.parseDouble(st.nextToken());
            this.y_upper = Double.parseDouble(st.nextToken());
            st = new StringTokenizer(fp_restore.readLine());
            this.y_min = Double.parseDouble(st.nextToken());
            this.y_max = Double.parseDouble(st.nextToken());
            this.y_scaling = true;
         } else {
            fp_restore.reset();
         }

         if (fp_restore.read() == 120) {
            fp_restore.readLine();
            st = new StringTokenizer(fp_restore.readLine());
            this.lower = Double.parseDouble(st.nextToken());
            this.upper = Double.parseDouble(st.nextToken());
            String restore_line = null;

            while((restore_line = fp_restore.readLine()) != null) {
               StringTokenizer st2 = new StringTokenizer(restore_line);
               idx = Integer.parseInt(st2.nextToken());
               double fmin = Double.parseDouble(st2.nextToken());
               double fmax = Double.parseDouble(st2.nextToken());
               if (idx <= this.max_index) {
                  this.feature_min[idx] = fmin;
                  this.feature_max[idx] = fmax;
               }
            }
         }

         fp_restore.close();
      }

      if (save_filename != null) {
         Formatter formatter = new Formatter(new StringBuilder());
         BufferedWriter fp_save = null;

         try {
            fp_save = new BufferedWriter(new FileWriter(save_filename));
         } catch (IOException var18) {
            System.err.println("can't open file " + save_filename);
            System.exit(1);
         }

         if (this.y_scaling) {
            formatter.format("y\n");
            formatter.format("%.16g %.16g\n", this.y_lower, this.y_upper);
            formatter.format("%.16g %.16g\n", this.y_min, this.y_max);
         }

         formatter.format("x\n");
         formatter.format("%.16g %.16g\n", this.lower, this.upper);

         for(i = 1; i <= this.max_index; ++i) {
            if (this.feature_min[i] != this.feature_max[i]) {
               formatter.format("%d %.16g %.16g\n", i, this.feature_min[i], this.feature_max[i]);
            }
         }

         fp_save.write(formatter.toString());
         fp_save.close();
      }

      while(this.readline(fp) != null) {
         idx = 1;
         st = new StringTokenizer(this.line, " \t\n\r\f:");
         target = Double.parseDouble(st.nextToken());
         this.output_target(target);

         while(st.hasMoreElements()) {
            index = Integer.parseInt(st.nextToken());
            value = Double.parseDouble(st.nextToken());

            for(i = idx; i < index; ++i) {
               this.output(i, 0.0D);
            }

            this.output(index, value);
            idx = index + 1;
         }

         for(i = idx; i <= this.max_index; ++i) {
            this.output(i, 0.0D);
         }

         System.out.print("\n");
      }

      if (this.new_num_nonzeros > this.num_nonzeros) {
         System.err.print("Warning: original #nonzeros " + this.num_nonzeros + "\n" + "         new      #nonzeros " + this.new_num_nonzeros + "\n" + "Use -l 0 if many original feature values are zeros\n");
      }

      fp.close();
   }

   public static void main(String[] argv) throws IOException {
      svm_scale s = new svm_scale();
      s.run(argv);
   }
}
