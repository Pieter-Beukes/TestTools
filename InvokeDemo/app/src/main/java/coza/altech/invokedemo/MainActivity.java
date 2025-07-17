package coza.altech.invokedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.device.PrinterManager;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    PrinterManager printerManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        String sToolbarColor  = "#021E4D";
        toolbar.setBackgroundColor(Color.parseColor(sToolbarColor));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.acslogonew);
        Spinner spinner = findViewById(R.id.spinner);



        List<String> spinnerList = new ArrayList<>();
        spinnerList.add("Purchase");
        spinnerList.add("Purchase+Cashback");
        spinnerList.add("Cashwithdraw");
        spinnerList.add("Refund");
        spinnerList.add("Cancel");
        spinnerList.add("Balance Enquiry");
        spinnerList.add("Reprint Last Trx");
        spinnerList.add("Reprint List");
        spinnerList.add("Reprint Bank Slip");
        spinnerList.add("Manual Settlement");
        spinnerList.add("Download Parameters");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
    }

    private String hashString(String type,String input) {
        Log.d("ACS", "Hashing: " + input);

        //val HEX_CHARS = "0123456789ABCDEF"
        byte[] bytes = new byte[0];
        try {
            bytes = MessageDigest.getInstance(type).digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x",bytes[i]));
        }

        String messageDigest = sb.toString();

        Log.d("ACS", "Hash result: " + messageDigest);

        return messageDigest;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView txtView;
        txtView = findViewById(R.id.rxData);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String sTxt = "";
                Bundle results = data.getExtras();

                if (results != null) {

                    Bundle payload = new Bundle();
                    payload = results.getBundle("Payload");
                    for (String key : payload.keySet()) {
                        Log.d("Bundle Debug", key + " = \"" + payload.get(key) + "\"");
                        sTxt += key + " = \"" + payload.get(key) + "\"" + "\n";
                    }
                }
                if (sTxt.isEmpty() == false) {
                    txtView.setText(sTxt);
                }
                String result=data.getStringExtra("result");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    public void execute(View view) {
        Spinner spinner = findViewById(R.id.spinner);
        String selectedItem = (String) spinner.getSelectedItem();
        String sCommand;
        switch (selectedItem) {
            case "Purchase":
                sCommand = "Sale";
                break;
            case "Purchase+Cashback":
                sCommand = "PurchaseCashback";
                break;
            case "Cashwithdraw":
                sCommand = "CashWithdraw";
                break;
            case "Refund":
                sCommand = "Refund";
                break;
            case "Cancel":
                sCommand = "Cancel";
                break;
            case "Balance Enquiry":
                sCommand = "BalanceEnq";
                break;
            case "Reprint Last Trx":
                sCommand = "Reprint";
                break;
            case "Reprint List":
                sCommand = "ReprintList";
                break;
            case "Reprint Bank Slip":
                sCommand = "ReprintBank";
                break;
            case "Manual Settlement":
                sCommand = "EndOfDay";
                break;
            case "Download Parameters":
                sCommand = "ParameterDownload";
                break;
            default:
                sCommand = "Sale";
                break;
        }

        Intent launchIntent =  new Intent();
        launchIntent.setAction("CenDroid");
        launchIntent.putExtra("Operation", sCommand);
        Log.d("invokeDemo","Operation: " + sCommand);
        // Get the time
        Long milliseconds = java.lang.System.currentTimeMillis();
        String timeString = milliseconds.toString();
        launchIntent.putExtra("Time", timeString);
        Log.d("invokeDemo","Time: " + timeString);
        launchIntent.putExtra("Caller", "Caller Name");

        String hashed = hashString("SHA-1",timeString);
        hashed = hashString("SHA-256",hashed);
        hashed = hashString("SHA-512",hashed);

        launchIntent.putExtra("InvocationKey", hashed);
        Log.d("invokeDemo","Invocation Key: " + hashed);
        EditText bAmount = findViewById(R.id.amount);
        EditText bCashAmount = findViewById(R.id.cashback);

        //Use the new Int fields for Amount and Cashback

        int iAmount = 0;
        BigDecimal dParse;
        String amtStr;
        if (bAmount.getText().length() > 0) {
            amtStr = bAmount.getText().toString();
        }
        else {
            amtStr = "0.00";
        }
        dParse = new BigDecimal(amtStr);
        dParse = dParse.multiply(BigDecimal.valueOf(100));
        iAmount = dParse.setScale(0, RoundingMode.HALF_UP).intValue();
        launchIntent.putExtra("IntAmount",iAmount);



        if (bCashAmount.getText().length() > 0) {
            amtStr = bCashAmount.getText().toString();
        }
        else {
            amtStr = "0.00";
        }
        dParse = new BigDecimal(amtStr);
        dParse = dParse.multiply(BigDecimal.valueOf(100));
        iAmount = dParse.setScale(0, RoundingMode.HALF_UP).intValue();
        launchIntent.putExtra("CashbackAmount", iAmount);
        launchIntent.putExtra("CustomHeading","DebiCheck");

        String sTmp;
        EditText bExtraData = findViewById(R.id.extradata);
        if (bExtraData.getText().length() > 0) {
            sTmp = bExtraData.getText().toString();
        }
        else {
            sTmp = "This is an Intent Test";
        }
        launchIntent.putExtra("EcrHostTransfer", sTmp);

        EditText bLic = findViewById(R.id.license);

        if (bLic.getText().length() > 0) {
            sTmp = bLic.getText().toString();
        }
        else {
            sTmp = "ACS-";
        }
        launchIntent.putExtra("AppName", sTmp);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Log.d("invokeDemo","Invocation Key: ");
        try {
            startActivityForResult(launchIntent, 1);
        } catch (Exception e) {
            Log.d("InvokeDemo",e.getMessage());
        }
    }

    public void testprint(View view) {
        int fontSize = 24;
        int fontStyle = 0x0000;
        String fontName = "simsun";
        int height = 0;
        printerManager = new PrinterManager();
        printerManager.setupPage(384,-1);
        String sTestString = "0123456789ABCDEF0123456789\n" +
                             "9876543210FEDCBA9876543210\n" +
                             "The Fox Jumps Over the Rope\n" ;

        String[] texts = ((String) sTestString).split("\n");   //Split print content into multiple lines
        for (String text : texts) {
            height += printerManager.drawText(text, 0, height, fontName, fontSize, false, false, 0);   //Printed text
        }
        for (String text : texts) {
            height += printerManager.drawTextEx(text, 5, height, 384, -1, fontName, fontSize, 0, fontStyle, 0);   ////Printed text
        }
        int iResult = printerManager.printPage(0);
        printerManager.paperFeed(16);

    }


}