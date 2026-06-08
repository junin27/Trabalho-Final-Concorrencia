package br.com.bancodigital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import br.com.bancodigital.config.BankConfig;
import br.com.bancodigital.domain.Account;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SafeTransferServiceTest validates the thread-safe implementation of TransferService.
 * Ensures data integrity under stress and guarantees deadlock prevention.
 */
class SafeTransferServiceTest {

    private static final Logger LOGGER = Logger.getLogger(SafeTransferServiceTest.class.getName());
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new SafeTransferService();
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
    void shouldMaintainBalancesCorrectlyUnderStress() throws InterruptedException {
        Account accountA = new Account("A", "Gilberto", BankConfig.INITIAL_BALANCE);
        Account accountB = new Account("B", "Gabriel", BankConfig.INITIAL_BALANCE);
        BigDecimal expectedTotalBalance = BankConfig.INITIAL_BALANCE.multiply(new BigDecimal("2"));

        int numThreads = 8;
        int totalTransfers = 1000;
        int transfersPerThread = totalTransfers / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        submitStressTasks(executor, startLatch, finishLatch, accountA, accountB, transfersPerThread, numThreads);

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        BigDecimal actualTotalBalance = accountA.getBalance().add(accountB.getBalance());
        LOGGER.info(String.format("Stress Test - Expected Total: %s, Actual Total: %s", expectedTotalBalance, actualTotalBalance));
        assertEquals(expectedTotalBalance, actualTotalBalance, "Balances corrupted under concurrent stress test");
    }

    @Test
    void shouldNotDeadlockOnCrossTransfers() {
        Account accountA = new Account("A", "Gilberto", BankConfig.INITIAL_BALANCE);
        Account accountB = new Account("B", "Gabriel", BankConfig.INITIAL_BALANCE);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            int numThreads = 2;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(numThreads);

            executor.submit(new CrossTransferTask(transferService, accountA, accountB, startLatch, finishLatch));
            executor.submit(new CrossTransferTask(transferService, accountB, accountA, startLatch, finishLatch));

            startLatch.countDown();
            finishLatch.await();
            executor.shutdown();
        }, "Deadlock detected during concurrent cross-transfers!");
    }

    private void submitStressTasks(ExecutorService executor, CountDownLatch startLatch, CountDownLatch finishLatch,
                                   Account accountA, Account accountB, int count, int numThreads) {
        for (int i = 0; i < numThreads; i++) {
            final boolean direction = (i % 2 == 0);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < count; j++) {
                        if (direction) {
                            transferService.transfer(accountA, accountB, BankConfig.TRANSFER_AMOUNT);
                        } else {
                            transferService.transfer(accountB, accountA, BankConfig.TRANSFER_AMOUNT);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }
    }

    private static class CrossTransferTask implements Runnable {
        private final TransferService service;
        private final Account origin;
        private final Account destination;
        private final CountDownLatch startLatch;
        private final CountDownLatch finishLatch;

        public CrossTransferTask(TransferService s, Account o, Account d, CountDownLatch sl, CountDownLatch fl) {
            this.service = s;
            this.origin = o;
            this.destination = d;
            this.startLatch = sl;
            this.finishLatch = fl;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                for (int i = 0; i < 100; i++) {
                    service.transfer(origin, destination, BigDecimal.ONE);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        }
    }
}
