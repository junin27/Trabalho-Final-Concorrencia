package br.com.bancodigital.config;

import java.math.BigDecimal;

/**
 * BankConfig holds system-wide configuration constants.
 * Intent: Centralize configuration parameters to avoid magic numbers.
 */
public final class BankConfig {

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");
    public static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("10.00");
    public static final int NUMBER_OF_THREADS = 4;
    public static final int TOTAL_TRANSFERS = 100;
    public static final long ARTIFICIAL_DELAY_MS = 10L;

    private BankConfig() {
        // Prevent instantiation of utility class
        throw new UnsupportedOperationException("Utility class");
    }
}
