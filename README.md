# OCR Translator
Android app that with the help of OpenCV (using SVM algorithm) detects the text in a image and format it to a string. Then if you wish to, you can translate it to any language that Google Tranlate supports.

</br>

## Feautures

* Take a photo with camera and extract whole block of text, line or a word from it. You can translate that text to a choosen language. 
* Choose an image from gallery and extract whole block of text, line or a word from it. You can translate that text to a choosen language.
* Translate your written text.


</br>

## API usage
* #### API URL: https://ocr-google-translate-api.herokuapp.com/api/v1



* #### Request : TranslateText (POST)
  * Headers:
    * 'Content-Type' : 'application/x-www-form-urlencoded'

  * Endpoint:
    * '/translate'

  * Params:
    * 'text' -> String that you want to translate.
    * 'language' -> String that you want to translate.

</br>


## Built With

* [google-translate-api](https://www.npmjs.com/package/google-translate-api)
* [OpenCV 3.41](https://opencv.org/)



</br>

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
