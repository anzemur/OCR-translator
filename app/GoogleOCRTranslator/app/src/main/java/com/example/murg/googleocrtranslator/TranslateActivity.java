package com.example.murg.googleocrtranslator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Locale;

public class TranslateActivity extends AppCompatActivity {




    public static String choosenLanguage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String recivedText = "";

        EditText editText = (EditText) findViewById(R.id.edit_text_translate);
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


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String language = adapter.getItem(position);
                choosenLanguage = language;
                editSearch.setText(language);
            }
        });


        editText.setText(recivedText);


    }


    public String [] getLanguages() {

        String []languages = {
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



        return languages;
    }
}

