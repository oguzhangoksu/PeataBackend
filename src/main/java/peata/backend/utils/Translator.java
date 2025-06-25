// package peata.backend.utils;

// import org.apache.commons.text.translate.CharSequenceTranslator;
// import org.apache.commons.text.translate.LookupTranslator;

// import java.text.Normalizer;
// import java.util.Locale;

// public class Translator {

//     private static final CharSequenceTranslator asciiTranslator = new LookupTranslator(new String[][]{
//         {"Ç", "C"}, {"ç", "c"},
//         {"Ğ", "G"}, {"ğ", "g"},
//         {"İ", "I"}, {"ı", "i"},
//         {"Ö", "O"}, {"ö", "o"},
//         {"Ş", "S"}, {"ş", "s"},
//         {"Ü", "U"}, {"ü", "u"}
//     });

//     public static String normalize(String input) {
//         String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
//         normalized = asciiTranslator.translate(normalized);
//         return normalized
//                 .toLowerCase(Locale.ROOT)
//                 .replaceAll("[^a-z0-9\\-\\.]", ""); // Sadece geçerli karakterler
//     }
// }