/**
 * Copyright (C) 2014 Open Ray Message
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecure.push;

import android.content.Context;
import android.util.Log;

import com.google.thoughtcrimegson.Gson;
import com.google.thoughtcrimegson.JsonParseException;
import com.google.thoughtcrimegson.annotations.SerializedName;

import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.whispersystems.textsecure.crypto.IdentityKey;
import org.whispersystems.textsecure.storage.PreKeyRecord;
import org.whispersystems.textsecure.util.Base64;
import org.whispersystems.textsecure.util.BlacklistingTrustManager;
import org.whispersystems.textsecure.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * Network interface to the TextSecure server API.
 *
 * @author Moxie Marlinspike
 */
public class PushServiceSocket {

  private static final String CREATE_ACCOUNT_SMS_PATH   = "/v1/accounts/sms/code/%s";
  private static final String CREATE_ACCOUNT_VOICE_PATH = "/v1/accounts/voice/code/%s";
  private static final String VERIFY_ACCOUNT_PATH       = "/v1/accounts/code/%s/%s";
  private static final String REGISTER_GCM_PATH         = "/v1/accounts/gcm/";
  private static final String PREKEY_METADATA_PATH      = "/v1/keys/";
  private static final String PREKEY_PATH               = "/v1/keys/%s";
  private static final String PREKEY_DEVICE_PATH        = "/v1/keys/%s/%s";

  private static final String DIRECTORY_TOKENS_PATH     = "/v1/directory/tokens";
  private static final String DIRECTORY_VERIFY_PATH     = "/v1/directory/%s";
  private static final String MESSAGE_PATH              = "/v1/messages/%s";
  private static final String ATTACHMENT_PATH           = "/v1/attachments/%s";

    private static final String REGISTER_FRONTIA_PATH         = "/v1/accounts/frontia/";

    private static final String UPDATE_GETUI_CLIENTID_PATH         = "/v1/accounts/getui/";

    private static final String UPLOAD_CONTACTS_INFO_PATH = "/v1/accounts/code/saveorupdate/%s";
    private static final String GET_CONTACTS_INFO_PATH = "/v1/accounts/code/get/%s";
    private static final String VERIFICATION_CODE_PATH = "/v1/accounts/reg/code/send/%s";
  private static final boolean ENFORCE_SSL = true;

  private final Context        context;
  private final String         serviceUrl;
  private final String         localNumber;
  private final String         password;
  private final TrustManager[] trustManagers;

  public PushServiceSocket(Context context, String serviceUrl, TrustStore trustStore,
                           String localNumber, String password)
  {
    this.context       = context.getApplicationContext();
    this.serviceUrl    = serviceUrl;
    this.localNumber   = localNumber;
    this.password      = password;
      //Commet by Wei.he 2014-08-21
//    this.trustManagers = initializeTrustManager(trustStore);
      this.trustManagers = mTrustAllCerts;
  }

  public void createAccount(boolean voice) throws IOException {
    String path = voice ? CREATE_ACCOUNT_VOICE_PATH : CREATE_ACCOUNT_SMS_PATH;
    makeRequest(String.format(path, localNumber), "GET", null);
  }

    public StatusModel requestVerificationCode(String phoneNumber) {
        try {
            String response = makeRequest(String.format(VERIFICATION_CODE_PATH, phoneNumber), "GET", null);
            StatusModel codeStatus = new Gson().fromJson(response, StatusModel.class);
            return codeStatus;
        } catch (IOException ioe) {
            Log.w("PushServiceSocket", ioe);
        }
        return null;
    }

  //add verification code verify by Real.J.Tian 29/10/2014
  public void verifyAccount(String code,String verificationCode, String signalingKey,
                            boolean supportsSms, int registrationId)
      throws IOException
  {
    AccountAttributes signalingKeyEntity = new AccountAttributes(signalingKey, supportsSms, registrationId);
      makeRequest(String.format(VERIFY_ACCOUNT_PATH, code, verificationCode),
              "PUT", new Gson().toJson(signalingKeyEntity));
  }

  public void registerGcmId(String gcmRegistrationId) throws IOException {
    GcmRegistrationId registration = new GcmRegistrationId(gcmRegistrationId);
    makeRequest(REGISTER_GCM_PATH, "PUT", new Gson().toJson(registration));
  }

    public void registerFrontia(String userId, String channelId) throws IOException {
        FrontiaUserChannel frontia = new FrontiaUserChannel();
        frontia.setChannelId(channelId);
        frontia.setUserId(userId);
        makeRequest(REGISTER_GCM_PATH, "PUT", new Gson().toJson(frontia));
    }

    public void updateGetuiClientId(String clientId) throws IOException {
        GetuiModel getui = new GetuiModel();
        getui.setGetuiclientid(clientId);
        makeRequest(UPDATE_GETUI_CLIENTID_PATH, "PUT", new Gson().toJson(getui));
    }

  public void unregisterGcmId() throws IOException {
    makeRequest(REGISTER_GCM_PATH, "DELETE", null);
  }

  public void sendMessage(OutgoingPushMessageList bundle)
      throws IOException
  {
    try {
      makeRequest(String.format(MESSAGE_PATH, bundle.getDestination()), "PUT", new Gson().toJson(bundle));
    } catch (NotFoundException nfe) {
      throw new UnregisteredUserException(bundle.getDestination(), nfe);
    }
  }

  public void registerPreKeys(IdentityKey identityKey,
                              PreKeyRecord lastResortKey,
                              List<PreKeyRecord> records)
      throws IOException
  {
    List<PreKeyEntity> entities = new LinkedList<PreKeyEntity>();

    for (PreKeyRecord record : records) {
      PreKeyEntity entity = new PreKeyEntity(record.getId(),
                                             record.getKeyPair().getPublicKey(),
                                             identityKey);

      entities.add(entity);
    }

    PreKeyEntity lastResortEntity = new PreKeyEntity(lastResortKey.getId(),
                                                     lastResortKey.getKeyPair().getPublicKey(),
                                                     identityKey);


    makeRequest(String.format(PREKEY_PATH, ""), "PUT",
                PreKeyList.toJson(new PreKeyList(lastResortEntity, entities)));
  }

  public int getAvailablePreKeys() throws IOException {
    String       responseText = makeRequest(PREKEY_METADATA_PATH, "GET", null);
    PreKeyStatus preKeyStatus = new Gson().fromJson(responseText, PreKeyStatus.class);

    return preKeyStatus.getCount();
  }

  public List<PreKeyEntity> getPreKeys(PushAddress destination) throws IOException {
    try {
      String deviceId = String.valueOf(destination.getDeviceId());

      if (deviceId.equals("1"))
        deviceId = "*";

      String path = String.format(PREKEY_DEVICE_PATH, destination.getNumber(), deviceId);

      if (!Util.isEmpty(destination.getRelay())) {
        path = path + "?relay=" + destination.getRelay();
      }

      String responseText = makeRequest(path, "GET", null);
      PreKeyList response = PreKeyList.fromJson(responseText);

      return response.getKeys();
    } catch (JsonParseException e) {
      throw new IOException(e);
    } catch (NotFoundException nfe) {
      throw new UnregisteredUserException(destination.getNumber(), nfe);
    }
  }

  public PreKeyEntity getPreKey(PushAddress destination) throws IOException {
    try {
      String path = String.format(PREKEY_DEVICE_PATH, destination.getNumber(),
              String.valueOf(destination.getDeviceId()));

      if (!Util.isEmpty(destination.getRelay())) {
        path = path + "?relay=" + destination.getRelay();
      }

      String     responseText = makeRequest(path, "GET", null);
      PreKeyList response     = PreKeyList.fromJson(responseText);

      if (response.getKeys() == null || response.getKeys().size() < 1)
        throw new IOException("Empty prekey list");

      return response.getKeys().get(0);
    } catch (JsonParseException e) {
      throw new IOException(e);
    } catch (NotFoundException nfe) {
      throw new UnregisteredUserException(destination.getNumber(), nfe);
    }
  }

  public long sendAttachment(PushAttachmentData attachment) throws IOException {
    String               response      = makeRequest(String.format(ATTACHMENT_PATH, ""), "GET", null);
    AttachmentDescriptor attachmentKey = new Gson().fromJson(response, AttachmentDescriptor.class);

    if (attachmentKey == null || attachmentKey.getLocation() == null) {
      throw new IOException("Server failed to allocate an attachment key!");
    }

    Log.w("PushServiceSocket", "Got attachment content location: " + attachmentKey.getLocation());

    uploadExternalFile("PUT", attachmentKey.getLocation(), attachment.getData());

    return attachmentKey.getId();
  }

  public File retrieveAttachment(String relay, long attachmentId) throws IOException {
    String path = String.format(ATTACHMENT_PATH, String.valueOf(attachmentId));

    if (!Util.isEmpty(relay)) {
      path = path + "?relay=" + relay;
    }

    String               response   = makeRequest(path, "GET", null);
    AttachmentDescriptor descriptor = new Gson().fromJson(response, AttachmentDescriptor.class);

    Log.w("PushServiceSocket", "Attachment: " + attachmentId + " is at: " + descriptor.getLocation());

    File attachment = File.createTempFile("attachment", ".tmp", context.getFilesDir());
    attachment.deleteOnExit();

    downloadExternalFile(descriptor.getLocation(), attachment);

    return attachment;
  }

  public List<ContactTokenDetails> retrieveDirectory(Set<String> contactTokens) {
    try {
      ContactTokenList        contactTokenList = new ContactTokenList(new LinkedList<String>(contactTokens));
      String                  response         = makeRequest(DIRECTORY_TOKENS_PATH, "PUT", new Gson().toJson(contactTokenList));
      ContactTokenDetailsList activeTokens     = new Gson().fromJson(response, ContactTokenDetailsList.class);

      return activeTokens.getContacts();
    } catch (IOException ioe) {
      Log.w("PushServiceSocket", ioe);
      return null;
    }
  }

  public ContactTokenDetails getContactTokenDetails(String contactToken) throws IOException {
    try {
      String response = makeRequest(String.format(DIRECTORY_VERIFY_PATH, contactToken), "GET", null);
      return new Gson().fromJson(response, ContactTokenDetails.class);
    } catch (NotFoundException nfe) {
      return null;
    }
  }

  private void downloadExternalFile(String url, File localDestination)
      throws IOException
  {
    URL               downloadUrl = new URL(url);
    HttpURLConnection connection  = (HttpURLConnection) downloadUrl.openConnection();
    connection.setRequestProperty("Content-Type", "application/octet-stream");
    connection.setRequestMethod("GET");
    connection.setDoInput(true);

    try {
      if (connection.getResponseCode() != 200) {
        throw new IOException("Bad response: " + connection.getResponseCode());
      }

      OutputStream output = new FileOutputStream(localDestination);
      InputStream input   = connection.getInputStream();
      byte[] buffer       = new byte[4096];
      int read;

      while ((read = input.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }

      output.close();
      Log.w("PushServiceSocket", "Downloaded: " + url + " to: " + localDestination.getAbsolutePath());
    } finally {
      connection.disconnect();
    }
  }

    public void uploadContactsInfo(byte[] avatar, boolean gender, Integer age, String name, String signature, List<String> pushNumbers) {
        try {
            Long attachmentId = null;
            if (avatar != null && avatar.length > 0) {
                String response = makeRequest(String.format(ATTACHMENT_PATH, ""), "GET", null);
                AttachmentDescriptor attachmentKey = new Gson().fromJson(response, AttachmentDescriptor.class);

                if (attachmentKey == null || attachmentKey.getLocation() == null) {
                    throw new IOException("Server failed to allocate an attachment key!");
                }

                Log.w("PushServiceSocket", "Got avatar attachment content location: " + attachmentKey.getLocation());
                attachmentId = attachmentKey.getId();
                uploadExternalFile("PUT", attachmentKey.getLocation(), avatar);
            }
            ContactsInfo contactsInfo = new ContactsInfo(localNumber, name, gender, age, null, attachmentId, signature);
            contactsInfo.setFriends(pushNumbers);

            makeRequest(String.format(UPLOAD_CONTACTS_INFO_PATH, localNumber), "PUT", new Gson().toJson(contactsInfo));
        } catch (IOException ioe) {
            Log.w("PushServiceSocket", ioe);
        }
    }

    public ContactsInfo getContactsInfo(String number) {
        try {
            String response = makeRequest(String.format(GET_CONTACTS_INFO_PATH, number), "GET", null);
            ContactsInfo contactsInfo = new Gson().fromJson(response, ContactsInfo.class);
            return contactsInfo;
        } catch (IOException ioe) {
            Log.w("PushServiceSocket", ioe);
        }
        return null;
    }

  private void uploadExternalFile(String method, String url, byte[] data)
    throws IOException
  {
    URL                uploadUrl  = new URL(url);
      //Modified by wei.he for not using https connection in our server
//    HttpsURLConnection connection = (HttpsURLConnection) uploadUrl.openConnection();
      HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", "application/octet-stream");
    connection.connect();

    try {
      OutputStream out = connection.getOutputStream();
      out.write(data);
      out.close();

//Comment by wei.he for our swift server is return 201 rather than amazon s3
//      if (connection.getResponseCode() != 200) {
        if (connection.getResponseCode() != 201) {
        throw new IOException("Bad response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
      }
    } finally {
      connection.disconnect();
    }
  }

  private String makeRequest(String urlFragment, String method, String body)
      throws IOException
  {
    HttpURLConnection connection = makeBaseRequest(urlFragment, method, body);
    String            response   = Util.readFully(connection.getInputStream());

    connection.disconnect();

    return response;
  }

  private HttpURLConnection makeBaseRequest(String urlFragment, String method, String body)
      throws IOException
  {
    HttpURLConnection connection = getConnection(urlFragment, method);

    if (body != null) {
      connection.setDoOutput(true);
    }

    connection.connect();

    if (body != null) {
      Log.w("PushServiceSocket", method +  "  --  " + body);
      OutputStream out = connection.getOutputStream();
      out.write(body.getBytes());
      out.close();
    }

    if (connection.getResponseCode() == 413) {
      connection.disconnect();
      throw new RateLimitException("Rate limit exceeded: " + connection.getResponseCode());
    }

    if (connection.getResponseCode() == 401 || connection.getResponseCode() == 403) {
      connection.disconnect();
      throw new AuthorizationFailedException("Authorization failed!");
    }

    if (connection.getResponseCode() == 404) {
      connection.disconnect();
      throw new NotFoundException("Not found");
    }

    if (connection.getResponseCode() == 409) {
      String response = Util.readFully(connection.getErrorStream());
      throw new MismatchedDevicesException(new Gson().fromJson(response, MismatchedDevices.class));
    }

    if (connection.getResponseCode() == 410) {
      String response = Util.readFully(connection.getErrorStream());
      throw new StaleDevicesException(new Gson().fromJson(response, StaleDevices.class));
    }

    if (connection.getResponseCode() == 417) {
      throw new ExpectationFailedException();
    }

    if (connection.getResponseCode() != 200 && connection.getResponseCode() != 204) {
      throw new IOException("Bad response: " + connection.getResponseCode() + " " + connection.getResponseMessage());
    }

    return connection;
  }

  private HttpURLConnection getConnection(String urlFragment, String method) throws IOException {
    try {
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, trustManagers, null);

      URL url = new URL(String.format("%s%s", serviceUrl, urlFragment));
      Log.w("PushServiceSocket", "Push service URL: " + serviceUrl);
      Log.w("PushServiceSocket", "Opening URL: " + url);

      HttpURLConnection connection = (HttpURLConnection)url.openConnection();

      if (ENFORCE_SSL) {
        ((HttpsURLConnection)connection).setSSLSocketFactory(context.getSocketFactory());
          //Comment by Wei.He 2014-08-21
//        ((HttpsURLConnection)connection).setHostnameVerifier(new StrictHostnameVerifier());
          ((HttpsURLConnection)connection).setHostnameVerifier(DO_NOT_VERIFY);
      }

      connection.setRequestMethod(method);
      connection.setRequestProperty("Content-Type", "application/json");

      if (password != null) {
        connection.setRequestProperty("Authorization", getAuthorizationHeader());
      }

      return connection;
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    } catch (KeyManagementException e) {
      throw new AssertionError(e);
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

    // Added by Wei.He for do not check for always verify the host - dont check for certificate
    private static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
            return true;
        }
    };

  private String getAuthorizationHeader() {
    try {
      return "Basic " + Base64.encodeBytes((localNumber + ":" + password).getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }

  private TrustManager[] initializeTrustManager(TrustStore trustStore) {
    try {
      InputStream keyStoreInputStream = trustStore.getKeyStoreInputStream();
      KeyStore    keyStore            = KeyStore.getInstance("BKS");

      keyStore.load(keyStoreInputStream, trustStore.getKeyStorePassword().toCharArray());

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
      trustManagerFactory.init(keyStore);

      return BlacklistingTrustManager.createFor(trustManagerFactory.getTrustManagers());
    } catch (KeyStoreException kse) {
      throw new AssertionError(kse);
    } catch (CertificateException e) {
      throw new AssertionError(e);
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    } catch (IOException ioe) {
      throw new AssertionError(ioe);
    }
  }

    // Adde by Wei.He 2014-08-21
    // Create a trust manager that does not validate certificate chains
    // Android 采用X509的证书信息机制
    TrustManager[] mTrustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        }
    };

  private static class GcmRegistrationId {
    private String gcmRegistrationId;

    public GcmRegistrationId() {}

    public GcmRegistrationId(String gcmRegistrationId) {
      this.gcmRegistrationId = gcmRegistrationId;
    }
  }

  private static class AttachmentDescriptor {
    private long id;
    private String location;

    public long getId() {
      return id;
    }

    public String getLocation() {
      return location;
    }
  }

  public interface TrustStore {
    public InputStream getKeyStoreInputStream();
    public String getKeyStorePassword();
  }

    private static class FrontiaUserChannel {

        private String userId;

        private String channelId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

    }

    private static class GetuiModel {
        private String clientId;

        public String getGetuiclientid() {
            return clientId;
        }

        public void setGetuiclientid(String clientId) {
            this.clientId = clientId;
        }
    }

    public static class StatusModel
    {
        //@SerializedName("code")
        private int code;

        public int geCode() {
            return code;
        }

        public void setCode(int status) {
            this.code = status;
        }
    }
}
