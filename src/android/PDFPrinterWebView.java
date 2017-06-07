package com.pdf.generator;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Printer;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.System;

import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.os.Environment;

/**
 * Created by cesar on 22/01/2017.
 */

public class PDFPrinterWebView extends WebViewClient {

    private PrintManager printManager = null;
    private static final String TAG = "PDFPrinterWebView";

    //Cordova Specific, delete this safely if not using cordova.
    private CallbackContext cordovaCallback;
    private Context ctx;

    private String fileName;

    public PDFPrinterWebView(PrintManager _printerManager, Context ctx){
        printManager = _printerManager;
        this.ctx = ctx;
    }

    public void setCordovaCallback(CallbackContext cordovaCallback){
        this.cordovaCallback = cordovaCallback;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);

        //PDFPrinter pdfPrinter = new PDFPrinter(webView, fileName);
        //printManager.print("PDF", pdfPrinter, null);

        String jobName = fileName + " Document";
        PrintAttributes attributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        PdfPrint pdfPrint = new PdfPrint(attributes);
        String temp_filename = "output_" + System.currentTimeMillis() + ".pdf";

        // permissions required in ANDROID.MANIFEST
        // this is a temporary measure in order to get the PDF saved to disk
        // after this is done we'll try to go straight to a ByteArrayOutputStream
        // (or something that allows in-memory only file saving)
        //    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
        pdfPrint.print(webView.createPrintDocumentAdapter(jobName), path, temp_filename);

        String encodedBase64 = null;
        try {
            File printedFile = new File(path, temp_filename);
            FileInputStream fileInputStreamReader = new FileInputStream(printedFile);
            byte[] bytes = new byte[(int)printedFile.length()];
            fileInputStreamReader.read(bytes);

            encodedBase64 = Base64.encodeToString( bytes, Base64.DEFAULT );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.cordovaCallback.success(encodedBase64);
    }

}
