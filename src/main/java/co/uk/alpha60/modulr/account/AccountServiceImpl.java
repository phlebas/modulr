package co.uk.alpha60.modulr.account;

import co.uk.alpha60.modulr.exceptions.InvalidAmountException;
import co.uk.alpha60.modulr.exceptions.NoSuchAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;

import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_1;
import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_2;
import static co.uk.alpha60.modulr.account.AccountNumbers.ACCOUNT_NUMBER_3;
/**
 * An implementation of the AccountService interface.
 * For the purposes of the exercise the account information is held as
 * a map of account numbers to account balances.
 */
public class AccountServiceImpl implements AccountService {

   private HashMap<String, BigDecimal> accounts = new HashMap<>();

   Logger logger = LoggerFactory.getLogger(this.getClass());

   /**
    * Sets up the account service and initialises account data.
    */
   public AccountServiceImpl() {
      logger.debug("Setting up new AccountServiceImpl");
      accounts.put(ACCOUNT_NUMBER_1, new BigDecimal(2738.57).setScale(2, BigDecimal.ROUND_FLOOR));
      accounts.put(ACCOUNT_NUMBER_2, new BigDecimal(23.00).setScale(2, BigDecimal.ROUND_FLOOR));
      accounts.put(ACCOUNT_NUMBER_3, new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_FLOOR));
   }

   /**
    * Returns the amount currently held in the designated account.
    * @param accountNumber the account number
    * @return the amount currently held in the account
    * @throws NoSuchAccountException if the account number parameter does not match an existing account
    */
   public BigDecimal checkBalance(String accountNumber) throws NoSuchAccountException {
      logger.info("Checking balance of account: " + accountNumber);
      if (accounts.containsKey(accountNumber)) {
         return accounts.get(accountNumber);
      } else {
         logger.error("Account "+ accountNumber + " does not exist");
         throw new NoSuchAccountException(accountNumber);
      }
   }

   /**
    * Withdraws a specified amount from an account.
    * @param requestedAmount the amount to be withdrawn
    * @throws InvalidAmountException when the amount cannot be withdrawn
    * @throws NoSuchAccountException when an invalid account number is passed as a parameter
    * @return true iff the amount was successfully withdrawn.
    */
   public boolean withdrawAmount(String accountNumber, BigDecimal requestedAmount) throws InvalidAmountException, NoSuchAccountException {
      logger.info("Attempting to withdraw " + requestedAmount.toPlainString() + " from account " + accountNumber);
      BigDecimal balance = checkBalance(accountNumber);
      if (balance.compareTo(requestedAmount) >= 0) {
         accounts.put(accountNumber, balance.subtract(requestedAmount));
         return true;
      } else {
         logger.error("The requested amount could not be withdrawn");
         throw new InvalidAmountException("Insufficient funds");
      }
   }

}
