# Application Manual

To run the program, make sure you have a webcam attached to your computer via usb or as an embedded camera.

From there, you should only need to run the program as a normal java/maven application. Maven should automatically install necessary libraries for the program, so there shouldn't be any prep here.

Lastly, you can adjust the dials in the **very** simple ui provided. Hovering over each slider tells you what they are. 

To close the application, just close the camera feed like a normal app. If you close the slider UI, you will need to re-launch the program to get it back as it is only provided on startup.

# StreamQL
Documentation:
http://lk21.web.rice.edu/source/streamql_project/tutorial/tutorial.html

Article:
https://dl.acm.org/doi/pdf/10.1145/3428251

## Works Cited
StreamQL: lk21.web.rice.edu/source/streamql_project/tutorial/tutorial.html, https://dl.acm.org/doi/pdf/10.1145/3428251

Webcam access: https://github.com/sarxos/webcam-capture

webcamProcessor influenced by: https://itqna.net/questions/52284/capture-webcam-image-and-save-every-1-second
    
- Was initially a timer-based camera capture, but realized processing shouldn't take too long

pixelIterator development influenced by: https://stackoverflow.com/questions/15791210/java-iterator-for-primitive-types

ByteBuffer -> byte[] by: https://stackoverflow.com/questions/28744096/convert-bytebuffer-to-byte-array-java

byte[] -> BufferedImage by: https://mkyong.com/java/how-to-convert-byte-to-bufferedimage-in-java/

byte[] -> int[] for aRGB by: https://stackoverflow.com/questions/11437203/how-to-convert-a-byte-array-to-an-int-array

Colors: https://flaviocopes.com/rgb-color-codes/