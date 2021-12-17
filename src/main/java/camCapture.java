import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import streamql.*;
import streamql.algo.Algo;
import streamql.algo.Sink;
import streamql.query.Q;


class pixel{
	public int a, r, g, b;
	public int index;
	
	public pixel(int a, int r, int g, int b, int in) {
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
		this.index = in;
	}
	
	@Override
	public String toString() {
		return "r-"+r+" g-"+g+" b-"+b+"\n";
	}
	
	public void edit(pixelIterator stream) {
		stream.data[index] = (byte) 255;
	}
}

class pixelSink<A> extends Sink<pixel>{
	public pixelIterator stream;

	@Override
	public void end() {
		System.out.print("Job done");
	}

	@Override
	public void next(pixel arg0) {
//		System.out.print(arg0);
		arg0.edit(stream);
		webcamProcessor.present = true;
	}
}

class pixelIterator implements Iterator<pixel>{
	protected byte[] data; 
    protected int i = 0;
    public static byte transValue = 0;
    
    pixelIterator(){
    	this.data = new byte[0];
    }
    
    pixelIterator(byte[] a) {
        this.data = a; 
    }
    
	@Override
	public boolean hasNext() {
		return i < data.length;
	}

	@Override
	public pixel next() {
		if (i == data.length) {
            throw new NoSuchElementException();
        }
		pixel p = new pixel(data[i],data[i+1],data[i+2],data[i+3], i);
		
		// Set transparency to full
		data[i] = transValue;
		i+=4;
		
		return p;
	}
	
	public void reset() {
		data = new byte[0];
		i = 0;
	}
	
	protected void setData(byte[] in) {
		this.reset();
		data = in;
	}
	
	@Override
	public String toString() {
		String ret = "";
		for(int a: this.data) {
			ret += a;
		}
		return ret;
	}
}

class webcamProcessor{
	protected final Webcam webcam = Webcam.getDefault();
	public static boolean present = false;
	
	public webcamProcessor() {
		this.webcam.setViewSize(new Dimension(640, 480));
	}
	
	public void take(pixelIterator stream, Algo<pixel, pixel> al, JLabel imagePanel, JFrame housing) throws IOException{
		BufferedImage img = getBufferedImage();
		ByteBuffer bBuffer;
		byte[] bArray;
		
		this.webcam.open();
		
		while(true) {
			try {
				present = false;
				// Get new image data and convert them into a byte[] instead of a ByteBuffer object
				bBuffer = this.webcam.getImageBytes();
				bArray = new byte[bBuffer.remaining()]; bBuffer.get(bArray);
				
				// Set data stream to current byte[]
				stream.setData(toARGB(bArray));
				
				// While we have unparsed data, try to find the pixel color we have specified (100, 100, 100 by default)
				while(stream.hasNext()) {
					pixel p = stream.next();
					al.next(p);
				}
				
				// Convert stream.data (byte[]) -> int[] 
				int[] bytesAsInts = convert(stream.data);
				
				// Things I've tried for getting an image to show in a panel...
				img.setRGB(0, 0, this.webcam.getViewSize().width, this.webcam.getViewSize().height, bytesAsInts, 0, this.webcam.getViewSize().width);
				
				imagePanel.setIcon(new ImageIcon(img));
				imagePanel.revalidate();
				imagePanel.repaint();
				
				// Once we have process all of the previous image, repeat
				
			}catch(Exception e) {}
			if(!present) {
				System.out.println("Color not detected.");
			}
		}
	}
	
	private BufferedImage getBufferedImage() throws IOException {
		return new BufferedImage(this.webcam.getViewSize().width, this.webcam.getViewSize().height, BufferedImage.TYPE_INT_ARGB);
	}
	
	// Via https://stackoverflow.com/questions/11437203/how-to-convert-a-byte-array-to-an-int-array
	private int[] convert(byte[] buf) {
	   int intArr[] = new int[buf.length/4]; // Save RGB as ARGB by saving them as ints
	   int offset = 0;
	   for(int i = 0; i < intArr.length; i++) {
		   				// A = 255			// R = Value of R				// G = Value of G					// B = Value of B
		   intArr[i] = ((buf[offset] & 0xFF) << 24 | ((buf[offset + 1] & 0xFF) << 16) | ((buf[offset + 2] & 0xFF) << 8) | (buf[offset + 3] & 0xFF));
		   offset += 4;
	   }
	   return intArr;
	}
	
	private byte[] toARGB(byte[] input) {
		byte[] ret = new byte[(input.length/3)*4];
		int iRet = 0;
		for(int i = 0; i < input.length; i+=3) {
			ret[iRet++] = (byte) 255;
			ret[iRet++] = input[i];
			ret[iRet++] = input[i+1];
			ret[iRet++] = input[i+2];
		}
		
		return ret;
	}
}

class settingsFrame{
	JFrame frame = new JFrame();
	JPanel display = new JPanel(new BorderLayout());
	JSlider rSlider = new JSlider(-255, 255, camCapture.valueR);
	JSlider gSlider = new JSlider(-255, 255, camCapture.valueG);
	JSlider bSlider = new JSlider(-255, 255, camCapture.valueB);
	JSlider aSlider = new JSlider(0, 255, pixelIterator.transValue);
	JSlider rangeSlider = new JSlider(0, 126, camCapture.rangeValue);
	
	public settingsFrame() {
		frame.add(display);
		frame.setSize(300,200);
		display.setSize(250,125);
		frame.setLocation(600, 100);
		frame.setVisible(true);

		rSlider.setOrientation(1);
		gSlider.setOrientation(1);
		bSlider.setOrientation(1);
		aSlider.setOrientation(0);
		rangeSlider.setOrientation(0);

		rSlider.setToolTipText("Intensity of reds");
		gSlider.setToolTipText("Intensity of greens");
		bSlider.setToolTipText("Intensity of blues");
		aSlider.setToolTipText("Transparency of bad pixels");
		rangeSlider.setToolTipText("Range of valid pixels from selected value");
		
		rSlider.addChangeListener(new ChangeListener(){
		      	public void stateChanged(ChangeEvent event) {
		      		camCapture.valueR = rSlider.getValue();
		      	}
		      });
		
		gSlider.addChangeListener(new ChangeListener(){
		      	public void stateChanged(ChangeEvent event) {
		      		camCapture.valueG = gSlider.getValue();
		      	}
		      });
		
		bSlider.addChangeListener(new ChangeListener(){
		      	public void stateChanged(ChangeEvent event) {
		      		camCapture.valueB = bSlider.getValue();
		      	}
		      });
		
		aSlider.addChangeListener(new ChangeListener(){
		      	public void stateChanged(ChangeEvent event) {
		      		pixelIterator.transValue = (byte) aSlider.getValue();
		      	}
		      });
		
		rangeSlider.addChangeListener(new ChangeListener(){
		      	public void stateChanged(ChangeEvent event) {
		      		camCapture.rangeValue = (byte) rangeSlider.getValue();
		      	}
		      });

		display.add(rSlider, BorderLayout.WEST);
		display.add(gSlider, BorderLayout.CENTER);
		display.add(bSlider, BorderLayout.EAST);
		display.add(aSlider, BorderLayout.SOUTH);
		display.add(rangeSlider, BorderLayout.NORTH);
		
		frame.revalidate();
	}
}

public class camCapture {
	public static int valueR = 100;
	public static int valueG = 100;
	public static int valueB = 100;
	public static int rangeValue = 40;
	
	public static void main(String[] args) throws IOException {
		webcamProcessor webcam = new webcamProcessor();
		new settingsFrame();
		if (webcam.webcam != null) {
			System.out.println("Webcam: " + webcam.webcam.getName());
			webcam.webcam.setViewSize(WebcamResolution.VGA.getSize());
			
			// Source of data for processing
			pixelIterator stream = new pixelIterator();
			
			// Visual stuff
			JLabel panel2 = new JLabel(new ImageIcon());
			panel2.setSize(640, 480);
			panel2.setVisible(true);

			JFrame window = new JFrame("Test webcam panel");
			window.add(panel2);
			window.setResizable(true);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.pack();
			window.setSize(640, 480);
			window.setVisible(true);
			// End visual stuff
			
//			// Process initial frame
//			
//			// Grab initial frame
//			ByteBuffer ibb = webcam.webcam.getImageBytes();
//			byte[] iba= new byte[ibb.remaining()]; ibb.get(iba);
//			stream.setData(iba);
//			
//			// While we have unparsed data, try to find the pixel color we have specified (44, 44, 44 by default)
//			while(stream.hasNext()) {
//				valueR += Math.abs(stream.next().r);
//			}
//			while(stream.hasNext()) {
//				valueG += Math.abs(stream.next().g);
//			}
//			stream.i = 0;
//			while(stream.hasNext()) {
//				valueB += Math.abs(stream.next().b);
//			}
//			valueR /= (stream.data.length/4);
//			valueG /= (stream.data.length/4);
//			valueB /= (stream.data.length/4);
//
//			System.out.println("iR: " + valueR);
//			System.out.println("iG: " + valueG);
//			System.out.println("iB: " + valueB);
//			// End process initial frame
			
			// Deals with excess crap
			pixelSink<pixel> ps = new pixelSink<pixel>();
			
			// Processes data
			Q<pixel, pixel> t = QL.filter(x -> 	x.r >= valueR-rangeValue && x.r <= valueR+rangeValue && 
												x.g >= valueG-rangeValue && x.g <= valueG+rangeValue &&
												x.b >= valueB-rangeValue && x.b <= valueB+rangeValue);
			// Interface to deal with connecting the processor and sink
			Algo<pixel, pixel> px = t.eval();
			
			// Connect sink to processor
			px.connect(ps);
			
			// Link pixel sink to the stream for overriding pixel colors
			ps.stream = stream;
			
			// Start the process
			px.init();
			
			// Begin webcam capture and data parse
			webcam.take(stream, px, panel2, window);
		} else {
			System.out.println("No webcam detected");
		}
	}

}


/*
	Works Cited
	------------
	StreamQL: lk21.web.rice.edu/source/streamql_project/tutorial/tutorial.html, https://dl.acm.org/doi/pdf/10.1145/3428251
	Webcam access: https://github.com/sarxos/webcam-capture
	webcamProcessor influenced by: https://itqna.net/questions/52284/capture-webcam-image-and-save-every-1-second
		- Was initially a timer-based camera capture
	pixelIterator development influenced by: https://stackoverflow.com/questions/15791210/java-iterator-for-primitive-types
	ByteBuffer -> byte[] by: https://stackoverflow.com/questions/28744096/convert-bytebuffer-to-byte-array-java
	byte[] -> BufferedImage by: https://mkyong.com/java/how-to-convert-byte-to-bufferedimage-in-java/
	byte[] -> int[] for aRGB by: https://stackoverflow.com/questions/11437203/how-to-convert-a-byte-array-to-an-int-array
	Colors: https://flaviocopes.com/rgb-color-codes/
*/