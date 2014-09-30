/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jack L (http://jack-l.com)
 */
public class BannedIPData {

    public String IPAddr;
    public long ExpireTime;

    public BannedIPData(String InIP, long ExpTime) {
        IPAddr = InIP;
        ExpireTime = ExpTime;
    }
}
