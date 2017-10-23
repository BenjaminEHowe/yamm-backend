package io.yamm.backend;

import com.mashape.unirest.http.Unirest;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CancellationException;

public class YAMM {
    private Map<UUID, Account> accounts  = new HashMap<>();
    private Random random = new SecureRandom();
    private final UserInterface ui;

    public YAMM(UserInterface ui) {
        this.ui = ui;
        Unirest.setDefaultHeader("User-Agent", "YAMMBot/" + getVersion() + "; +https://yamm.io/bot");
    }

    public void addAccount(String providerSlug) throws Exception {
        // get the provider class, or throw ClassNotFoundException
        Class<?> provider = Class.forName("io.yamm.backend.providers." + providerSlug);

        // get the "nice" name of the provider and the credentials it requires
        String name;
        String[] requiredCredentials;
        try {
            name = (String) provider.getDeclaredField("name").get(null);
            requiredCredentials = (String[]) provider.getDeclaredField("requiredCredentials").get(null);
        } catch (IllegalAccessException|NoSuchFieldException e) {
            ui.showException(e);
            return;
        }

        // ask the user for each credential
        char[][] credentials = new char[requiredCredentials.length][];
        try {
            for (int i = 0; i < requiredCredentials.length; i++) {
                credentials[i] = ui.requestCharArray(
                        "Please enter your " + name + " " + requiredCredentials[i] + ":");
            }
        } catch (NullPointerException e) {
            throw new CancellationException();
        }

        // try to instantiate the object & add it to the accounts list
        try {
            Account account = (Account) provider.getConstructor(char[][].class, YAMM.class)
                    .newInstance(credentials, this);
            accounts.put(account.getUUID(), account);
        } catch (InstantiationException|
                IllegalAccessException|
                InvocationTargetException|
                NoSuchMethodException|
                NullPointerException e) {
            throw new Exception("Error instantiating account!", e);
        }
    }

    public static Long currencyInMinorUnits(Currency currency, BigDecimal amount) {
        BigDecimal multiplier = new BigDecimal (Math.pow(10, currency.getDefaultFractionDigits()));
        return amount.multiply(multiplier).longValue();
    }

    public char[] generateSecureRandom(int length) {
        return YAMM.generateRandom(random, length);
    }

    public static char[] generateRandom(Random random, int length) {
        assert length > 0;
        final String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        char[] buffer = new char[length];
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = symbols.charAt(random.nextInt(symbols.length()));
        return buffer;
    }

    public Map<UUID, Account> getAccounts() {
        return accounts;
    }

    public String getVersion() {
        return "0.1";
    }

    public void overwriteSensitiveData() {
        for (Map.Entry<UUID, Account> account : getAccounts().entrySet()) {
            account.getValue().overwriteSensitiveData();
        }
    }

    public void raiseException(Exception e) {
        ui.showException(e);
    }
}
