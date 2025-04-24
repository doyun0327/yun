package com.example.yun.controller;
import java.net.InetAddress;

import java.net.NetworkInterface;

import java.net.SocketException;

import java.net.UnknownHostException;
public class MacAddressTest {
     public static void main(String[] args){
  try {

   NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

   byte[] mac = ni.getHardwareAddress();

   String macAddr="";

   for (int i = 0; i < mac.length; i++) {

    macAddr += String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "");

   }

   System.out.println(macAddr);

  } catch (SocketException | UnknownHostException e) {

   e.printStackTrace();

  }

 }
}
