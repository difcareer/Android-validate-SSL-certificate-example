package org.ssl;


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import android.util.Log;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author olamy
 * @version $Id: EasyX509TrustManager.java 765355 2009-04-15 20:59:07Z evenisse $
 * @since 1.2.3
 */
public class EasyX509TrustManager implements X509TrustManager {

    private X509TrustManager standardTrustManager = null;

    private static final String targetMd5 = "B00F8583DBBB242F9005B7156C1EAD30";  //你们网站证书的md5值，可以通过Log获取

    /**
     * Constructor for EasyX509TrustManager.
     */
    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        standardTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * 这里是校验服务端证书的接口
     *  certificates 证书链  自己网站的证书A在第一位，签署A的证书B在第二位，签署B的证书在第三位，以此类推，直到根证书
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        // 如果证书链只有1， 说明没有被可信ca签署过
        if ((certificates != null) && (certificates.length == 1)) {
            throw new CertificateException();
        } else {
            //获取自己的证书，检验signature的md5值
            //如果想做得安全一些，可以把每一级证书都校验一下
            X509Certificate mycert = certificates[0];
            byte[] signature = mycert.getSignature();
            String md5 = StringUtils.toHexString(md5NonE(signature));
            Log.e("md5:", md5);         //打个log，可以帮助获取正常证书的md5
            //不匹配，直接抛异常
            if (!md5.equals(targetMd5)) {
                throw new CertificateException();
            }
            // 最后再校验一下证书链
            standardTrustManager.checkServerTrusted(certificates, authType);
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }

    public static final byte[] md5(byte buffer[]) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(buffer, 0, buffer.length);
        return digest.digest();
    }

    public static final byte[] md5NonE(byte buffer[]) {
        try {
            return md5(buffer);
        } catch (NoSuchAlgorithmException e) {

        }
        return new byte[0];
    }

}
