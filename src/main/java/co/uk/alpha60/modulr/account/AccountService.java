package co.uk.alpha60.modulr.account;

import co.uk.alpha60.modulr.exceptions.InvalidAmountException;
import co.uk.alpha60.modulr.exceptions.NoSuchAccountException;

import java.math.BigDecimal;

/**
 *  Interface AccountService declares:
 * - Check balance
 * - Withdraw an amount
 */
public interface AccountService {
   /**
    * Returns the amount currently held in the designated account.
    * @param accountNumber the account number
    * @return the amount currently held in the account
    * @throws NoSuchAccountException if the account number parameter does not match an existing account
    */
   BigDecimal checkBalance(String accountNumber) throws NoSuchAccountException;

   /**
    * Withdraws a specified amount from an account
    * @param requestedAmount the amount to be withdrawn
    * @throws InvalidAmountException when the amount cannot be withdrawn
    * @throws NoSuchAccountException when an invalid account number is passed as a parameter
    * @return true iff the amount was successfully withdrawn.
    */
   boolean withdrawAmount (String accountNumber, BigDecimal requestedAmount)
         throws InvalidAmountException, NoSuchAccountException;
}
