package com.example.hj.call;
public class RtcTokenBuilderSample {
    private static int expirationTimeInSeconds = 3600*2;
    public static String buildTokenWithUserAccount(String appId,String appCertificate,String channelName,String userAccount){
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        String result = token.buildTokenWithUserAccount(appId, appCertificate,
                channelName, userAccount, RtcTokenBuilder.Role.Role_Publisher, timestamp);
        return result;
    }

}
