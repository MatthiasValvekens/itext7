package com.itextpdf.bouncycastle.tsp;

import com.itextpdf.bouncycastle.asn1.cmp.PKIFailureInfoBC;
import com.itextpdf.commons.bouncycastle.asn1.cmp.IPKIFailureInfo;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampRequest;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampResponse;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampToken;

import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;

public class TimeStampResponseBC implements ITimeStampResponse {

    private final TimeStampResponse timeStampResponse;

    public TimeStampResponseBC(TimeStampResponse timeStampRequest) {
        this.timeStampResponse = timeStampRequest;
    }

    public TimeStampResponse getTimeStampResponse() {
        return timeStampResponse;
    }

    @Override
    public void validate(ITimeStampRequest request) throws TSPExceptionBC {
        try {
            timeStampResponse.validate(((TimeStampRequestBC) request).getTimeStampRequest());
        } catch (TSPException e) {
            throw new TSPExceptionBC(e);
        }
    }

    @Override
    public IPKIFailureInfo getFailInfo() {
        return new PKIFailureInfoBC(timeStampResponse.getFailInfo());
    }

    @Override
    public ITimeStampToken getTimeStampToken() {
        return new TimeStampTokenBC(timeStampResponse.getTimeStampToken());
    }

    @Override
    public String getStatusString() {
        return timeStampResponse.getStatusString();
    }
}

