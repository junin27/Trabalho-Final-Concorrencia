package br.com.bancodigital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        boolean corrupted = false;
        
        // Try up to 10 attempts to observe the race condition and balance corruption
        for (int attempt = 0; attempt < 10; attempt++) {
            Account accountA = new Account("A", "Gilberto", BankConfig.INITIAL_BALANCE);
            Account accountB = new Account("B", "Gabriel", BankConfig.INITIAL_BALANCE);
            BigDecimal expectedTotalBalance = BankConfig.INITIAL_BALANCE.multiply(new BigDecimal("2"));

            int numThreads = BankConfig.NUMBER_OF_THREADS;
            int transfersPerThread = 100; // Increased transfers count to maximize collision chance

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(numThreads);

            submitTransferTasks(executor, startLatch, finishLatch, accountA, accountB, transfersPerThread);

            startLatch.countDown();
            finishLatch.await();
            executor.shutdown();

            BigDecimal actualTotalBalance = accountA.getBalance().add(accountB.getBalance());
            if (!expectedTotalBalance.equals(actualTotalBalance)) {
                corrupted = true;
                LOGGER.info(String.format("Corrupted balance observed on attempt %d. Total: %s", attempt, actualTotalBalance));
                break;
            }
        }
        
        assertTrue(corrupted, 
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
