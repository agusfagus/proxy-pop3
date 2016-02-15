package mail;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Mail {

    /**
     * The class Logger
     */
    private static transient Logger LOGGER = Logger.getLogger(Mail.class);

    /**
     * Headers constants used for parsing
     */
	private static final String FROM = "From:", DATE = "Date: ", MULTIPART = "Content-Type: multipart", CONTENTTYPE = "Content-Type: ",
			TEXT = "Content-Type: text/plain", CTE = "Content-Transfer-Encoding: ", PIC = "Content-Type: image",
			CONTENTDISP = "Content-Disposition: ", HTML = "Content-Type: text/html", Q_PRINT = "Content-Transfer-Encoding: quoted-printable";

    /**
     * The Mail serial number
     */
   	private static int serial;

    /**
     * The maximum mails that can be stored to disk
     */
	public static int cantMails = 100;

    /**
     * The Mail id
     */
	private int id;

    /**
     * The Mail sender
     */
	private String from;

    /**
     * Lists of Content Types and Dispositions
     */
	private Set<String> contentTypes = new HashSet<String>(), contentDispositions = new HashSet<String>();

    /**
     * The location of important HTML tags
     */
	private int bodyIndex, bodyEnd, htmlBeg, htmlEnd;

    /**
     * The list of images contained in the Mail
     */
	private List<Image> photos = new ArrayList<Image>();

    /**
     * The files used to dump the Mail to disk
     */
	private RandomAccessFile reader, writer;

    /**
     * A Quoted-Printable character
     */
	private boolean quotedPrint;

    /**
     * A Mail header
     */
	private String header = "";

    /**
     * Create new mail
     * @throws IOException
     */
	public Mail() throws IOException {
        File mailDirectory = new File("mails");
        if(!mailDirectory.exists()) {
            mailDirectory.mkdir();
        }
		id = (serial++) % cantMails;
		File f = new File("mails/mail" +id+".txt");
		f.delete();
		f.createNewFile();
		reader = new RandomAccessFile("mails/mail" +id+".txt", "r");
		writer = new RandomAccessFile("mails/mail" +id+".txt", "rw");
	}

    /**
     * Write a new line to the Mail
     * @param line The line to write
     * @throws IOException
     */
	public void add(String line) throws IOException {
		writer.write((line).getBytes());
	}

    /**
     * Parse the Mail
     * @throws IOException
     */
	public void parse() throws IOException {
		LOGGER.info("Parsing mail...");
		boolean flag = false;
		Set<String> bounds = new HashSet<String>();
		int i = 0;
		writer.close();
		String line;
		while ((line = reader.readLine()) != null) {
			header += line+"\r\n";
			if (line.toLowerCase().startsWith(FROM.toLowerCase())) {
				from = line.split(FROM)[1];
			} else if (line.toLowerCase().startsWith(DATE.toLowerCase())) {
				String[] d2 = line.split(" ");
			} else if (line.toLowerCase().startsWith(CONTENTTYPE.toLowerCase())) {
				contentTypes.add(line.split(CONTENTTYPE)[1]);
				if (line.startsWith(MULTIPART)) {
					String b;
					while (line != null && !line.contains("boundary")) {
						i++;
						header += line+"\r\n";
						line = reader.readLine();
					}
					header += line + "\r\n";
					b = line.split("boundary=")[1];
					if (b.contains("\"")) {
						b = b.split("\"")[1];
					}
					bounds.add(b);
				} else if (line.toLowerCase().startsWith(TEXT.toLowerCase())) {
					while (line != null & !line.equals("")) {
						if (line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())) {
							String disp = line.split(CONTENTDISP)[1];
							disp = disp.split(";")[0];
							contentDispositions.add(disp);
						}
						if (line.toLowerCase().contains(Q_PRINT.toLowerCase())) {
							quotedPrint = true;
						}
						header += line + "\r\n";
						i++;
						line = reader.readLine();
					}
					header += line+"\r\n";
					bodyIndex = ++i;
					line = reader.readLine();
					flag = false;
					while (!flag && line != null && !line.equals("--=20")) {
						for (String b: bounds) {
							if (line.startsWith("--"+b) || line.equals(b)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							line = reader.readLine();
							i++;
						}
					}
					bodyEnd = i;
				} else if (line.toLowerCase().startsWith(PIC.toLowerCase())) {
					i++;
					line = reader.readLine();
					while (line!=null && !line.equals("")) {
						if (line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())) {
							String disp = line.split(CONTENTDISP)[1];
							disp = disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header += line + "\r\n";
						i++;
						line = reader.readLine();
					}
					header += line+"\r\n";
					i++;
					line = reader.readLine();
					Image image = new Image();
					image.setStartLine(i);;
					while (line != null && !line.contains("--") && !line.equals("")) {
						i++;
						line = reader.readLine();
					}
					image.setEndLine(i-1);
					photos.add(image);
				} else if (line.toLowerCase().startsWith(HTML.toLowerCase())) {
					while (line != null && !line.equals("")) {
						if (line.toLowerCase().contains(Q_PRINT.toLowerCase())) {
							quotedPrint=true;
						}
						if (line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())) {
							String disp = line.split(CONTENTDISP)[1];
							disp = disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header += line + "\r\n";
						i++;
						line = reader.readLine();
					}
					header += line + "\r\n";
					htmlBeg = i++;
					flag = false;
					while (!flag && line != null && !line.equals("--=20")) {
						for(String b: bounds) {
							if(line.startsWith("--"+b) || line.equals(b)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							line = reader.readLine();
							i++;
						}
					}
					htmlEnd = i - 1;
				} else {
					while (line != null && !line.equals("")) {
						if (line.toLowerCase().startsWith(CONTENTDISP.toLowerCase())) {
							String disp = line.split(CONTENTDISP)[1];
							disp = disp.split(";")[0];
							contentDispositions.add(disp);
						}
						header += line+"\r\n";
						i++;
						line = reader.readLine();
					}
					while (line != null && line.equals("")) {
						line = reader.readLine();
					}
					boolean bound = false;
					while (line != null && !line.equals("") && !bound) {
						for (String b: bounds) {
							if (line.startsWith("--"+b) || line.equals(b)) {
								bound = true;
								break;
							}
						}
						i++;
						line = reader.readLine();
					}
				}
			}
			i++;
		}
		reader.close();
	}


    /**
     * Get the index of the HTML body tag
     * @return The index of the HTML body tag
     */
	public int getBodyIndex() {
		return bodyIndex;
	}

    /**
     * Get the index of the HTML body closing tag
     * @return The index of the HTML body closing tag
     */
	public int getBodyEnd() {
		return bodyEnd;
	}

    /**
     * Get the index of the HTML tag
     * @return The index of the HTML tag
     */
	public int getHtmlBeg() {
		return htmlBeg;
	}

    /**
     * Get the index of the HTML closing tag
     * @return The index of the HTML closing tag
     */
	public int getHtmlEnd() {
		return htmlEnd;
	}

    /**
     * Get the list of Images contained in the Mail
     * @return The list of Images contained in the Mail
     */
	public List<Image> getImages() {
		return photos;
	}

    /**
     * Get the Mail sender
     * @return The Mail sender
     */
	public String getFrom(){
		return from;
	}

    /**
     * Get the Mail id
     * @return The Mail id
     */
	public int id(){
		return id;
	}

    /**
     * Check if the Mail has Quoted-Printable characters
     * @return If the Mail contains Quoted-Printable characters
     */
	public boolean hasQP() {
		return quotedPrint;
	}
}