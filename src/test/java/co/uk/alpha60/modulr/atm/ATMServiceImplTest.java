package co.uk.alpha60.modulr.atm;

import co.uk.alpha60.modulr.account.AccountNumbers;
import co.uk.alpha60.modulr.account.AccountService;
import co.uk.alpha60.modulr.account.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ATM Service Tests")
class ATMServiceImplTest {

   private ATMService atmService;

   @BeforeEach
   void init() {
      AccountService accountService = new AccountServiceImpl();
      atmService = new ATMServiceImpl(accountService);
   }

   @Test
   @DisplayName("Check filling a newly created ATM")
   void replenish() {
      assertEquals(atmService.checkAmountAvailable(), Integer.valueOf(0), "The ATM should be empty when initialised");

      atmService.replenish(1, 0, 0, 0);
      assertEquals(atmService.checkAmountAvailable(), Integer.valueOf(5),
            "The ATM should now contain five pounds");

      atmService.replenish(0, 1, 0, 0);
      assertEquals(atmService.checkAmountAvailable(), Integer.valueOf(15),
            "The ATM should now contain fifteen pounds");

      atmService.replenish(0, 0, 1, 0);
      assertEquals(atmService.checkAmountAvailable(), Integer.valueOf(35),
            "The ATM should now contain five pounds");

      atmService.replenish(0, 0, 0, 1);
      assertEquals(atmService.checkAmountAvailable(), Integer.valueOf(85),
            "The ATM should now contain five pounds");
   }

   @Test
   @DisplayName("Check balance using an incorrect account number.")
   void checkBalanceInvalidAccount() {
      assertEquals(ATMMessages.NO_BALANCE_FOUND, atmService.checkBalance(""));
   }

   @Test
   @DisplayName("Check balance using a valid account number.")
   void checkBalance() {
      assertEquals("£2,738.57", atmService.checkBalance("01001"));
   }

   @Test
   @DisplayName("Check withdrawal amount below minimum")
   void withdrawalBelowMinimum() {
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 4);
      assertEquals(ATMMessages.AMOUNT_OUTSIDE_OF_LIMITS, result);
   }

   @Test
   @DisplayName("Check withdrawal amount above maximum")
   void withdrawalAboveMaximum() {
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 251);
      assertEquals(ATMMessages.AMOUNT_OUTSIDE_OF_LIMITS, result);
   }

   @Test
   @DisplayName("Check withdrawal amount greater than account balance")
   void withdrawalAboveBalance() {
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_3, 20);
      assertEquals(ATMMessages.INSUFFICIENT_FUNDS, result);
   }

   @Test
   @DisplayName("Check withdrawal amount not a multiple of five pounds")
   void withdrawalNotAMultipleOfFive() {
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_3, 27);
      assertEquals(ATMMessages.MULTIPLE_OF_FIVE, result);
   }

   @Test
   @DisplayName("Check withdrawal amount cannot be supplied with available notes")
   void withdrawalNotPossibleWithAvailableNotes() {
      atmService.replenish(0, 0, 2, 1);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 30);
      assertEquals(ATMMessages.UNABLE_TO_HANDLE_REQUEST, result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 4 five-pound notes and 1 twenty-pound note. Request is for twenty pounds")
   void withdrawCheckingFiverRulePrecedence() {
      atmService.replenish(4, 0, 1, 0);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 20);
      assertEquals("4 five-pound, 0 ten-pound, 0 twenty-pound, 0 fifty-pound", result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 4 five-pound, 1 ten and 1 twenty-pound note. Request is for twenty pounds")
   void withdrawLowestNumberOfNotes() {
      atmService.replenish(4, 1, 1, 0);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 25);
      assertEquals("1 five-pound, 0 ten-pound, 1 twenty-pound, 0 fifty-pound", result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 3 ten-pound notes, 1 twenty-pound note and 1 fifty. Request is for fifty pounds")
   void withdrawCheckingMinimumNumberOfNotesRule() {
      atmService.replenish(0, 3, 1, 1);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 50);
      assertEquals("0 five-pound, 0 ten-pound, 0 twenty-pound, 1 fifty-pound", result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 5 fives, 5 tens, 1 twenty and 1 fifty. Request is for 250 pounds")
   void withdrawMaximum() {
      atmService.replenish(10, 5, 5, 1);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 250);
      assertEquals("10 five-pound, 5 ten-pound, 5 twenty-pound, 1 fifty-pound", result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 5 fives, 5 tens, 1 twenty and 1 fifty. Request is for 250 pounds followed by a request for 20.")
   void withdrawAfterEmptyingMachine() {
      atmService.replenish(10, 5, 5, 1);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 250);
      assertEquals("10 five-pound, 5 ten-pound, 5 twenty-pound, 1 fifty-pound", result);
      result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_1, 20);
      assertEquals(ATMMessages.UNABLE_TO_HANDLE_REQUEST, result);
   }

   @Test
   @DisplayName("Withdrawal test. ATM contains 5 fives, 5 tens, 1 twenty and 1 fifty. Request is for 20 pounds twice.")
   void withdrawAfterEmptyingAccount() {
      atmService.replenish(10, 5, 5, 1);
      String result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_2, 20);
      assertEquals("2 five-pound, 1 ten-pound, 0 twenty-pound, 0 fifty-pound", result);
      result = atmService.withdraw(AccountNumbers.ACCOUNT_NUMBER_2, 20);
      assertEquals(ATMMessages.INSUFFICIENT_FUNDS, result);
   }

   //TODO check multiple withdrawals until account emptied.
}