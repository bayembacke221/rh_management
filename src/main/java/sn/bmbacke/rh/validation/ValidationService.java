package sn.bmbacke.rh.validation;


import org.springframework.stereotype.Service;


import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final String SENEGAL_PHONE_REGEX = "^(221|\\+221)?(7[76508]|33)[0-9]{7}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(SENEGAL_PHONE_REGEX);

    /**
     * Valide un numéro de téléphone sénégalais
     * Formats acceptés:
     * - 771234567
     * - 2217712345678
     * - +2217712345678
     */
    public boolean isValidSenegalPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Formate un numéro de téléphone sénégalais au format standard
     * Retourne le numéro au format: 221XXXXXXXXX
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (!isValidSenegalPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid Senegal phone number");
        }

        // Supprimer les caractères non numériques
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // Ajouter le préfixe 221 si nécessaire
        if (cleaned.length() == 9) {
            return "221" + cleaned;
        } else if (cleaned.length() == 12) {
            return cleaned;
        }

        throw new IllegalArgumentException("Invalid phone number format");
    }

}