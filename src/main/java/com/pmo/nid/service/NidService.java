package com.pmo.nid.service;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.pmo.nid.soap.schemas.AddressType;
import com.pmo.nid.soap.schemas.AlphaAuth;
import com.pmo.nid.soap.schemas.BiometricAuth;
import com.pmo.nid.soap.schemas.BiometricAuthResponse;
import com.pmo.nid.soap.schemas.BiometricDataType;
import com.pmo.nid.soap.schemas.CitizenDataProviderForPublicServiceCommissions;
import com.pmo.nid.soap.schemas.CitizenDataProviderForPublicServiceCommissionsResponse;
import com.pmo.nid.soap.schemas.CitizenDataProviderForPublicServiceCommissionsType;
import com.pmo.nid.soap.schemas.CustomHeaderType;
import com.pmo.nid.soap.schemas.DemographicDataType;
import com.pmo.nid.soap.schemas.FingerPosition;
import com.pmo.nid.soap.schemas.FingerPrintDataType;
import com.pmo.nid.soap.schemas.ImageType;
import com.pmo.nid.soap.schemas.Nin;
import com.pmo.nid.soap.schemas.ObjectFactory;
import com.pmo.nid.soap.schemas.VerifyNIN;
import com.pmo.nid.soap.schemas.VerifyNINResponse;
import com.pmo.nid.soap.schemas.VerifyNINType;

import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;

import com.pmo.nid.dto.BiometricAuthRequestDto;
import com.pmo.nid.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class NidService {
    private final WebServiceTemplate webServiceTemplate;
    private String qualifiedUser = "PMO";
    private String qualifiedOrigin = "pmo.gov.np";
    private final ObjectFactory objectFactory = new ObjectFactory();

    public boolean verifyNin(String ninIdentifier) {
        try {
            var verifyNin = new VerifyNIN();
            var request = new VerifyNINType();

            // Custom Header
            CustomHeaderType customHeader = new CustomHeaderType();
            customHeader.setQualifiedUser(qualifiedUser);
            customHeader.setQualifiedOrigin(qualifiedOrigin);
            customHeader.setPurpose("Verification");

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new java.util.Date());
            var ts = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            customHeader.setTimeStamp(ts);

            // NIN
            Nin nin = new Nin();
            nin.setIdentifier(ninIdentifier);
            nin.setSource("NIR");

            request.setCustomHeader(customHeader);
            request.setNin(nin);

            verifyNin.setRequest(request);

            // var jaxbElement = new JAXBElement<>(
            //         new QName("http://type.eservices.nidmc.morpho.com/", "verifyNIN"),
            //         VerifyNIN.class,
            //         verifyNin);

            // var jaxbElement = new JAXBElement<>(
            //         new QName("http://wsdl.eservices.nidmc.morpho.com/", "verifyNIN"),
            //         VerifyNIN.class,
            //         verifyNin);

            var jaxbElement = objectFactory.createVerifyNIN(verifyNin);

            // Send the request and receive the response
            var soapAction = new SoapActionCallback("verifyNIN");
            var response =  webServiceTemplate.marshalSendAndReceive(jaxbElement, soapAction);

            var ninResponse = ((JAXBElement<VerifyNINResponse>) response).getValue();
            return ninResponse.getStatus().equals("VALID");
        } catch (Exception e) {
            //
            throw new BadRequestException(e.getMessage());
        }
    }

    public CitizenDataProviderForPublicServiceCommissionsResponse getCitizenData(String ninIdentifier) {
        try {
            var entity = new CitizenDataProviderForPublicServiceCommissions();
            var request = new CitizenDataProviderForPublicServiceCommissionsType();

            // Custom Header
            CustomHeaderType customHeader = new CustomHeaderType();
            customHeader.setQualifiedUser(qualifiedUser);
            customHeader.setQualifiedOrigin(qualifiedOrigin);
            customHeader.setPurpose("Verification");

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new java.util.Date());
            var ts = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            customHeader.setTimeStamp(ts);

            // NIN
            Nin nin = new Nin();
            nin.setIdentifier(ninIdentifier);
            nin.setSource("NIR");

            request.setCustomHeader(customHeader);
            request.setNin(nin);
            request.setCcNumberLoc("");

            entity.setRequest(request);

            var jaxbElement = objectFactory.createCitizenDataProviderForPublicServiceCommissions(entity);

            // Send the request and receive the response
            var soapAction = new SoapActionCallback("citizenDataProviderForPublicServiceCommissions");
            var soapResponse =  webServiceTemplate.marshalSendAndReceive(jaxbElement, soapAction);

            var response = ((JAXBElement<CitizenDataProviderForPublicServiceCommissionsResponse>) soapResponse).getValue();
            return response;
        } catch (Exception e) {
            //
            throw new BadRequestException(e.getMessage());
        }
    }

    public BiometricAuthResponse authenticateWithFingerprint(BiometricAuthRequestDto dto) {
        try {
            var entity = new BiometricAuth();

            // Custom Header
            CustomHeaderType customHeader = new CustomHeaderType();
            customHeader.setQualifiedUser(qualifiedUser);
            customHeader.setQualifiedOrigin(qualifiedOrigin);
            customHeader.setPurpose("Verification");

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new java.util.Date());
            var ts = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            customHeader.setTimeStamp(ts);

            // NIN
            Nin nin = new Nin();
            nin.setIdentifier(dto.getNin());
            nin.setSource("NIR");

            var fingerprintData = new FingerPrintDataType();
            fingerprintData.setFingerPosition(FingerPosition.valueOf(dto.getFingerprintPosition()));
            fingerprintData.setFingerImageResolution(500);
            fingerprintData.setFingerImageType(ImageType.PNG);
            fingerprintData.setFingerImage(dto.getImageData());

            var biometricData = new BiometricDataType();
            biometricData.setFingerPrintData(fingerprintData);

            entity.setCustomHeader(customHeader);
            entity.setNin(nin);
            entity.setBiometricData(biometricData);

            var jaxbElement = objectFactory.createBiometricAuth(entity);

            // Send the request and receive the response
            var soapAction = new SoapActionCallback("biometricAuth");
            var soapResponse =  webServiceTemplate.marshalSendAndReceive(jaxbElement, soapAction);

            var response = ((JAXBElement<BiometricAuthResponse>) soapResponse).getValue();
            return response;
        } catch (Exception e) {
            //
            throw new BadRequestException(e.getMessage());
        }
    }
}
