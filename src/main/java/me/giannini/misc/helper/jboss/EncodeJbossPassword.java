package me.giannini.misc.helper.jboss;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;

import org.picketbox.datasource.security.SecureIdentityLoginModule;

public class EncodeJbossPassword {

  public static void main(final String[] args) throws Exception {
    final Console console = System.console();
    if (console == null) {
      System.out.println("Enter the password to be encoded:");
      final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      final String readPassword = bufferedReader.readLine();
      SecureIdentityLoginModule.main(new String[] { String.valueOf(readPassword) });
    } else {
      console.printf("Enter the password to be encoded:%n");
      final char[] readPassword = console.readPassword();
      SecureIdentityLoginModule.main(new String[] { String.valueOf(readPassword) });
    }
  }
}
