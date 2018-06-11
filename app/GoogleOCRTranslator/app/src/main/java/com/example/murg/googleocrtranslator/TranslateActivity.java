package com.example.murg.googleocrtranslator;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TranslateActivity extends AppCompatActivity {




    public static String choosenLanguage = "";
    public static String recivedText = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        getSupportActionBar().hide();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final ApiClient apiClient = retrofit.create(ApiClient.class);


        Button translateButton = (Button) findViewById(R.id.translate_button);



        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        final EditText editText = (EditText) findViewById(R.id.edit_text_translate);
        ListView listView = (ListView) findViewById(R.id.languages_list);

        if(bundle != null){
            recivedText = bundle.getString("detectedText");
        }

        String []languages = getLanguages();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages);
        listView.setAdapter(adapter);

        final EditText editSearch = (EditText) findViewById(R.id.search_edit_text);
        editSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = editSearch.getText().toString().toLowerCase(Locale.getDefault());
                adapter.getFilter().filter(text);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });



        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

                recivedText = editText.getText().toString().toLowerCase(Locale.getDefault());

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });






        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String language = adapter.getItem(position);
                choosenLanguage = language;
                editSearch.setText(language);
            }
        });


        editText.setText(recivedText);



        translateButton.setOnClickListener(new View.OnClickListener() {






            @Override
            public void onClick(View v) {

                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }



                if(recivedText.equals("")) {
                    Toast.makeText(TranslateActivity.this, "Please enter the text you want to translate.", Toast.LENGTH_SHORT).show();

                } else if (choosenLanguage.equals("")) {
                    Toast.makeText(TranslateActivity.this, "Please choose the language.", Toast.LENGTH_SHORT).show();

                } else {
                    final ProgressDialog pDialog = ProgressDialog.show(TranslateActivity.this, "", "Translating. Please wait.", true);

                    Call<TranslatedText> callTranslate = apiClient.translateString(recivedText, choosenLanguage);
                    callTranslate.enqueue(new Callback<TranslatedText>() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                        @Override
                        public void onResponse(Call<TranslatedText> call, Response<TranslatedText> response) {
                            TranslatedText translatedText = response.body();
                            pDialog.dismiss();

                            if(translatedText != null) {
                                System.out.println(translatedText.getTranslatedText());


                                View popupView = LayoutInflater.from(TranslateActivity.this).inflate(R.layout.popup_window, null);
                                final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


                                TextView textView = (TextView) popupView.findViewById(R.id.txt_pop_up);

                                textView.setText(translatedText.getTranslatedText());


                                Button closeBtn = (Button) popupView.findViewById(R.id.btn_close);
                                closeBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        popupWindow.dismiss();
                                    }
                                });


                                View mainView = findViewById(R.id.translate_relative);
                                BitmapDrawable ob = new BitmapDrawable(getResources(), BlurBuilder.blur(mainView));
                                popupView.setBackground(ob);

                                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 20);

                            } else {
                                Toast.makeText(TranslateActivity.this, "Couldn't translate, please check your internet connection.", Toast.LENGTH_LONG).show();

                            }

                        }

                        @Override
                        public void onFailure(Call<TranslatedText> call, Throwable t) {
                            Toast.makeText(TranslateActivity.this, "Couldn't translate, please check your internet connection.", Toast.LENGTH_LONG).show();
                            pDialog.dismiss();
                        }
                    });


                }


            }
        });



        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }



    }


    public String [] getLanguages() {


        return new String[]{
                                "Afrikaans",
                                "Albanian",
                                "Amharic",
                                "Arabic",
                                "Armenian",
                                "Azerbaijani",
                                "Basque",
                                "Belarusian",
                                "Bengali",
                                "Bosnian",
                                "Bulgarian",
                                "Catalan",
                                "Cebuano",
                                "Chichewa",
                                "Chinese Simplified",
                                "Chinese Traditional",
                                "Corsican",
                                "Croatian",
                                "Czech",
                                "Danish",
                                "Dutch",
                                "English",
                                "Esperanto",
                                "Estonian",
                                "Filipino",
                                "Finnish",
                                "French",
                                "Frisian",
                                "Galician",
                                "Georgian",
                                "German",
                                "Greek",
                                "Gujarati",
                                "Haitian Creole",
                                "Hausa",
                                "Hawaiian",
                                "Hebrew",
                                "Hindi",
                                "Hmong",
                                "Hungarian",
                                "Icelandic",
                                "Igbo",
                                "Indonesian",
                                "Irish",
                                "Italian",
                                "Japanese",
                                "Javanese",
                                "Kannada",
                                "Kazakh",
                                "Khmer",
                                "Korean",
                                "Kurdish (Kurmanji)",
                                "Kyrgyz",
                                "Lao",
                                "Latin",
                                "Latvian",
                                "Lithuanian",
                                "Luxembourgish",
                                "Macedonian",
                                "Malagasy",
                                "Malay",
                                "Malayalam",
                                "Maltese",
                                "Maori",
                                "Marathi",
                                "Mongolian",
                                "Myanmar (Burmese)",
                                "Nepali",
                                "Norwegian",
                                "Pashto",
                                "Persian",
                                "Polish",
                                "Portuguese",
                                "Punjabi",
                                "Romanian",
                                "Russian",
                                "Samoan",
                                "Scots Gaelic",
                                "Serbian",
                                "Sesotho",
                                "Shona",
                                "Sindhi",
                                "Sinhala",
                                "Slovak",
                                "Slovenian",
                                "Somali",
                                "Spanish",
                                "Sundanese",
                                "Swahili",
                                "Swedish",
                                "Tajik",
                                "Tamil",
                                "Telugu",
                                "Thai",
                                "Turkish",
                                "Ukrainian",
                                "Urdu",
                                "Uzbek",
                                "Vietnamese",
                                "Welsh",
                                "Xhosa",
                                "Yiddish",
                                "Yoruba",
                                "Zulu"};
    }
}

