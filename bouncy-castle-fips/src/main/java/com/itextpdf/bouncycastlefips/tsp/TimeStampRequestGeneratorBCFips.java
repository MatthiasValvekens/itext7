package com.itextpdf.bouncycastlefips.tsp;

import com.itextpdf.bouncycastlefips.asn1.ASN1ObjectIdentifierBCFips;
import com.itextpdf.commons.bouncycastle.asn1.IASN1ObjectIdentifier;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampRequest;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampRequestGenerator;

import java.math.BigInteger;
import java.util.Objects;
import org.bouncycastle.tsp.TimeStampRequestGenerator;

public class TimeStampRequestGeneratorBCFips implements ITimeStampRequestGenerator {

    private final TimeStampRequestGenerator requestGenerator;

    public TimeStampRequestGeneratorBCFips(TimeStampRequestGenerator requestGenerator) {
        this.requestGenerator = requestGenerator;
    }

    public TimeStampRequestGenerator getRequestGenerator() {
        return requestGenerator;
    }


    @Override
    public void setCertReq(boolean var1) {
        requestGenerator.setCertReq(var1);
    }

    @Override
    public void setReqPolicy(String reqPolicy) {
        requestGenerator.setReqPolicy(reqPolicy);
    }

    @Override
    public ITimeStampRequest generate(IASN1ObjectIdentifier objectIdentifier, byte[] imprint, BigInteger nonce) {
        return new TimeStampRequestBCFips(requestGenerator.generate(
                ((ASN1ObjectIdentifierBCFips) objectIdentifier).getASN1ObjectIdentifier(), imprint, nonce));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeStampRequestGeneratorBCFips that = (TimeStampRequestGeneratorBCFips) o;
        return Objects.equals(requestGenerator, that.requestGenerator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestGenerator);
    }

    @Override
    public String toString() {
        return requestGenerator.toString();
    }
}
