
//Code by madongxian
//Student id: 12330235


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;

import java.io.IOException;

import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

public class proj extends JFrame {

  // 图像的像素矩阵
  private int sourcePixArray[] = null;   //待去雾图像
  private int currentPixArray[] = null;  //当前图像
  private int darkPixArray[] = null;     //暗通道图像
  
  private double grayArray[] = null;     //RGB图像的灰度值矩阵
  private double guideArray[] = null;    //导向通透图的灰度值矩阵
  private int guidePixArray[] = null;    //导向通透图像

  private int targetPixArray[] = null;   //去雾后的目标图像

  // 图像的路径
  private String fileString = null;

  // 用于显示图像的标签
  private JLabel imageLabel = null;

  // 加载的图像
  private BufferedImage newImage;
  
  
  //当前图像
  private Image currentpic;
  private BufferedImage currentImage;
  
  //将要进行滤波处理的 补“0”扩大图像矩阵
  private int enlarge_array[] = null;
  private double enlarge[] = null;


  // 图像的高和宽
  private int w;
  private int h;
  //
  private double A_r;
  private double A_g;
  private double A_b;
  
  private double tx[] = null;


  public proj(String title) {
      super(title);
      this.setSize(800, 800);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // 创建菜单
      JMenuBar jb = new JMenuBar();
      JMenu fileMenu = new JMenu("文件");
      jb.add(fileMenu);
      
      //open
      JMenuItem openImageMenuItem = new JMenuItem("打开图像");
      fileMenu.add(openImageMenuItem);
      openImageMenuItem.addActionListener(new OpenListener());
      
      //save
      JMenuItem saveImageMenuItem = new JMenuItem("保存图像");
      fileMenu.add(saveImageMenuItem);
      saveImageMenuItem.addActionListener(new SaveListener());
      
      //exit
      JMenuItem exitMenu = new JMenuItem("退出");
      fileMenu.add(exitMenu);
      exitMenu.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              System.exit(0);
          }
      });
      //////////////////////////////////////////////////////////////
      
     
      
      ///////////////////////////////////////////////////////////////

      
      
      JMenu FilterMenu = new JMenu("去雾处理");
      jb.add(FilterMenu);
      
      JMenuItem averageFilterMenuItem = new JMenuItem("获取暗通道图");
      FilterMenu.add(averageFilterMenuItem);
      averageFilterMenuItem.addActionListener(new getDarkChannelMenuActionListener());
      
      JMenuItem HazeMenuItem = new JMenuItem("预估去雾");
      FilterMenu.add(HazeMenuItem);
      HazeMenuItem.addActionListener(new HazeRemovalMenuActionListener());
      
      JMenu sMenu = new JMenu("soft matting");
      jb.add(sMenu);
      
      JMenuItem guideFilterMenuItem = new JMenuItem("获取导向滤波通透率图");
      sMenu.add(guideFilterMenuItem);
      guideFilterMenuItem.addActionListener(new getGuidepicMenuActionListener());
      
      JMenuItem softMenuItem = new JMenuItem("soft matting 去雾");
      sMenu.add(softMenuItem);
      softMenuItem.addActionListener(new softMenuActionListener());

      this.setJMenuBar(jb);

      imageLabel = new JLabel("");
      JScrollPane pane = new JScrollPane(imageLabel);
      this.add(pane, BorderLayout.CENTER);

      this.setVisible(true);

  }
  
  //the interface of Open
  private class OpenListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
          JFileChooser jc = new JFileChooser();
          jc.setFileFilter(new FileFilter() {
              public boolean accept(File f) { // 设定可用的文件的后缀名
                  if (f.getName().endsWith(".jpg") || f.isDirectory()|| f.getName().endsWith(".gif") || f.getName().endsWith(".png") || f.getName().endsWith(".bmp")) {
                      return true;
                  }
                  return false;
              }

              public String getDescription() {
                  return "图片(*.jpg,*.gif,*bmp,*png)";
              }
          });
          int returnValue = jc.showOpenDialog(null);
          if (returnValue == JFileChooser.APPROVE_OPTION) {
              File selectedFile = jc.getSelectedFile();
              if (selectedFile != null) {
                  fileString = selectedFile.getAbsolutePath();
                  try {
                      newImage = ImageIO.read(new File(fileString));
                      w = newImage.getWidth();
                      h = newImage.getHeight();
                      currentPixArray = getPixArray(newImage, w, h);
                      sourcePixArray = getPixArray(newImage, w, h);
                      currentpic = createImage(new MemoryImageSource(w, h, currentPixArray, 0, w));
                      currentImage = toBufferedImage(currentpic);
                      imageLabel.setIcon(new ImageIcon(newImage));

                  } catch (IOException ex) {
                      System.out.println(ex);
                  }

              }
          }
          proj.this.repaint();
          // MyShowImage.this.pack();
      }
  }
  
  //the interface of Save
  private class SaveListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
          JFileChooser jc = new JFileChooser();
          
          int returnValue = jc.showSaveDialog(null);
          
          File f = jc.getSelectedFile();
          
          
          if(returnValue == JFileChooser.APPROVE_OPTION)
          {
              try {
					ImageIO.write(currentImage, "png", f);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
          }
          proj.this.repaint();
          // MyShowImage.this.pack();
      }
  }
 
  //the interface of get Dark Channel
  private class getDarkChannelMenuActionListener implements ActionListener{
	  	
	  	public void actionPerformed(ActionEvent e) {

	  		  darkPixArray = getDarkchannel0(currentPixArray);
	          currentpic = createImage(new MemoryImageSource(w, h, darkPixArray, 0, w));
	          currentImage = toBufferedImage(currentpic);
	          showImage(darkPixArray);
	          
	      }
	  }
  
  //HazeRemoval without soft matting
  private class HazeRemovalMenuActionListener implements ActionListener{
	  	
	  	public void actionPerformed(ActionEvent e) {
	  		
	          darkPixArray = getDarkchannel0(sourcePixArray);
	          getA();
	          tx = gettx(sourcePixArray);
	          ColorModel colorModel = ColorModel.getRGBdefault();
	          targetPixArray = new int[w*h];
	          for(int i=0;i<h*w;i++){
	        	  int tr,tg,tb;
	        	  tr = (int)((colorModel.getRed(currentPixArray[i])-A_r)/Max(tx[i],0.1)+A_r);
	        	  tg = (int)((colorModel.getGreen(currentPixArray[i])-A_g)/Max(tx[i],0.1)+A_g);
	        	  tb = (int)((colorModel.getBlue(currentPixArray[i])-A_b)/Max(tx[i],0.1)+A_b);
	        	  tr = tr>255?255:tr;tr = tr<0?0:tr;
	        	  tg = tg>255?255:tg;tg = tg<0?0:tg;
	        	  tb = tb>255?255:tb;tb = tb<0?0:tb;
	        	  targetPixArray[i] = (255 << 24) | (tr << 16) | (tg << 8) | tb;
	          }
	          currentpic = createImage(new MemoryImageSource(w, h, targetPixArray, 0, w));
	          currentImage = toBufferedImage(currentpic);
	          showImage(targetPixArray);
	          
	      }
	  }
  
  //get Guide_Image_filter Transmission image
  private class getGuidepicMenuActionListener implements ActionListener{
	  	
	  	public void actionPerformed(ActionEvent e) {

	  		  grayArray = getgray_I(sourcePixArray);
	  		  darkPixArray = getDarkchannel(sourcePixArray);
	          getA();
	          tx = gettx(sourcePixArray);
	          
	          for(int i=0;i<w*h;i++){
	        	  grayArray[i]=grayArray[i]/255.00;
	        	  tx[i]=tx[i]/255.00;
	          }
	  		  guideArray = gettx_better(grayArray,tx);
	  		  
	  		  
	  		  for(int i=0;i<w*h;i++){
	        	  guideArray[i] = guideArray[i]*255*220;
	          }
	          
	  		  
	  		  guidePixArray = new int[w*h];
	  		  for(int i=0;i<h*w;i++){
	        	  int r,g,b;
	        	  r = g = b = (int)guideArray[i]; 
	        	  r = r>255?255:r;r = r<0?0:r;
	        	  g = g>255?255:g;g = g<0?0:g;
	        	  b = b>255?255:b;b = b<0?0:b;
	        	  guidePixArray[i] = (255 << 24) | (r << 16) | (g << 8) | b;
	          }

	          currentpic = createImage(new MemoryImageSource(w, h, guidePixArray, 0, w));
	          currentImage = toBufferedImage(currentpic);
	          showImage(guidePixArray);
	          
	      }
	  }
  
  private class softMenuActionListener implements ActionListener{
	  	
	  	public void actionPerformed(ActionEvent e) {
	  		 
	  		  grayArray = getgray_I(sourcePixArray);
	  		  darkPixArray = getDarkchannel(sourcePixArray);
	          getA();
	          tx = gettx(sourcePixArray);
	          
	          //normalization of I and p
	          for(int i=0;i<w*h;i++){
	        	  grayArray[i]=grayArray[i]/255.00;
	        	  tx[i]=tx[i]/255.00;
	          }
	  		  guideArray = gettx_better(grayArray,tx);
	  		  
	  		  
	  		  for(int i=0;i<w*h;i++){
	        	  guideArray[i] = guideArray[i]*255;
	        	  guideArray[i] = guideArray[i]<0?0:guideArray[i];
	        	  guideArray[i] = guideArray[i]>255?255:guideArray[i];
	          }
	          ColorModel colorModel = ColorModel.getRGBdefault();
	          targetPixArray = new int[w*h];
	          for(int i=0;i<h*w;i++){
	        	  int tr,tg,tb;
	        	  tr = (int)((colorModel.getRed(currentPixArray[i])-A_r)/Max(guideArray[i],0.1)+A_r);
	        	  tg = (int)((colorModel.getGreen(currentPixArray[i])-A_g)/Max(guideArray[i],0.1)+A_g);
	        	  tb = (int)((colorModel.getBlue(currentPixArray[i])-A_b)/Max(guideArray[i],0.1)+A_b);
	        	  tr = tr>255?255:tr;tr = tr<0?0:tr;
	        	  tg = tg>255?255:tg;tg = tg<0?0:tg;
	        	  tb = tb>255?255:tb;tb = tb<0?0:tb;
	        	  targetPixArray[i] = (255 << 24) | (tr << 16) | (tg << 8) | tb;
	          }
	          currentpic = createImage(new MemoryImageSource(w, h, targetPixArray, 0, w));
	          currentImage = toBufferedImage(currentpic);
	          showImage(targetPixArray);
	          
	      }
	  }
  
    /////get dark channel in estimate //////
   private int[] getDarkchannel0(int[] current){
	  //the size of filter window 
	  int f_size = 15;
    
      ColorModel colorModel = ColorModel.getRGBdefault();
    
       enlarge_array = new int[(w+f_size)*(h+f_size)];
    
     //edge process
    for(int i=0;i<f_size/2;i++)
  	  for(int j=0;j<w+f_size-1;j++){
  		  enlarge_array[i*(w+f_size-1)+j] = 255;
  	  }
    
    for(int i=h+f_size/2;i<h+f_size-1;i++)
  	  for(int j=0;j<w+f_size-1;j++){
  		  enlarge_array[i*(w+f_size-1)+j] = 255;
  	  }
    
    for(int i=f_size/2;i<f_size/2+h;i++)
  	  for(int j=0;j<f_size/2;j++){
  		  enlarge_array[i*(w+f_size-1)+j] = 255;
  	  }
    
    for(int i=f_size/2;i<f_size/2+h;i++)
  	  for(int j=w+f_size/2;j<w+f_size-1;j++){
  		  enlarge_array[i*(w+f_size-1)+j] = 255;
  	  }
    
    for(int i=f_size/2;i<f_size/2+h;i++)
  	  for(int j=f_size/2;j<w+f_size/2;j++){
  		  int M=(i-f_size/2)*w+(j-f_size/2);
  		  enlarge_array[i*(w+f_size-1)+j] = Min(colorModel.getRed(current[M]),colorModel.getGreen(current[M]),colorModel.getBlue(current[M]));
  	  }
    
    
    
    for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
      	int k=0;
      	int temp[] = new int[225];
        	for(int x=i;x<i+f_size;x++)
        		for(int y=j;y<j+f_size;y++){
        			temp[k]=enlarge_array[x*(w+f_size-1)+y];
        			k++;
        		}
        	Arrays.sort(temp);
        	int gray = temp[0];
        	current[i*w+j] = (255 << 24) | (gray << 16) | (gray << 8) | gray;
        }
    }
    
    return current;
  }
  
  /////get dark channel min value//////
  private int[] getDarkchannel(int[] current){
	  int f_size = 15;
      
      ColorModel colorModel = ColorModel.getRGBdefault();
      
      enlarge_array = new int[(w+f_size)*(h+f_size)];
      
      for(int i=0;i<f_size/2;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_array[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=h+f_size/2;i<h+f_size-1;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_array[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=0;j<f_size/2;j++){
    		  enlarge_array[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=w+f_size/2;j<w+f_size-1;j++){
    		  enlarge_array[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=f_size/2;j<w+f_size/2;j++){
    		  int M=(i-f_size/2)*w+(j-f_size/2);
    		  enlarge_array[i*(w+f_size-1)+j] = Min(colorModel.getRed(current[M]),colorModel.getGreen(current[M]),colorModel.getBlue(current[M]));
    	  }
      
      
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	int k=0;
        	int temp[] = new int[f_size*f_size];
          	for(int x=i;x<i+f_size;x++)
          		for(int y=j;y<j+f_size;y++){
          			temp[k]=enlarge_array[x*(w+f_size-1)+y];
          			k++;
          		}
          	Arrays.sort(temp);
          	int gray = temp[0];
          	current[i*w+j] = (255 << 24) | (gray << 16) | (gray << 8) | gray;
          }
      }
      
      return current;
  }
  
  ////////get max value///
  private double Max(double a,double b){
	  return a>b?a:b;
  }
  /////get Min value//////
  private int Min(int a,int b,int c){
	  if(a<b)
		  return a<c?a:c;
	  else
          return b<c?b:c;
  }
  
  private double Min(double a,double b,double c){
	  if(a<b)
		  return a<c?a:c;
	  else
          return b<c?b:c;
  }
  
  ///////////get channel A value//////////
  private void getA(){
	  ColorModel colorModel = ColorModel.getRGBdefault();
	  int M = (w*h)/1000;                 //the number of 0.1% pix
	  int dark[] = new int[w*h];
	  int temp[] = new int[w*h];
	  for(int i=0;i<w*h;i++){
		  dark[i]=temp[i]=colorModel.getRed(darkPixArray[i]);
	  }
	  Arrays.sort(temp);
	  
	  //get 0.1% brightest pix
	  int N = temp[w*h-M];
	  int tempr=0;int tempg=0;int tempb=0;int k=0;
	  for(int i=0;i<w*h;i++){
		  if(dark[i]>=N){
			  tempr+=colorModel.getRed(sourcePixArray[i]);
			  tempg+=colorModel.getGreen(sourcePixArray[i]);
			  tempb+=colorModel.getBlue(sourcePixArray[i]);
			  k++;
		  }
	  }
	  //get A
	  A_r=(double)(tempr/k)>220?220:(double)(tempr/k);
	  A_g=(double)(tempg/k)>220?220:(double)(tempg/k);
	  A_b=(double)(tempb/k)>220?220:(double)(tempb/k);
  }
  
  //Get gray guide image I
  private double[] getgray_I(int[] ImageSource){
	  double[] grayArray = new double[h * w];
      ColorModel colorModel = ColorModel.getRGBdefault();
      int i, j, k, r, g, b;
      for (i = 0; i < h; i++) {
          for (j = 0; j < w; j++) {
              k = i * w + j;
              r = colorModel.getRed(ImageSource[k]);
              g = colorModel.getGreen(ImageSource[k]);
              b = colorModel.getBlue(ImageSource[k]);
              double gray = (double) (r * 0.3 + g * 0.59 + b * 0.11);
       
              grayArray[i * w + j] = (double) gray;
          }
      }
      return grayArray;
  }
  //////////////soft matting get t(x) by guide filtering///////////////////
  private double[] gettx_better(double[] I,double[] p){
	  //int r = 56;
	  int f_size = 113;
      double[] t_x = new double[h*w];
      
      
      double[] mean_I = new double[h*w];
      double[] mean_p = new double[h*w];
      
      double[] corr_I = new double[h*w];
      double[] corr_Ip = new double[h*w];
      
      double[] var_I = new double[h*w];
      double[] cov_Ip = new double[h*w];
      
      double[] a = new double[h*w];
      double[] b = new double[h*w];
      
      double[] mean_a = new double[h*w];
      double[] mean_b = new double[h*w];
      
      
      //edge process
      double[] enlarge_I = new double[(w+f_size)*(h+f_size)];
      double[] enlarge_p = new double[(w+f_size)*(h+f_size)];
      double[] enlarge_II =new double[(w+f_size)*(h+f_size)];
      double[] enlarge_Ip =new double[(w+f_size)*(h+f_size)];
      double[] enlarge_a =new double[(w+f_size)*(h+f_size)];
      double[] enlarge_b =new double[(w+f_size)*(h+f_size)];
      
      for(int i=0;i<f_size/2;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_I[i*(w+f_size-1)+j] = 0;
    		  enlarge_p[i*(w+f_size-1)+j] = 0;
    		  enlarge_II[i*(w+f_size-1)+j] = 0;
    		  enlarge_Ip[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=h+f_size/2;i<h+f_size-1;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_I[i*(w+f_size-1)+j] = 0;
    		  enlarge_p[i*(w+f_size-1)+j] = 0;
    		  enlarge_II[i*(w+f_size-1)+j] = 0;
    		  enlarge_Ip[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=0;j<f_size/2;j++){
    		  enlarge_I[i*(w+f_size-1)+j] = 0;
    		  enlarge_p[i*(w+f_size-1)+j] = 0;
    		  enlarge_II[i*(w+f_size-1)+j] = 0;
    		  enlarge_Ip[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=w+f_size/2;j<w+f_size-1;j++){
    		  enlarge_I[i*(w+f_size-1)+j] = 0;
    		  enlarge_p[i*(w+f_size-1)+j] = 0;
    		  enlarge_II[i*(w+f_size-1)+j] = 0;
    		  enlarge_Ip[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=f_size/2;j<w+f_size/2;j++){
    		  int M=(i-f_size/2)*w+(j-f_size/2);
    		  enlarge_I[i*(w+f_size-1)+j] = I[M];
    		  enlarge_p[i*(w+f_size-1)+j] = p[M];
    		  enlarge_II[i*(w+f_size-1)+j] = I[M]*I[M];
    		  enlarge_Ip[i*(w+f_size-1)+j] = I[M]*p[M];
    	  }   
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	double fi = 0;
        	double fp = 0;
        	double fii = 0;
        	double fip = 0;
        	double total = (double)(f_size*f_size);
          	for(int x=i;x<i+f_size;x++)
          		for(int y=j;y<j+f_size;y++){
          			fi  = fi + enlarge_I[x*(w+f_size-1)+y];
          			fp  = fp + enlarge_p[x*(w+f_size-1)+y];
          			fii = fii+enlarge_II[x*(w+f_size-1)+y];
          			fip = fip+enlarge_Ip[x*(w+f_size-1)+y];
          			
          			//Ignore the pix of edge process
          			if(enlarge_I[x*(w+f_size-1)+y]==0)
          				total--;
          		}
          	
          	double gray_i = fi/total;
          	double gray_p = fp/total;
          	double gray_ii = fii/total;
          	double gray_ip = fip/total;
          	mean_I[i*w+j]= gray_i;
          	mean_p[i*w+j]= gray_p;
          	corr_I[i*w+j]= gray_ii;
          	corr_Ip[i*w+j]= gray_ip;
          }
      }
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	  var_I[i*w+j] = corr_I[i*w+j]-mean_I[i*w+j]*mean_I[i*w+j];
        	  cov_Ip[i*w+j] = corr_Ip[i*w+j]-mean_I[i*w+j]*mean_p[i*w+j];
          }
      }
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	  a[i*w+j] = cov_Ip[i*w+j]/(var_I[i*w+j]+0.001);
        	  b[i*w+j] = mean_p[i*w+j]-a[i*w+j]*mean_I[i*w+j];
          }
      }
      
      for(int i=0;i<f_size/2;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_a[i*(w+f_size-1)+j] = 0;
    		  enlarge_b[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=h+f_size/2;i<h+f_size-1;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge_a[i*(w+f_size-1)+j] = 0;
    		  enlarge_b[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=0;j<f_size/2;j++){
    		  enlarge_a[i*(w+f_size-1)+j] = 0;
    		  enlarge_b[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=w+f_size/2;j<w+f_size-1;j++){
    		  enlarge_a[i*(w+f_size-1)+j] = 0;
    		  enlarge_b[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=f_size/2;j<w+f_size/2;j++){
    		  int M=(i-f_size/2)*w+(j-f_size/2);
    		  enlarge_a[i*(w+f_size-1)+j] = a[M];
    		  enlarge_b[i*(w+f_size-1)+j] = b[M];
    	  }   
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	double fa = 0;
        	double fb = 0;
        	double total = (double)(f_size*f_size);
          	for(int x=i;x<i+f_size;x++)
          		for(int y=j;y<j+f_size;y++){
          			fa  = fa + enlarge_a[x*(w+f_size-1)+y];
          			fb  = fb + enlarge_b[x*(w+f_size-1)+y];
          			
          		    //Ignore the pix of edge process
          			if(enlarge_a[x*(w+f_size-1)+y]==0)
          				total--;
          		}
          	
          	double gray_a = fa/total;
          	double gray_b = fb/total;

          	mean_a[i*w+j]= gray_a;
          	mean_b[i*w+j]= gray_b;

          }
      }
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	  t_x[i*w+j] = mean_a[i*w+j]*I[i*w+j]+mean_b[i*w+j];
          }
      }
      
      return t_x;
  }
  
  ////Estimating the transmission map to get t(x)////////////
  private double[] gettx(int[] current){
      int f_size = 15;
      double[] a = new double[h*w];
      ColorModel colorModel = ColorModel.getRGBdefault();
      
      enlarge = new double[(w+f_size)*(h+f_size)];
      
      for(int i=0;i<f_size/2;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=h+f_size/2;i<h+f_size-1;i++)
    	  for(int j=0;j<w+f_size-1;j++){
    		  enlarge[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=0;j<f_size/2;j++){
    		  enlarge[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=w+f_size/2;j<w+f_size-1;j++){
    		  enlarge[i*(w+f_size-1)+j] = 0;
    	  }
      
      for(int i=f_size/2;i<f_size/2+h;i++)
    	  for(int j=f_size/2;j<w+f_size/2;j++){
    		  int M=(i-f_size/2)*w+(j-f_size/2);
    		  enlarge[i*(w+f_size-1)+j] = Min(colorModel.getRed(current[M])/A_r,colorModel.getGreen(current[M])/A_g,colorModel.getBlue(current[M])/A_b);
    	  }
      
      
      
      for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
        	int k=0;
        	double temp[] = new double[225];
          	for(int x=i;x<i+f_size;x++)
          		for(int y=j;y<j+f_size;y++){
          			temp[k]=enlarge[x*(w+f_size-1)+y];
          			k++;
          		}
          	Arrays.sort(temp);
          	double g = temp[0];
          	a[i*w+j] = 1-0.95*g;
          }
      }
      
      return a;
  }

  // ////////////////获取图像像素矩阵/////////
  private int[] getPixArray(Image im, int w, int h) {
      int[] pix = new int[w * h];
      PixelGrabber pg = null;
      try {
          pg = new PixelGrabber(im, 0, 0, w, h, pix, 0, w);
          if (pg.grabPixels() != true)
              try {
                  throw new java.awt.AWTException("pg error" + pg.status());
              } catch (Exception eq) {
                  eq.printStackTrace();
              }
      } catch (Exception ex) {
          ex.printStackTrace();

      }
      return pix;
  }
  
  // ////////////////转换成BufferedImage/////////////
  public static BufferedImage toBufferedImage(Image image){
  	if (image instanceof BufferedImage){
  		return (BufferedImage)image;
  	}
  	image = new ImageIcon(image).getImage();
  	BufferedImage bimage = null;
  	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  	try{
  		int transparency = Transparency.OPAQUE;
  		GraphicsDevice gs = ge.getDefaultScreenDevice();
  		GraphicsConfiguration gc = gs.getDefaultConfiguration();
  		bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null),transparency);
  	}catch(HeadlessException e){
  		//
  	}
  	if (bimage == null){
  		int type = BufferedImage.TYPE_INT_RGB;
  		bimage = new BufferedImage(image.getWidth(null),image.getHeight(null),type);
  	}
  	
  	Graphics g = bimage.createGraphics();
  	
  	g.drawImage(image,0,0,null);
  	g.dispose();
  	
  	return bimage;
  }

  // ////////////////显示图片///////////
  private void showImage(int[] srcPixArray) {
      Image pic = createImage(new MemoryImageSource(w, h, srcPixArray, 0, w));
      ImageIcon ic = new ImageIcon(pic);
      imageLabel.setIcon(ic);
      imageLabel.repaint();
  }
  

  public static void main(String[] args) {
      new proj("图像去雾处理器");
  }

}



