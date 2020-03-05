import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.*;
import ij.process.*;
import java.awt.*;
import ij.plugin.PlugIn.*;
import ij.process.ImageConverter;
import ij.gui.*;
import java.awt.Label;
import java.util.*;
import ij.measure.*;

public class ColorTypeSeparator_ implements PlugInFilter {

	public int setup(String arg, ImagePlus imp) {
		if (IJ.versionLessThan("1.37j"))
			return DONE;
		else
			return DOES_ALL+DOES_STACKS+SUPPORTS_MASKING;
	}

	public void run(ImageProcessor ip) {

		if (IJ.getImage().getBitDepth()!=24){IJ.showMessage("RGB Measure", "RGB Image required");}

		ImagePlus imp = IJ.getImage();
		Calibration cal = imp.getCalibration();

		double redchoice = 0;
		double greenchoice = 0;
		double bluechoice = 0;
		double tolerance = 0;
		double colorsum = 0;
		double originalpix = 0;
		double choicepix = 0;
		double remainpix = 0;
		double originalval = 0;
		double choiceval = 0;
		double remainval = 0;

		int[] pixelarray = new int[3];
		int redpixel = 0;
		int greenpixel = 0;
		int bluepixel = 0;
		int count = 0;

		int w = imp.getWidth();
		int h = imp.getHeight();
		double pixres = cal.pixelWidth;

		ImagePlus imp1a = imp.duplicate();
		imp1a.setCalibration(imp.getCalibration());
		ImageStack rgbStack1a = imp1a.getStack();
		ColorProcessor farbe;
		farbe = (ColorProcessor)rgbStack1a.getProcessor(1);
		int w1 = imp1a.getWidth();
		int h1 = imp1a.getHeight();
		for (int y=0; y<h1;y++)
		  for (int x=0; x<w1;x++){
		    farbe.getPixel(x,y,pixelarray);
		    redpixel = pixelarray[0];
		    redchoice = redchoice + redpixel;
		    greenpixel = pixelarray[1];
		    greenchoice = greenchoice + greenpixel;
		    bluepixel = pixelarray[2];
		    bluechoice = bluechoice + bluepixel;
		    count++;
		  }
		redchoice = redchoice/count;
		greenchoice = greenchoice/count;
		bluechoice = bluechoice/count;

		GenericDialog gd = new GenericDialog("Color Choice");
		gd.addNumericField("Red:",redchoice,3);
		gd.addNumericField("Green:",greenchoice,3);
		gd.addNumericField("Blue:",bluechoice,3);
		gd.addNumericField("Tolerance (%):",10.0,3);


		gd.showDialog();
		redchoice = gd.getNextNumber();
		greenchoice = gd.getNextNumber();
		bluechoice = gd.getNextNumber();
		tolerance = gd.getNextNumber();
		colorsum = redchoice + greenchoice + bluechoice;
		if (gd.wasCanceled()) {
		  IJ.error("PlugIn canceled!");
		  return;
		}
		imp.setRoi(0,0,w,h);
		ImagePlus imp2 = imp.duplicate();
		ImageStack rgbStack = imp2.getStack();
		ImageStack redStack = new ImageStack(w,h);
		ImageStack greenStack = new ImageStack(w,h);
		ImageStack blueStack = new ImageStack(w,h);

		byte[] r,g,b;
		ColorProcessor cp;
		int n = rgbStack.getSize();
		for (int i=1; i<=n; i++) {

		  r = new byte[w*h];
		  g = new byte[w*h];
		  b = new byte[w*h];
		  cp = (ColorProcessor)rgbStack.getProcessor(1);
		  cp.getRGB(r,g,b);
		  rgbStack.deleteSlice(1);
		  redStack.addSlice(null,r);
		  greenStack.addSlice(null,g);
		  blueStack.addSlice(null,b);

		  }

		String title = imp.getTitle();

		ImageProcessor redip = redStack.getProcessor(1);
		ImageProcessor blueip = blueStack.getProcessor(1);
		ImageProcessor greenip = greenStack.getProcessor(1);//

		ImageProcessor red = redip.duplicate();
		ImageProcessor green = greenip.duplicate();
		ImageProcessor blue = blueip.duplicate();
		ImageProcessor red2 = redip.duplicate();
		ImageProcessor green2 = greenip.duplicate();
		ImageProcessor blue2 = blueip.duplicate();

		for (int y=0; y<h;y++)
		  for (int x=0; x<w;x++){
		    double sum = (redip.get(x,y)+greenip.get(x,y)+blueip.get(x,y));
		    double redpart = (redip.get(x,y)/sum*100);
		    double greenpart = (greenip.get(x,y)/sum*100);
		    double bluepart = (blueip.get(x,y)/sum*100);
		    if ((redpart > ((redchoice/colorsum*100)-tolerance) )&&(redpart<((redchoice/colorsum*100)+tolerance))&&(greenpart>((greenchoice/colorsum*100)-tolerance))&&(greenpart<((greenchoice/colorsum*100)+tolerance))&&(bluepart>((bluechoice/colorsum*100)-tolerance))&&(bluepart<((bluechoice/colorsum*100)+tolerance))){
		      red.putPixel(x,y,redip.get(x,y));
		      red2.putPixel(x,y,0);
		      green.putPixel(x,y,greenip.get(x,y));
		      green2.putPixel(x,y,0);
		      blue.putPixel(x,y,blueip.get(x,y));
		      blue2.putPixel(x,y,0);
		      choicepix++;
		      }
		     else if ((redpart > ((redchoice/colorsum*100)+tolerance))||(redpart<((redchoice/colorsum*100)-tolerance))||(greenpart>((greenchoice/colorsum*100)+tolerance))||(greenpart<((greenchoice/colorsum*100)-tolerance))||(bluepart>((bluechoice/colorsum*100)+tolerance))||(bluepart<((bluechoice/colorsum*100)-tolerance))){
		      red.putPixel(x,y,0);
		      red2.putPixel(x,y,redip.get(x,y));
		      green.putPixel(x,y,0);
		      green2.putPixel(x,y,greenip.get(x,y));
		      blue.putPixel(x,y,0);
		      blue2.putPixel(x,y,blueip.get(x,y));
		      remainpix++;
		      }
		      }

		ImageStack rgbStack1 = new ImageStack(w,h);
		rgbStack1.addSlice(red);
		rgbStack1.addSlice(green);
		rgbStack1.addSlice(blue);
		ImagePlus imp1 = new ImagePlus("Choice_"+title,rgbStack1);
		ImageConverter ic1 = new ImageConverter(imp1);
		ic1.convertRGBStackToRGB();
		imp1.getStack().getProcessor(1);
		imp1.setCalibration(imp.getCalibration());
		imp1.show();

		ImageStack rgbStack2 = new ImageStack(w,h);
		rgbStack2.addSlice(red2);
		rgbStack2.addSlice(green2);
		rgbStack2.addSlice(blue2);
		ImagePlus imp3 = new ImagePlus("Remaining_"+title,rgbStack2);
		ImageConverter ic2 = new ImageConverter(imp3);
		ic2.convertRGBStackToRGB();
		imp3.getStack().getProcessor(1);
		imp3.setCalibration(imp.getCalibration());
		imp3.show();
		originalval = (w*h)*pixres;
		choiceval = choicepix*pixres;
		remainval = remainpix*pixres;
		IJ.log("All: "+originalval+" Choice area:  "+choiceval+" Remaining area: "+remainval);
	}


}
