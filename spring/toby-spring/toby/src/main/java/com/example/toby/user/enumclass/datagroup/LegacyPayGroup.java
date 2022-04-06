package com.example.toby.user.enumclass.datagroup;

public class LegacyPayGroup {

    public static String getPayGroup(String payCode) {

        if("ACCOUNT_TRANSFER".equals(payCode) || "PERMITTANCE".equals(payCode) || "ON_SITE_PAYMENT".equals(payCode) || "TOSS".equals(payCode)) {
            return "CASH";
        } else if("PAYCO".equals(payCode) || "CARD".equals(payCode) || "KAKAO_PAY".equals(payCode) || "BAEMIN_PAY".equals(payCode)) {
            return "CARD";
        } else if("POINT".equals(payCode) || "COUPON".equals(payCode)) {
            return "ETC";
        } else {
            return "EMPTY";
        }
    }

    public static String useEnumClass(PayType payType) {
        PayGroup payGroup = PayGroup.findByPayCode(payType);
        return payGroup.name();
    }
}
