package transformations;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import mail.Image;
import mail.Mail;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * A Transformer to Rotate an Image in a Mail
 */
public class Rotation implements Transformer {

    /**
     * Indicates if the transformer class is enabled
     */
    public static AtomicBoolean enabled = new AtomicBoolean(false);

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Rotation.class);

	/** Rotate an Image
	 * @param mail the image to convert in String base64 format
	 * @throws IOException 
	 */
	public void transform(Mail mail) throws FileNotFoundException, IOException {
		int cant = mail.getImages().size();
		LOGGER.info("Images to rotate: " + cant);
		if(cant==0){
			return;
		}

		File file = new File("./mails/mail"+mail.id()+"T.txt");
		file.createNewFile();
		File file2 = new File("./mails/mail"+mail.id()+".txt");
		RandomAccessFile reader = new RandomAccessFile("./mails/mail"+mail.id()+".txt", "r");
		RandomAccessFile writer = new RandomAccessFile("./mails/mail"+mail.id()+"T.txt", "rw");
		String line="";
		int i = 0;
		Iterator<Image> iter = mail.getImages().iterator();
		while(iter.hasNext()){
			Image image = iter.next();
			int beg = image.getStartLine();
			int end = image.getEndLine();
			while( i<beg-1 && (line=reader.readLine())!=null){
				writer.write((line+"\r\n").getBytes());
				i++;
			}
			StringBuilder sBuilder = new StringBuilder();
			while( i<=end && (line=reader.readLine())!=null){
				sBuilder.append(line);
				i++;
			}
			String rotated = imageRotation(sBuilder.toString());
			writer.write((rotated+"\r\n").getBytes());
			writer.write((line+"\n\r").getBytes());
		}
		while((line=reader.readLine())!=null){
			writer.write((line+"\r\n").getBytes());
		}
		file2.delete();
		file.renameTo(file2);
		reader.close();
		writer.close();
	}

	public String imageRotation(String image) {

		byte[] b  = decodeBase64(image);

		ByteArrayInputStream in = new ByteArrayInputStream(b);

		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BufferedImage outputImg =rotateImage(img,180);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write( outputImg, "jpg", bos);
		} catch (IOException e) {
			e.printStackTrace();
			return image;
		}
		try {
			bos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return image;
		}
		try {
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return image;
		}

		byte[] o = bos.toByteArray();

		return encodeBase64(o);

	}

    /**
     * Decode a String in Base64
     * @param s The String to decode
     * @return The Decoded String as a byte array
     */
	public byte[] decodeBase64(String s) {
		return Base64.decodeBase64(s);
	}

    /**
     * Encode a String in Base64
     * @param b The byte array to encode
     * @return The Encoded byte array as a String
     */
	public String encodeBase64(byte[] b) {
		return Base64.encodeBase64String(b);
	}

    /**
     * Rotate an image
     * @param image The Image to rotate
     * @param angle The angle to rotate it at
     * @return The rotated Image
     */
	public static BufferedImage rotateImage(BufferedImage image, double angle) {
		AffineTransform tx = new AffineTransform();

		tx.translate(image.getWidth()/2, image.getHeight()/2);

		tx.rotate(Math.PI); // 1 radians (180 degrees)

		// first - center image at the origin so rotate works OK
		tx.translate(-image.getWidth()/2,-image.getHeight()/2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		LOGGER.info("Height: " + image.getHeight() + " width: " + image.getWidth());
		BufferedImage outputImage =new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		return op.filter(image, outputImage);
	}

	@Override
	public String toString() {
		return "Image Rotation Transformer";
	}
}
