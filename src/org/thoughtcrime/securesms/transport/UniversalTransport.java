/**
 * Copyright (C) 2013 Open Ray Message
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
package org.thoughtcrime.securesms.transport;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.MmsDatabase;
import org.thoughtcrime.securesms.database.model.SmsMessageRecord;
import org.thoughtcrime.securesms.mms.MmsSendResult;
import org.thoughtcrime.securesms.push.PushServiceSocketFactory;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.RecipientFormattingException;
import org.thoughtcrime.securesms.sms.IncomingGroupMessage;
import org.thoughtcrime.securesms.sms.IncomingIdentityUpdateMessage;
import org.thoughtcrime.securesms.util.GroupUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;
import org.whispersystems.textsecure.crypto.MasterSecret;
import org.whispersystems.textsecure.directory.Directory;
import org.whispersystems.textsecure.directory.NotInDirectoryException;
import org.whispersystems.textsecure.push.ContactTokenDetails;
import org.whispersystems.textsecure.push.PushServiceSocket;
import org.whispersystems.textsecure.push.UnregisteredUserException;
import org.whispersystems.textsecure.storage.SessionUtil;
import org.whispersystems.textsecure.util.DirectoryUtil;
import org.whispersystems.textsecure.util.InvalidNumberException;

import java.io.IOException;

import ws.com.google.android.mms.pdu.SendReq;

public class UniversalTransport {

  private final Context       context;
  private final MasterSecret  masterSecret;
  private final PushTransport pushTransport;
  private final SmsTransport  smsTransport;
  private final MmsTransport  mmsTransport;

  public UniversalTransport(Context context, MasterSecret masterSecret) {
    this.context       = context;
    this.masterSecret  = masterSecret;
    this.pushTransport = new PushTransport(context, masterSecret);
    this.smsTransport  = new SmsTransport(context, masterSecret);
    this.mmsTransport  = new MmsTransport(context, masterSecret);
  }

  public void deliver(SmsMessageRecord message)
      throws UndeliverableMessageException, UntrustedIdentityException, RetryLaterException,
             SecureFallbackApprovalException, InsecureFallbackApprovalException
  {
    if (message.isForcedSms()) {
      smsTransport.deliver(message);
      return;
    }

    if (!TextSecurePreferences.isPushRegistered(context)) {
      deliverDirectSms(message);
      return;
    }

    try {
      Recipient recipient = message.getIndividualRecipient();
      String    number    = Util.canonicalizeNumber(context, recipient.getNumber());

        if (isPushTransport(number) && !message.isKeyExchange()) {
        boolean isSmsFallbackSupported = isSmsFallbackSupported(number);

        try {
          Log.w("UniversalTransport", "Using GCM as transport...");
          pushTransport.deliver(message);
        } catch (UnregisteredUserException uue) {
          Log.w("UniversalTransport", uue);
          if (isSmsFallbackSupported) fallbackOrAskApproval(message, number);
          else                        throw new UndeliverableMessageException(uue);
        } catch (IOException ioe) {
          Log.w("UniversalTransport", ioe);
          if (isSmsFallbackSupported) fallbackOrAskApproval(message, number);
          else                        throw new RetryLaterException(ioe);
        }
      } else {
        Log.w("UniversalTransport", "Using SMS as transport...");
        deliverDirectSms(message);
      }
    } catch (InvalidNumberException e) {
      Log.w("UniversalTransport", e);
      deliverDirectSms(message);
    }
  }

  public MmsSendResult deliver(SendReq mediaMessage, long threadId)
      throws UndeliverableMessageException, RetryLaterException, UntrustedIdentityException,
             SecureFallbackApprovalException, InsecureFallbackApprovalException
  {
    if (MmsDatabase.Types.isForcedSms(mediaMessage.getDatabaseMessageBox())) {
      return mmsTransport.deliver(mediaMessage);
    }

    if (Util.isEmpty(mediaMessage.getTo())) {
      return deliverDirectMms(mediaMessage);
    }

    if (GroupUtil.isEncodedGroup(mediaMessage.getTo()[0].getString())) {
      return deliverGroupMessage(mediaMessage, threadId);
    }

    if (!TextSecurePreferences.isPushRegistered(context)) {
      return deliverDirectMms(mediaMessage);
    }

    if (isMultipleRecipients(mediaMessage)) {
      return deliverDirectMms(mediaMessage);
    }

    try {
      String destination = Util.canonicalizeNumber(context, mediaMessage.getTo()[0].getString());

      if (isPushTransport(destination)) {
        boolean isSmsFallbackSupported = isSmsFallbackSupported(destination);

        try {
          Log.w("UniversalTransport", "Using GCM as transport...");
          pushTransport.deliver(mediaMessage, threadId);
          return new MmsSendResult("push".getBytes("UTF-8"), 0, true, true);
        } catch (IOException ioe) {
          Log.w("UniversalTransport", ioe);
          if (isSmsFallbackSupported) return fallbackOrAskApproval(mediaMessage, destination);
          else                        throw new RetryLaterException(ioe);
        } catch (RecipientFormattingException e) {
          Log.w("UniversalTransport", e);
          if (isSmsFallbackSupported) return fallbackOrAskApproval(mediaMessage, destination);
          else                        throw new UndeliverableMessageException(e);
        } catch (EncapsulatedExceptions ee) {
          Log.w("UniversalTransport", ee);
          if (!ee.getUnregisteredUserExceptions().isEmpty()) {
            if (isSmsFallbackSupported) return mmsTransport.deliver(mediaMessage);
            else                        throw new UndeliverableMessageException(ee);
          } else {
            throw new UntrustedIdentityException(ee.getUntrustedIdentityExceptions().get(0));
          }
        }
      } else {
        Log.w("UniversalTransport", "Delivering media message with MMS...");
        return deliverDirectMms(mediaMessage);
      }
    } catch (InvalidNumberException ine) {
      Log.w("UniversalTransport", ine);
      return deliverDirectMms(mediaMessage);
    }
  }

  private MmsSendResult fallbackOrAskApproval(SendReq mediaMessage, String destination)
      throws SecureFallbackApprovalException, UndeliverableMessageException, InsecureFallbackApprovalException
  {
    try {
      Recipient recipient                     = RecipientFactory.getRecipientsFromString(context, destination, false).getPrimaryRecipient();
      boolean   isSmsFallbackApprovalRequired = isSmsFallbackApprovalRequired(destination);

      if (!isSmsFallbackApprovalRequired) {
        Log.w("UniversalTransport", "Falling back to MMS");
        DatabaseFactory.getMmsDatabase(context).markAsForcedSms(mediaMessage.getDatabaseMessageId());
        return mmsTransport.deliver(mediaMessage);
      } else if (!SessionUtil.hasEncryptCapableSession(context, masterSecret, recipient)) {
        Log.w("UniversalTransport", "Marking message as pending insecure SMS fallback");
        throw new InsecureFallbackApprovalException("Pending user approval for fallback to insecure SMS");
      } else {
        Log.w("UniversalTransport", "Marking message as pending secure SMS fallback");
        throw new SecureFallbackApprovalException("Pending user approval for fallback secure to SMS");
      }
    } catch (RecipientFormattingException rfe) {
      throw new UndeliverableMessageException(rfe);
    }
  }

  private void fallbackOrAskApproval(SmsMessageRecord smsMessage, String destination)
      throws SecureFallbackApprovalException, UndeliverableMessageException, InsecureFallbackApprovalException
  {
    Recipient recipient                     = smsMessage.getIndividualRecipient();
    boolean   isSmsFallbackApprovalRequired = isSmsFallbackApprovalRequired(destination);

    if (!isSmsFallbackApprovalRequired) {
      Log.w("UniversalTransport", "Falling back to SMS");
      DatabaseFactory.getSmsDatabase(context).markAsForcedSms(smsMessage.getId());
      smsTransport.deliver(smsMessage);
    } else if (!SessionUtil.hasEncryptCapableSession(context, masterSecret, recipient)) {
      Log.w("UniversalTransport", "Marking message as pending insecure fallback.");
      throw new InsecureFallbackApprovalException("Pending user approval for fallback to insecure SMS");
    } else {
      Log.w("UniversalTransport", "Marking message as pending secure fallback.");
      throw new SecureFallbackApprovalException("Pending user approval for fallback to secure SMS");
    }
  }

  private MmsSendResult deliverGroupMessage(SendReq mediaMessage, long threadId)
      throws RetryLaterException, UndeliverableMessageException
  {
    if (!TextSecurePreferences.isPushRegistered(context)) {
      throw new UndeliverableMessageException("Not push registered!");
    }

    try {
      pushTransport.deliver(mediaMessage, threadId);
      return new MmsSendResult("push".getBytes("UTF-8"), 0, true, true);
    } catch (IOException e) {
      Log.w("UniversalTransport", e);
      throw new RetryLaterException(e);
    } catch (RecipientFormattingException e) {
      throw new UndeliverableMessageException(e);
    } catch (InvalidNumberException e) {
      throw new UndeliverableMessageException(e);
    } catch (EncapsulatedExceptions ee) {
      Log.w("UniversalTransport", ee);
      try {
        for (UnregisteredUserException unregistered : ee.getUnregisteredUserExceptions()) {
          IncomingGroupMessage quitMessage = IncomingGroupMessage.createForQuit(mediaMessage.getTo()[0].getString(), unregistered.getE164Number());
          DatabaseFactory.getEncryptingSmsDatabase(context).insertMessageInbox(masterSecret, quitMessage);
          DatabaseFactory.getGroupDatabase(context).remove(GroupUtil.getDecodedId(mediaMessage.getTo()[0].getString()), unregistered.getE164Number());
        }

        for (UntrustedIdentityException untrusted : ee.getUntrustedIdentityExceptions()) {
          IncomingIdentityUpdateMessage identityMessage = IncomingIdentityUpdateMessage.createFor(untrusted.getE164Number(), untrusted.getIdentityKey(), mediaMessage.getTo()[0].getString());
          DatabaseFactory.getEncryptingSmsDatabase(context).insertMessageInbox(masterSecret, identityMessage);
        }

        return new MmsSendResult("push".getBytes("UTF-8"), 0, true, true);
      } catch (IOException ioe) {
        throw new AssertionError(ioe);
      }
    }
  }

  private void deliverDirectSms(SmsMessageRecord message)
      throws InsecureFallbackApprovalException, UndeliverableMessageException
  {
    if (TextSecurePreferences.isDirectSmsAllowed(context)) {
      smsTransport.deliver(message);
    } else {
      throw new UndeliverableMessageException("Direct SMS delivery is disabled!");
    }
  }

  private MmsSendResult deliverDirectMms(SendReq message)
      throws InsecureFallbackApprovalException, UndeliverableMessageException
  {
    if (TextSecurePreferences.isDirectSmsAllowed(context)) {
      return mmsTransport.deliver(message);
    } else {
      throw new UndeliverableMessageException("Direct MMS delivery is disabled!");
    }
  }

  public boolean isMultipleRecipients(SendReq mediaMessage) {
    int recipientCount = 0;

    if (mediaMessage.getTo() != null) {
      recipientCount += mediaMessage.getTo().length;
    }

    if (mediaMessage.getCc() != null) {
      recipientCount += mediaMessage.getCc().length;
    }

    if (mediaMessage.getBcc() != null) {
      recipientCount += mediaMessage.getBcc().length;
    }

    return recipientCount > 1;
  }

  private boolean isSmsFallbackApprovalRequired(String destination) {
    return (isSmsFallbackSupported(destination) && TextSecurePreferences.isFallbackSmsAskRequired(context));
  }

  private boolean isSmsFallbackSupported(String destination) {
    if (GroupUtil.isEncodedGroup(destination)) {
      return false;
    }

    if (TextSecurePreferences.isPushRegistered(context) &&
        !TextSecurePreferences.isFallbackSmsAllowed(context))
    {
      return false;
    }

    Directory directory = Directory.getInstance(context);
    return directory.isSmsFallbackSupported(destination);
  }

  private boolean isPushTransport(String destination) {
    if (GroupUtil.isEncodedGroup(destination)) {
      return true;
    }
Log.d("UniversalTransport","destination="+destination);
    Directory directory = Directory.getInstance(context);

    try {
      return directory.isActiveNumber(destination);
    } catch (NotInDirectoryException e) {
      try {
        PushServiceSocket   socket         = PushServiceSocketFactory.create(context);
        String              contactToken   = DirectoryUtil.getDirectoryServerToken(destination);
Log.d("UniversalTransport","contactToken="+contactToken);
        ContactTokenDetails registeredUser = socket.getContactTokenDetails(contactToken);

        if (registeredUser == null) {
Log.d("UniversalTransport","registeredUser==null");
          registeredUser = new ContactTokenDetails();
          registeredUser.setNumber(destination);
          directory.setNumber(registeredUser, false);
          return false;
        } else {
          registeredUser.setNumber(destination);
          directory.setNumber(registeredUser, true);
          return true;
        }
      } catch (IOException e1) {
        Log.w("UniversalTransport", e1);
        return false;
      }
    }
  }
}
