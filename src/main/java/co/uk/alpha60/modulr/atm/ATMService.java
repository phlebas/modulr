package co.uk.alpha60.modulr.atm;

import java.math.BigDecimal;

public interface ATMService {

   /**
    * Replenishes the supply of notes in the ATM
    * @param fives the number of five pound notes to add to the ATM
    * @param tens the number of ten pound notes to add to the ATM
    * @param twenties the number of twenty pound notes to add to the ATM
    * @param fifties the number of fifty pound notes to add to the ATM
    */
   public void replenish(Integer fives, Integer tens, Integer twenties, Integer fifties);

   /**
    * Checks an account balance
    * @param accountNumber the number of the account
    * @return a string containing the balance of the account
    * or an error message if the account could not be found.
    */
   String checkBalance(String accountNumber);

   /**
    * Withdraws cash from an account
    * @param accountNumber the account's identifying number
    * @param amount the amount to be withdrawn
    * @return a string describing the notes making up the withdrawal
    * or an error message if the requested amount could not be withdrawn.
    */
   String withdraw(String accountNumber, Integer amount);

   /**
    * Computes the total amount of cash available in the ATM
    * @return the value of cash stored in the ATM
    */
   public Integer checkAmountAvailable();
}
