package co.uk.alpha60.modulr.atm;

public enum Notes {
   FIVE(5),
   TEN(10),
   TWENTY(20),
   FIFTY(50);

   private int value;

   Notes(int value) {
      this.value = value;
   }

   public int getValue() {
      return value;
   }
}
