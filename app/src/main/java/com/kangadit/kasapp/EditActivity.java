package com.kangadit.kasapp;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.kangadit.kasapp.helper.SqliteHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

public class EditActivity extends AppCompatActivity {

    MainActivity M = new MainActivity();

    RadioGroup radio_status;
    RadioButton radio_masuk, radio_keluar;

    EditText edt_jumlah, edt_keterangan;
    RippleView rip_simpan;
    EditText edit_tanggal;

    String tanggal, status;
    DatePickerDialog datePickerDialog;
    SqliteHelper sqliteHelper;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        status  = "";
        tanggal = ""; //untuk menyimpan tanggal ke sqlite = yyyy-mm-dd

        radio_status    = (RadioGroup) findViewById(R.id.radio_status);
        radio_masuk     = (RadioButton) findViewById(R.id.radio_masuk);
        radio_keluar    = (RadioButton) findViewById(R.id.radio_keluar);

        edt_jumlah      = (EditText) findViewById(R.id.edt_jumlah);
        edt_keterangan  = (EditText) findViewById(R.id.edt_keterangan);
        rip_simpan      = (RippleView) findViewById(R.id.rip_simpan);

        edit_tanggal = (EditText) findViewById(R.id.edit_tanggal);

        sqliteHelper = new SqliteHelper(this);
        //menyambungkan ke DB & Menampilkan data
        SQLiteDatabase database = sqliteHelper.getReadableDatabase();
        cursor = database.rawQuery(
                "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi WHERE transaksi_id='" + M.transaksi_id + "'",
                null);
        //Mulai dari awal
        cursor.moveToFirst();

        status = cursor.getString(1);
        switch (status){
            case "MASUK": radio_masuk.setChecked(true);
                break;
            case "KELUAR": radio_keluar.setChecked(true);
                break;
        }

        radio_status.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case  R.id.radio_masuk:
                        status = "MASUK";
                        break;
                    case R.id.radio_keluar:
                        status = "KELUAR";
                        break;
                }
                Log.d("Log status", status);
            }
        });

        edt_jumlah.setText( cursor.getString(2) );
        edt_keterangan.setText( cursor.getString(3) );

        tanggal = cursor.getString(4);
        edit_tanggal.setText(cursor.getString(5));

        Calendar calendar = Calendar.getInstance();
        final int year    = calendar.get(Calendar.YEAR);
        final int month   = calendar.get(Calendar.MONTH);
        final int day     = calendar.get(Calendar.DAY_OF_MONTH);

        edit_tanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                datePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        NumberFormat numberFormat = new DecimalFormat("00");
                        tanggal = numberFormat.format(year) + "-" + numberFormat.format((month + 1)) + "-" +
                                    numberFormat.format(dayOfMonth);

                        edit_tanggal.setText(numberFormat.format(dayOfMonth) + "/" + numberFormat.format((month + 1)) + "/" + numberFormat.format(year));
                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });

        rip_simpan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

                if (status.equals("") || edt_jumlah.getText().toString().equals("") || edt_keterangan.getText().toString().equals("") ){
                    Toast.makeText(EditActivity.this,"Isi data dengan benar", Toast.LENGTH_LONG).show();
                } else {
                    //menyambungkan dengan database
                    SQLiteDatabase database = sqliteHelper.getWritableDatabase();
//                    database.execSQL(
//                            "INSERT INTO transaksi (status, jumlah, keterangan) VALUES ('" + status + "','" + edt_jumlah.getText().toString() + "','" + edt_keterangan.getText().toString() + "')"
//                    );
                    database.execSQL(
                            "UPDATE transaksi SET status = '" + status + "', jumlah = '" + edt_jumlah.getText().toString() +
                                    "', keterangan = '" + edt_keterangan.getText().toString() + "', tanggal = '" + tanggal +
                                    "' WHERE transaksi_id = '" + M.transaksi_id + "'"
                    );
                    //jika sudah menginput akan otomatis memunculkan toast
                    Toast.makeText(EditActivity.this,"Perubahan data transaksi berhasil disimpan",
                            Toast.LENGTH_LONG).show();
                    finish();
                }

            }
        });

        //set judul
        getSupportActionBar().setTitle("Edit");
        //set Kembali
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Tombol kembali
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
