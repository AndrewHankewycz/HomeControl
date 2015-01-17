package com.homecontrol.andrew.homecontrol;

public class PasscodeHelper {
    public static void checkPasscode(String pc1, String pc2) throws InvalidPasscodeException{
        if(!pc1.equals(pc2)) {
            throw new InvalidPasscodeException("The passcodes do not match");
        }
        if(pc1.matches("\\[0-9]+") && pc1.length() >= 4) {
            Log.d(TAG, pc1 + " " + pc2);
            throw new InvalidPasscodeException("Invalid passcode");
        }
    }
}