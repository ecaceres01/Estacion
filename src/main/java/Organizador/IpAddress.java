package Organizador;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class IpAddress {
    public ArrayList<String> getIp() {
        ArrayList<String> ip = new ArrayList<String>();
        boolean i = true;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip.add(addr.getHostAddress());
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }
}
