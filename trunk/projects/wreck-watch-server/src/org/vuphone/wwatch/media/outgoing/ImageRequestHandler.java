package org.vuphone.wwatch.media.outgoing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.vuphone.wwatch.media.incoming.ImageHandler;
import org.vuphone.wwatch.notification.InvalidFormatException;
import org.vuphone.wwatch.notification.Notification;
import org.vuphone.wwatch.notification.NotificationHandler;

public class ImageRequestHandler implements NotificationHandler{
	
	// XML will instantiate these
	private ImageRequestParser parser_;
	private DataSource ds_;
	
	private static final Logger logger_ = Logger
	.getLogger(ImageRequestHandler.class.getName());
	
	public Notification handle(Notification n) {
		// TODO - Make this degrade gracefully on IOExceptions.
		/*
		 * The format of the response is as follows:
		 * Headers
		 * 
		 * Content-Type		application/octet-stream
		 * Content-Length	number of bytes in the entity stream
		 * ImageCount		number of images in the response stream
		 * ImageXSize		size in bytes of image X
		 * 
		 * The actual image data are held in the stream as an array of bytes, 
		 * back to back
		 */
		
		ImageRequestNotification irn = null;

		
		Connection db = null;
		try {
			irn = parser_.getImage(n.getRequest(), n.getResponse());
			db = ds_.getConnection();
		} catch (InvalidFormatException ife) {
			
			ife.printStackTrace();
			logger_.log(Level.SEVERE,
					"Unable to parse the notification, stopping");
			closeDatabase(db);
			return n;
			
		} catch (SQLException e) {

			e.printStackTrace();
			logger_.log(Level.SEVERE,
			" Unable to continue without database, stopping");
			closeDatabase(db);
			return n;
		}

		try {
			db.setAutoCommit(true);

		} catch (SQLException e) {
			e.printStackTrace();
			logger_.log(Level.WARNING,
					"SQLException when setting auto commit: ", e);
		}

		int id = irn.getWreckID();
		// Prepare the SQL statement
		PreparedStatement prep = null;
		ResultSet rs;

		List<String> filenames = new ArrayList<String>();
		try {
			prep = db.prepareStatement(
					"select * from WreckImages where WreckID = ?;");
			prep.setInt(1, id);

			rs = prep.executeQuery();
					
			while (rs.next()) {
				String name = rs.getString("FileName");
				filenames.add(ImageHandler.MINI_PREFIX + name);
			}
			rs.close();

		}catch (SQLException e) {
			logger_.log(Level.SEVERE,
					"SQLException in the statement: ", e);
			closeDatabase(db);
			return n;
		}	

		HttpServletResponse response = irn.getResponse();
		
		// Preprocess the files to get sizes and set some headers
		response.setIntHeader("ImageCount", filenames.size());
		File[] files = new File[filenames.size()];
		int totalSize = 0;
		for (int i = 0; i < filenames.size(); ++i) {
			files[i] = new File(ImageHandler.getIMAGE_DIRECTORY(), filenames.get(i));
			int size = (int) files[i].length();
			response.setIntHeader("Image" + i + "Size", size);
			totalSize += size;
		}
		response.setContentLength(totalSize);
		response.setContentType("application/octet-stream");
		
		ByteArrayOutputStream toSend = new ByteArrayOutputStream(totalSize);
		
		for (int i = 0; i < filenames.size(); ++i) {

			int sz = (int) files[i].length();
			byte[] array = new byte[sz];
			int offset = 0;
			int numRead = 0;

			InputStream is = null;

			try {
				is = new FileInputStream(files[i]);
				
				while (offset < sz && (numRead = is.read(array, offset, sz - offset)) >= 0){
					offset += numRead;
				}
				
				toSend.write(array);
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
				return null;
			} catch (IOException e) {

				e.printStackTrace();
				return null;
			}

		}
		
		try {
			toSend.flush();
			response.getOutputStream().write(toSend.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	
		return irn;
	}
	
	private void closeDatabase(Connection db) {
		try {
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger_.log(Level.WARNING, "Unable to close database");
		}
	}

	public ImageRequestParser getParser() {
		return parser_;
	}

	public void setParser(ImageRequestParser parser) {
		parser_ = parser;
	}

	public DataSource getDataConnection() {
		return ds_;
	}

	public void setDataConnection(DataSource ds) {
		ds_ = ds;
	}

}
