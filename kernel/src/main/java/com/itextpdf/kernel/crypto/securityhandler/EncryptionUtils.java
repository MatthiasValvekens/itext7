/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.kernel.crypto.securityhandler;

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.asn1.IASN1InputStream;
import com.itextpdf.commons.bouncycastle.asn1.IASN1Primitive;
import com.itextpdf.commons.bouncycastle.asn1.x509.IAlgorithmIdentifier;
import com.itextpdf.commons.bouncycastle.cert.IX509CertificateHolder;
import com.itextpdf.commons.bouncycastle.cms.ICMSEnvelopedData;
import com.itextpdf.commons.bouncycastle.cms.IRecipientInformation;
import com.itextpdf.kernel.exceptions.KernelExceptionMessageConstant;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfEncryptor;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.security.IExternalDecryptionProcess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

final class EncryptionUtils {

    private static final IBouncyCastleFactory BOUNCY_CASTLE_FACTORY = BouncyCastleFactoryCreator.getFactory();

    static byte[] generateSeed(int seedLength) {
        byte[] seedBytes;
        KeyGenerator key;
        try {
            key = KeyGenerator.getInstance("AES");
            key.init(192, new SecureRandom());
            SecretKey sk = key.generateKey();
            seedBytes = new byte[seedLength];
            // create the 20 bytes seed
            System.arraycopy(sk.getEncoded(), 0, seedBytes, 0, seedLength);
        } catch (NoSuchAlgorithmException e) {
            seedBytes = SecureRandom.getSeed(seedLength);
        }
        return seedBytes;
    }

    static byte[] fetchEnvelopedData(Key certificateKey, Certificate certificate, String certificateKeyProvider,
                                     IExternalDecryptionProcess externalDecryptionProcess, PdfArray recipients) {
        boolean foundRecipient = false;
        byte[] envelopedData = null;

        IX509CertificateHolder certHolder;
        try {
            certHolder = BOUNCY_CASTLE_FACTORY.createX509CertificateHolder(certificate.getEncoded());
        } catch (Exception f) {
            throw new PdfException(KernelExceptionMessageConstant.PDF_DECRYPTION, f);
        }
        if (externalDecryptionProcess == null) {
            for (int i = 0; i < recipients.size(); i++) {
                PdfString recipient = recipients.getAsString(i);
                ICMSEnvelopedData data;
                try {
                    data = BOUNCY_CASTLE_FACTORY.createCMSEnvelopedData(recipient.getValueBytes());
                    Iterator<IRecipientInformation> recipientCertificatesIt =
                            data.getRecipientInfos().getRecipients().iterator();
                    while (recipientCertificatesIt.hasNext()) {
                        IRecipientInformation recipientInfo = recipientCertificatesIt.next();

                        if (recipientInfo.getRID().match(certHolder) && !foundRecipient) {
                            envelopedData = PdfEncryptor.getContent(recipientInfo, (PrivateKey) certificateKey, certificateKeyProvider);
                            foundRecipient = true;
                        }
                    }
                } catch (Exception f) {
                    throw new PdfException(KernelExceptionMessageConstant.PDF_DECRYPTION, f);
                }
            }
        } else {
            for (int i = 0; i < recipients.size(); i++) {
                PdfString recipient = recipients.getAsString(i);
                ICMSEnvelopedData data;
                try {
                    data = BOUNCY_CASTLE_FACTORY.createCMSEnvelopedData(recipient.getValueBytes());
                    IRecipientInformation recipientInfo =
                            data.getRecipientInfos().get(externalDecryptionProcess.getCmsRecipientId());
                    if (recipientInfo != null) {
                        envelopedData = recipientInfo.getContent(externalDecryptionProcess.getCmsRecipient());
                        foundRecipient = true;
                    }
                } catch (Exception f) {
                    throw new PdfException(KernelExceptionMessageConstant.PDF_DECRYPTION, f);
                }
            }
        }

        if (!foundRecipient || envelopedData == null) {
            throw new PdfException(KernelExceptionMessageConstant.BAD_CERTIFICATE_AND_KEY);
        }
        return envelopedData;
    }

    static byte[] cipherBytes(X509Certificate x509certificate, byte[] abyte0, IAlgorithmIdentifier algorithmidentifier)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algorithmidentifier.getAlgorithm().getId());
        try {
            cipher.init(Cipher.ENCRYPT_MODE, x509certificate);
        } catch (InvalidKeyException e) {
            cipher.init(Cipher.ENCRYPT_MODE, x509certificate.getPublicKey());
        }
        return cipher.doFinal(abyte0);
    }

    static DERForRecipientParams calculateDERForRecipientParams(byte[] in) throws IOException, GeneralSecurityException {
        /*
        According to ISO 32000-2 (7.6.5.3 Public-key encryption algorithms) RC-2 algorithm is outdated
        and should be replaced with a safer one 256-bit AES-CBC:
            The algorithms that shall be used to encrypt the enveloped data in the CMS object are:
            • RC4 with key lengths up to 256-bits (deprecated);
            • DES, Triple DES, RC2 with key lengths up to 128 bits (deprecated);
            • 128-bit AES in Cipher Block Chaining (CBC) mode (deprecated);
            • 192-bit AES in CBC mode (deprecated);
            • 256-bit AES in CBC mode.
         */
        String s = "1.2.840.113549.3.2";
        DERForRecipientParams parameters = new DERForRecipientParams();

        AlgorithmParameterGenerator algorithmparametergenerator = AlgorithmParameterGenerator.getInstance(s);
        AlgorithmParameters algorithmparameters = algorithmparametergenerator.generateParameters();
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(algorithmparameters.getEncoded("ASN.1"));
        IASN1Primitive derobject;
        try (IASN1InputStream asn1inputstream = BOUNCY_CASTLE_FACTORY.createASN1InputStream(bytearrayinputstream)) {
            derobject = asn1inputstream.readObject();
        }
        KeyGenerator keygenerator = KeyGenerator.getInstance(s);
        keygenerator.init(128);
        SecretKey secretkey = keygenerator.generateKey();
        Cipher cipher = Cipher.getInstance(s);
        cipher.init(Cipher.ENCRYPT_MODE, secretkey, algorithmparameters);

        parameters.abyte0 = secretkey.getEncoded();
        parameters.abyte1 = cipher.doFinal(in);
        parameters.algorithmIdentifier = BOUNCY_CASTLE_FACTORY.createAlgorithmIdentifier(
                BOUNCY_CASTLE_FACTORY.createASN1ObjectIdentifier(s), derobject);

        return parameters;
    }

    static class DERForRecipientParams {
        byte[] abyte0;
        byte[] abyte1;
        IAlgorithmIdentifier algorithmIdentifier;
    }
}
