package com.ak2006.tools.pronounceaword;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class PronounceAWordActivity extends Activity implements View.OnClickListener,TextToSpeech.OnInitListener {

    private Button selectAFileBtn;
    private Button talkNextWordBtn;
    private Button talkRndWordBtn;
    private Button talkRepeatWordBtn;
    private Button verifyBtn;
    private TextView wordTxt;
    private TextView selectedFile;
    private TextView status;
    private EditText enteredTxt;

    private TextToSpeech textToSpeech;



    private List<String> words;
    private int wordIndex;
    private int lastWordIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronounce_aword);

        textToSpeech = new TextToSpeech(this, this);

        selectAFileBtn = (Button)findViewById(R.id.selectFileBtn);
        talkNextWordBtn = (Button)findViewById(R.id.talkNextBtn);
        talkRndWordBtn = (Button)findViewById(R.id.talkRandomBtn);
        talkRepeatWordBtn = (Button)findViewById(R.id.talkRepeatBtn);
        verifyBtn = (Button)findViewById(R.id.verifyBtn);

        wordTxt = (TextView)findViewById(R.id.yourWordTxt);
        selectedFile = (TextView)findViewById(R.id.selectedFile);
        status = (TextView)findViewById(R.id.status);
        enteredTxt = (EditText)findViewById(R.id.typedWord);

        selectAFileBtn.setOnClickListener(this);
        talkNextWordBtn.setOnClickListener(this);
        talkRndWordBtn.setOnClickListener(this);
        talkRepeatWordBtn.setOnClickListener(this);
        verifyBtn.setOnClickListener(this);

        enableButtons(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pronounce_aword, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Toast.makeText(this,"Created by Biju B Nair",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
    if(v.getId() == R.id.selectFileBtn){
        wordTxt.setText("");
        status.setText("");
        selectedFile.setText("");
        showFileChooserDialog();
    }else if(v.getId() == R.id.talkNextBtn){
       talk(wordIndex);
        if(wordIndex == words.size() - 1) {
            wordIndex = 0;
        } else {
            wordIndex++;
        }
    }else if(v.getId() == R.id.talkRandomBtn){
        talk(getRandomWordIndex());
    }else if(v.getId() == R.id.talkRepeatBtn){
        talk(lastWordIndex);
    }else if(v.getId() == R.id.verifyBtn){
        verify();
    }
    }

    private void verify() {
        String word = words.get(lastWordIndex);
        String entered = enteredTxt.getText().toString();
        if(word.equalsIgnoreCase(entered)){
            textToSpeech.speak("Correct", TextToSpeech.QUEUE_FLUSH, null);

        } else {
            wordTxt.setText(word);
            textToSpeech.speak("Wrong", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private int getRandomWordIndex() {
        Random rand = new Random();
        int min = 0;
        int max = words.size();
        int index = rand.nextInt(max-min+1)+min;
        return index;
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            }
        } else {
            Log.e("error", "Initialization Failed!");
        }
    }

    private void talk(int wordIndex){
        String word = words.get(wordIndex);
        lastWordIndex = wordIndex;
        status.setText(String.format("Showing %d/%d",(wordIndex+1),words.size()));
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        textToSpeech.shutdown();
    }

    private void showFileChooserDialog() {
        FileChooser fileChooser =  new FileChooser(this);
        fileChooser.setFileListener(
                new FileChooser.FileSelectedListener() {
                        @Override
                        public void fileSelected(final File file) {
                            processFile(file);
                        }
                }
        );
        fileChooser.showDialog();

    }

    private void processFile(File file) {
        readFileContent(file);
         if (words.size() > 0) {
            enableButtons(true);
            selectedFile.setText(file.getName());
        } else {
             enableButtons(false);
            Toast.makeText(this, "No Content or Error File Selected", Toast.LENGTH_LONG).show();
        }
    }

    private void enableButtons(boolean flag) {
        talkNextWordBtn.setEnabled(flag);
        talkRndWordBtn.setEnabled(flag);
        talkRepeatWordBtn.setEnabled(flag);
        verifyBtn.setEnabled(flag);

        enteredTxt.setEnabled(flag);
    }

    public boolean isValidText(String line) {
        char[] chars = line.toCharArray();

        StringBuffer b = new StringBuffer();

        for (char c : chars) {
            b.append(c);
             if(!(c >= ' ' && c<='z')) {
                return false;
            }
        }

        return true;
    }

    private void readFileContent(File file) {
        BufferedReader reader = null;
         try{
            words = new ArrayList<String>();
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while((line = reader.readLine())!=null){
               if(!isValidText(line)){
                    words.clear();
                    return;
                }
                String[] dic = line.split("\\W+");
                words.addAll(Arrays.asList(dic));
            }
            wordIndex = 0;
         }catch(Exception e){
        } finally{
           if(reader != null){
               try{reader.close();}catch(Exception e){}
           }
        }
     }
}
