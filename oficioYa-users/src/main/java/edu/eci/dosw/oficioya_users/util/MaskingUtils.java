package edu.eci.dosw.oficioya_users.util;

/** Oculta parcialmente correo y teléfono para el perfil propio (RF-03). */
public final class MaskingUtils {

    private static final String MASK = "*****";
    private static final int VISIBLE_PHONE_DIGITS = 4;

    private MaskingUtils() {
    }

    /** juan@gmail.com -> *****@gmail.com */
    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        return at < 0 ? MASK : MASK + email.substring(at);
    }

    /** 3001234567 -> ******4567 */
    public static String maskPhone(String phone) {
        if (phone == null) {
            return null;
        }
        if (phone.length() <= VISIBLE_PHONE_DIGITS) {
            return "*".repeat(phone.length());
        }
        int hidden = phone.length() - VISIBLE_PHONE_DIGITS;
        return "*".repeat(hidden) + phone.substring(hidden);
    }
}
