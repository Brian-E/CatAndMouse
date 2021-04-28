package com.catandmouse;

import android.content.Context;
import android.provider.Settings.Secure;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;

public class CMLicenseCheck {
    public static final String BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmfBpPRWl9cUPdJUdwExhFagTLzivCcZQdkDinrMkse4Fmt345EXJUeyroZviLDSEv/+nJy0q2o4R7ONLiggocr/NqHEDpl7uAavtZ7VoEKqE35e/2aBe/t/Z/NdJvXcCIUYrUWzPEAefXtCLnBnH8QtCoeWCF+tqKHwm7+E1cmhuFsuI1D0b/u2gQyeeSebWCutBTIAIxrPjVYLAojVnbGuaxqcPXx2LFjsgL9+CGsulrHArUYZ5IC7DOE2wyMXFFgOuhpF9kc7+Nu/kx8aXp7u4aR+rugHGzrytzCorIx/G6J4y/bxBnxVR6x4VOIJwFivzlQ3ylkYR/8k2LmLrEwIDAQAB";
    // Generate 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[] {
     -41, 65, 30, -128, -03, -57, 74, -64, 59, 88, -97,
     -42, 77, -117, -31, -113, -11, 32, -64, 83
     };

	public static void doLicenseCheck(Context context, LicenseCheckerCallback callback) {
	    LicenseChecker mChecker;
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
        		context, new ServerManagedPolicy(context,
                new AESObfuscator(SALT, CMConstants.PRO_PACKAGE, deviceId)),
            BASE64_PUBLIC_KEY);
    	
		mChecker.checkAccess(callback);
	}
	
}
