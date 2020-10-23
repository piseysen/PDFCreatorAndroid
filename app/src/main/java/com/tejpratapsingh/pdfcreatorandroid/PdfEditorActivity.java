package com.tejpratapsingh.pdfcreatorandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.print.PDFPrint;
import android.print.PrintAttributes;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.tejpratapsingh.pdfcreator.utils.FileManager;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;

import static com.tejpratapsingh.pdfcreatorandroid.R.*;

public class PdfEditorActivity extends AppCompatActivity {
    private static final String TAG = "PdfEditorActivity";

    private WebView webView;

    public static class MyWebViewClient extends WebViewClient {

        public interface OnSourceReceived {
            void success(String html);
        }

        private OnSourceReceived onSourceReceived;

        public MyWebViewClient(OnSourceReceived onSourceReceived) {
            this.onSourceReceived = onSourceReceived;
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("source://")) {
                try {
                    String html = URLDecoder.decode(url, "UTF-8").substring(9);
                    onSourceReceived.success(html);
                } catch (UnsupportedEncodingException e) {
                    Log.e("example", "failed to decode source", e);
                }
                return true;
            }
            // For all other links, let the WebView do it's normal thing
            return false;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_pdf_editor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pdf Editor");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(color.colorTransparentBlack)));
        }

        webView = (WebView) findViewById(id.webViewEditor);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new MyWebViewClient(new MyWebViewClient.OnSourceReceived() {
            @Override
            public void success(String html) {
                Log.d(TAG, "success: html: " + html);
                FileManager.getInstance().cleanTempFolder(getApplicationContext());
                final File savedPDFFile = FileManager.getInstance().createTempFile(getApplicationContext(), "pdf", false);
                PDFUtil.generatePDFFromWebView(savedPDFFile, webView, new PDFPrint.OnPDFPrintListener() {
                    @Override
                    public void onSuccess(File file) {
                        Uri pdfUri = Uri.fromFile(savedPDFFile);

                        Intent intentPdfViewer = new Intent(PdfEditorActivity.this, PdfViewerActivity.class);
                        intentPdfViewer.putExtra(PdfViewerActivity.PDF_FILE_URI, pdfUri);

                        startActivity(intentPdfViewer);
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                    }
                });
            }
        }));

//        webView.loadData("<!DOCTYPE html>\n" +
//                "<html>\n" +
//                "<body>\n" +
//                "\n" +
//                "<p contenteditable=\"true\">This is a paragraph. It is editable. Try to change this text.</p>\n" +
//                "\n" +
//                "</body>\n" +
//                "</html>\n", "text/HTML", "UTF-8");

        webView.loadUrl("https://scyrencop.github.io/invoice-html5/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pdf_editor, menu);
        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case id.menuPrintPdf: {
                webView.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}