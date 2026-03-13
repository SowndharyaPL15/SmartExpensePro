package com.smartexpensepro.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.models.Transaction;
import com.smartexpensepro.utils.SessionManager;
import com.smartexpensepro.utils.SmsParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        SessionManager sessionManager = new SessionManager(context);
        if (!sessionManager.isLoggedIn()) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            if (smsMessage != null) {
                fullMessage.append(smsMessage.getMessageBody());
            }
        }

        String message = fullMessage.toString();
        Log.d(TAG, "SMS received: " + message);

        if (SmsParser.isDebitSms(message)) {
            double amount = SmsParser.extractAmount(message);
            if (amount > 0) {
                String category = SmsParser.detectCategory(message);
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

                Transaction transaction = new Transaction(
                        amount,
                        category,
                        "Auto-detected from SMS",
                        date,
                        sessionManager.getUserId(),
                        true
                );

                DatabaseHelper db = DatabaseHelper.getInstance(context);
                long result = db.addTransaction(transaction);
                Log.d(TAG, "Transaction saved with ID: " + result + ", Amount: " + amount + ", Category: " + category);
            }
        }
    }
}
