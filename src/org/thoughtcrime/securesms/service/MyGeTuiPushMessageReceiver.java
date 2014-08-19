package org.thoughtcrime.securesms.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.textsecure.push.IncomingEncryptedPushMessage;
import org.whispersystems.textsecure.push.IncomingPushMessage;
import org.whispersystems.textsecure.crypto.InvalidVersionException;
import org.whispersystems.textsecure.push.ContactTokenDetails;
import org.whispersystems.textsecure.directory.Directory;
import org.whispersystems.textsecure.util.Util;
import org.whispersystems.textsecure.directory.NotInDirectoryException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;

import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;

/**
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下： 
 *  0 - Success
 *  10001 - Network Problem
 *  30600 - Internal Server Error
 *  30601 - Method Not Allowed 
 *  30602 - Request Params Not Valid
 *  30603 - Authentication Failed 
 *  30604 - Quota Use Up Payment Required 
 *  30605 - Data Required Not Found 
 *  30606 - Request Time Expires Timeout 
 *  30607 - Channel Token Timeout 
 *  30608 - Bind Relation Not Found 
 *  30609 - Bind Number Too Many
 * 
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 * 
 */
public class MyGeTuiPushMessageReceiver extends BroadcastReceiver {
    /** TAG to Log */
    public static final String TAG = MyGeTuiPushMessageReceiver.class
            .getSimpleName();

        @Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {

		case PushConsts.GET_MSG_DATA:
			// 获取透传数据
			// String appid = bundle.getString("appid");
			byte[] payload = bundle.getByteArray("payload");
			
			String taskid = bundle.getString("taskid");
			String messageid = bundle.getString("messageid");

			// smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
			boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
			Log.d("GetuiSdkDemo", "第三方回执接口调用" + (result ? "成功" : "失败"));
			
			if (payload != null) {
				String data = new String(payload);

				Log.d(TAG, "Got Payload:" + data);

			    try {
				    Log.w(TAG, "GCM message...");

				    if (Util.isEmpty(data))
					return;

				    if (!TextSecurePreferences.isPushRegistered(context)) {
					Log.w("GcmIntentService", "Not push registered!");
					return;
				    }

				    String                       sessionKey       = TextSecurePreferences.getSignalingKey(context);
				    IncomingEncryptedPushMessage encryptedMessage = new IncomingEncryptedPushMessage(data, sessionKey);
				    IncomingPushMessage          incomingMessage          = encryptedMessage.getIncomingPushMessage();

				    if (!isActiveNumber(context, incomingMessage.getSource())) {
					Directory           directory           = Directory.getInstance(context);
					ContactTokenDetails contactTokenDetails = new ContactTokenDetails();
					contactTokenDetails.setNumber(incomingMessage.getSource());

					directory.setNumber(contactTokenDetails, true);
				    }

				    Intent service = new Intent(context, SendReceiveService.class);
				    service.setAction(SendReceiveService.RECEIVE_PUSH_ACTION);
				    service.putExtra("message", incomingMessage);
				    context.startService(service);
				    Log.w(TAG, "startService SendReceiveService...");
				} catch (IOException e) {
				    Log.w(TAG, e);
				} catch (InvalidVersionException e) {
				    Log.w(TAG, e);
				}
			}
			break;
		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			// 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
			String cid = bundle.getString("clientid");
			Log.d(TAG, "Got cid:" + cid);
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			String preClientId = sharedPreferences.getString("clientid","");
			if (!TextUtils.isEmpty(preClientId) && !TextUtils.isEmpty(cid) && !preClientId.equals(cid)) {
			    //update the client by push service socket or start the send recieve service
                Intent service = new Intent(context, SendReceiveService.class);
                service.setAction(SendReceiveService.UPDATE_CLIENTID_ACTION);
                service.putExtra("client_id", cid);
                context.startService(service);
			}
		        SharedPreferences.Editor editor = sharedPreferences.edit()  ;
		        editor.putString("clientid", cid);
                        editor.commit();

			break;
		case PushConsts.THIRDPART_FEEDBACK:
			/*String appid = bundle.getString("appid");
			String taskid = bundle.getString("taskid");
			String actionid = bundle.getString("actionid");
			String result = bundle.getString("result");
			long timestamp = bundle.getLong("timestamp");

			Log.d("GetuiSdkDemo", "appid = " + appid);
			Log.d("GetuiSdkDemo", "taskid = " + taskid);
			Log.d("GetuiSdkDemo", "actionid = " + actionid);
			Log.d("GetuiSdkDemo", "result = " + result);
			Log.d("GetuiSdkDemo", "timestamp = " + timestamp);*/
			break;
		default:
			break;
		}
	}

    private boolean isActiveNumber(Context context, String e164number) {
        boolean isActiveNumber;

        try {
            isActiveNumber = Directory.getInstance(context).isActiveNumber(e164number);
        } catch (NotInDirectoryException e) {
            isActiveNumber = false;
        }

        return isActiveNumber;
    }

}
