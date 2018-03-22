/*L
 *  Copyright SAIC, Ellumen and RSNA (CTP)
 *
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/national-biomedical-image-archive/LICENSE.txt for details.
 */

/**
 *
 */
package gov.nih.nci.nbia;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;

import gov.nih.nci.nbia.download.SeriesData;
import gov.nih.nci.nbia.ui.DownloadManagerFrame;
import gov.nih.nci.nbia.ui.ProgressIndicator;
import gov.nih.nci.nbia.util.JnlpArgumentsParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * @author Q. Pan
 *
 */
public class StandaloneDMV3 extends StandaloneDM {
	private final static String serverVersionMsg = "The Download server version is not compatible with this TCIA Downloader.  Please check if downloadServerUrl is pointing to the correct server.";
	private static final String GuestBtnLbl = "Guest Login";
//	private String basketId;
//	private String fileLoc;
//	private String ManifestVersion = null;
	private String key = null;
	private List<String> seriesList = null;
//	private final static ProgressIndicator pi = new ProgressIndicator();

	/**
	 * @param args
	 */

	public StandaloneDMV3() {
		super();
//		this.basketId = System.getProperty("databasketId");
//		this.ManifestVersion = System.getProperty("manifestVersion");
	}

	void checkCompatibility() {
		if (serverUrl.endsWith("DownloadServlet")) {
			this.serverUrl = serverUrl.concat("V3");
		}

		if (!serverUrl.endsWith("DownloadServletV3")) {
			Object[] options = { "OK" };

			int n = JOptionPane.showOptionDialog(frame, serverVersionMsg, "Incompatible Server Notification",
					JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			System.exit(n);
		}
	}

	private String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	protected List<SeriesData> getSeriesInfo(String userId, String password, List seriesList) {
		List<String> seriesInfo = null;
		try {
			seriesInfo = connectAndReadFromURL(new URL(serverUrl), seriesList, userId, password);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		String[] strResult = new String[seriesInfo.size()];
		seriesInfo.toArray(strResult);
		return JnlpArgumentsParser.parse(strResult);
	}

	public void launch(List<String> seriesList) {
		checkCompatibility();
		if ((seriesList == null) || (seriesList.size() <= 0)) {
			JOptionPane.showMessageDialog(null,
					"This version of Download App requires to have at least one series instance UID in manifest file.");
			System.exit(0);
		} else {
			this.seriesList = seriesList;
			if (seriesList.size() > 9999) {
				int result = JOptionPane.showConfirmDialog(null,
						"The number of series in manifest file exceeds the maximum 9,999 series threshold. Only the first 9,999 series will be downloaded.",
						"Threshold Exceeded Notification", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE);

				if (result == JOptionPane.OK_OPTION) {
					constructLoginWin();
				} else
					System.exit(0);
			} else
				constructLoginWin();
		}
	}

	void submitUserCredential(String userId, String password) {
		List<String> seriesInfo = null;
		String encryptedPassword = null;

		try {
			if (password != null)
				encryptedPassword = this.encrypt(password, getKey());
			else
				encryptedPassword = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			seriesInfo = connectAndReadFromURL(new URL(serverUrl), seriesList, userId, encryptedPassword);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		if (seriesInfo == null) {
			statusLbl.setText("Invalid User name and/or password.  Please try it again.");
			statusLbl.setForeground(Color.red);
			return;
		} else {
			try {
				String[] strResult = new String[seriesInfo.size()];
				seriesInfo.toArray(strResult);

				List<SeriesData> seriesData = JnlpArgumentsParser.parse(strResult);
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				DownloadManagerFrame manager = new DownloadManagerFrame(userId, encryptedPassword, includeAnnotation,
						seriesData, serverUrl, noOfRetry);
				manager.setTitle(winTitle);
				manager.setDefaultDownloadDir(System.getProperty("user.home") + File.separator + "Desktop");
				// manager.setBounds(100, 100, 928, 690);
				manager.setVisible(true);
				frame.setVisible(false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static List<String> connectAndReadFromURL(URL url, List<String> seriesList, String userId, String passWd) {
		List<String> data = null;
		DefaultHttpClient httpClient = null;
		TrustStrategy easyStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] certificate, String authType) throws CertificateException {
				return true;
			}
		};
		try {
			// SSLContext sslContext = SSLContext.getInstance("SSL");
			// set up a TrustManager that trusts everything
			// sslContext.init(null, new TrustManager[] { new
			// EasyX509TrustManager(null)}, null);

			SSLSocketFactory sslsf = new SSLSocketFactory(easyStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme httpsScheme = new Scheme("https", 443, sslsf);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(schemeRegistry);

			HttpParams httpParams = new BasicHttpParams();
			// HttpConnectionParams.setConnectionTimeout(httpParams, 50000);
			// HttpConnectionParams.setSoTimeout(httpParams, new
			// Integer(12000));
			HttpConnectionParams.setConnectionTimeout(httpParams, 500000);
			HttpConnectionParams.setSoTimeout(httpParams, new Integer(120000));
			httpClient = new DefaultHttpClient(ccm, httpParams);
			httpClient.setRoutePlanner(new ProxySelectorRoutePlanner(schemeRegistry, ProxySelector.getDefault()));
			// // Additions by lrt for tcia -
			// // attempt to reduce errors going through a Coyote Point
			// Equalizer load balance switch
			// httpClient.getParams().setParameter("http.socket.timeout", new
			// Integer(12000));
			httpClient.getParams().setParameter("http.socket.timeout", new Integer(120000));
			httpClient.getParams().setParameter("http.socket.receivebuffer", new Integer(16384));
			httpClient.getParams().setParameter("http.tcp.nodelay", true);
			httpClient.getParams().setParameter("http.connection.stalecheck", false);
			// // end lrt additions

			HttpPost httpPostMethod = new HttpPost(url.toString());

			List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();

			if (userId != null && passWd != null) {
				postParams.add(new BasicNameValuePair("userId", userId));
				httpPostMethod.addHeader("password", passWd);
			}
			postParams.add(new BasicNameValuePair("numberOfSeries", Integer.toString(seriesList.size())));
			int i = 0;
			for (String s : seriesList) {
				postParams.add(new BasicNameValuePair("series" + Integer.toString(++i), s));
			}

			UrlEncodedFormEntity query = new UrlEncodedFormEntity(postParams);
			httpPostMethod.setEntity(query);
			HttpResponse response = httpClient.execute(httpPostMethod);
			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode != HttpURLConnection.HTTP_OK) {
				return null;
			} else {
				InputStream inputStream = response.getEntity().getContent();
				data = IOUtils.readLines(inputStream);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}

		return data;
	}

	public void constructLoginWin() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		frame = new JFrame("Standalone Download Manager");
		frame.setBounds(100, 100, 928, 690);
		frame.setContentPane(constructLoginPanel());
		frame.setTitle(winTitle);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void setDefaultSize(int size) {
		Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);

		for (Object key : keys) {
			if (key != null && key.toString().toLowerCase().contains("font")) {
				Font font = UIManager.getDefaults().getFont(key);
				if (font != null) {
					font = font.deriveFont((float) size);
					UIManager.put(key, font);
				}
			}
		}
	}

	private JPanel constructLoginPanel() {
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(36, 36, 36, 36));
		contentPane.setLayout(null);

		JPanel guestPanel = new JPanel();
		guestPanel.setBounds(38, 40, 825, 140);
		guestPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(153, 180, 209), null),
				"  For Guest  ", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 120, 215)));
		contentPane.add(guestPanel);
		guestPanel.setLayout(null);

		JButton guestBtn = new JButton(GuestBtnLbl);
		guestBtn.setBounds(606, 50, 140, 36);
		guestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				submitRequest(null, null);
			}
		});
		guestPanel.add(guestBtn);

		JLabel guestLbl = new JLabel("Log in as a guest to download public data only");
		guestLbl.setBounds(70, 50, 460, 42);
		guestPanel.add(guestLbl);

		JPanel loginUserPanel = new JPanel();
		loginUserPanel.setBounds(40, 258, 825, 306);
		contentPane.add(loginUserPanel);
		loginUserPanel
				.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(153, 180, 209), null),
						"  Login to Download Authorized Data  ", TitledBorder.CENTER, TitledBorder.TOP, null,
						new Color(0, 120, 215)));
		loginUserPanel.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("User Name");
		lblNewLabel_1.setBounds(70, 80, 118, 36);
		loginUserPanel.add(lblNewLabel_1);

		JButton submitBtn = new JButton(SubmitBtnLbl);
		submitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				userId = userNameFld.getText();
				password = passwdFld.getText();
				if ((userId.length() < 1) || (password.length() < 1)) {
					statusLbl.setText("Please enter a valid user name and password.");
					statusLbl.setForeground(Color.red);
				} else {
					submitRequest(userId, password);
				}
			}
		});
		submitBtn.setBounds(606, 238, 140, 36);
		loginUserPanel.add(submitBtn);

		userNameFld = new JTextField();
		userNameFld.setBounds(200, 80, 333, 36);
		loginUserPanel.add(userNameFld);
		userNameFld.setColumns(10);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(70, 156, 118, 36);
		loginUserPanel.add(lblPassword);

		passwdFld = new JPasswordField();
		passwdFld.setBounds(200, 156, 333, 36);
		loginUserPanel.add(passwdFld);

		statusLbl = new JLabel("");
		statusLbl.setBounds(70, 226, 463, 36);
		loginUserPanel.add(statusLbl);

		JLabel lblOr = new JLabel("--- OR ---");
		lblOr.setBounds(419, 212, 81, 26);
		contentPane.add(lblOr);

		JLabel versionLabel = new JLabel("Release " + StandaloneDMDispatcher.getAppVersion() + " Build \""
				+ StandaloneDMDispatcher.getBuildTime() + "\"");
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		versionLabel.setForeground(new Color(70, 130, 180));
		versionLabel.setBounds(319, 584, 260, 20);
		contentPane.add(versionLabel);

		userNameFld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				passwdFld.requestFocus();
			}
		});

		passwdFld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				userId = userNameFld.getText();
				password = passwdFld.getText();
				if ((userId.length() < 1) || (password.length() < 1)) {
					statusLbl.setText("Please enter a valid user name and password.");
					statusLbl.setForeground(Color.red);
				} else
					submitRequest(userId, password);
			}
		});

		return contentPane;
	}

	private void submitRequest(String userId, String password) {
		Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(waitCursor);
		submitUserCredential(userId, password);
		frame.setCursor(defaultCursor);
		// pi.showPB(true);
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// submitUserCredential(userId, password);
		// } catch (Exception e) {
		// }
		// pi.dismiss();
		// }
		// }).start();
	}
}
