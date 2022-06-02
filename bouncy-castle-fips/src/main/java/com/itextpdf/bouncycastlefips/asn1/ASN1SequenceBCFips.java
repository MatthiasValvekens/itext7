package com.itextpdf.bouncycastlefips.asn1;

import com.itextpdf.commons.bouncycastle.asn1.IASN1EncodableWrapper;
import com.itextpdf.commons.bouncycastle.asn1.IASN1Sequence;

import org.bouncycastle.asn1.ASN1Sequence;

public class ASN1SequenceBCFips extends ASN1PrimitiveBCFips implements IASN1Sequence {
    public ASN1SequenceBCFips(ASN1Sequence sequence) {
        super(sequence);
    }

    public ASN1SequenceBCFips(Object obj) {
        super(ASN1Sequence.getInstance(obj));
    }

    public ASN1Sequence getSequence() {
        return (ASN1Sequence) getPrimitive();
    }

    public IASN1EncodableWrapper getObjectAt(int i) {
        return new ASN1EncodableWrapperBCFips(getSequence().getObjectAt(i));
    }

    public int size() {
        return getSequence().size();
    }
}
