package net.respectnetwork.sdk.csp.example;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.respectnetwork.sdk.csp.BasicCSP;
import net.respectnetwork.sdk.csp.CSP;
import net.respectnetwork.sdk.csp.CSPInformation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;

public class ExampleRegister {

	/* CHOOSE THE INDIVIDUAL's CLOUD NUMBER HERE */
	private static CloudNumber cloudNumber = CloudNumber.createRandom(XDIConstants.CS_EQUALS);

	/* CHOOSE THE INDIVIDUAL's CLOUD NAME HERE */
	private static CloudName cloudName = CloudName.create("=dev.test.71");

	/* CHOOSE THE INDIVIDUAL's SECRET TOKEN HERE */
	private static String secretToken = "mysecret";

	/* CHOOSE THE INDIVIDUAL's VERIFIED PHONE NUMBER HERE */
	private static String verifiedPhone = "myphone-" + UUID.randomUUID().toString();

	/* CHOOSE THE INDIVIDUAL's VERIFIED EMAIL HERE */
	private static String verifiedEmail = "test" + UUID.randomUUID().toString() + "@test.com";

	public static void main(String[] args) throws Exception {

		// Step 0: Set up CSP

		CSPInformation cspInformation = new CSPInformationRespectNetwork();
		//CSPInformation cspInformation = new CSPInformationTTCC();

		CSP csp = new BasicCSP(cspInformation);

		// Step 1: Register Cloud with Cloud Number and Shared Secret

		csp.registerCloudInCSP(cloudNumber, secretToken);

		// step 2: Set Cloud Services in Cloud

		Map<XDI3Segment, String> services = new HashMap<XDI3Segment, String> ();

		services.put(XDI3Segment.create("<$https><$connect><$xdi>"), "http://mycloud-ote.neustar.biz:8085/personalclouds/" + URLEncoder.encode(cloudNumber.toString(), "UTF-8") + "/connect/request");

		csp.setCloudServicesInCloud(cloudNumber, secretToken, services);

		// step 3: Check if the Cloud Name is available

		CloudNumber existingCloudNumber = csp.checkCloudNameAvailableInRN(cloudName);

		if (existingCloudNumber != null) throw new RuntimeException("Cloud Name " + cloudName + " is already registered with Cloud Number " + existingCloudNumber + ".");

		// Step 4: Check if the phone number and e-mail address are available

		CloudNumber[] existingCloudNumbers = csp.checkPhoneAndEmailAvailableInRN(verifiedPhone, verifiedEmail);

		if (existingCloudNumbers[0] != null) throw new RuntimeException("This verified phone number is already registered with Cloud Number " + existingCloudNumbers[0] + ".");
		if (existingCloudNumbers[1] != null) throw new RuntimeException("This verified e-mail address is already registered with Cloud Number " + existingCloudNumbers[1] + ".");

		// step 5: Register Cloud Name

		csp.registerCloudNameInRN(cloudName, cloudNumber, verifiedPhone, verifiedEmail);
		csp.registerCloudNameInCSP(cloudName, cloudNumber);
		csp.registerCloudNameInCloud(cloudName, cloudNumber, secretToken);

		// done

		System.out.println("Done registering Cloud Name " + cloudName + " with Cloud Number " + cloudNumber + " and " + services.size() + " services.");
	}
}
