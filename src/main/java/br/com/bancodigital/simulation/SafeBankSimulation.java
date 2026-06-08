package br.com.bancodigital.simulation;

import br.com.bancodigital.config.BankConfig;
import br.com.bancodigital.domain.Account;
import br.com.bancodigital.logging.CustomLogFormatter;
import br.com.bancodigital.service.SafeTransferService;
import br.com.bancodigital.service.TransferService;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SafeBankSimulation is the main executable for the thread-safe implementation.
 * SRP: Coordinate and execute the thread-safe concurrent simulation run.
 */
public class SafeBankSimulation {

    private static final Logger LOGGER = Logger.getLogger(SafeBankSimulation.class.getName());

    public static void main(String[] args) {
        configureLogging();

        LOGGER.info("Starting Safe Digital Bank Concurrency Simulation...");

        Account accountA = new Account("1", "Gilberto (Acc A)", BankConfig.INITIAL_BALANCE);
        Account accountB = new Account("2", "Gabriel (Acc B)", BankConfig.INITIAL_BALANCE);
        BigDecimal expectedTotal = BankConfig.INITIAL_BALANCE.multiply(new BigDecimal("2"));

        LOGGER.info("Initial Balance Account A: " + accountA.getBalance());
        LOGGER.info("Initial Balance Account B: " + accountB.getBalance());
        LOGGER.info("Expected invariant total balance: " + expectedTotal);

        TransferService service = new SafeTransferService();
        runSimulation(service, accountA, accountB);

        BigDecimal actualTotal = accountA.getBalance().add(accountB.getBalance());
        LOGGER.info("=== SIMULATION COMPLETED ===");
        LOGGER.info("Final Balance Account A: " + accountA.getBalance());
        LOGGER.info("Final Balance Account B: " + accountB.getBalance());
        LOGGER.info("Actual total balance: " + actualTotal);
        LOGGER.info("Invariant Total: " + expectedTotal);
        LOGGER.info("Balance divergence (Corruption): " + expectedTotal.subtract(actualTotal));
    }

    private static void configureLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new CustomLogFormatter());
        handler.setLevel(Level.INFO);
        for (java.util.logging.Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }
        rootLogger.addHandler(handler);
    }

    private static void runSimulation(TransferService service, Account accountA, Account accountB) {
        int threads = BankConfig.NUMBER_OF_THREADS;
        int transfersPerThread = BankConfig.TOTAL_TRANSFERS / threads;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final boolean direction = (i % 2 == 0);
            executor.submit(new SafeTransferTask(service, accountA, accountB, direction, transfersPerThread, startLatch, finishLatch));
        }

        startLatch.countDown();
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Safe simulation interrupted", e);
            Thread.currentThread().interrupt();
        }
        executor.shutdown();
    }

    private static class SafeTransferTask implements Runnable {
        private final TransferService service;
        private final Account accountA;
        private final Account accountB;
        private final boolean forward;
        private final int count;
        private final CountDownLatch startLatch;
        private final CountDownLatch finishLatch;

        public SafeTransferTask(TransferService s, Account a, Account b, boolean f, int c, CountDownLatch sl, CountDownLatch fl) {
            this.service = s;
            this.accountA = a;
            this.accountB = b;
            this.forward = f;
            this.count = c;
            this.startLatch = sl;
            this.finishLatch = fl;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                for (int i = 0; i < count; i++) {
                    if (forward) {
                        service.transfer(accountA, accountB, BankConfig.TRANSFER_AMOUNT);
                    } else {
                        service.transfer(accountB, accountA, BankConfig.TRANSFER_AMOUNT);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        }
    }
}
