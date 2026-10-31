package edu.eci.dosw.oficioya_users.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilsTest {

    @Test
    void maskEmailHidesLocalPart() {
        assertThat(MaskingUtils.maskEmail("juan@gmail.com")).isEqualTo("*****@gmail.com");
    }

    @Test
    void maskEmailWithoutAtReturnsOnlyMask() {
        assertThat(MaskingUtils.maskEmail("sin-arroba")).isEqualTo("*****");
    }

    @Test
    void maskEmailNullReturnsNull() {
        assertThat(MaskingUtils.maskEmail(null)).isNull();
    }

    @Test
    void maskPhoneKeepsLastFourDigits() {
        assertThat(MaskingUtils.maskPhone("3001234567")).isEqualTo("******4567");
    }

    @Test
    void maskPhoneShortNumberIsFullyHidden() {
        assertThat(MaskingUtils.maskPhone("123")).isEqualTo("***");
    }

    @Test
    void maskPhoneNullReturnsNull() {
        assertThat(MaskingUtils.maskPhone(null)).isNull();
    }
}
