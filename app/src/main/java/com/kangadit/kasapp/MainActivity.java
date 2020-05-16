package com.kangadit.kasapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kangadit.kasapp.helper.SqliteHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView text_masuk, text_keluar, text_saldo;
    ListView list_kas;
    SwipeRefreshLayout swipe_refresh;

    String query_kas, query_total;
    SqliteHelper sqliteHelper;
    Cursor cursor;

    ArrayList<HashMap<String, String>> aruskas = new ArrayList<>();

    public static TextView text_filter;
    public static String transaksi_id, tgl_dari, tgl_ke;
    public static boolean filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        transaksi_id    = "";
        tgl_dari        = "";
        tgl_ke          = "";
        filter          = false;

        text_masuk      = (TextView) findViewById(R.id.text_masuk);
        text_keluar     = (TextView) findViewById(R.id.text_keluar);
        text_saldo      = (TextView) findViewById(R.id.text_saldo);
        list_kas        = (ListView) findViewById(R.id.list_kas);
        swipe_refresh   = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        text_filter     = (TextView) findViewById(R.id.text_filter);

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                query_kas = "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
                query_total =
                        "SELECT SUM(jumlah) AS total, (SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') as masuk," +
                                "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') as keluar FROM transaksi";

                KasAdapter();
            }
        });

        sqliteHelper = new SqliteHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TambahActivity.class);
                startActivity(intent);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        query_kas =
                "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi ORDER BY transaksi_id DESC";
        query_total =
                "SELECT SUM(jumlah) AS total, (SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK') as masuk," +
                        "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR') as keluar FROM transaksi";

        if (filter){
            query_kas =
                    "SELECT *, strftime('%d/%m/%Y', tanggal) AS tgl FROM transaksi " +
                            "WHERE (tanggal >= '"+ tgl_dari +"') AND (tanggal <= '"+ tgl_ke +"') ORDER BY transaksi_id DESC";
            query_total =
                    "SELECT SUM(jumlah) AS total, " +
                            "(SELECT SUM(jumlah) FROM transaksi WHERE status='MASUK' AND (tanggal >= '"+ tgl_dari +"') AND (tanggal <= '"+ tgl_ke +"') ) ," +
                            "(SELECT SUM(jumlah) FROM transaksi WHERE status='KELUAR' AND (tanggal >= '"+ tgl_dari +"') AND (tanggal <= '"+ tgl_ke +"') ) " +
                            " FROM transaksi WHERE (tanggal >= '"+ tgl_dari +"') AND (tanggal <= '"+ tgl_ke +"') ";
        }

        KasAdapter();
    }

    private void KasAdapter(){

        swipe_refresh.setRefreshing(false);
        aruskas.clear(); list_kas.setAdapter(null);

        //menyambungkan ke DB & Menampilkan data
        SQLiteDatabase database = sqliteHelper.getReadableDatabase();
        cursor = database.rawQuery(query_kas, null);
        //Mulai dari awal
        cursor.moveToFirst();

        for (int i=0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);

            HashMap<String, String> map = new HashMap<>();
            map.put("transaksi_id", cursor.getString(0));
            map.put("status",       cursor.getString(1));
            map.put("jumlah",       cursor.getString(2));
            map.put("keterangan",   cursor.getString(3));
//            map.put("tanggal",      cursor.getString(4));
            map.put("tanggal",      cursor.getString(5));

            aruskas.add(map);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, aruskas, R.layout.list_kas,
               new String[] { "transaksi_id", "status", "jumlah", "keterangan", "tanggal" },
               new int[] {R.id.text_transaksi_id, R.id.text_status, R.id.text_jumlah, R.id.text_keterangan, R.id.text_tanggal});

        list_kas.setAdapter(simpleAdapter);
        list_kas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                transaksi_id = ( (TextView) view.findViewById(R.id.text_transaksi_id)).getText().toString();
                ListMenu();
            }
        });

        KasTotal();
    }

    private void KasTotal(){

        NumberFormat rupiah = NumberFormat.getInstance(Locale.GERMANY);

        //menyambungkan ke DB & Menampilkan data
        SQLiteDatabase database = sqliteHelper.getReadableDatabase();
        cursor = database.rawQuery(query_total, null);
        //Mulai dari awal
        cursor.moveToFirst();

        text_masuk.setText(rupiah.format(cursor.getDouble(1)));
        text_keluar.setText(rupiah.format(cursor.getDouble(2)));
        text_saldo.setText(
                rupiah.format(cursor.getDouble(1) - cursor.getDouble(2))
        );

        if (!filter){
            text_filter.setText("SEMUA");
        }
        filter = false;

    }

    private void ListMenu(){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.list_menu);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        dialog.show(); //untuk memunculkan

        TextView text_edit = (TextView)dialog.findViewById(R.id.text_edit);
        TextView text_hapus = (TextView)dialog.findViewById(R.id.text_hapus);

        text_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); //untuk menghilangkan
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        text_hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); //untuk menghilangkan
                hapus();
            }
        });
    }

    private void hapus(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Yakin untuk menghapus transaksi ini?");
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //menyambungkan dengan database
                        SQLiteDatabase database = sqliteHelper.getWritableDatabase();
                        database.execSQL(
                                "DELETE FROM transaksi WHERE transaksi_id='" + transaksi_id + "'"
                        );
                        //jika sudah menginput akan otomatis memunculkan toast
                        Toast.makeText(MainActivity.this,"Data transaksi berhasil dihapus",
                                Toast.LENGTH_LONG).show();
                        KasAdapter();

                    }
                });
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            startActivity(new Intent(MainActivity.this, FilterActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
