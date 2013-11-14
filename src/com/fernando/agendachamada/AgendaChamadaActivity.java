package com.fernando.agendachamada;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * @author Fernando Simao
 * @version 0.1
 * Agenda ligações na data e hora selecionados.
 * Ainda está em fase alfa.
 * */
public class AgendaChamadaActivity extends Activity {

	private Button btData, btHora, btChamar, btCancelar;
	private ImageButton btContatos;

	private int mAno, mMes, mDia, mHora, mMin;

	static final int PICK_CONTACT_REQUEST = 1;
	static final int DATE_DIALOG_ID = 1;
	static final int TIME_DIALOG_ID = 2;
	static Calendar c;
	static final String TEL = "";
	private SharedPreferences shPref;
	private Date data;
	private SimpleDateFormat dataSimples;
	private AutoCompleteTextView num;
	
	private static final String [] ULTIMONUM = new String []{"123456789"};
	
	private void preencherComUltimoNumero(){
		ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, ULTIMONUM);
		num = (AutoCompleteTextView) findViewById(R.id.etNum);
		num.setAdapter(adapter);
	}
	
	@SuppressLint("SimpleDateFormat")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wake);
		
		
		preencherComUltimoNumero();

		num = (AutoCompleteTextView) findViewById(R.id.etNum);
		shPref = getSharedPreferences(TEL, MODE_PRIVATE);
		
		long dataHora = shPref.getLong("dateTime", 0);
		
		btData = (Button) findViewById(R.id.btDate);
		btHora = (Button) findViewById(R.id.btTime);
		btChamar = (Button) findViewById(R.id.btCall);
		btCancelar = (Button) findViewById(R.id.btCancel);
		btContatos = (ImageButton) findViewById(R.id.btContacts);
		dataSimples = new SimpleDateFormat();
		c = Calendar.getInstance();
		
		//Se tiver recuperado a data do sharedPreferences, insere esse dado na view
		if( dataHora != 0) {
			String numeroTexto = shPref.getString("tel", "");
			num.setText(numeroTexto);
			data = new Date(dataHora);
			AgendaChamadaActivity.c.setTime(data);
		} else {
			data = AgendaChamadaActivity.c.getTime();
		}
		
		this.mAno = AgendaChamadaActivity.c.get(Calendar.YEAR);
		this.mMes = AgendaChamadaActivity.c.get(Calendar.MONTH);
		this.mDia = AgendaChamadaActivity.c.get(Calendar.DAY_OF_MONTH);
		this.mHora = AgendaChamadaActivity.c.get(Calendar.HOUR_OF_DAY);
		this.mMin = AgendaChamadaActivity.c.get(Calendar.MINUTE);
		
		dataSimples.applyPattern("dd/MM/yyyy");
		btData.setText(dataSimples.format(data));
		
		dataSimples.applyPattern("HH:mm");
		btHora.setText(dataSimples.format(data));
		
		btData.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		btHora.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		
		btChamar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				num = (AutoCompleteTextView) findViewById(R.id.etNum);
				String numero = "tel:" + num.getText();
				
				c.set(mAno, mMes, mDia, mHora, mMin, 0);
				if (!num.getText().toString().equals("")) {
					
					//Grava os dados
					shPref = getSharedPreferences(TEL, MODE_PRIVATE);
					SharedPreferences.Editor editor = shPref.edit();
					long dateTime = c.getTimeInMillis();
					
					editor.clear().commit();
					
					editor.putLong("dateTime", dateTime);
					editor.putString("tel", ""+num.getText());
					editor.commit();
					
					ULTIMONUM[0] = num.getText().toString();
					agendar(c, numero);
				} else {
					Toast.makeText(
							AgendaChamadaActivity.this,
							"É necessario informar o número para criar o alarme",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		btCancelar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AutoCompleteTextView num = (AutoCompleteTextView) findViewById(R.id.etNum);
				if(!num.getText().toString().equals("")){
				Intent it = new Intent("EXECUTAR_ALARME");
				PendingIntent p = PendingIntent.getBroadcast(AgendaChamadaActivity.this, 0, it,
						0);

				// Cancela o alarme
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				am.cancel(p);
				
				SharedPreferences preferences = getSharedPreferences(TEL, MODE_PRIVATE);
				preferences.edit().clear().commit();
				Toast.makeText(AgendaChamadaActivity.this, "Alarme cancelado.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		btContatos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
			    pickContactIntent.setType(Phone.CONTENT_TYPE); // Mostra somente contatos com telefone
			    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
			}
		});
	
	}
	
	@Override
	protected void onActivityResult(int codigoSolicitado, int resultadoCodigo, Intent dado) {
	    if (codigoSolicitado == PICK_CONTACT_REQUEST) {
	        if (resultadoCodigo == RESULT_OK) {
	            Uri contactUri = dado.getData();
	            String[] projecao = {Phone.NUMBER};

	            Cursor cursor = getContentResolver()
	                    .query(contactUri, projecao, null, null, null);
	            cursor.moveToFirst();
	            int column = cursor.getColumnIndex(Phone.NUMBER);
	            String number = cursor.getString(column);

	            AutoCompleteTextView num = (AutoCompleteTextView) findViewById(R.id.etNum);
				num.setText(number);
	        }
	    }
	}

	private void agendar(Calendar calendar, String tel) {
		// Intent para disparar o broadcast
		Intent it = new Intent("EXECUTAR_ALARME");
		it.putExtra("numero", tel);
		PendingIntent p = PendingIntent.getBroadcast(AgendaChamadaActivity.this, 0, it,
				PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Agenda o alarme
		AlarmManager alarme = (AlarmManager) getSystemService(ALARM_SERVICE);
		long time = calendar.getTimeInMillis();
		
		alarme.set(AlarmManager.RTC_WAKEUP, time, p);

    	Toast.makeText(AgendaChamadaActivity.this, "Alarme agendado com súcesso", Toast.LENGTH_LONG).show();
		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mAno, mMes,
					mDia);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeListener, mHora, mMin, true);
		}
		return null;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {

		case DATE_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(mAno, mMes, mDia);
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mHora, mMin);
			break;

		}
	}

	private void atualizaDisplay() {
		// Mes inicial eh 0 entao adiciona 1 para mostrar corretamente na view
		btData.setText(
	            new StringBuilder()
	                    .append(mDia).append("/")
	                    .append(mMes + 1).append("/")
	                    .append(mAno));
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int ano, int mesDoAno,
				int diaDoMes) {
			AgendaChamadaActivity.this.mAno = ano;
			AgendaChamadaActivity.this.mMes = mesDoAno;
			AgendaChamadaActivity.this.mDia = diaDoMes;
			atualizaDisplay();
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hora, int minuto) {
			AgendaChamadaActivity.this.mHora = hora;
			AgendaChamadaActivity.this.mMin = minuto;
			atualizaHora(hora, minuto);
		}
	};

	public void atualizaHora(int hora, int minuto) {
		btHora.setText(
	            new StringBuilder()
	                    .append(pad(hora)).append(":")
	                    .append(pad(minuto)));
	}
	
   private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
