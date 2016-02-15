package transformations;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;

import mail.Mail;
import org.apache.log4j.Logger;

/**
 * A Transformer to Leetify a Text in a Mail
 */
public class Leetifier implements Transformer {

    /**
     * Indicates if the transformer class is enabled
     */
    public static AtomicBoolean enabled = new AtomicBoolean(false);

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Leetifier.class);

    /**
     * Leetify a Text
     * @param mail
     * @throws IOException
     */
	public void transform(Mail mail) throws IOException {
        LOGGER.info("Leetifying text");
		File file = new File("mails/mail" + mail.id() + "T.txt");
		file.createNewFile();
		File file2 = new File("mails/mail" + mail.id() + ".txt");
		RandomAccessFile reader = new RandomAccessFile("mails/mail" + mail.id()
				+ ".txt", "r");
		RandomAccessFile writer = new RandomAccessFile("mails/mail" + mail.id()
				+ "T.txt", "rw");
		String line;
		int i = 1;
		writer.write((reader.readLine() + "\r\n").getBytes());
		int bodyEnd = mail.getBodyEnd();
		int bodyBeg = mail.getBodyIndex();
		int htmlEnd = mail.getHtmlEnd();
		int htmlBeg = mail.getHtmlBeg();
		while ((line = reader.readLine()) != null) {
			if (i == bodyBeg) {
				while (i < bodyEnd) {
					if (line == null) {
						file2.delete();
						file.renameTo(file2);
						reader.close();
						writer.close();
						return;
					}
					writer.write((leet(line) + "\r\n").getBytes());
					i++;
					line = reader.readLine();
				}
			}
			if (i == htmlBeg) {
				while (i < htmlEnd) {
					if (line == null) {
						file2.delete();
						file.renameTo(file2);
						reader.close();
						writer.close();
						return;
					}
					writer.write((leet(line) + "\r\n").getBytes());
					i++;
					line = reader.readLine();
				}
			}
			i++;
			if (line != null) {
				writer.write((line + "\r\n").getBytes());
			}
		}
		file2.delete();
		file.renameTo(file2);
		reader.close();
		writer.close();
	}

    /**
     * Leetify a String
     * @param line The String to leetify
     * @return The leetified String
     */
	private String leet(String line) {
		boolean inTag = false;
		char[] c = line.toCharArray();
		for (int i = 0; i < c.length; i++) {
			switch (c[i]) {
			case '<':
				inTag = true;
				break;
			case '>':
				inTag = false;
				break;
			case 'a':
			case 'A':
				if (!inTag)
					c[i] = '4';
				break;
			case 'e':
			case 'E':
				if (!inTag)
					c[i] = '3';
				break;
			case 'i':
			case 'I':
				if (!inTag)
					c[i] = '1';
				break;
			case 'o':
			case 'O':
				if (!inTag)
					c[i] = '0';
				break;
			case 'c':
			case 'C':
				if (!inTag)
					c[i] = '<';
			}
		}
		return new String(c);

	}

	@Override
	public String toString() {
		return "Leetifier";
	}
}