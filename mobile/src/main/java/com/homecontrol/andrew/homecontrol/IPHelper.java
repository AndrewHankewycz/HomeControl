package com.homecontrol.andrew.homecontrol;

public class IPHelper {

    /**
     * @param ip Address to validate
     * @return The IP address if it is valid
     * @throws IllegalIpAddressException
     */
    public static String validateIP(String ip) throws IllegalIpAddressException {
        String finalIp = null;
        String ipMatch = "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}";
        String portMatch = ":" + "\\d{1,4}";

        if(ip.matches("http://" + ipMatch))                     // submitted http://111.111.111.111
            finalIp = ip;
        else if(ip.matches("http://" + ipMatch + portMatch))    // submitted http://111.111.111.111:8080
            finalIp = ip;
        else if(ip.matches("https://" + ipMatch))               // submitted http://111.111.111.111
            finalIp = ip;
        else if(ip.matches("https://" + ipMatch + portMatch))   // submitted http://111.111.111.111:8080
            finalIp = ip;
        else if(ip.matches(ipMatch))                            // submitted 111.111.111.111
            finalIp = "http://" + ip;
        else if(ip.matches(ipMatch + portMatch))                // submitted 111.111.111.111:8080
            finalIp = "http://" + ip;
        else
            throw new IllegalIpAddressException("Illegal IP Address found");
        return finalIp;
    }
}