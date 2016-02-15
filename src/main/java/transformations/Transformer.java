package transformations;

import java.io.FileNotFoundException;
import java.io.IOException;

import mail.Mail;

/**
 * An object to apply a Transformation to a Mail
 */
public interface Transformer {
	
	public void transform(Mail mail) throws FileNotFoundException, IOException;

}
