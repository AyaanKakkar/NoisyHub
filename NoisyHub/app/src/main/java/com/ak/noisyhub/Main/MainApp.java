
package com.ak.noisyhub.Main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;


import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
//import edu.cmu.pocketsphinx.demo.R;


public class MainApp extends Activity implements
        RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static  String KWS_SEARCH = "wakeup";
    private static  String S1ON;
    private static  String S1OFF;
    private static  String S2ON;
    private static  String S2OFF;
    private static  String S3ON;
    private static  String S3OFF;
    private static  String S4ON;
    private static  String S4OFF;

    static Spinner switch1;
    static Spinner switch2;
    static Spinner switch3;
    static Spinner switch4;

    private static final String MENU_SEARCH = "menu";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "noisy boys";

    File menugram2;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    static int akakak=0;

    private SpeechRecognizer recognizer;

    private Socket socket=null;

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mServiceInfo;
    public String MCUAddress;

    public String sendstuff="";


    private static final String SERVICE_TYPE = "_http._tcp.";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.main);

        switch1=(Spinner) findViewById(R.id.spinner1);
        switch2=(Spinner) findViewById(R.id.spinner2);
        switch3=(Spinner) findViewById(R.id.spinner3);
        switch4=(Spinner) findViewById(R.id.spinner4);



        MCUAddress = "";

        mNsdManager = (NsdManager)(getApplicationContext().getSystemService(Context.NSD_SERVICE));
        initializeResolveListener();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

//        this.sockThread = new Thread(new socketThread());
//        this.sockThread.run();


        Sensey.getInstance().init(this);
        ShakeDetector.ShakeListener shakeListener=new ShakeDetector.ShakeListener() {
            @Override public void onShakeDetected() {
                // Shake detected, do something
                Calendar c=Calendar.getInstance();
                akakak=c.get(Calendar.HOUR_OF_DAY)*60*60*1000;
                akakak+=c.get(Calendar.MINUTE)*60*1000;
                akakak+=c.get(Calendar.SECOND)*1000;
                akakak+=c.get(Calendar.MILLISECOND);
            }

            @Override public void onShakeStopped() {

            }
        };


        Sensey.getInstance().startShakeDetection(shakeListener);

        // Check if user has given permission to record audio



        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();





    }

//    class socketThread implements Runnable{
//        public void run()
//        {
//            try {
//                if(socket==null) {
//                    socket = new Socket("192.168.43.126", 80);
//                }
////            InetAddress ip=socket.getInetAddress();
////            String hostname=ip.getHostName();
////            System.out.println(hostname);
//                DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
//                for(int i=0;i<2;i++)
//                {
//                    DOS.writeUTF("Hello World");
//                }
//                DOS.flush();
//
////                DOS.close();
////            System.out.println(socket.isConnected());
//                socket.close();
//            }
//            catch(IOException e)
//            {
////            System.out.println("abc");
//            }
//        }
//
//    }

    public class ClientThread implements Runnable {

        public void run() {
            try {
//                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
//                Log.d("ClientActivity", "C: Connecting...");
                if(socket==null) {
                    socket = new Socket(MCUAddress, 80);
                }
                //                connected = true;
//                Calendar c = Calendar.getInstance();
//                int seconds = c.get(Calendar.SECOND);

                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                        .getOutputStream())), true);
                out.println(sendstuff);
//                }while (connected) {
//                    try {
//                        Log.d("ClientActivity", "C: Sending command.");
//                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
//                                .getOutputStream())), true);
//                        out.println(seconds);
//                        Log.d("ClientActivity", "C: Sent."+seconds);
//                    } catch (Exception e) {
////                        Log.e("ClientActivity", "S: Error!", e);
//                    }
//                }
                //Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {

            }
        }


    }
    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
//                ((TextView) findViewById(R.id.textview1)).setText("OKK");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
//                ((TextView) findViewById(R.id.textview1)).setText("OKK");
                // A service was found!  Do something with it.
                String name = service.getServiceName();
                String type = service.getServiceType();
                Log.d("NSD", "Service Name=" + name);
                Log.d("NSD", "Service Type=" + type);

                if (name.contains("esp8266")) {
//                    ((TextView) findViewById(R.id.textview1)).setText(service.getHost().toString());
                    Log.d("NSD", "Service Found @ '" + name + "'");

                    mNsdManager.resolveService(service, mResolveListener);

                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
//                ((TextView) findViewById(R.id.textview1)).setText("OKK2");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e("NSD", "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mServiceInfo = serviceInfo;

                // Port is being returned as 9. Not needed.
                //int port = mServiceInfo.getPort();

                InetAddress host = mServiceInfo.getHost();
                String address = host.getHostAddress();
                Log.d("NSD", "Resolved address = " + address);
                MCUAddress = address;
                ((TextView) findViewById(R.id.textView1)).setText(address);


            }
        };
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainApp> activityReference;
        SetupTask(MainApp activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
            } else {
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        socket.close();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        Sensey.getInstance().stop();
    }


    public void setCommands(View view)
    {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }



        final int SIZE=8;
        String commands[]=new String[SIZE];

//        EditText editText=findViewById(R.id.editText1);
//        commands[0]=editText.getText().toString();
//        editText=findViewById(R.id.editText2);
//        commands[1]=editText.getText().toString();
//        editText=findViewById(R.id.editText3);
//        commands[2]=editText.getText().toString();
//        editText=findViewById(R.id.editText4);
//        commands[3]=editText.getText().toString();
//        editText=findViewById(R.id.editText5);
//        commands[4]=editText.getText().toString();
//        editText=findViewById(R.id.editText6);
//        commands[5]=editText.getText().toString();
//        editText=findViewById(R.id.editText7);
//        commands[6]=editText.getText().toString();
//        editText=findViewById(R.id.editText8);
//        commands[7]=editText.getText().toString();
//
//
        String sp1=String.valueOf(switch1.getSelectedItem()).toLowerCase();
        String sp2=String.valueOf(switch2.getSelectedItem()).toLowerCase();
        String sp3=String.valueOf(switch3.getSelectedItem()).toLowerCase();
        String sp4=String.valueOf(switch4.getSelectedItem()).toLowerCase();

        String s1[]=sp1.split("/",2);
        String s2[]=sp2.split("/",2);
        String s3[]=sp3.split("/",2);
        String s4[]=sp4.split("/",2);

        commands[0]=s1[0];
        commands[1]=s1[1];
        commands[2]=s2[0];
        commands[3]=s2[1];
        commands[4]=s3[0];
        commands[5]=s3[1];
        commands[6]=s4[0];
        commands[7]=s4[1];





        String str="#JSGF V1.0;\n\ngrammar menu;\n\npublic <item> =";
        for(int i=0;i<SIZE-1;i++)
        {
            str+=" "+commands[i]+" |";
        }
        str+=" "+commands[SIZE-1]+";\n";
        try{
            FileWriter fw=new FileWriter(menugram2);
            for (int i = 0; i < str.length(); i++)
                fw.write(str.charAt(i));
            fw.close();
        }
        catch(IOException e)
        {

        }

        new SetupTask(this).execute();
    }

    public void calcCorrect(String command)
    {
        Calendar c=Calendar.getInstance();
        int x=c.get(Calendar.HOUR_OF_DAY)*60*60*1000;
        x+=c.get(Calendar.MINUTE)*60*1000;
        x+=c.get(Calendar.SECOND)*1000;
        x+=c.get(Calendar.MILLISECOND);
        if(x-akakak<3500)
        {
            TextView texting=findViewById(R.id.textView1);
            texting.setText(""+command+" "+x+" "+akakak+S1ON);

            if(command.equals(S1ON))
            {
                sendstuff="1";
            }
            else if(command.equals(S1OFF)) sendstuff="2";
            else if(command.equals(S2ON)) sendstuff="3";
            else if(command.equals(S2OFF)) sendstuff="4";
            else if(command.equals(S3ON)) sendstuff="5";
            else if(command.equals(S3OFF)) sendstuff="6";
            else if(command.equals(S4ON)) sendstuff="7";
            else if(command.equals(S4OFF)) sendstuff="8";
            else sendstuff="9";
            Thread cThread = new Thread(new ClientThread());
            cThread.start();

//            this.sockThread.run();



            akakak=0;
//            this.sockThread.start();
        }
        else
        {
            TextView texting=findViewById(R.id.textView1);
            texting.setText("You forgot to shake buddy");
        }

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            switchSearch(MENU_SEARCH);

        }
        else if (text.equals(S1ON)) {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S1OFF))
        {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S2ON)) {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S2OFF))
        {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S3ON)) {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S3OFF))
        {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S4ON)) {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
        else if (text.equals(S4OFF))
        {
            calcCorrect(text);
            switchSearch(KWS_SEARCH);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onBeginningOfSpeech() {
    }


    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);
    }

    private void setupRecognizer(File assetsDir) throws IOException {

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))


                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        File menuGrammar = new File(assetsDir, "menu.gram");
        menugram2=menuGrammar;


        int ch;
        String s="";
        FileReader fr=null;
        try
        {
            fr = new FileReader(menugram2);
        }
        catch(FileNotFoundException e)
        {
//            System.out.println("File not found");
        }
        try
        {
            while ((ch=fr.read())!=-1)
                s+=(char)ch;
            fr.close();
        }
        catch(IOException f)
        {

        }
        s=s.replace(" ", "");
        int equalto=s.indexOf("=");
        int f1=s.indexOf("|", equalto);
        S1ON=s.substring(equalto+1, f1);
        int f2=s.indexOf("|", f1+1);
        S1OFF=s.substring(f1+1, f2);
        int f3=s.indexOf("|", f2+1);
        S2ON=s.substring(f2+1, f3);
        int f4=s.indexOf("|", f3+1);
        S2OFF=s.substring(f3+1, f4);
        int f5=s.indexOf("|", f4+1);
        S3ON=s.substring(f4+1, f5);
        int f6=s.indexOf("|", f5+1);
        S3OFF=s.substring(f5+1, f6);
        int f7=s.indexOf("|", f6+1);
        S4ON=s.substring(f6+1, f7);
        int f8=s.indexOf(";", f7+1);
        S4OFF=s.substring(f7+1,f8);


        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);


        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

    }

    @Override
    public void onError(Exception error) {
//        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
}
