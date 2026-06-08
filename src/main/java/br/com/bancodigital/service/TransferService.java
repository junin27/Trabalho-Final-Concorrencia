package br.com.bancodigital.service;

import br.com.bancodigital.domain.Account;
import java.math.BigDecimal;

/**
 * TransferService defines the contract for account transfers.
 * SRP: Decouple transfer contract from actual concurrency implementation.
 */
public interface TransferService {

    /**
     * Transfers funds from origin account to destination account.
     *
     * @param origin      Account to withdraw from.
     * @param destination Account to deposit into.
     * @param amount      The sum to transfer.
     */
    void transfer(Account origin, Account destination, BigDecimal amount);
}
