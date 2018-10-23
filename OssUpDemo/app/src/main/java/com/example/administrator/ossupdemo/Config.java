package com.example.administrator.ossupdemo;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;

/**
 * Created by Administrator 2018/10/22.
 */

public class Config {
    public static final String endpoint = "";// 访问的endpoint地址
    public static final String STSSERVER = "";//STS服务,获取参数
    public static final String bucket = "";//桶名
    public static OssService initOSS(Context context,String endpoint, String bucket) {
        OSSCredentialProvider credentialProvider;
        //使用自己的获取STSToken的类
        credentialProvider = new OSSAuthCredentialsProvider(Config.STSSERVER);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(context, endpoint, credentialProvider, conf);
        OSSLog.enableLog();
        return new OssService(oss, bucket);

    }
}
