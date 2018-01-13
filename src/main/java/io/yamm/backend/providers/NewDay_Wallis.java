package io.yamm.backend.providers;

import io.yamm.backend.YAMM;

import java.rmi.RemoteException;

@SuppressWarnings("unused") // instantiated by YAMM by reflection
public class NewDay_Wallis extends NewDay {
    public static final String name = "Wallis Mastercard";

    public NewDay_Wallis(char[][] credentials, YAMM yamm) throws RemoteException {
        super(credentials, yamm);
    }

    protected String getSlug() {
        return "wallis";
    }
}
