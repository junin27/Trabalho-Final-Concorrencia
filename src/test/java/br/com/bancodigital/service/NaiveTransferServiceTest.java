package br.com.bancodigital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import br.com.bancodigital.config.BankConfig;
import br.com.bancodigital.domain.Account;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * NaiveTransferServiceTest tests NaiveTransferService behavior.
 * Demonstrates the presence of race conditions under concurrent executions.
 */
class NaiveTransferServiceTest {

    private static final Logger LOGGER = Logger.getLogger(NaiveTransferServiceTest.class.getName());
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new NaiveTransferService();
    }

    @Test
    void shouldPerformSequentialTransferCorrectly() {
        Account accountA = new Account("A", "Gilberto", new BigDecimal("100.00"));
        Account accountB = new Account("B", "Gabriel", new BigDecimal("100.00"));

        transferService.transfer(accountA, accountB, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), accountA.getBalance());
        assertEquals(new BigDecimal("130.00"), accountB.getBalance());
    }

    @Test
    void shouldCorruptBalancesUnderConcurrentTransfers() throws InterruptedException {
        Account accountA = new Account("A", "Gilberto", BankConfig.INITIAL_BALANCE);
        Account accountB = new Account("B", "Gabriel", BankConfig.INITIAL_BALANCE);
        BigDecimal expectedTotalBalance = BankConfig.INITIAL_BALANCE.multiply(new BigDecimal("2"));

        int numThreads = BankConfig.NUMBER_OF_THREADS;
        int transfersPerThread = BankConfig.TOTAL_TRANSFERS / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        submitTransferTasks(executor, startLatch, finishLatch, accountA, accountB, transfersPerThread);

        // Start all threads simultaneously
        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        BigDecimal actualTotalBalance = accountA.getBalance().add(accountB.getBalance());
        LOGGER.info(String.format("Initial Total: %s, Final Total: %s", expectedTotalBalance, actualTotalBalance));
        LOGGER.info(String.format("Account A Balance: %s, Account B Balance: %s", accountA.getBalance(), accountB.getBalance()));

        // Due to race conditions and lost updates, the total balance should be corrupted.
        assertNotEquals(expectedTotalBalance, actualTotalBalance,
                "Balances should be corrupted due to lack of synchronization in NaiveTransferService");
    }

    private void submitTransferTasks(ExecutorService executor, CountDownLatch startLatch, CountDownLatch finishLatch,
                                     Account accountA, Account accountB, int count) {
        for (int i = 0; i < BankConfig.NUMBER_OF_THREADS; i++) {
            final boolean direction = (i % 2 == 0);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    executeMultipleTransfers(accountA, accountB, direction, count);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }
    }

    private void executeMultipleTransfers(Account a, Account b, boolean forward, int count) {
        for (int i = 0; i < count; i++) {
            if (forward) {
                transferService.transfer(a, b, BankConfig.TRANSFER_AMOUNT);
            } else {
                transferService.transfer(b, a, BankConfig.TRANSFER_AMOUNT);
            }
        }
    }
}
