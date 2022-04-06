package com.example.toby.user.enumclass.datarelationship;

public class LegacyCase {

    public String toTable1Value(String originValue) {
        if("Y".equals(originValue)) {
            return "1";
        } else {
            return "0";
        }
    }

    public boolean toTable2Value(String originValue) {
        if("Y".equals(originValue)) {
            return true;
        } else {
            return false;
        }
    }

    public void useEnumClass(String originValue) {
        TableStatus tableStatus = TableStatus.valueOf(originValue);
        String toTable1Value = tableStatus.getTable1Value();
        boolean toTable2Value = tableStatus.isTable2Value();
    }
}
