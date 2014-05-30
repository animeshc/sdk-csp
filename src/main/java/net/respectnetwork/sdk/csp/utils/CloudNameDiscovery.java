package net.respectnetwork.sdk.csp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import net.respectnetwork.sdk.csp.ssl.TLSv1Support;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

public class CloudNameDiscovery
{

   /* CHOOSE THE CLOUD NAME HERE */
   private static CloudName          cloudName;
   
   /* CHOOSE THE CLOUD NAME HERE */
   private static CloudNumber          cloudNumber;

   private static XDIDiscoveryClient discovery;

   /* CHOOSE THE endpoints */
   private static ArrayList<String>  endpoints  = new ArrayList<String>();

   private static XDI3Segment[]      epSegments = null;

   static
   {
      TLSv1Support.supportTLSv1();
      try
      {
         System.out.print("Enter environment: PROD or OTE :");
         String env = new BufferedReader(new InputStreamReader(System.in))
               .readLine();
         if (env.equalsIgnoreCase("OTE"))
         {
            discovery = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
         } else if (env.equalsIgnoreCase("PROD"))
         {
            discovery = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;
         } else
         {
            System.out.print("Environment has to be one of PROD or OTE ");
            System.exit(0);
         }

         System.out.print("Enter Cloud Name/Number: ");
         
         String cloudNameOrNumber = new BufferedReader(new InputStreamReader(System.in))
         .readLine();
          if(cloudNameOrNumber.contains("UUID") || cloudNameOrNumber.contains("uuid"))
          {
             cloudNumber = CloudNumber.create(cloudNameOrNumber);
          }
          else if(cloudNameOrNumber.startsWith("=") || cloudNameOrNumber.startsWith("+") )
          {
            cloudName = CloudName.create(cloudNameOrNumber);  
          }
         
          if(cloudName == null && cloudNumber == null)
          {
             System.out.println("Invalid cloudname/number");
             System.exit(0);
          }
         System.out
               .print("Enter EndpointURI address. Type DONE when finished. :");
         BufferedReader br = new BufferedReader(
               new InputStreamReader(System.in));
         String endpointAddress = new String();
         while (!(endpointAddress = br.readLine()).equalsIgnoreCase("DONE"))
         {
            System.out.print("Enter EndpointURI address: ");
            endpoints.add(endpointAddress);
         }

         epSegments = new XDI3Segment[endpoints.size()];
         for (int i = 0; i < endpoints.size(); i++)
         {
            epSegments[i] = XDI3Segment.create(endpoints.get(i));
         }

      } catch (IOException ex)
      {

         throw new RuntimeException(ex.getMessage(), ex);
      }
   }

   public static void main(String[] args) throws Exception
   {

      // Resolve Cloud Numbers from Name

      try
      {
         XDIDiscoveryResult discResult = null;
         if (cloudName != null)
         {
            discResult = discovery.discover(
               XDI3Segment.create(cloudName.toString()), epSegments);
         } else if (cloudNumber != null) {
            discResult = discovery.discover(
                  XDI3Segment.create(cloudNumber.toString()), epSegments);
         }         
         System.out.println("CloudNumber : " + discResult.getCloudNumber());
         System.out.println("xdi endpoint : " + discResult.getXdiEndpointUri());
         System.out.println("public key (sig) : " + discResult.getSignaturePublicKey());
         System.out.println("public key (enc) : " + discResult.getEncryptionPublicKey());
         if (discResult.getEndpointUris() != null)
         {
            Set<XDI3Segment> keyset = discResult.getEndpointUris().keySet();
            XDI3Segment[] keys = keyset.toArray(new XDI3Segment[0]);
            for (int i = 0; i < keys.length; i++)
            {
               System.out.println("Endpoint Address :" + keys[i] + ", URI : "
                     + discResult.getEndpointUris().get(keys[i]));
            }
         }

      } catch (Xdi2ClientException e)
      {
         System.out.println("Xdi2ClientException: " + e.getMessage());
         e.printStackTrace();
      }

   }
}
