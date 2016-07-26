package me.pejas.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * The annotated element, must be valid PESEL according to  act of Dz. U. z 2016 r. poz. 722.
 * <p>
 * PESEL (Polish Powszechny Elektroniczny System Ewidencji Ludności,
 * Universal Electronic System for Registration of the Population)
 * is the national identification number used in Poland since 1979.
 * It always has 11 digits, identifies just one person
 * and cannot be changed to another one (except some specific situations).
 * <p>
 * PESEL number has the form of YYMMDDZZZXQ,
 * where YYMMDD is the date of birth (with century encoded in month field),
 * ZZZX is the personal identification number,
 * where X codes sex (even number for females, odd number for males)
 * and Q is a check digit, which is used to verify whether a given PESEL is correct or not. [1]
 * <p>
 * [1] https://en.wikipedia.org/wiki/PESEL">PESEL
 *
 * @author Piotr Pejas
 */
public class PeselValidator implements ConstraintValidator<Pesel, String> {
  public static final LocalDate YEAR1850 = LocalDate.of(1850, 1, 1);
  public static final Random RANDOM = new Random();

  private boolean allowPastNow;
  private boolean allowBefore1850;
  public static final int[] WEIGHTS = new int[]{1, 3, 7, 9, 1, 3, 7, 9, 1, 3};

  @Override
  public void initialize(Pesel constraintAnnotation) {
    allowPastNow = constraintAnnotation.allowPastNow();
    allowBefore1850 = constraintAnnotation.allowBefore1850();
  }

  @Override
  public boolean isValid(String pesel, ConstraintValidatorContext context) {
    return isValid(pesel, allowPastNow, allowBefore1850);
  }

  public static boolean isValid(String pesel) {
    return isValid(pesel, false, false);
  }

  public static boolean isValid(String pesel, boolean allowPastNow, boolean allowBefore1850) {
    if (!isChecksumValid(pesel)) {
      return false;
    }
    try {
      LocalDate birthDate = getBirthDate(pesel);
      if (!allowPastNow && birthDate.isAfter(LocalDate.now())) {
        return false;
      }
      if (!allowBefore1850 && birthDate.isBefore(YEAR1850)) {
        return false;
      }
    } catch (DateTimeException ex) {
      return false;
    }

    return true;
  }

  /**
   * Calculates and validates PESEL checksum.
   * <p>
   * Having a PESEL in the form of ABCDEF GHIJK,
   * one can check the validity of the number by computing the following expression:
   * A*1 + B*3 + C*7 + D*9 + E*1 + F*3 + G*7 + H*9 + I*1 + J*3
   * Then the last digit of the result should be subtracted from 10.
   * If the result of the last operation is not equal to the last digit of a given PESEL, the PESEL is incorrect.
   *
   * @param pesel number to validate
   * @return {@code false} if checksum of {@code pesel} is invalid
   */
  public static boolean isChecksumValid(String pesel) {
    if (pesel == null) return false;

    if (pesel.length() != 11) return false;

    int checksum = 0;
    for (int i = 0; i < 10; i++)
      checksum += Character.getNumericValue(pesel.charAt(i)) * WEIGHTS[i];

    checksum %= 10;
    checksum = 10 - checksum;
    checksum %= 10;

    int expectedChecksum = Character.getNumericValue(pesel.charAt(10));

    return checksum == expectedChecksum;

  }

  public static LocalDate getBirthDate(String pesel) {
    int month = Integer.parseInt(pesel.substring(2, 4));
    int year = Integer.parseInt(pesel.substring(0, 2));
    int day = Integer.parseInt(pesel.substring(4, 6));

    if (month > 80) {
      year = 1800 + year;
      month = month - 80;
    } else if (month > 60) {
      year = 2200 + year;
      month = month - 60;
    } else if (month > 40) {
      year = 2100 + year;
      month = month - 40;
    } else if (month > 20) {
      year = 2000 + year;
      month = month - 20;
    } else {
      year = 1900 + year;
    }

    return LocalDate.of(year, month, day);
  }

  public static String generate(LocalDate date, boolean male) {
    int month = date.getMonth().getValue();
    int day = date.getDayOfMonth();
    int year = date.getYear();
    if (year < 1900)      month = month + 80;
    else if (year < 2000) {/* nothing to do */}
    else if (year < 2100) month = month + 20;
    else if (year < 2200) month = month + 40;
    else if (year < 2400) month = month + 60;
    else throw new IllegalArgumentException("Date must be between 1800 and 2499");

    year %= 100;

    int pin = RANDOM.nextInt(9999);
    if (male && pin % 2 == 0) {
      if(pin % 10 == 9) pin--;
      else pin++;
    }

    String pesel = String.format("%02d%02d%02d%04d", year, month, day, pin);
    int checksum = 0;
    for (int i = 0; i < 10; i++)
      checksum += Character.getNumericValue(pesel.charAt(i)) * WEIGHTS[i];

    checksum %= 10;
    checksum = 10 - checksum;
    checksum %= 10;

    return pesel + checksum;
  }

  //TODO generate(int fromYear, int ToYear)
  public static String generate() {
    Long daysBetween = ChronoUnit.DAYS.between(YEAR1850, LocalDate.now());
    return generate(YEAR1850.plusDays(RANDOM.nextInt(daysBetween.intValue())), RANDOM.nextBoolean());
  }


  public static boolean isMale(String pesel) {
    return Character.getNumericValue(pesel.charAt(10)) % 2 == 1;
  }

  public static boolean isFemale(String pesel) {
    return !isMale(pesel);
  }
}
