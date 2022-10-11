package com.github.anidb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.anidb.vo.Anime;

/**
 * See: http://wiki.anidb.net/w/HTTP_API_Definition
 * 
 * @author scavenger156
 *
 */
public class AnidDB {
	private static final String VERSION = "1";
	private static final String CLIENT_NAME = "adbhac";
	private static final String BASE_URL = "http://api.anidb.net:9001/httpapi?";
	/**
	 * Write the loaded XML
	 */
	private boolean writeFile = false;
	/**
	 * Milliseconds
	 */
	private int timeOut = 5000;

	private static final String CONNECTION_URL = BASE_URL + "client=" + CLIENT_NAME + "&clientver=" + VERSION
			+ "&protover=1";

	private Proxy proxy;

	private URLConnection anidbConnection;
	private boolean disableSSLTrustmanager = false;
	private static final Logger LOG = LoggerFactory.getLogger(AnidDB.class);
	

	/**
	 * 
	 */
	public AnidDB() {

	}

	public void setWriteFile(boolean writeFile) {
		this.writeFile = writeFile;
	}

	public void setDisableSSLTrustmanager(boolean disableSSLTrustmanager) {
		this.disableSSLTrustmanager = disableSSLTrustmanager;
	}

	private void disableSSLTrustManager() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			;
		}
	}

	/**
	 * 
	 * @param timeOut Timeout default 5000
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * Create a connection to anidb.
	 */
	private void connect(URLParameter... parameter) {
		if (disableSSLTrustmanager) {
			disableSSLTrustManager();
		}
		StringBuffer urlSb = new StringBuffer(CONNECTION_URL);
		for (URLParameter value : parameter) {
			urlSb.append('&');
			urlSb.append(value.name);
			urlSb.append('=');
			urlSb.append(value.value);
		}
		try {
			URL url = new URL(urlSb.toString());
			LOG.info("Connect:" + urlSb);
			if (proxy != null) {
				anidbConnection = url.openConnection(proxy);
			} else {
				anidbConnection = url.openConnection();
			}
			anidbConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			anidbConnection.setRequestProperty("Referer", "http://anidb.net/perl-bin/animedb.pl?show=main");
			anidbConnection.setConnectTimeout(timeOut);
			anidbConnection.setReadTimeout(timeOut);
			anidbConnection.connect();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the Connection
	 */
	private void cleanup() {
		if (anidbConnection instanceof HttpURLConnection) {
			((HttpURLConnection) anidbConnection).disconnect();
			anidbConnection = null;
		}
	}

	/**
	 * 
	 * @param anidbId
	 * @return XML Data from the Anime
	 */
	public String loadAnimeXML(int anidbId) {
		if (anidbId <= 0) {
			throw new IllegalArgumentException("anidbIdparameter is wrong");
		}
		try {
			if (writeFile) {
				File cache = new File(anidbId + ".xml");
				if (cache.exists()) {
					try (FileInputStream readSource = new FileInputStream(cache);
							InputStreamReader isr = new InputStreamReader(readSource)) {
						return readDataAsString(isr);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			connect(new URLParameter("request", "anime"), new URLParameter("aid", Integer.toString(anidbId)));
			String xmlData = readDataStringFromConnection();
			if (writeFile) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(new File(anidbId + ".xml"));

					fos.write(xmlData.getBytes());
				} catch (IOException e1) {
					LOG.error(e1.getLocalizedMessage(),e1);
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e1) {
							LOG.error(e1.getLocalizedMessage(),e1);
						}
					}
				}
			}
			return xmlData;
		} finally {
			cleanup();
		}
	}

	/**
	 * 
	 * @return Anime from httpapi.xml
	 * @throws IOException Loadingerror
	 */
	public Anime test() throws IOException {
		try {
			anidbConnection = getClass().getResource("/httpapi.xml").toURI().toURL().openConnection();
			String xml = readDataStringFromConnection();

			JAXBContext context = JAXBContext.newInstance(Anime.class);
			Unmarshaller unMarshaller = context.createUnmarshaller();
			Anime param = (Anime) unMarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
			return param;
		} catch (JAXBException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loading the animedata from anidb.net
	 * 
	 * @param anidbId ID
	 * @return Anime
	 */
	public Anime loadAnime(int anidbId) {
		InputStream readSource = null;
		if (writeFile) {
			File cache = new File(anidbId + ".xml");
			if (cache.exists()) {
				try {
					readSource = new FileInputStream(cache);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		if (readSource == null) {
			String xml = loadAnimeXML(anidbId);

			try {
				readSource = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getLocalizedMessage(),e);
			}
		}

		return convertXMLToAnime(readSource);
	}

	public Anime convertXMLToAnime(String xml) {
		try (ByteArrayInputStream readSource = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
			return convertXMLToAnime(readSource);
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage(),e);
			throw new RuntimeException(e);
		}
	}

	public Anime convertXMLToAnime(InputStream readSource) {
		try {

			JAXBContext context = JAXBContext.newInstance(Anime.class);
			Unmarshaller unMarshaller = context.createUnmarshaller();
			Anime param = (Anime) unMarshaller.unmarshal(readSource);
			return param;
		} catch (UnmarshalException e) {

			LOG.info("Could not read fron anidb:" + e.getLocalizedMessage());
			e.printStackTrace();

			return null;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @return Data from the current connection
	 */
	private String readDataStringFromConnection() {
		try {
			if (anidbConnection == null) {
				throw new IllegalStateException("No active connection found");
			}
			String xmlData = readDataFromConnection();
			// Postfix
			xmlData = xmlData.replaceAll("xml:lang", "lang");
			return xmlData;
		} finally {
			cleanup();
		}
	}

	private String readDataFromConnection() {

		if (anidbConnection == null) {
			throw new IllegalStateException("No active connection found");
		}
		try (InputStream inputStream = anidbConnection.getInputStream()) {

			if ("gzip".equalsIgnoreCase(anidbConnection.getHeaderField("Content-Encoding"))) {
				try (GZIPInputStream gzip = new GZIPInputStream(inputStream);
						InputStreamReader isr = new InputStreamReader(gzip, "UTF-8")) {

					return readDataAsString(isr);
				}
			} else {
				try (InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8")) {

					return readDataAsString(isr);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public String readDataAsString(InputStreamReader is) throws IOException {
		int read = 0;
		char[] data = new char[1024];
		StringBuffer baos = new StringBuffer();
		while ((read = is.read(data)) != -1) {
			baos.append(data, 0, read);
		}
		return baos.toString();
	}

	/**
	 * Parameter for the urlconnection
	 * 
	 * @author scavenger156
	 */
	private static class URLParameter {
		private String name;
		private String value;

		public URLParameter(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

	}
}
