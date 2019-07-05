package com.pdftron.pdf.utils;

import android.content.res.Resources;

import com.pdftron.common.PDFNetException;
import com.pdftron.sdf.SignatureHandler;

import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

public class MySignatureHandler extends SignatureHandler {

    private ArrayList<Byte> m_data;
    private String m_pfx;
    private int m_pfx_res = 0;
    private Resources m_resources;
    private String m_password;

    /**
     * Class constructor
     */
    public MySignatureHandler(String pfx, String password) {
        this.m_pfx = pfx;
        init(password);
    }

    /**
     * Class constructor
     */
    public MySignatureHandler(int pfxRes, Resources resources, String password) {
        this.m_pfx_res = pfxRes;
        this.m_resources = resources;
        init(password);
    }

    private void init(String password) {
        this.m_password = password;
        m_data = new ArrayList<Byte>();
    }

    @Override
    public String getName() throws PDFNetException {
        return ("Adobe.PPKLite");
    }

    @Override
    public void appendData(byte[] data) throws PDFNetException {
        for (byte b : data) {
            m_data.add(b);
        }
    }

    @Override
    public boolean reset() throws PDFNetException {
        m_data.clear();
        return true;
    }

    @Override
    public byte[] createSignature() throws PDFNetException {
        InputStream is = null;
        try {
            java.security.Security.addProvider(new BouncyCastleProvider());
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            if (m_pfx_res != 0 && m_resources != null) {
                is = m_resources.openRawResource(m_pfx_res);
            } else if (!Utils.isNullOrEmpty(m_pfx)) {
                is = new FileInputStream(m_pfx);
            }
            if (is == null) {
                return null;
            }
            keyStore.load(is, m_password.toCharArray());
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, m_password.toCharArray());
            Certificate[] certChain = keyStore.getCertificateChain(alias);

            Store certStore = new JcaCertStore(Arrays.asList(certChain));
            CMSSignedDataGenerator sigGen = new CMSSignedDataGenerator();
            ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("SC").build(privateKey);
            sigGen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("SC").build()).build(signer, (X509Certificate) certChain[0]));
            sigGen.addCertificates(certStore);
            byte[] byteData = new byte[m_data.size()];
            for (int i = 0; i < m_data.size(); i++) {
                byteData[i] = m_data.get(i);
            }
            CMSTypedData data = new CMSProcessableByteArray(byteData);
            CMSSignedData sigData = sigGen.generate(data, false);

            return (sigData.getEncoded());

        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            Utils.closeQuietly(is);
        }

        return null;
    }
}
