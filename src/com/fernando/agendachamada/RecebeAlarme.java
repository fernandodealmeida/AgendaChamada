package com.fernando.agendachamada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * BroadcastReceiver para receber o alarme depois do tempo especificado
 * 
 */
public class RecebeAlarme extends BroadcastReceiver {
	final String CATEGORIA = "MEULOG";

	public void onReceive(Context context, Intent intent) {
		String numero = intent.getStringExtra("numero");
		try {
			Uri uri = Uri.parse(numero);
			Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
			callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(callIntent);
		} catch (Exception exc) {
			Log.e(CATEGORIA, "STACKTRACE");
			Log.e(CATEGORIA, Log.getStackTraceString(exc));
		}
	}
}
