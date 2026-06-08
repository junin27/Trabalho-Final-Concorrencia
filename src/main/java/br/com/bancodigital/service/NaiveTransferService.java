package br.com.bancodigital.service;

import br.com.bancodigital.config.BankConfig;
import br.com.bancodigital.domain.Account;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * NaiveTransferService implements TransferService.
 * Single Responsibility: Execute transfers without synchronization to demonstrate race conditions.
 */
public class NaiveTransferService implements TransferService {

    private static final Logger LOGGER = Logger.getLogger(NaiveTransferService.class.getName());

    @Override
    public void transfer(Account origin, Account destination, BigDecimal amount) {
        if (origin.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(String.format(
                    "Transfer failed: Insufficient balance in account %s. Balance: %s, Required: %s",
                    origin.getId(), origin.getBalance(), amount));
        }

        BigDecimal originBalance = origin.getBalance();
        BigDecimal destBalance = destination.getBalance();

        // Introduce artificial processing delay to force context switching
        simulateDelay();

        origin.setBalance(originBalance.subtract(amount));
        destination.setBalance(destBalance.add(amount));

        LOGGER.info(String.format("Transferred %s: Account %s (bal: %s) -> %s (bal: %s)",
                amount, origin.getId(), origin.getBalance(), destination.getId(), destination.getBalance()));
    }

    private void simulateDelay() {
        try {
            Thread.sleep(BankConfig.ARTIFICIAL_DELAY_MS);
        } catch (InterruptedException e) {
            LOGGER.warning("Transfer execution was interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
