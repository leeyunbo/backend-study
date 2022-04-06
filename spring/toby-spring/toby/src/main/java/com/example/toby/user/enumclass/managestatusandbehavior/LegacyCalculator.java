package com.example.toby.user.enumclass.managestatusandbehavior;

public class LegacyCalculator {

    public static long calculate(String code, long originValue) {

        if("CALC_A".equals(code)) {
            return originValue;
        } else if("CALC_B".equals(code)) {
            return originValue * 10;
        } else if("CALC_C".equals(code)) {
            return originValue * 3;
        } else {
            return 0;
        }
    }

    public static long useEnumClass(String code, long originValue) {
        CalculatorType calculatorType = CalculatorType.valueOf(code);
        return calculatorType.calculate(originValue);
    }
}
