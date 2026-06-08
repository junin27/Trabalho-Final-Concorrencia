package br.com.bancodigital.service;

import br.com.bancodigital.domain.Account;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * SafeTransferService implements TransferService.
 * Single Responsibility: Execute transfers thread-safely using ordered locking to prevent deadlocks.
 */
public class SafeTransferService implements TransferService {

    private static final Logger LOGGER = Logger.getLogger(SafeTransferService.class.getName());

    @Override
    public void transfer(Account origin, Account destination, BigDecimal amount) {
        if (origin.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account: " + origin.getId());
        }

        // Establish a global lock acquisition order using account ID comparison
        Account firstLock = origin.getId().compareTo(destination.getId()) < 0 ? origin : destination;
        Account secondLock = firstLock == origin ? destination : origin;

        synchronized (firstLock) {
            synchronized (secondLock) {
                executeTransfer(origin, destination, amount);
            }
        }
    }

    private void executeTransfer(Account origin, Account destination, BigDecimal amount) {
        if (origin.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(String.format(
                    "Transfer failed: Insufficient balance in account %s. Balance: %s, Required: %s",
                    origin.getId(), origin.getBalance(), amount));
        }

        origin.withdraw(amount);
        destination.deposit(amount);

        LOGGER.info(String.format("Transferred %s: Account %s (bal: %s) -> %s (bal: %s)",
                amount, origin.getId(), origin.getBalance(), destination.getId(), destination.getBalance()));
    }
}
