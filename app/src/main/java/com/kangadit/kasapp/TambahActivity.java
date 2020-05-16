package com.kangadit.kasapp;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.kangadit.kasapp.helper.SqliteHelper;

public class TambahActivity extends AppCompatActivity {

    RadioGroup radio_status;
    EditText edt_jumlah, edt_keterangan;
    Button btn_simpan;
    RippleView rip_simpan;

    String status;
    SqliteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah);

        status = "";
        sqliteHelper = new SqliteHelper(this);

        radio_status = (RadioGroup) findViewById(R.id.radio_status);
        edt_jumlah = (EditText) findViewById(R.id.edt_jumlah);
        edt_keterangan = (EditText) findViewById(R.id.edt_keterangan);
        btn_simpan = (Button) findViewById(R.id.btn_simpan);
        rip_simpan = (RippleView) findViewById(R.id.rip_simpan);

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

        rip_simpan.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

                if (status.equals("") || edt_jumlah.getText().toString().equals("") || edt_keterangan.getText().toString().equals("") ){
                    Toast.makeText(TambahActivity.this,"Isi data dengan benar", Toast.LENGTH_LONG).show();
                } else {
                    //menyambungkan dengan database
                    SQLiteDatabase database = sqliteHelper.getWritableDatabase();
                    database.execSQL(
                            "INSERT INTO transaksi (status, jumlah, keterangan) VALUES ('" + status + "','" + edt_jumlah.getText().toString() + "','" + edt_keterangan.getText().toString() + "')"
                    );
                    //jika sudah menginput akan otomatis memunculkan toast
                    Toast.makeText(TambahActivity.this,"Data transaksi berhasil disimpan",
                            Toast.LENGTH_LONG).show();
                    finish();
                 }

            }
        });

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //set judul
        getSupportActionBar().setTitle("Tambah Baru");
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
