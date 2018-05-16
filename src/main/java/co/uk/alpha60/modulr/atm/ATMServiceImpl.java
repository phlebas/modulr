package co.uk.alpha60.modulr.atm;

import co.uk.alpha60.modulr.account.AccountService;

import co.uk.alpha60.modulr.exceptions.ATMException;
import co.uk.alpha60.modulr.exceptions.InvalidAmountException;
import co.uk.alpha60.modulr.exceptions.InvalidWithdrawalAmount;
import co.uk.alpha60.modulr.exceptions.NoSuchAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ATMServiceImpl implements ATMService {

   private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.UK);

   //Allow withdrawals between 20 and 250 inclusive, in multiples of 5
   private static final Integer MINIMUM_WITHDRAWAL = 20;
   private static final Integer MAXIMUM_WITHDRAWAL = 250;

   // Holds the amount of cash held in the ATM. Each note denomination is a key into the map
   // and the value represents the number of notes in the ATM.
   private Map<Integer, Integer> notes = new HashMap<>();

   private AccountService accountService;

   private Logger logger = LoggerFactory.getLogger(this.getClass());

   /**
    * Constructor.
    * @param accountService an implementation of the Account Service interface.
    */
   public ATMServiceImpl(AccountService accountService) {
      logger.info("Setting up ATM");
      this.accountService = accountService;
      replenish(0, 0, 0 ,0);
   }

   /**
    * Computes the total amount of cash available in the ATM
    * @return the value of cash stored in the ATM
    */
   public Integer checkAmountAvailable() {
      Integer result = 0;
      for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
         result += entry.getKey() * entry.getValue();
      }

      return result;
   }

   /**
    * Replenishes the supply of notes in the ATM.
    * @param fives the number of five pound notes to add to the ATM
    * @param tens the number of ten pound notes to add to the ATM
    * @param twenties the number of twenty pound notes to add to the ATM
    * @param fifties the number of fifty pound notes to add to the ATM
    */
   public void replenish(Integer fives, Integer tens, Integer twenties, Integer fifties) {
      logger.info("Replenishing ATM.");
      notes.merge(Notes.FIVE.getValue(), fives, Integer::sum);
      notes.merge(Notes.TEN.getValue(), tens, Integer::sum);
      notes.merge(Notes.TWENTY.getValue(), twenties, Integer::sum);
      notes.merge(Notes.FIFTY.getValue(), fifties, Integer::sum);
   }

   /**
    * Removes notes from the supply in the ATM.
    * @param fives the number of five pound notes to remove from the ATM
    * @param tens the number of ten pound notes to remove from the ATM
    * @param twenties the number of twenty pound notes to remove from the ATM
    * @param fifties the number of fifty pound notes to remove from the ATM
    */
   private void updateNotesAvailable(Integer fives, Integer tens, Integer twenties, Integer fifties) {
      logger.info("Replenishing ATM.");
      notes.merge(Notes.FIVE.getValue(), -fives, Integer::sum);
      notes.merge(Notes.TEN.getValue(), -tens, Integer::sum);
      notes.merge(Notes.TWENTY.getValue(), -twenties, Integer::sum);
      notes.merge(Notes.FIFTY.getValue(), -fifties, Integer::sum);
   }

   /**
    * Checks the balance of an account.
    * @param accountNumber the number of the account
    * @return a formatted string containing the current balance or an appropriate message if
    * the balance cannot be found.
    */
   public String checkBalance(String accountNumber) {
      logger.info("Check balance for account: " + accountNumber);
      try {
         BigDecimal balance = accountService.checkBalance(accountNumber);
         return numberFormat.format(balance);
      } catch (NoSuchAccountException e) {
         logger.error("Account not found");
         return ATMMessages.NO_BALANCE_FOUND;
      }
   }

   /**
    * Checks that a withdrawal can be made.
    * Assumes that there are no daily limits for withdrawals.
    * and the account balance is equal to or greater than the amount requested.
    *
    * @param accountNumber the account to withdraw the money from
    * @param amount the amount to be withdrawn
    * @throws InvalidWithdrawalAmount when the amount requested cannot be dispensed
    * @throws NoSuchAccountException when an invalid account number is passed
    */
   private void checkWithdrawalAmount(String accountNumber, Integer amount) throws InvalidWithdrawalAmount, NoSuchAccountException {
      logger.info("Check withdrawal of " + amount + " from account "  + accountNumber);
      // Check that the amount is within limits
      if (amount < MINIMUM_WITHDRAWAL || amount > MAXIMUM_WITHDRAWAL) {
         throw new InvalidWithdrawalAmount(ATMMessages.AMOUNT_OUTSIDE_OF_LIMITS);
      }
      // Check that the amount is a multiple of the smallest note.
      if (amount % Notes.FIVE.getValue() != 0) {
         throw new InvalidWithdrawalAmount(ATMMessages.MULTIPLE_OF_FIVE);
      }
      // Check that the account has sufficient funds.
      if (amount > accountService.checkBalance(accountNumber).intValue()) {
         throw new InvalidWithdrawalAmount(ATMMessages.INSUFFICIENT_FUNDS);
      }
   }

   /**
    * Performs the withdrawal.
    * @param accountNumber the account's identifying number
    * @param amount the amount to be withdrawn
    * @return a formatted string containing the number of each type of note to be issued or an error message
    * if the amount cannot be dispensed.
    */
   public String withdraw(String accountNumber, Integer amount) {
      logger.info("Withdraw " + amount + " from account "  + accountNumber);
      try {
         checkWithdrawalAmount(accountNumber, amount);
         return doWithdrawal(accountNumber, amount);
      } catch (ATMException e) {
         return e.getMessage();
      }
   }

   /**
    * Returns notes of the correct denominations.
    * Always supplies at least one five-pound note if possible
    * Uses the smallest amount of notes.
    *
    * @param accountNumber the account to be debited if the transaction is successful
    * @param amount the amount to be withdrawn
    * @return a formatted string describing the notes dispensed.
    */
   private String doWithdrawal(String accountNumber, Integer amount) throws InvalidAmountException, NoSuchAccountException {

      Integer noteCounts[] = {0, 0, 0, 0};
      Integer availableNotes[] = {
            notes.get(Notes.FIVE.getValue()),
            notes.get(Notes.TEN.getValue()),
            notes.get(Notes.TWENTY.getValue()),
            notes.get(Notes.FIFTY.getValue())
      };
      if (withdrawalSuccessful(noteCounts, availableNotes, amount)) {

         // Debit the account
         accountService.withdrawAmount(accountNumber, BigDecimal.valueOf(amount));
         // Update the cash in the ATM
         updateNotesAvailable(noteCounts[0], noteCounts[1], noteCounts[2], noteCounts[3]);

         return String.format("%d five-pound, %d ten-pound, %d twenty-pound, %d fifty-pound",
               noteCounts[0], noteCounts[1], noteCounts[2], noteCounts[3]);
      }
      return ATMMessages.UNABLE_TO_HANDLE_REQUEST;
   }

   /**
    * Provides the correct combination of notes if one is available.
    * @param noteCounts the current number of notes making up the withdrawal
    * @param availableNotes the notes left in the ATM
    * @param amount the amount remaining to be dispensed
    * @return true iff the amount requested can be dispensed.
    */
   private boolean withdrawalSuccessful(Integer[] noteCounts, Integer[] availableNotes, Integer amount) {

      if (amount == 0) {
         return true;
      } else {
         // Fiver rule. If the amount is a multiple of ten and there are
         // at least two five pound notes available
         // we should dispense these first.
         if (amount % 10 == 0 && availableNotes[0] >= 2 && noteCounts[0] == 0) {
            noteCounts[0] += 2;
            availableNotes[0] -= 2;
            return withdrawalSuccessful(noteCounts, availableNotes, amount - 10);
         } else {
            // Check for the largest note we can dispense towards the total
            // And recurse for the remaining amount
            if (amount >= 50 && availableNotes[3] > 0) {
               noteCounts[3] += 1;
               availableNotes[3] -= 1;
               return withdrawalSuccessful(noteCounts, availableNotes, amount - 50);
            } else if (amount >= 20 && availableNotes[2] > 0) {
               noteCounts[2] += 1;
               availableNotes[2] -= 1;
               return withdrawalSuccessful(noteCounts, availableNotes, amount - 20);
            } else if (amount >= 10 && availableNotes[1] > 0) {
               noteCounts[1] += 1;
               availableNotes[1] -= 1;
               return withdrawalSuccessful(noteCounts, availableNotes, amount - 10);
            } else if (amount >= 5 && availableNotes[0] > 0) {
               noteCounts[0] += 1;
               availableNotes[0] -= 1;
               return withdrawalSuccessful(noteCounts, availableNotes, amount - 5);
            }
         }
      }
      return false;
   }
}
